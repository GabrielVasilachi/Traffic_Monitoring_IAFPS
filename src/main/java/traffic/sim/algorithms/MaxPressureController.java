package traffic.sim.algorithms;

import traffic.sim.model.Car;
import traffic.sim.model.Direction;
import traffic.sim.model.Intersection;
import traffic.sim.model.TrafficLight;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MaxPressureController implements SignalAlgorithm {
    private static final double MIN_HOLD = 3.0;
    private static final double MAX_HOLD = 12.0;
    private static final int SWITCH_THRESHOLD = 2;

    private double timer;
    private boolean eastWestGreen;

    public MaxPressureController() {
        reset();
    }

    @Override
    public void update(double deltaSeconds, Intersection intersection, Map<Direction, List<Car>> approachQueues) {
        timer += deltaSeconds;
        int ewPressure = pressureForPair(approachQueues, Direction.EAST, Direction.WEST);
        int nsPressure = pressureForPair(approachQueues, Direction.NORTH, Direction.SOUTH);

        boolean shouldSwitch = false;
        if (eastWestGreen) {
            if ((nsPressure - ewPressure) >= SWITCH_THRESHOLD && timer >= MIN_HOLD) {
                shouldSwitch = true;
            } else if (timer >= MAX_HOLD) {
                shouldSwitch = true;
            }
        } else {
            if ((ewPressure - nsPressure) >= SWITCH_THRESHOLD && timer >= MIN_HOLD) {
                shouldSwitch = true;
            } else if (timer >= MAX_HOLD) {
                shouldSwitch = true;
            }
        }

        if (shouldSwitch) {
            timer = 0.0;
            eastWestGreen = !eastWestGreen;
            applyState(intersection);
        }
    }

    private int pressureForPair(Map<Direction, List<Car>> queues, Direction dirA, Direction dirB) {
        return queues.getOrDefault(dirA, List.of()).size() + queues.getOrDefault(dirB, List.of()).size();
    }

    private void applyState(Intersection intersection) {
        Set<Direction> ew = EnumSet.of(Direction.EAST, Direction.WEST);
        Set<Direction> ns = EnumSet.of(Direction.NORTH, Direction.SOUTH);
        TrafficLight.LightState ewState = eastWestGreen ? TrafficLight.LightState.GREEN : TrafficLight.LightState.RED;
        TrafficLight.LightState nsState = eastWestGreen ? TrafficLight.LightState.RED : TrafficLight.LightState.GREEN;
        ew.forEach(dir -> intersection.getLight(dir).setState(ewState));
        ns.forEach(dir -> intersection.getLight(dir).setState(nsState));
    }

    @Override
    public String name() {
        return "Max Pressure";
    }

    @Override
    public void reset() {
        timer = 0.0;
        eastWestGreen = true;
    }
}
