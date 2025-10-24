package traffic.sim;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import traffic.sim.algorithms.FixedTimeController;
import traffic.sim.algorithms.GreenWaveController;
import traffic.sim.algorithms.MaxPressureController;
import traffic.sim.algorithms.SignalAlgorithm;
import traffic.sim.ui.SimulationCanvas;

import java.util.function.Supplier;

public class TrafficSimulationApp extends Application {
    private static final double CANVAS_WIDTH = 900;
    private static final double CANVAS_HEIGHT = 600;

    private SimulationEngine engine;
    private SimulationCanvas canvas;
    private AnimationTimer timer;
    private long lastTimestamp = -1L;
    private boolean running = false;
    private Supplier<SignalAlgorithm> algorithmFactory;

    @Override
    public void start(Stage primaryStage) {
        engine = new SimulationEngine(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas = new SimulationCanvas(CANVAS_WIDTH, CANVAS_HEIGHT, engine);

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        root.setRight(buildControls());

        Scene scene = new Scene(root, CANVAS_WIDTH + 220, CANVAS_HEIGHT);
        primaryStage.setTitle("Traffic Signal Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        canvas.render();
        setupAnimationTimer();
    }

    private VBox buildControls() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Algoritmi Semafor");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ToggleGroup toggleGroup = new ToggleGroup();
        RadioButton fixed = new RadioButton("Fixed Time");
        fixed.setToggleGroup(toggleGroup);
        fixed.setSelected(true);
        fixed.setOnAction(evt -> selectAlgorithm(FixedTimeController::new));

        RadioButton greenWave = new RadioButton("Green Wave");
        greenWave.setToggleGroup(toggleGroup);
        greenWave.setOnAction(evt -> selectAlgorithm(GreenWaveController::new));

        RadioButton maxPressure = new RadioButton("Max Pressure");
        maxPressure.setToggleGroup(toggleGroup);
        maxPressure.setOnAction(evt -> selectAlgorithm(MaxPressureController::new));

        Button startStop = new Button("Start");
        startStop.setMaxWidth(Double.MAX_VALUE);
        startStop.setOnAction(evt -> {
            if (!running) {
                if (algorithmFactory == null) {
                    selectAlgorithm(FixedTimeController::new);
                }
                running = true;
                lastTimestamp = -1L;
                timer.start();
                startStop.setText("Pause");
            } else {
                running = false;
                timer.stop();
                startStop.setText("Start");
            }
        });

        Button resetBtn = new Button("Reset");
        resetBtn.setMaxWidth(Double.MAX_VALUE);
        resetBtn.setOnAction(evt -> {
            running = false;
            timer.stop();
            engine.reset();
            if (algorithmFactory == null) {
                selectAlgorithm(FixedTimeController::new);
            } else {
                engine.setAlgorithm(algorithmFactory.get());
            }
            canvas.render();
        });

        Label info = new Label("Timer peste mașină = timpul de așteptare curent");
        info.setWrapText(true);

        box.getChildren().addAll(title, fixed, greenWave, maxPressure, startStop, resetBtn, info);
        selectAlgorithm(FixedTimeController::new);
        return box;
    }

    private void selectAlgorithm(Supplier<SignalAlgorithm> factory) {
        this.algorithmFactory = factory;
        engine.reset();
        engine.setAlgorithm(factory.get());
        canvas.render();
    }

    private void setupAnimationTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTimestamp < 0) {
                    lastTimestamp = now;
                    return;
                }
                double deltaSeconds = (now - lastTimestamp) / 1_000_000_000.0;
                lastTimestamp = now;
                engine.update(deltaSeconds);
                canvas.render();
            }
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}
