package traffic.sim.algorithms;

import traffic.sim.model.Direction;
import traffic.sim.model.Intersection;
import traffic.sim.model.TrafficLight;
import traffic.sim.model.Car;

import java.util.List;
import java.util.Map;

public class FixedTimeController implements SignalAlgorithm {
    private final double phaseDuration;
    private double timer;
    private boolean eastWestGreen;

    public FixedTimeController(double phaseDurationSeconds) {
        this.phaseDuration = phaseDurationSeconds;
        reset();
    }

    public FixedTimeController() {
        this(8.0);
    }

    @Override
    public void update(double deltaSeconds, Intersection intersection, Map<Direction, List<Car>> approachQueues) {
        timer += deltaSeconds;
        if (timer >= phaseDuration) {
            timer = 0.0;
            eastWestGreen = !eastWestGreen;
            applyState(intersection);
        }
    }

    private void applyState(Intersection intersection) {
        TrafficLight.LightState ewState = eastWestGreen ? TrafficLight.LightState.GREEN : TrafficLight.LightState.RED;
        TrafficLight.LightState nsState = eastWestGreen ? TrafficLight.LightState.RED : TrafficLight.LightState.GREEN;
        intersection.setPairState(Direction.EAST, Direction.WEST, ewState);
        intersection.setPairState(Direction.NORTH, Direction.SOUTH, nsState);
    }

    @Override
    public String name() {
        return "Fixed Time";
    }

    @Override
    public void reset() {
        timer = 0.0;
        eastWestGreen = true;
    }
}
