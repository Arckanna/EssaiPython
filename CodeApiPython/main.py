from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
from models import UserData
from data_loader import load_training_data
from recommendation import Recommender, to_pairs
import pandas as pd

app = FastAPI(title="GamesUP Reco API", version="1.0.0")

recommender = Recommender()
# tampon optionnel d’événements récents pour ré-entrainement
_event_buffer: List[dict] = []

@app.get("/")
async def root():
    return {"message": "API de recommandation en ligne", "trained": recommender.fitted}

class TrainRequest(BaseModel):
    csv_path: Optional[str] = None
    interactions: Optional[List[UserData]] = None  # permet d’envoyer des interactions en JSON

@app.post("/train")
async def train(req: TrainRequest):
    try:
        if req.csv_path:
            df = load_training_data(req.csv_path)  # colonnes: user_id, game_id, rating
        elif req.interactions:
            # transforme la liste de UserData en DataFrame interactions
            rows = []
            for ud in req.interactions:
                for p in ud.purchases:
                    rows.append({"user_id": ud.user_id, "game_id": p.game_id, "rating": p.rating if p.rating is not None else 1.0})
            df = pd.DataFrame(rows)
        else:
            raise HTTPException(status_code=400, detail="Provide csv_path or interactions")

        if df.empty or not {"user_id","game_id","rating"}.issubset(df.columns):
            raise HTTPException(status_code=400, detail="Training data must have columns: user_id, game_id, rating")

        recommender.fit(df)
        return {"status": "ok", "items": len(recommender.game_index)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# (Optionnel) ingérer un événement (achat/note) pour futur ré-entrainement
class Event(BaseModel):
    user_id: int
    game_id: int
    rating: float = 1.0

@app.post("/events")
async def ingest_event(ev: Event):
    _event_buffer.append(ev.model_dump())
    return {"buffer_size": len(_event_buffer)}

@app.post("/recommendations")
async def get_recommendations(data: UserData, k: int = 10):
    if not recommender.fitted:
        # Retourne vide ou 501 selon ton choix
        raise HTTPException(status_code=503, detail="Model not trained yet")
    try:
        recs = recommender.recommend_for_user(to_pairs(data.purchases), top_k=k)
        return {"user_id": data.user_id, "recommendations": recs}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
