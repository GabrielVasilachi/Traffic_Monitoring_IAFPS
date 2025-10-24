Quick start — Traffic Signal Simulator

Scriptul `run.sh` din rădăcina proiectului pornește aplicația cu o singură comandă.

Markează scriptul executabil (doar prima dată):

```bash
chmod +x ./run.sh
```

Rulează aplicația:

```bash
./run.sh
```

Observații:
- Scriptul folosește invocarea explicită a pluginului JavaFX Maven pentru stabilitate:
  `mvn org.openjfx:javafx-maven-plugin:0.0.8:run -DmainClass=traffic.sim.TrafficSimulationApp -Djavafx.version=21.0.2`
- Dacă nu ai Maven instalat, instalează-l cu Homebrew:

```bash
brew update
brew install maven
```

- Alternativ, poți deschide proiectul în IntelliJ și rula clasa `traffic.sim.TrafficSimulationApp`.
