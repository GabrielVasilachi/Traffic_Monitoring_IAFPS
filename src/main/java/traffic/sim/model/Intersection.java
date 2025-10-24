package traffic.sim.model;

import java.util.EnumMap;
import java.util.Map;

public class Intersection {
    private final Map<Direction, TrafficLight> lights = new EnumMap<>(Direction.class);

    public Intersection() {
        lights.put(Direction.NORTH, new TrafficLight(Direction.NORTH, TrafficLight.LightState.RED));
        lights.put(Direction.SOUTH, new TrafficLight(Direction.SOUTH, TrafficLight.LightState.RED));
        lights.put(Direction.EAST, new TrafficLight(Direction.EAST, TrafficLight.LightState.GREEN));
        lights.put(Direction.WEST, new TrafficLight(Direction.WEST, TrafficLight.LightState.GREEN));
    }

    public void resetToDefaultPhase() {
        lights.get(Direction.EAST).setState(TrafficLight.LightState.GREEN);
        lights.get(Direction.WEST).setState(TrafficLight.LightState.GREEN);
        lights.get(Direction.NORTH).setState(TrafficLight.LightState.RED);
        lights.get(Direction.SOUTH).setState(TrafficLight.LightState.RED);
    }

    public TrafficLight getLight(Direction direction) {
        return lights.get(direction);
    }

    public Map<Direction, TrafficLight> getLights() {
        return lights;
    }

    public void setPairState(Direction dirA, Direction dirB, TrafficLight.LightState state) {
        lights.get(dirA).setState(state);
        lights.get(dirB).setState(state);
    }

    public void updateLights(double deltaSeconds) {
        lights.values().forEach(light -> light.update(deltaSeconds));
    }
}
