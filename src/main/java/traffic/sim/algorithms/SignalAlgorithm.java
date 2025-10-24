package traffic.sim.algorithms;

import traffic.sim.controller.TrafficController;
import traffic.sim.model.Car;
import traffic.sim.model.Direction;

import java.util.List;
import java.util.Map;

public interface SignalAlgorithm {
    void update(double deltaSeconds, TrafficController controller, Map<Direction, List<Car>> approachQueues);

    String name();

    default void reset(TrafficController controller) {
        // Optional to override when algorithm keeps additional state
    }
}
