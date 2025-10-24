#!/usr/bin/env bash
# Simple runner for the Traffic Signal Simulator
# Usage: ./run.sh

set -eu
cd "$(dirname "$0")"

# Run the JavaFX application via the OpenJFX Maven plugin (explicit invocation to avoid plugin lookup issues)
mvn org.openjfx:javafx-maven-plugin:0.0.8:run -DmainClass=traffic.sim.TrafficSimulationApp -Djavafx.version=21.0.2
