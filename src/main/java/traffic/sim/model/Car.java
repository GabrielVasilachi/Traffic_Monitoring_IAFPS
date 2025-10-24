package traffic.sim.model;

import javafx.scene.paint.Color;

public class Car {
    private final Direction direction;
    private double x;
    private double y;
    private boolean moving;
    private double waitTimer;
    private double cumulativeWait;
    private final double speed;
    private final double length;
    private final Color color;

    public Car(Direction direction, double x, double y, double speed, double length, Color color) {
        this.direction = direction;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.length = length;
        this.color = color;
        this.moving = true;
        this.waitTimer = 0.0;
        this.cumulativeWait = 0.0;
    }

    public void update(double deltaSeconds, boolean allowedToMove) {
        if (allowedToMove) {
            moving = true;
            waitTimer = 0.0;
            double distance = speed * deltaSeconds;
            x += distance * direction.dx();
            y += distance * direction.dy();
        } else {
            moving = false;
            waitTimer += deltaSeconds;
            cumulativeWait += deltaSeconds;
        }
    }

    public void resetWaitTimer() {
        waitTimer = 0.0;
    }

    public Direction getDirection() {
        return direction;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isMoving() {
        return moving;
    }

    public double getWaitTimer() {
        return waitTimer;
    }

    public double getCumulativeWait() {
        return cumulativeWait;
    }

    public double getSpeed() {
        return speed;
    }

    public double getLength() {
        return length;
    }

    public Color getColor() {
        return color;
    }
}
