package traffic.sim;

import javafx.scene.paint.Color;
import traffic.sim.algorithms.SignalAlgorithm;
import traffic.sim.model.Car;
import traffic.sim.model.Direction;
import traffic.sim.model.Intersection;
import traffic.sim.model.TrafficLight;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SimulationEngine {
    private static final double CAR_SPEED = 90.0;
    private static final double CAR_LENGTH = 26.0;
    private static final double MIN_GAP = 6.0;
    private static final double CAR_WIDTH = 16.0;
    private static final double SPAWN_MIN = 2.0;
    private static final double SPAWN_MAX = 5.0;
    private static final double INTERSECTION_HALF_SIZE = 45.0;
    private static final double LANE_OFFSET = 28.0;
    private static final double SPAWN_OFFSET = 140.0;

    private final double width;
    private final double height;

    private final Random random = new Random();
    private final Intersection intersection = new Intersection();
    private final Map<Direction, List<Car>> laneCars = new EnumMap<>(Direction.class);
    private final Map<Direction, Double> spawnTimers = new EnumMap<>(Direction.class);
    private final Map<Direction, Double> laneCoordinate = new EnumMap<>(Direction.class);

    private final Color[] palette = new Color[]{
            Color.DODGERBLUE, Color.ORANGE, Color.CRIMSON,
            Color.SEAGREEN, Color.GOLDENROD, Color.MEDIUMPURPLE
    };

    private SignalAlgorithm algorithm;
    private double totalWaitTime;
    private int completedCars;
    private double simulationClock;

    public SimulationEngine(double width, double height) {
        this.width = width;
        this.height = height;
        initLaneStorage();
        algorithm = null;
        reset();
    }

    public void reset() {
        laneCars.values().forEach(List::clear);
        spawnTimers.replaceAll((d, v) -> randomInterval());
        totalWaitTime = 0.0;
        completedCars = 0;
        simulationClock = 0.0;
        intersection.resetToDefaultPhase();
    }

    private void initLaneStorage() {
        for (Direction direction : Direction.values()) {
            laneCars.put(direction, new ArrayList<>());
            spawnTimers.put(direction, randomInterval());
        }
        laneCoordinate.put(Direction.EAST, height / 2.0 - LANE_OFFSET);
        laneCoordinate.put(Direction.WEST, height / 2.0 + LANE_OFFSET);
        laneCoordinate.put(Direction.NORTH, width / 2.0 + LANE_OFFSET);
        laneCoordinate.put(Direction.SOUTH, width / 2.0 - LANE_OFFSET);
    }

    private double randomInterval() {
        return SPAWN_MIN + random.nextDouble() * (SPAWN_MAX - SPAWN_MIN);
    }

    public void setAlgorithm(SignalAlgorithm algorithm) {
        this.algorithm = algorithm;
        if (this.algorithm != null) {
            this.algorithm.reset();
        }
        intersection.resetToDefaultPhase();
    }

    public void update(double deltaSeconds) {
        if (algorithm == null) {
            return;
        }

        simulationClock += deltaSeconds;
        intersection.updateLights(deltaSeconds);
        algorithm.update(deltaSeconds, intersection, laneCars);

        handleSpawning(deltaSeconds);
        updateCars(deltaSeconds);
    }

    private void handleSpawning(double deltaSeconds) {
        for (Direction direction : Direction.values()) {
            double remaining = spawnTimers.get(direction) - deltaSeconds;
            if (remaining <= 0.0) {
                spawnCar(direction);
                spawnTimers.put(direction, randomInterval());
            } else {
                spawnTimers.put(direction, remaining);
            }
        }
    }

    private void spawnCar(Direction direction) {
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
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        }
        Color color = palette[random.nextInt(palette.length)];
        Car car = new Car(direction, x, y, CAR_SPEED, CAR_LENGTH, color);
        laneCars.get(direction).add(car);
    }

    private void updateCars(double deltaSeconds) {
        for (Direction direction : Direction.values()) {
            List<Car> cars = laneCars.get(direction);
            cars.sort((a, b) -> Double.compare(projectAlong(b), projectAlong(a)));
            Car previous = null;
            for (Car car : cars) {
                boolean atSignal = !hasClearedStopLine(car);
                boolean lightGreen = intersection.getLight(direction).isGreen();
                boolean frontHasSpace = previous == null || gapToPrevious(car, previous) > (CAR_LENGTH + MIN_GAP);
                boolean allowedToMove = !atSignal || (lightGreen && frontHasSpace);
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
                    totalWaitTime += car.getCumulativeWait();
                    completedCars++;
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
        if (completedCars == 0) {
            return 0.0;
        }
        return totalWaitTime / completedCars;
    }

    public int getCompletedCars() {
        return completedCars;
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

    public Set<Direction> getActiveGreens() {
        EnumSet<Direction> greens = EnumSet.noneOf(Direction.class);
        for (Direction direction : Direction.values()) {
            if (intersection.getLight(direction).isGreen()) {
                greens.add(direction);
            }
        }
        return greens;
    }
}
