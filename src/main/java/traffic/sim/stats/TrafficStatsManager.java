package traffic.sim.stats;

import traffic.sim.model.Car;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrafficStatsManager {
    private static final double SAMPLE_INTERVAL_SECONDS = 1.0;

    private double sampleAccumulator;
    private double latestTotalWait;
    private final List<StatsSample> pendingSamples = new ArrayList<>();

    public void update(double deltaSeconds, double simulationTimeSeconds, Iterable<? extends Iterable<? extends Car>> carLanes) {
        latestTotalWait = computeTotalWait(carLanes);
        sampleAccumulator += deltaSeconds;
        while (sampleAccumulator >= SAMPLE_INTERVAL_SECONDS) {
            sampleAccumulator -= SAMPLE_INTERVAL_SECONDS;
            pendingSamples.add(new StatsSample(simulationTimeSeconds, latestTotalWait));
        }
    }

    public double getLatestTotalWait() {
        return latestTotalWait;
    }

    public List<StatsSample> drainSamples() {
        if (pendingSamples.isEmpty()) {
            return Collections.emptyList();
        }
        List<StatsSample> snapshot = new ArrayList<>(pendingSamples);
        pendingSamples.clear();
        return snapshot;
    }

    public void reset() {
        sampleAccumulator = 0.0;
        latestTotalWait = 0.0;
        pendingSamples.clear();
    }

    private double computeTotalWait(Iterable<? extends Iterable<? extends Car>> carLanes) {
        double aggregate = 0.0;
        for (Iterable<? extends Car> lane : carLanes) {
            for (Car car : lane) {
                aggregate += car.getWaitTimer();
            }
        }
        return aggregate;
    }

    public record StatsSample(double timeSeconds, double totalWaitSeconds) {
    }
}
