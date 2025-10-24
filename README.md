# Traffic Signal Simulator

Aplicație JavaFX care simulează o intersecție în cruce și compară trei strategii de control al semafoarelor: Fixed-Time, Green Wave și Max Pressure.

## Cerințe minime

- JDK 17 sau mai nou
- Maven 3.8+

## Lansare rapidă

```bash
mvn clean javafx:run
```

Alternativ, deschide proiectul în IntelliJ IDEA / Eclipse cu suport Maven și rulează clasa `traffic.sim.TrafficSimulationApp`.

## Utilizare

1. Alege algoritmul dorit din panoul din dreapta.
2. Apasă `Start` pentru a porni simularea (sau `Pause` pentru pauză).
3. `Reset` golește intersecția și repornește algoritmul curent.
4. Timpul de așteptare pentru fiecare mașină este afișat numeric deasupra acesteia.
5. Media timpilor de așteptare este afișată în colțul din stânga-jos.

## Extensii

Pentru a adăuga un algoritm nou:

1. Creează o clasă în `src/main/java/traffic/sim/algorithms` care implementează `SignalAlgorithm`.
2. Gestionează tranzițiile de fază în metoda `update(...)` folosind datele din `Intersection` și cozile `Map<Direction, List<Car>>`.
3. Adaugă noul algoritm în panoul de control (vezi `TrafficSimulationApp#buildControls`).
