. Fonctionnalités                
Vous n’avez pas eu de cahier des charges formels, mais vous savez que les informations importantes se basent sur les notions suivantes : client, jeu, éditeur, auteur, commande… D’autres informations se trouvent directement dans le code Java fourni.              
Les fonctionnalités CRUD de base doivent être présentes.              
Deux types de compte seront accessibles : client et administrateur.              
Vous savez également que le site aura besoin d’un système de recherche portant sur les jeux.  

2. Reprise de l’API SPRING                
Refondre l'API Spring pour respecter une architecture cohérente et les principes SOLID.              
Mettre en place Hibernate.      

3. Sécuriser et tester l’application                
Mise en place de la sécurité via Spring Security              
Garantir l’intégrité des fonctionnalités à travers des tests de non-régression. Rédiger des tests unitaires et d'intégrations pour valider le bon fonctionnement des services et des contrôleurs (uniquement l’API Spring, ne pas tester l’api Python).                  

4. Mise en place du système de recommandation                
N’étant vous-même pas expert en Machine Learning, on vous a conseillé le mise en place d’un modèle KNN. Il vous faudra l’implémenter dans une API Python FastAPI dont vous avez la base. Vous manquez pour l’instant de données véritables, les données des anciennes versions du site semblant peu exploitables.              
Il vous faudra identifier les données nécessaires pour qu’un algorithme de recommandation puisse être efficace.              
Il faudra également vous assurer de mettre en place l’algorithme créant le modèle de ML, même si celui ci sera entrainé plus tard, et vous assurez que l'API Spring communique efficacement avec l'API Python pour envoyer les données des utilisateurs et recevoir les recommandations.     
