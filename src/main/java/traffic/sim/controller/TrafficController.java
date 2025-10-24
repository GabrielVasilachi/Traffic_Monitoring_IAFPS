package traffic.sim.controller;

import traffic.sim.model.Direction;
import traffic.sim.model.Intersection;
import traffic.sim.model.TrafficLight;

import java.util.EnumSet;
import java.util.Set;

public class TrafficController {
    public enum DirectionGroup {
        EAST_WEST(EnumSet.of(Direction.EAST, Direction.WEST)),
        NORTH_SOUTH(EnumSet.of(Direction.NORTH, Direction.SOUTH));

        private final Set<Direction> directions;

        DirectionGroup(Set<Direction> directions) {
            this.directions = directions;
        }

        public Set<Direction> directions() {
            return directions;
        }

        public DirectionGroup opposite() {
            return this == EAST_WEST ? NORTH_SOUTH : EAST_WEST;
        }
    }

    private enum PhaseState {
        GREEN,
        YELLOW
    }

    private static final double BASE_GREEN_MIN = 5.0;
    private static final double YELLOW_DURATION = 2.0;

    private final Intersection intersection;
    private DirectionGroup activeGroup;
    private DirectionGroup targetGroup;
    private PhaseState phaseState;
    private double stateTimer;
    private double minGreenForCurrentPhase;

    public TrafficController(Intersection intersection) {
        this.intersection = intersection;
        reset(DirectionGroup.EAST_WEST);
    }

    public void reset(DirectionGroup startGroup) {
        this.activeGroup = startGroup;
        this.targetGroup = startGroup;
        this.phaseState = PhaseState.GREEN;
        this.stateTimer = 0.0;
        this.minGreenForCurrentPhase = BASE_GREEN_MIN;
        applyGroupState(activeGroup, TrafficLight.LightState.GREEN);
        applyGroupState(activeGroup.opposite(), TrafficLight.LightState.RED);
    }

    public void requestSwitch(DirectionGroup desiredGroup) {
        if (desiredGroup == null) {
            return;
        }
        this.targetGroup = desiredGroup;
    }

    public void enforceMinimumGreen(double minDurationSeconds) {
        this.minGreenForCurrentPhase = Math.max(BASE_GREEN_MIN, minDurationSeconds);
    }

    public void update(double deltaSeconds) {
        stateTimer += deltaSeconds;

        switch (phaseState) {
            case GREEN -> {
                if (shouldBeginYellowPhase()) {
                    startYellowPhase();
                }
            }
            case YELLOW -> {
                if (stateTimer >= YELLOW_DURATION) {
                    completeTransition();
                }
            }
        }
    }

    private boolean shouldBeginYellowPhase() {
        if (targetGroup == activeGroup) {
            return false;
        }
        return stateTimer >= minGreenForCurrentPhase;
    }

    private void startYellowPhase() {
        phaseState = PhaseState.YELLOW;
        stateTimer = 0.0;
        applyGroupState(activeGroup, TrafficLight.LightState.YELLOW);
    }

    private void completeTransition() {
        applyGroupState(activeGroup, TrafficLight.LightState.RED);
        activeGroup = targetGroup;
        phaseState = PhaseState.GREEN;
        stateTimer = 0.0;
        minGreenForCurrentPhase = BASE_GREEN_MIN;
        applyGroupState(activeGroup, TrafficLight.LightState.GREEN);
        applyGroupState(activeGroup.opposite(), TrafficLight.LightState.RED);
    }

    private void applyGroupState(DirectionGroup group, TrafficLight.LightState state) {
        for (Direction direction : group.directions()) {
            intersection.getLight(direction).setState(state);
        }
    }

    public Intersection getIntersection() {
        return intersection;
    }

    public DirectionGroup getActiveGroup() {
        return activeGroup;
    }

    public DirectionGroup getTargetGroup() {
        return targetGroup;
    }

    public boolean isTransitioning() {
        return phaseState == PhaseState.YELLOW;
    }

    public double getStateTimer() {
        return stateTimer;
    }

    public TrafficLight.LightState getState(Direction direction) {
        return intersection.getLight(direction).getState();
    }

    public boolean isGreen(Direction direction) {
        return intersection.getLight(direction).isGreen();
    }

    public boolean isYellow(Direction direction) {
        return intersection.getLight(direction).isYellow();
    }

    public boolean isRed(Direction direction) {
        return intersection.getLight(direction).isRed();
    }

    public DirectionGroup groupFor(Direction direction) {
        return DirectionGroup.EAST_WEST.directions().contains(direction)
                ? DirectionGroup.EAST_WEST
                : DirectionGroup.NORTH_SOUTH;
    }
}
