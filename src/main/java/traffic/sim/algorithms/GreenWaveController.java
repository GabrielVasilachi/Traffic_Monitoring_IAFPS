package traffic.sim.algorithms;

import traffic.sim.model.Car;
import traffic.sim.model.Direction;
import traffic.sim.model.Intersection;
import traffic.sim.model.TrafficLight;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GreenWaveController implements SignalAlgorithm {
    private static final double BASE_GREEN_DURATION = 8.0;
    private static final double EXTENSION = 4.0;
    private static final double MIN_DURATION = 4.0;

    private double timer;
    private boolean eastWestGreen;

    public GreenWaveController() {
        reset();
    }

    @Override
    public void update(double deltaSeconds, Intersection intersection, Map<Direction, List<Car>> approachQueues) {
        timer += deltaSeconds;
        double activeDuration = eastWestGreen ? durationForPair(approachQueues, Direction.EAST, Direction.WEST)
                                              : durationForPair(approachQueues, Direction.NORTH, Direction.SOUTH);
        if (timer >= activeDuration) {
            timer = 0.0;
            eastWestGreen = !eastWestGreen;
            applyState(intersection);
        }
    }

    private double durationForPair(Map<Direction, List<Car>> queues, Direction dirA, Direction dirB) {
        int queued = queueSize(queues, dirA) + queueSize(queues, dirB);
        if (queued >= 3) {
            return BASE_GREEN_DURATION + EXTENSION;
        }
        if (queued == 0) {
            return MIN_DURATION;
        }
        return BASE_GREEN_DURATION;
    }

    private int queueSize(Map<Direction, List<Car>> queues, Direction direction) {
        return queues.getOrDefault(direction, List.of()).size();
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
        return "Green Wave";
    }

    @Override
    public void reset() {
        timer = 0.0;
        eastWestGreen = true;
    }
}
