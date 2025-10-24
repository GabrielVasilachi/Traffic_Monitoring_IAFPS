package traffic.sim.model;

public class TrafficLight {
    public enum LightState {
        RED,
        GREEN
    }

    private final Direction direction;
    private LightState state;
    private double timeInState;

    public TrafficLight(Direction direction, LightState initialState) {
        this.direction = direction;
        this.state = initialState;
        this.timeInState = 0.0;
    }

    public void update(double deltaSeconds) {
        timeInState += deltaSeconds;
    }

    public void setState(LightState newState) {
        if (this.state != newState) {
            this.state = newState;
            this.timeInState = 0.0;
        }
    }

    public LightState getState() {
        return state;
    }

    public double getTimeInState() {
        return timeInState;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isGreen() {
        return state == LightState.GREEN;
    }
}
