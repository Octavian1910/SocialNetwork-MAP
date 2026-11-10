========================================================================
DOCUMENTATIE LABORATOR 4 - REFACTORIZARE JAVA 8 & FUNCTIONAL PROGRAMMING
========================================================================

1. TABEL BUCLE INLOCUITE (MINIM 5 BUCLE REFACTORIZATE CU STREAM API)
------------------------------------------------------------------------
Clasa                   | Metoda                 | Operatii Stream API folosite
------------------------------------------------------------------------
UserValidator           | validate               | filter, map, toList
FriendShipValidator     | validate               | filter, map, toList
Repository              | findAll(Predicate)     | stream, filter, toList
FriendShipService       | getFriendIds           | stream, filter, map, toList
FriendShipService       | removeAllFriendships.. | forEach (peste colectie)
NetworkService          | getNumberOfCommunities | stream, filter, count
NetworkService          | getMostSociableComm... | stream, max, map, orElseGet
AbstractFileRepository  | loadFromFile           | IntStream.range, filter, forEach
AbstractFileRepository  | writeToFile            | stream, map (method reference), toList


2. INTERFETE FUNCTIONALE UTILIZATE SI LOCALIZARE
------------------------------------------------------------------------
- Predicate<T>:
  * Repository.findAll(Predicate<E> filter)
  * UserValidator si FriendShipValidator (in record-ul ValidationRule)
  * UserCriteria (metode statice care compun filtre)
- Supplier<T>:
  * RepositoryFactory (in map-urile de creare pe baza tipului)
- Consumer<T>:
  * Folosit intern in forEach (ex: adaugarea entitatilor sau parcurgerea BFS/DFS)
- Function<T, R>:
  * Folosit in stream.map() pentru extragerea campurilor si transformarea entitatilor
- Comparator<T>:
  * Folosit in NetworkService.getMostSociableCommunity (Comparator.comparingInt)


3. DECIZII DE ARHITECTURA SI JUSTIFICARI
------------------------------------------------------------------------
A. Optional in Repository:
   Interfata Repository foloseste Optional<E> pentru metodele findById, remove si update.
   Justificare:
   Evita riscul de NullPointerException la nivelul apelantilor din Service si UI.
   Obliga codul client sa trateze explicit cazul in care o entitate nu exista
   prin metode functionale sigure precum orElseThrow, orElse sau ifPresent,
   facand contractul interfetei expresiv si predictibil.

B. Template Method vs. Function in FileRepository:
   A fost pastrat pattern-ul Template Method prin clasa generica AbstractFileRepository
   si clasele concrete UserFileRepository si FriendShipFileRepository.
   Justificare:
   Pastreaza responsabilitatile clare (SRP) si separarea scheletului de persistenta
   (creare directoare, citire, rescriere) de algoritmul specific de serializare/deserializare
   al fiecarei entitati. In plus, FriendShipFileRepository necesita metode specifice precum
   normalizarea cheii compuse, lucru care s-ar fi complicat daca se folosea un singur repo generic
   injectat cu Function.


4. BUCLE CLASICE PASTRATE SI JUSTIFICARE
------------------------------------------------------------------------
- NetworkService.getDistance (Algoritmul BFS):
  A fost pastrata bucla while (!queue.isEmpty()).
  Justificare: Dimensiunea cozii (Queue) variaza dinamic pe masura ce se exploreaza graful.
  Parcurgerea algoritmilor pe grafuri de tip BFS este mult mai naturala si eficienta
  cu bucle decat prin recursivitate fortata cu Stream API.

- NetworkService.getLongestPathInCommunity:
  Au fost pastrate buclele for (i) si for (j = i + 1) pentru a genera combinatiile de perechi.
  Justificare: Evita alocarea masiva de obiecte temporare (Pair/Tuple) in memorie
  si pastreaza algoritmul simplu de urmarit.