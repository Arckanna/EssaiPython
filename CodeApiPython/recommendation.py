# recommendation.py
from __future__ import annotations
import pandas as pd
import numpy as np
from typing import List, Dict, Optional, Tuple
from sklearn.neighbors import NearestNeighbors

# Modèle global simple (in-memory)
class Recommender:
    def __init__(self):
        self.fitted = False
        self.model: Optional[NearestNeighbors] = None
        self.item_matrix: Optional[pd.DataFrame] = None  # index=user_id cols=game_id (ratings), mais transposé pour KNN item-item
        self.game_index: Dict[int, int] = {}   # game_id -> colonne index
        self.index_game: Dict[int, int] = {}   # colonne index -> game_id
        self.item_popularity: pd.Series | None = None    # fallback

    def fit(self, interactions: pd.DataFrame):
        """
        interactions: DataFrame colonnes ['user_id','game_id','rating']
        """
        if interactions.empty:
            raise ValueError("No training data provided")

        # pivot users x items : valeurs = rating, NaN -> 0
        user_item = interactions.pivot_table(index='user_id', columns='game_id', values='rating', aggfunc='mean').fillna(0.0)

        # On entraîne un KNN item-item sur les colonnes (items)
        item_vectors = user_item.values.T  # shape: n_items x n_users
        self.model = NearestNeighbors(metric='cosine', algorithm='brute')
        self.model.fit(item_vectors)

        # Index <-> game_id
        self.game_index = {gid: i for i, gid in enumerate(user_item.columns.tolist())}
        self.index_game = {i: gid for gid, i in self.game_index.items()}

        # Popularité (fallback) : nb d’interactions (ou moyenne)
        self.item_popularity = interactions.groupby('game_id')['rating'].count().sort_values(ascending=False)

        # On garde pour debug/analyse
        self.item_matrix = user_item
        self.fitted = True

    def recommend_for_user(self, user_purchases: List[Tuple[int, float]], top_k: int = 10) -> List[int]:
        """
        user_purchases: liste (game_id, rating) pour cet utilisateur
        Retourne une liste de game_id recommandés.
        """
        if not self.fitted:
            return []  # ou raise

        # Agrège les voisins de chaque item possédé/noté
        scores: Dict[int, float] = {}

        for game_id, weight in user_purchases:
            if game_id not in self.game_index:
                continue  # item inconnu du modèle
            idx = self.game_index[game_id]

            # k voisins proches (on prend par ex. 20 pour élargir)
            distances, indices = self.model.kneighbors([self.item_matrix.values.T[idx]], n_neighbors=21)  # inclut l'item lui-même
            distances, indices = distances[0], indices[0]

            for dist, j in zip(distances, indices):
                neighbor_gid = self.index_game[j]
                if neighbor_gid == game_id:
                    continue
                # Similarité = 1 - distance_cosine
                sim = 1.0 - float(dist)
                # Score agrégé pondéré par "weight" (rating)
                scores[neighbor_gid] = scores.get(neighbor_gid, 0.0) + sim * float(weight)

        # Retire les items déjà achetés
        owned = {gid for gid, _ in user_purchases}
        for gid in owned:
            scores.pop(gid, None)

        # Si rien (nouvel utilisateur ou items inconnus), fallback popularité
        if not scores:
            if self.item_popularity is None:
                return []
            candidates = [int(gid) for gid in self.item_popularity.index.tolist() if gid not in owned]
            return candidates[:top_k]

        # Trie par score décroissant
        ranked = sorted(scores.items(), key=lambda kv: kv[1], reverse=True)
        return [gid for gid, _ in ranked[:top_k]]


# utilitaire simple pour convertir le payload Pydantic en liste (game_id, rating)
def to_pairs(purchases) -> List[Tuple[int, float]]:
    return [(int(p.game_id), float(p.rating if p.rating is not None else 1.0)) for p in purchases]
