package traffic.sim.algorithms;

import traffic.sim.controller.TrafficController;
import traffic.sim.model.Car;
import traffic.sim.model.Direction;

import java.util.List;
import java.util.Map;

public class FixedTimeController implements SignalAlgorithm {
    private final double phaseDuration;
    private double timer;
    private TrafficController.DirectionGroup lastGroup;

    public FixedTimeController(double phaseDurationSeconds) {
        this.phaseDuration = phaseDurationSeconds;
    }

    public FixedTimeController() {
        this(8.0);
    }

    @Override
    public void update(double deltaSeconds, TrafficController controller, Map<Direction, List<Car>> approachQueues) {
        timer += deltaSeconds;

        if (lastGroup == null) {
            lastGroup = controller.getActiveGroup();
        }

        if (controller.getActiveGroup() != lastGroup) {
            lastGroup = controller.getActiveGroup();
            timer = 0.0;
        }

        if (controller.isTransitioning()) {
            return;
        }

        if (controller.getTargetGroup() != controller.getActiveGroup()) {
            return;
        }

        if (timer >= phaseDuration) {
            controller.requestSwitch(controller.getActiveGroup().opposite());
            timer = 0.0;
        }
    }

    @Override
    public String name() {
        return "Fixed Time";
    }

    @Override
    public void reset(TrafficController controller) {
        timer = 0.0;
        lastGroup = controller.getActiveGroup();
    }
}
