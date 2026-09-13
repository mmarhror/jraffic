package traffic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import shared.Config;
import shared.Direction;
import shared.LightColor;
import simulation.Simulation;

public class LightController {
  private static final double LIGHT_SWITCH_INTERVAL = 3000.0;
  private static final List<Direction> CYCLE =
      List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);

  private Map<Direction, TrafficLight> lights = new HashMap<>();
  private Direction currGreen;
  private double greenTimer;
  private boolean extended;

  public LightController() {
    for (Direction d : Direction.values()) {
      this.lights.put(d, new TrafficLight(d));
    }

    this.currGreen = Direction.NORTH;
    this.greenTimer = LIGHT_SWITCH_INTERVAL;
    this.extended = false;

    applyLights();
  }

  public LightColor getLightFor(Direction dir) {
    return lights.get(dir).getColor();
  }

  public List<TrafficLight> getLights() {
    return new ArrayList<>(lights.values());
  }

  private void applyLights() {
    for (Direction d : Direction.values()) {
      lights.get(d).setColor(d == currGreen ? LightColor.GREEN : LightColor.RED);
    }
  }

  public void update(double secs, Simulation sim) {
    double ms = secs * 1000.0;
    greenTimer -= ms;

    if (greenTimer <= 0) {

      // 1. Check congestion extension first (while still green)
      if (shouldExtend(sim)) {
        applyExtension();
        verifySafety();
        return;
      }

      // 2. Clear intersection: Turn ALL lights RED immediately.
      // This forces incoming spammed cars to halt at their stop lines.
      turnAllRed();

      // 3. Safety Interlock: wait for cars already inside to exit
      if (isOccupied(sim)) {
        delaySwitch(); // Wait 100ms with all lights remaining RED
        verifySafety();
        return;
      }

      // 4. Once intersection is completely clear, switch to next green light
      switchToNextLight(sim);
    }

    verifySafety();
  }

  // Delay
  private boolean isOccupied(Simulation sim) {
    if (sim == null) return false;
    return sim.isIntersectionOccupied();
  }

  private void delaySwitch() {
    greenTimer = 100.0;
  }

  private void turnAllRed() {
    for (TrafficLight tl : lights.values()) {
      tl.setColor(LightColor.RED);
    }
  }

  // Extension
  private boolean shouldExtend(Simulation sim) {
    if (extended) {
      return false;
    }

    int waiting = getWaiting(currGreen, sim);
    int threshold = (int) Math.ceil(Config.LANE_CAPACITY * 0.8);

    return waiting >= threshold;
  }

  private void applyExtension() {
    this.greenTimer = 3000.0;
    this.extended = true;
  }

  // Switch
  private void switchToNextLight(Simulation sim) {
    Direction next = pickNextDirection(sim);
    this.currGreen = next;
    this.greenTimer = 3000.0;
    this.extended = false;

    applyLights();
  }

  private Direction pickNextDirection(Simulation sim) {
    int start = CYCLE.indexOf(currGreen);

    for (int i = 1; i <= 4; i++) {
      Direction candidate = CYCLE.get((start + i) % 4);
      if (getWaiting(candidate, sim) > 0) {
        return candidate;
      }
    }

    return CYCLE.get((start + 1) % 4);
  }

  private int getWaiting(Direction dir, Simulation sim) {
    if (sim == null) return 0;
    return sim.getWaitingCount(dir);
  }

  // Verify mutual exclusion
  private void verifySafety() {
    int greenCount = 0;
    for (TrafficLight tl : lights.values()) {
      if (tl.getColor() == LightColor.GREEN) {
        greenCount++;
      }
    }
    if (greenCount > 1) {
      throw new AssertionError("SAFETY VIOLATION: " + greenCount + " green lights active!");
    }
  }
}
