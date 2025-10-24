package traffic.sim;

import javafx.scene.paint.Color;
import traffic.sim.algorithms.SignalAlgorithm;
import traffic.sim.controller.TrafficController;
import traffic.sim.model.Car;
import traffic.sim.model.Direction;
import traffic.sim.model.Intersection;
import traffic.sim.model.TrafficLight;
import traffic.sim.stats.PerformanceTracker;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SimulationEngine {
    private static final double CAR_SPEED = 90.0;
    private static final double CAR_LENGTH = 26.0;
    private static final double MIN_GAP = 6.0;
    private static final double CAR_WIDTH = 16.0;
    private static final double SPAWN_INTERVAL_MIN = 1.0;
    private static final double SPAWN_INTERVAL_MAX = 5.0;
    private static final int WAVE_SIZE_MIN = 1;
    private static final int WAVE_SIZE_MAX = 3;
    private static final double WAVE_GAP_MIN = 50.0;
    private static final double WAVE_GAP_MAX = 200.0;
    private static final double INTERSECTION_HALF_SIZE = 45.0;
    private static final double LANE_OFFSET = 28.0;
    private static final double SPAWN_OFFSET = 140.0;

    private final double width;
    private final double height;

    private final Random random = new Random();
    private final Intersection intersection = new Intersection();
    private final TrafficController controller = new TrafficController(intersection);
    private final Map<Direction, List<Car>> laneCars = new EnumMap<>(Direction.class);
    private final Map<Direction, Double> laneCoordinate = new EnumMap<>(Direction.class);

    private final Color[] palette = new Color[]{
            Color.DODGERBLUE, Color.ORANGE, Color.CRIMSON,
            Color.SEAGREEN, Color.GOLDENROD, Color.MEDIUMPURPLE
    };

    private SignalAlgorithm algorithm;
    private double spawnTimer;
    private double simulationClock;
    private final PerformanceTracker performanceTracker = new PerformanceTracker();

    public SimulationEngine(double width, double height) {
        this.width = width;
        this.height = height;
        initLaneStorage();
        algorithm = null;
        reset();
    }

    public void reset() {
        laneCars.values().forEach(List::clear);
        spawnTimer = randomInterval();
        simulationClock = 0.0;
        performanceTracker.reset();
        controller.reset(TrafficController.DirectionGroup.EAST_WEST);
        if (algorithm != null) {
            algorithm.reset(controller);
        }
    }

    private void initLaneStorage() {
        for (Direction direction : Direction.values()) {
            laneCars.put(direction, new ArrayList<>());
        }
        laneCoordinate.put(Direction.EAST, height / 2.0 - LANE_OFFSET);
        laneCoordinate.put(Direction.WEST, height / 2.0 + LANE_OFFSET);
        laneCoordinate.put(Direction.NORTH, width / 2.0 + LANE_OFFSET);
        laneCoordinate.put(Direction.SOUTH, width / 2.0 - LANE_OFFSET);
    }

    private double randomInterval() {
        return SPAWN_INTERVAL_MIN + random.nextDouble() * (SPAWN_INTERVAL_MAX - SPAWN_INTERVAL_MIN);
    }

    public void setAlgorithm(SignalAlgorithm algorithm) {
        this.algorithm = algorithm;
        controller.reset(TrafficController.DirectionGroup.EAST_WEST);
        performanceTracker.reset();
        simulationClock = 0.0;
        if (this.algorithm != null) {
            this.algorithm.reset(controller);
        }
    }

    public void update(double deltaSeconds) {
        if (algorithm == null) {
            return;
        }

        simulationClock += deltaSeconds;
        controller.update(deltaSeconds);
        intersection.updateLights(deltaSeconds);
        algorithm.update(deltaSeconds, controller, laneCars);

        handleSpawning(deltaSeconds);
        updateCars(deltaSeconds);
        performanceTracker.update(deltaSeconds, simulationClock);
    }

    private void handleSpawning(double deltaSeconds) {
        spawnTimer -= deltaSeconds;
        if (spawnTimer > 0.0) {
            return;
        }

        Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
        int waveSize = random.nextInt(WAVE_SIZE_MAX - WAVE_SIZE_MIN + 1) + WAVE_SIZE_MIN;
        spawnWave(direction, waveSize);
        spawnTimer = randomInterval();
    }

    private void spawnWave(Direction direction, int count) {
        List<Car> cars = laneCars.get(direction);

        double[] basePosition = baseSpawnPosition(direction, cars);
        double baseX = basePosition[0];
        double baseY = basePosition[1];

        double offset = 0.0;
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                offset += randomWaveSpacing();
            }
            double x = baseX - direction.dx() * offset;
            double y = baseY - direction.dy() * offset;
            Color color = palette[random.nextInt(palette.length)];
            Car car = new Car(direction, x, y, CAR_SPEED, CAR_LENGTH, color);
            cars.add(car);
        }
    }

    private double[] baseSpawnPosition(Direction direction, List<Car> existing) {
        double x;
        double y;
        switch (direction) {
            case EAST -> {
                x = -SPAWN_OFFSET;
                y = laneCoordinate.get(Direction.EAST);
            }
            case WEST -> {
                x = width + SPAWN_OFFSET;
                y = laneCoordinate.get(Direction.WEST);
            }
            case NORTH -> {
                x = laneCoordinate.get(Direction.NORTH);
                y = height + SPAWN_OFFSET;
            }
            case SOUTH -> {
                x = laneCoordinate.get(Direction.SOUTH);
                y = -SPAWN_OFFSET;
            }
            default -> throw new IllegalStateException("Unexpected direction: " + direction);
        }

        if (!existing.isEmpty()) {
            Car tail = existing.stream()
                    .min((a, b) -> Double.compare(projectAlong(a), projectAlong(b)))
                    .orElse(null);
            if (tail != null) {
                double spacing = randomWaveSpacing();
                x = tail.getX() - direction.dx() * spacing;
                y = tail.getY() - direction.dy() * spacing;
            }
        }

        return new double[]{x, y};
    }

    private double randomWaveSpacing() {
        double gap = WAVE_GAP_MIN + random.nextDouble() * (WAVE_GAP_MAX - WAVE_GAP_MIN);
        return gap + CAR_LENGTH;
    }

    private void updateCars(double deltaSeconds) {
        for (Direction direction : Direction.values()) {
            List<Car> cars = laneCars.get(direction);
            cars.sort((a, b) -> Double.compare(projectAlong(b), projectAlong(a)));
            Car previous = null;
            for (Car car : cars) {
                boolean atSignal = !hasClearedStopLine(car);
                TrafficLight.LightState lightState = controller.getState(direction);
                boolean frontHasSpace = previous == null || gapToPrevious(car, previous) > (CAR_LENGTH + MIN_GAP);
                boolean lightAllowsMovement = lightState == TrafficLight.LightState.GREEN
                        || (lightState == TrafficLight.LightState.YELLOW && !atSignal);
                boolean allowedToMove = frontHasSpace && (!atSignal || lightAllowsMovement);
                car.update(deltaSeconds, allowedToMove);
                previous = car;
            }
        }

        removeFinishedCars();
    }

    private double projectAlong(Car car) {
        Direction direction = car.getDirection();
        if (direction.dx() != 0) {
            return car.getX() * direction.dx();
        }
        return car.getY() * direction.dy();
    }

    private boolean hasClearedStopLine(Car car) {
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        return switch (car.getDirection()) {
            case EAST -> car.getX() >= centerX - INTERSECTION_HALF_SIZE;
            case WEST -> car.getX() <= centerX + INTERSECTION_HALF_SIZE;
            case NORTH -> car.getY() <= centerY - INTERSECTION_HALF_SIZE;
            case SOUTH -> car.getY() >= centerY + INTERSECTION_HALF_SIZE;
        };
    }

    private double gapToPrevious(Car car, Car previous) {
        if (previous == null) {
            return Double.MAX_VALUE;
        }
        return switch (car.getDirection()) {
            case EAST -> previous.getX() - car.getX();
            case WEST -> car.getX() - previous.getX();
            case NORTH -> car.getY() - previous.getY();
            case SOUTH -> previous.getY() - car.getY();
        };
    }

    private void removeFinishedCars() {
        for (Direction direction : Direction.values()) {
            List<Car> cars = laneCars.get(direction);
            cars.removeIf(car -> {
                boolean finished = switch (direction) {
                    case EAST -> car.getX() > width + SPAWN_OFFSET;
                    case WEST -> car.getX() < -SPAWN_OFFSET;
                    case NORTH -> car.getY() < -SPAWN_OFFSET;
                    case SOUTH -> car.getY() > height + SPAWN_OFFSET;
                };
                if (finished) {
                    performanceTracker.recordCarFinished(car.getCumulativeWait());
                }
                return finished;
            });
        }
    }

    public List<Car> getAllCars() {
        List<Car> all = new ArrayList<>();
        laneCars.values().forEach(all::addAll);
        return all;
    }

    public Map<Direction, List<Car>> getLaneCars() {
        return laneCars;
    }

    public Intersection getIntersection() {
        return intersection;
    }

    public double getAverageWait() {
        return performanceTracker.getAverageWait();
    }

    public int getCompletedCars() {
        return performanceTracker.getCompletedCars();
    }

    public double getSimulationClock() {
        return simulationClock;
    }

    public double getIntersectionHalfSize() {
        return INTERSECTION_HALF_SIZE;
    }

    public double getLaneOffset(Direction direction) {
        return laneCoordinate.get(direction);
    }

    public double getCarLength() {
        return CAR_LENGTH;
    }

    public double getCarWidth() {
        return CAR_WIDTH;
    }

    public TrafficController getController() {
        return controller;
    }

    public PerformanceTracker getPerformanceTracker() {
        return performanceTracker;
    }
}
