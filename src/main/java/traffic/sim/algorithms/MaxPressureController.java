package traffic.sim.algorithms;

import traffic.sim.controller.TrafficController;
import traffic.sim.model.Car;
import traffic.sim.model.Direction;

import java.util.List;
import java.util.Map;

public class MaxPressureController implements SignalAlgorithm {
    private static final double MIN_HOLD = 3.0;
    private static final double MAX_HOLD = 12.0;
    private static final int SWITCH_THRESHOLD = 2;

    private double timer;
    private TrafficController.DirectionGroup lastGroup;

    public MaxPressureController() {
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

        int ewPressure = pressureForPair(approachQueues, Direction.EAST, Direction.WEST);
        int nsPressure = pressureForPair(approachQueues, Direction.NORTH, Direction.SOUTH);

        boolean shouldSwitch = false;
        if (active == TrafficController.DirectionGroup.EAST_WEST) {
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
            controller.requestSwitch(active.opposite());
            timer = 0.0;
        }
    }

    private int pressureForPair(Map<Direction, List<Car>> queues, Direction dirA, Direction dirB) {
        return queues.getOrDefault(dirA, List.of()).size() + queues.getOrDefault(dirB, List.of()).size();
    }

    @Override
    public String name() {
        return "Max Pressure";
    }

    @Override
    public void reset(TrafficController controller) {
        timer = 0.0;
        lastGroup = controller.getActiveGroup();
    }
}
