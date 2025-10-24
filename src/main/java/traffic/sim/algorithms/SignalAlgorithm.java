package traffic.sim.algorithms;

import traffic.sim.model.Car;
import traffic.sim.model.Direction;
import traffic.sim.model.Intersection;

import java.util.List;
import java.util.Map;

public interface SignalAlgorithm {
    void update(double deltaSeconds, Intersection intersection, Map<Direction, List<Car>> approachQueues);

    String name();

    default void reset() {
        // Optional to override when algorithm keeps additional state
    }
}
