========================================================================
DOCUMENTAȚIE LABORATOR 5 - BAZĂ DE DATE (JDBC & POSTGRESQL)
Rețea de Socializare - Metode Avansate de Programare
========================================================================

------------------------------------------------------------------------
1. STRUCTURA BAZEI DE DATE (DDL)
------------------------------------------------------------------------
Baza de date relațională "socialnetwork" a fost creată pe serverul PostgreSQL
cu două tabele corelate prin chei străine:

CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL
);

CREATE TABLE friendships (
    id1 BIGINT NOT NULL,
    id2 BIGINT NOT NULL,
    friends_from TIMESTAMP NOT NULL,
    CONSTRAINT pk_friendships PRIMARY KEY (id1, id2),
    CONSTRAINT fk_friendships_user1 FOREIGN KEY (id1) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_user2 FOREIGN KEY (id2) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_friendship_order CHECK (id1 < id2)
);

Caracteristici:
- users.id: generat automat de PostgreSQL (GENERATED ALWAYS AS IDENTITY), garantând
  unicitatea fără intervenția aplicației client.
- friendships: cheia primară compusă (id1, id2) împreună cu constrângerea de integritate
  CHECK (id1 < id2) garantează că o relație de prietenie este memorată o singură dată
  în mod normalizat (id1 reprezentând întotdeauna utilizatorul cu ID-ul mai mic).


------------------------------------------------------------------------
2. JUSTIFICARE: ON DELETE CASCADE vs. ȘTERGERE EXPLICITĂ ÎN SERVICE (B.1)
------------------------------------------------------------------------
În cadrul proiectului nostru s-a adoptat o abordare combinată/hibridă, 
prioritizând integritatea bazei de date prin ON DELETE CASCADE, dar menținând
și metoda de orchestrare din Service:

A. Rolul ON DELETE CASCADE (Bază de date):
- Integritate referențială garantată: Baza de date garantează la nivel de engine
  relațional că nu pot exista înregistrări orfane în tabela "friendships" dacă un
  utilizator este șters direct (fie printr-un client extern precum pgAdmin, fie
  prin interogări directe).
- Performanță: Baza de date rezolvă ștergerea tuturor rândurilor asociate într-o
  singură operație optimizată intern pe baza indecșilor cheilor străine, evitând
  round-trip-uri multiple prin rețea între serverul Java și serverul PostgreSQL.

B. Rolul ștergerii orchestrate din Service (NetworkService.removeUserAndFriendships):
- Consistență cross-storage: Pentru modurile FILE și MEMORY (unde nu există un motor
  SQL care să execute automat reguli de cascadare), logica din NetworkService
  (`friendShipService.removeAllFriendshipsOfUser(userId)` urmat de `userService.removeUser(userId)`)
  asigură că fișierele și colecțiile rămân complet consistente.
- Decuplare: Serviciile nu trebuie să depindă de ipoteza că mediul de persistență
  suportă triggers sau cascade.


------------------------------------------------------------------------
3. IMPLEMENTAREA REPOSITORY-URILOR JDBC ȘI CONTRACTUL CRUD (B.2)
------------------------------------------------------------------------
Clasele `UserDbRepository` și `FriendshipDbRepository`:
- Obținerea conexiunii: Conexiunea se deschide exclusiv prin
  `DriverManager.getConnection(url, username, password)`, cu parametrii transmiși
  prin constructor de către `RepositoryFactory`.
- Managementul resurselor: Toate conexiunile, instrucțiunile SQL și cursoarele
  (`Connection`, `PreparedStatement`, `ResultSet`) sunt gestionate prin blocuri
  idiomatice `try-with-resources`, eliminând riscul de scurgeri de conexiuni (resource leaks).
- Prevenirea SQL Injection: Toate interogările folosesc `PreparedStatement` și
  placeholders (`?`), niciun parametru nefiind concatenat în șirul SQL.
  (Demonstrat la testul: prenumele "'); DROP TABLE users; --" este inserat ca simplu text).
- Recuperarea ID-ului generat: La adăugarea unui utilizator, `PreparedStatement` este creat
  cu flag-ul `Statement.RETURN_GENERATED_KEYS`. Valoarea generată de secvența PostgreSQL
  este extrasă din cursorul `getGeneratedKeys()` și setată pe instanța `User` (`entity.setId(...)`).
- Contractul interfeței Repository:
  * Argumente null: aruncă `IllegalArgumentException`.
  * remove(ID): returnează `Optional<E>` conținând entitatea ștearsă sau `Optional.empty()`
    dacă ID-ul nu există.
  * update(E): verifică dacă entitatea există. Dacă nu a fost modificat niciun rând
    (`executeUpdate() == 0`), returnează `Optional.of(entity)`; altfel returnează `Optional.empty()`.


------------------------------------------------------------------------
4. CONFIGURARE ȘI FABRICĂ (FACTORY PATTERN) (B.3)
------------------------------------------------------------------------
Trecerea între mecanismele de stocare se face exclusiv prin proprietatea:
    `repository.type=DATABASE`  (valori permise: DATABASE, FILE, MEMORY)
situată în `src/resources/config.properties`.

Clasa `RepositoryFactory` mapează tipul selectat către un `Supplier<Repository<...>>`:
- Pentru DATABASE: instanțiază `UserDbRepository` și `FriendshipDbRepository` folosind
  URL-ul, utilizatorul și parola din fișierul de configurare.
- Pentru FILE: instanțiază `UserFileRepository` și `FriendshipFileRepository`.
- Pentru MEMORY: instanțiază `InMemoryRepository`.

Consola (UI) primește tipul de repository activ:
- La modul DATABASE: operația "Add user" nu solicită introducerea unui ID manual,
  acesta fiind generat automat de PostgreSQL și afișat utilizatorului.
- La modurile FILE / MEMORY: consola solicită ID-ul de la tastatură.


------------------------------------------------------------------------
5. TRATAREA EXCEPȚIILOR (B.4)
------------------------------------------------------------------------
- Toate excepțiile de nivel jos (`SQLException`) aruncate de driverul JDBC sunt
  interceptate în interiorul repository-urilor de DB și împachetate în excepții unchecked
  `RepositoryException(message, e)`.
- Codurile SQL State specifice sunt mapate semantic:
  * Cod 23505 (Unique violation) -> Mesaj clar de duplicat ("already exists").
  * Cod 08001 / 08006 (Connection failure) -> Mesaj de server PostgreSQL inaccesibil.
- În consolă (`Console.java`) nu există blocuri `catch` goale și nici `catch (Exception e)`.
  Excepțiile cunoscute (`ValidationException`, `RepositoryException`, `ServiceException`,
  `IllegalArgumentException`, `NumberFormatException`) sunt prinse specific și afișează
  doar mesajul de eroare descriptiv, fără stack trace tehnic.


------------------------------------------------------------------------
6. GHID DE RULARE ȘI CONFIGURARE LA PREDARE
------------------------------------------------------------------------
1. Fișiere de configurare:
   - `src/resources/config.properties`: conține credențialele locale (adăugat în .gitignore).
   - `src/resources/config.example.properties`: șablon public cu parola goală.

2. Dependințe:
   - Driver PostgreSQL JDBC (ex: `org.postgresql:postgresql:42.7.4`) adăugat în classpath / build tool.

3. Scenarii de verificare:
   a. Adăugare utilizator -> ID generat automat (ex: ID=1) și verificat în pgAdmin.
   b. SQL Injection test -> inserare nume: "'; DROP TABLE users; --" -> tabela rămâne intactă.
   c. Simetrie prietenie -> adăugare prietenie între 1 și 2; căutare după "2-1" o găsește.
   d. Persistență -> repornirea aplicației afișează datele existente anterior.
   e. Server oprit -> oprirea serviciului PostgreSQL din Servicii / Docker determină UI-ul
      să afișeze un mesaj prietenos de eroare de conectare, fără crash sau stack trace.
   f. Comutare la FILE/MEMORY -> schimbare în `config.properties`, fără modificări de cod Java.
========================================================================