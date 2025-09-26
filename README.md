# Shop Manager (JavaFX)

Application de gestion de boutique (Java 17 + JavaFX) avec MVC, Hibernate, BCrypt, génération PDF, thèmes Light/Dark et navigation fluide.

## Sommaire
- Présentation rapide
- Prérequis
- Démarrage rapide (PostgreSQL)
- Configuration base de données (MySQL ou PostgreSQL)
- Lancer l'application
- Comptes par défaut
- Fonctionnalités livrées
- Structure du projet
- Navigation dans l'app
- Dépannage (FAQ)
- Licences et mentions

## Présentation rapide
Shop Manager permet de gérer les produits, clients et ventes (panier, TVA, facture PDF), avec un tableau de bord et un thème clair/sombre.

## Prérequis
- Java 17+
- Maven 3.9+
- PostgreSQL 13+ ou MySQL 8+

## Démarrage rapide (PostgreSQL recommandé)
1. Créez la base de données `gestiondb` dans PostgreSQL:
   ```sql
   CREATE DATABASE gestiondb;
   ```
2. Ouvrez `src/main/resources/application.properties` et vérifiez:
   ```properties
   db.type=postgresql
   db.postgresql.url=jdbc:postgresql://localhost:5432/gestiondb
   db.username=postgres
   db.password=postgres
   
   # Hibernate
   hibernate.hbm2ddl.auto=update
   hibernate.show_sql=false
   hibernate.format_sql=true
   
   # App
   app.theme=light
   app.vat=0.2
   ```
3. Lancez l’application:
   ```bash
   mvn -q clean javafx:run
   ```

## Configuration base de données (MySQL ou PostgreSQL)
- Fichier: `src/main/resources/application.properties`
- Choisissez un type et renseignez l’URL JDBC correspondante.

Exemple MySQL:
```properties
db.type=mysql
db.mysql.url=jdbc:mysql://localhost:3306/shop_manager?useSSL=false&serverTimezone=UTC
db.username=root
db.password=secret
```

Exemple PostgreSQL:
```properties
db.type=postgresql
db.postgresql.url=jdbc:postgresql://localhost:5432/gestiondb
db.username=postgres
db.password=postgres
```

Schéma Hibernate:
- `hibernate.hbm2ddl.auto=update` crée et met à jour le schéma automatiquement.
- Si vous avez un schéma incohérent en développement, vous pouvez remettre à plat temporairement avec:
  ```properties
  hibernate.hbm2ddl.auto=create
  ```
  puis relancer l’app et repasser à `update`.

## Lancer l’application
Dans le dossier du projet `shop-manager/`:
```bash
mvn -q clean javafx:run
```
Maven télécharge JavaFX et lance l’app (pas besoin d’installer JavaFX à part).

## Comptes par défaut
- Admin: `admin@shop.com` / `admin123`
- Caissier: `cashier@shop.com` / `cashier123`
Les comptes par défaut sont créés/garantis au démarrage (méthode `ensureDefaultUsers`).

## Fonctionnalités livrées
- Login sécurisé (BCrypt)
- Dashboard (cartes et navigation vers modules)
- Produits: CRUD, recherche, filtre par catégorie
- Clients: CRUD, recherche
- Ventes: sélection produits, panier, calcul TVA, décrémentation du stock, génération de facture PDF
- Thèmes: Light/Dark avec toggle
- Navigation: boutons "← Retour" dans chaque module pour revenir au Dashboard

## Structure du projet
- `src/main/java/com/shopmanager/`
  - `core/` gestion scène (`SceneManager`), config (`AppConfig`, `HibernateUtil`)
  - `controller/` contrôleurs JavaFX (Login, Dashboard, Products, Customers, Sales)
  - `model/` entités JPA (`User`, `Role`, `Category`, `Product`, `Customer`, `Sale`, `SaleItem`)
  - `repository/` accès base (BaseRepository + Repositories dédiés)
  - `service/` logique métier (Auth, Product, Customer, Sale, Report)
- `src/main/resources/`
  - `view/*.fxml` vues JavaFX
  - `css/*.css` thèmes et styles
  - `application.properties`, `hibernate.cfg.xml`
- `db/` scripts SQL éventuels

## Navigation dans l'app
- Depuis le Dashboard, accédez aux modules via les boutons.
- Dans chaque module (Produits, Clients, Ventes), utilisez `← Retour` pour revenir au Dashboard.
- Le thème se change via le bouton "Light/Dark" sur le Dashboard.

## Dépannage (FAQ)
- Erreur JavaFX: "JavaFX runtime components are missing"
  - Lancez via Maven: `mvn -q clean javafx:run` (ne lancez pas MainApp directement sans module-path).

- `command not found: mvn`
  - Installez Maven (macOS/Homebrew): `brew install maven`

- Log4j2: "could not find a logging implementation"
  - Ce n’est pas bloquant. Optionnel: ajouter `log4j-to-slf4j` et configurer `slf4j-simple`/`logback`.

- Erreurs FXML de type Insets ("Unable to coerce ... to javafx.geometry.Insets")
  - Utilisez la syntaxe:
    ```xml
    <padding><Insets top="12" right="12" bottom="12" left="12"/></padding>
    ```

- PostgreSQL: erreurs de colonnes/tables (ex. `user` réservé)
  - Les tables ont été renommées en `users` et `roles`.
  - Si la base a un ancien schéma, passez temporairement `hibernate.hbm2ddl.auto=create` pour repartir proprement.

- PDF iText
  - Les factures PDF sont générées via iText 7. iText est AGPL: pour usage commercial, remplacez par OpenPDF ou JasperReports.

- Avertissements Unsafe/System::load
  - Avertissements JavaFX/Maven connus, sans impact fonctionnel en développement.

## Licences et mentions
- JavaFX: OpenJFX
- Hibernate ORM
- BCrypt (jBCrypt)
- iText 7 (AGPL). Pour usage commercial, envisager OpenPDF ou JasperReports.
