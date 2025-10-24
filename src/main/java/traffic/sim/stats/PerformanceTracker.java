package traffic.sim.stats;

import java.util.ArrayList;
import java.util.List;

public class PerformanceTracker {
    private static final double SAMPLE_INTERVAL = 1.0;

    private double totalWaitTime;
    private int completedCars;
    private double sampleAccumulator;
    private final List<PerformanceSample> pendingSamples = new ArrayList<>();

    public void recordCarFinished(double waitTimeSeconds) {
        totalWaitTime += waitTimeSeconds;
        completedCars++;
    }

    public double getAverageWait() {
        if (completedCars == 0) {
            return 0.0;
        }
        return totalWaitTime / completedCars;
    }

    public int getCompletedCars() {
        return completedCars;
    }

    public void update(double deltaSeconds, double simulationTimeSeconds) {
        sampleAccumulator += deltaSeconds;
        while (sampleAccumulator >= SAMPLE_INTERVAL) {
            sampleAccumulator -= SAMPLE_INTERVAL;
            pendingSamples.add(new PerformanceSample(simulationTimeSeconds, getAverageWait()));
        }
    }

    public List<PerformanceSample> drainSamples() {
        List<PerformanceSample> snapshot = new ArrayList<>(pendingSamples);
        pendingSamples.clear();
        return snapshot;
    }

    public void reset() {
        totalWaitTime = 0.0;
        completedCars = 0;
        sampleAccumulator = 0.0;
        pendingSamples.clear();
    }

    public record PerformanceSample(double timeSeconds, double averageWaitSeconds) {
    }
}
