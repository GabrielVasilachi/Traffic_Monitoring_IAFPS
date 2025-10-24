package traffic.sim.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import traffic.sim.SimulationEngine;
import traffic.sim.model.Car;
import traffic.sim.model.Direction;
import traffic.sim.model.TrafficLight;

import java.util.Map;

public class SimulationCanvas extends Canvas {
    private static final double ROAD_WIDTH = 120.0;
    private final SimulationEngine engine;

    public SimulationCanvas(double width, double height, SimulationEngine engine) {
        super(width, height);
        this.engine = engine;
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFont(Font.font("Monospaced", 12));
    }

    public void render() {
        GraphicsContext gc = getGraphicsContext2D();
        double width = getWidth();
        double height = getHeight();
        double centerX = width / 2.0;
        double centerY = height / 2.0;

        gc.setFill(Color.DARKSLATEGRAY);
        gc.fillRect(0, 0, width, height);

        gc.setFill(Color.DIMGRAY);
        gc.fillRect(0, centerY - ROAD_WIDTH / 2.0, width, ROAD_WIDTH);
        gc.fillRect(centerX - ROAD_WIDTH / 2.0, 0, ROAD_WIDTH, height);

        gc.setFill(Color.DARKGRAY);
        gc.fillRect(centerX - ROAD_WIDTH / 6.0, 0, ROAD_WIDTH / 3.0, height);
        gc.fillRect(0, centerY - ROAD_WIDTH / 6.0, width, ROAD_WIDTH / 3.0);

        drawIntersectionBox(gc, centerX, centerY);
        drawTrafficLights(gc, centerX, centerY);
        drawCars(gc);
        drawStatistics(gc);
    }

    private void drawIntersectionBox(GraphicsContext gc, double centerX, double centerY) {
        double size = engine.getIntersectionHalfSize() * 2.0;
        gc.setFill(Color.web("#2f2f2f"));
        gc.fillRect(centerX - size / 2.0, centerY - size / 2.0, size, size);
    }

    private void drawTrafficLights(GraphicsContext gc, double centerX, double centerY) {
        double offset = engine.getIntersectionHalfSize() + 20.0;
        double radius = 10.0;
        Map<Direction, TrafficLight> lights = engine.getIntersection().getLights();

        drawLight(gc, centerX - offset, centerY - offset, lights.get(Direction.NORTH));
        drawLight(gc, centerX + offset - radius, centerY - offset, lights.get(Direction.EAST));
        drawLight(gc, centerX - offset, centerY + offset - radius, lights.get(Direction.WEST));
        drawLight(gc, centerX + offset - radius, centerY + offset - radius, lights.get(Direction.SOUTH));
    }

    private void drawLight(GraphicsContext gc, double x, double y, TrafficLight light) {
        double radius = 12.0;
        gc.setFill(light.isGreen() ? Color.LIMEGREEN : Color.RED);
        gc.fillOval(x, y, radius, radius);
    }

    private void drawCars(GraphicsContext gc) {
        double carLength = engine.getCarLength();
        double carWidth = engine.getCarWidth();
        for (Car car : engine.getAllCars()) {
            Direction direction = car.getDirection();
            double drawX;
            double drawY;
            double w;
            double h;
            if (direction.dx() != 0) {
                w = carLength;
                h = carWidth;
                drawX = car.getX() - w / 2.0;
                drawY = car.getY() - h / 2.0;
            } else {
                w = carWidth;
                h = carLength;
                drawX = car.getX() - w / 2.0;
                drawY = car.getY() - h / 2.0;
            }
            gc.setFill(car.getColor());
            gc.fillRoundRect(drawX, drawY, w, h, 6, 6);

            gc.setFill(Color.BLACK);
            gc.fillText(String.format("%.1f", car.getWaitTimer()), car.getX() - 12, drawY - 4);
        }
    }

    private void drawStatistics(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        String text = "Average wait: " + String.format("%.1f s", engine.getAverageWait());
        gc.fillText(text, 20, getHeight() - 20);
    }
}
