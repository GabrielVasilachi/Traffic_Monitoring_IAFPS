package traffic.sim.algorithms;

import traffic.sim.controller.TrafficController;
import traffic.sim.model.Car;
import traffic.sim.model.Direction;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GreenWaveController implements SignalAlgorithm {
    private static final double BASE_GREEN_DURATION = 8.0;
    private static final double EXTENSION = 4.0;
    private static final double MIN_DURATION = 5.0;

    private double timer;
    private TrafficController.DirectionGroup lastGroup;

    public GreenWaveController() {
    }

    @Override
    public void update(double deltaSeconds, TrafficController controller, Map<Direction, List<Car>> approachQueues) {
        timer += deltaSeconds;

        TrafficController.DirectionGroup active = controller.getActiveGroup();

        if (lastGroup == null || active != lastGroup) {
            lastGroup = active;
            timer = 0.0;
        }

        if (controller.isTransitioning() || controller.getTargetGroup() != controller.getActiveGroup()) {
            return;
        }

        double activeDuration = durationForGroup(approachQueues, active);

        if (timer >= activeDuration) {
            controller.requestSwitch(active.opposite());
            timer = 0.0;
        }
    }

    private double durationForGroup(Map<Direction, List<Car>> queues, TrafficController.DirectionGroup group) {
        Set<Direction> dirs = group == TrafficController.DirectionGroup.EAST_WEST
                ? EnumSet.of(Direction.EAST, Direction.WEST)
                : EnumSet.of(Direction.NORTH, Direction.SOUTH);
        int queued = dirs.stream().mapToInt(dir -> queueSize(queues, dir)).sum();
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

    @Override
    public String name() {
        return "Green Wave";
    }

    @Override
    public void reset(TrafficController controller) {
        timer = 0.0;
        lastGroup = controller.getActiveGroup();
    }
}
