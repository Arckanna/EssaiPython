# GamesUP — API Spring Boot + Reco FastAPI

Ce README résume **comment lancer, tester et intégrer** les deux API (Java & Python) après la refonte. Il est pensé pour ton dépôt : [`EssaiPython`](https://github.com/Arckanna/EssaiPython).

---

## 🧱 Architecture générale

* **`gamesUP/`** — API **Spring Boot** (Java 21, Spring Boot 3.x, MySQL, JPA/Hibernate, Security JWT).

  * CRUD : *Game*, *Publisher* ; recherche (Specifications) ; rôles **CLIENT**/**ADMIN**.
  * Intégration HTTP vers l’API Python (recommandations).
* **`ANNEXES/CodeApiPython/`** — API **FastAPI** (Python) pour recommandations **KNN item-item (cosine)**.

  * Endpoints : `POST /train` (entrainement), `POST /recommendations` (recos).

---

## ✅ Prérequis

* **Java 21** + **Maven** 3.9+
* **MySQL** (Wamp/Windows ok) – port 3306
* **Python 3.10+** (recommandé 3.11) + `pip`
* **Postman / curl** (tests manuels)

---

## ⚙️ Configuration (Spring)

Fichier : `gamesUP/src/main/resources/application.properties`

```properties
# --- MySQL ---
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/gamesup?useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&serverTimezone=UTC
spring.datasource.username=gamesup
spring.datasource.password=gamesup
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false

# --- JWT ---
app.jwt.issuer=gamesup
app.jwt.secret=${JWT_SECRET:hx6PjN4yF2Q9tA1zUe7rBk5mW8sY3c0vL9qR2nT6pH8dJ1fE4kM7vB2sD5gX9zQ}
app.jwt.expiry-minutes=120

# --- Initialisation SQL (optionnel: data.sql) ---
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

# --- Reco Python ---
python.api.base-url=http://127.0.0.1:8000
python.api.key=dev-reco-key
```

> 🔐 **JWT\_SECRET** : en prod, passe une vraie clé via variable d’environnement (`>= 32` octets). Changer le secret invalide les tokens existants.

---

## 🗃️ Base de données (MySQL)

Créer la base & l’utilisateur (si pas déjà faits) :

```sql
CREATE DATABASE IF NOT EXISTS gamesup CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'gamesup'@'localhost' IDENTIFIED BY 'gamesup';
GRANT ALL PRIVILEGES ON gamesup.* TO 'gamesup'@'localhost';
FLUSH PRIVILEGES;
```

> Sous Wamp, `root` a souvent un mot de passe **vide**. Tu peux utiliser `root`/\`\` en dev, mais l’exemple ci-dessus crée un user dédié.

---

## ▶️ Lancer l’API Spring

Dans `gamesUP/` :

```bash
mvn spring-boot:run
```

* API disponible : `http://localhost:8080`
* Swagger (si ajouté) : `http://localhost:8080/swagger-ui`

### Comptes & rôles

* `POST /api/auth/register` → crée un **CLIENT** par défaut.
* Promouvoir en **ADMIN** (SQL) :

  ```sql
  UPDATE users SET role='ADMIN' WHERE email='admin@example.com';
  ```
* `POST /api/auth/login` → renvoie `{ accessToken, role }`.
* Utilise le header `Authorization: Bearer <token>` pour les routes protégées.

---

## 🔁 Endpoints Spring (extrait)

### Auth

* `POST /api/auth/register` — body : `{ "email":"...", "password":"..." }`
* `POST /api/auth/login` — body : idem

### Games

* `GET /api/games` — **public** ; filtres : `q, publisherId, players, durationMax, priceMin, priceMax, page, size, sortBy, dir`
* `GET /api/games/{id}` — **public**
* `POST /api/games` — **ADMIN**
* `PUT /api/games/{id}` — **ADMIN**
* `DELETE /api/games/{id}` — **ADMIN**

### Publishers

* `GET /api/publishers` — **public**
* `GET /api/publishers/{id}` — **public**
* `POST /api/publishers` — **ADMIN**
* `PUT /api/publishers/{id}` — **ADMIN**
* `DELETE /api/publishers/{id}` — **ADMIN**

### Recommandations

* `GET /api/recommendations/{userId}?k=10` — appelle l’API Python (retourne une liste d’`id` de jeux). Les achats envoyés côté Java sont actuellement simulés : à remplacer par les vraies lignes de commande/avis quand disponibles.

---

## 🐍 API Python (FastAPI)

Arbo recommandée : `ANNEXES/CodeApiPython/`

```
CodeApiPython/
  .venv/
  main.py
  models.py
  recommendation.py
  data_loader.py
  requirements.txt (optionnel)
```

### Installation (une fois)

```bash
cd ANNEXES/CodeApiPython
python -m venv .venv
# PowerShell
.\.venv\Scripts\Activate.ps1
# ou CMD
.\.venv\Scripts\activate.bat
python -m pip install --upgrade pip
pip install fastapi uvicorn pandas numpy scikit-learn pydantic
```

### Lancement

```bash
python -m uvicorn main:app --reload --port 8000
```

* Swagger : `http://127.0.0.1:8000/docs`
* Clé d’accès (header) : `X-Api-Key: dev-reco-key` (configurable)

### Entraîner le modèle

```bash
curl -X POST http://127.0.0.1:8000/train \
 -H "X-Api-Key: dev-reco-key" -H "Content-Type: application/json" \
 -d '{
  "interactions":[
    {"user_id":1,"purchases":[{"game_id":1,"rating":5},{"game_id":2,"rating":4}]},
    {"user_id":2,"purchases":[{"game_id":2,"rating":5},{"game_id":3,"rating":4}]}
  ]
}'
```

### Obtenir des recommandations

```bash
curl -X POST "http://127.0.0.1:8000/recommendations?k=5" \
 -H "X-Api-Key: dev-reco-key" -H "Content-Type: application/json" \
 -d '{"user_id":99,"purchases":[{"game_id":1,"rating":5}]}'
```

> **Données minimales** nécessaires : interactions `(user_id, game_id, rating)` ; rating = 1 par défaut pour un achat. Plus tard, enrichir avec catégories, nb joueurs, durée, etc. pour une reco hybride.

---

## 🔗 Intégration Spring ↔ Python

* Propriété Spring : `python.api.base-url` → `http://127.0.0.1:8000`
* Client HTTP Java : `RestClient` avec en-tête `X-Api-Key: ${python.api.key}`.
* Workflow recommandé en dev :

  1. **Démarrer** FastAPI `:8000` ;
  2. **POST /train** (données synthétiques) ;
  3. Démarrer Spring `:8080` ;
  4. Appeler `GET /api/recommendations/{userId}`.

---

## 🧪 Tests (Spring seulement)

* Profil test : `src/test/resources/application-test.properties` (H2 en mémoire).
* Lancer les tests :

```bash
mvn -Dspring.profiles.active=test test
```

* Ce qui est couvert :

  * **Security/MockMvc** : GET publics, POST protégé (403 pour CLIENT/sans token), login ADMIN → POST OK.
  * **JPA slice** : ex. `findByTitleIgnoreCase`.

> Les tests de l’API Python **ne sont pas requis** (consigne).

---

## 🧰 Dépannage rapide

* **Connexion MySQL** : `Communications link failure` → vérifier que MySQL écoute `127.0.0.1:3306`, user/mdp corrects, base `gamesup` créée, firewall.
* **Collation inconnue** : utiliser `utf8mb4_unicode_ci` (MySQL 5.7/MariaDB) ; `utf8mb4_0900_ai_ci` = MySQL >= 8.0.1.
* **JWT secret trop court** : utiliser une chaîne >= 32 octets.
* **`ModuleNotFoundError: fastapi`** : activer la venv et `pip install fastapi`.
* **503 `Model not trained yet`** : appeler `/train` d’abord.
* **401 côté Python** : ajouter le header `X-Api-Key: dev-reco-key` (ou mettre la même clé dans Spring `python.api.key`).

---

## 🗺️ Roadmap (checklist)

* [x] CRUD Game/Publisher + recherche
* [x] JPA/Hibernate (MySQL) + `open-in-view=false`
* [x] Sécurité JWT (CLIENT/ADMIN) + `@PreAuthorize`
* [x] Tests d’intégration (MockMvc) & JPA slice
* [x] API FastAPI KNN + endpoints `/train` & `/recommendations`
* [x] Intégration Spring ↔ Python (RestClient)
* [ ] **(option)** Flyway + `ddl-auto=validate`
* [ ] **(option)** Reco hybride (métadonnées jeu)

---

## 📎 Annexes utiles

* Exemple **promotion ADMIN** :

  ```sql
  UPDATE users SET role='ADMIN' WHERE email='admin@example.com';
  ```
* Exemple **header** Postman pour routes ADMIN :

  * `Authorization: Bearer <token>`
* Exemple d’appel Spring → Python (pseudo) :

  ```http
  POST /recommendations?k=10
  Headers: X-Api-Key: dev-reco-key
  Body: {"user_id":42,"purchases":[{"game_id":1,"rating":1}]}
  ```

---

Si tu veux, on ajoute une section **Swagger/OpenAPI** côté Spring, et un petit **script CSV synthétique** pour entraîner la reco automatiquement au boot en dev.
