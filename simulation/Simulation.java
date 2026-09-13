package simulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import shared.Config;
import shared.Direction;
import shared.LightColor;
import shared.Turn;

public class Simulation {
  private final List<Vehicle> vehicles = new ArrayList<>();

  public List<Vehicle> getVehicles() {
    return List.copyOf(vehicles);
  }

  public int getQueueLength(Direction dir) {
    int length = 0;
    for (Vehicle vehicle : vehicles) {
      if (vehicle.getDirection() == dir && vehicle.isBeforeStopLine()) {
        length++;
      }
    }
    return length;
  }

  public void spawnVehicle(Direction dir) {
    if (dir == null || isSpawnBlocked(dir)) {
      return;
    }

    if (getQueueLength(dir) >= Config.LANE_CAPACITY) {
      return;
    }

    Turn[] turns = Turn.values();
    Turn turn = turns[ThreadLocalRandom.current().nextInt(turns.length)];
    vehicles.add(new Vehicle(turn, dir));
  }

  public void update(double dt, traffic.LightController lights) {
    if (dt <= 0.0 || lights == null) {
      return;
    }

    List<Vehicle> updateOrder = new ArrayList<>(vehicles);
    updateOrder.sort(Comparator.comparingDouble(Vehicle::getProgress).reversed());
    for (Vehicle vehicle : updateOrder) {
      Vehicle ahead = findVehicleAhead(vehicle);
      boolean intersectionBlocked =
          !vehicle.isInIntersection()
              && vehicles.stream().anyMatch(other -> other != vehicle && other.isInIntersection());
      LightColor light =
          intersectionBlocked ? LightColor.RED : lights.getLightFor(vehicle.getDirection());
      vehicle.update(dt, ahead, light);
    }

    vehicles.removeIf(Vehicle::isOffScreen);
  }

  public boolean isIntersectionOccupied() {
    return vehicles.stream().anyMatch(Vehicle::isInIntersection);
  }

  public int getWaitingCount(Direction dir) {
    return getQueueLength(dir);
  }

  private boolean isSpawnBlocked(Direction dir) {
    return vehicles.stream()
        .anyMatch(
            vehicle ->
                vehicle.getDirection() == dir
                    && vehicle.getProgress() >= 0.0
                    && vehicle.getProgress() < Config.SPAWN_MIN_GAP);
  }

  private Vehicle findVehicleAhead(Vehicle vehicle) {
    Vehicle closest = null;
    for (Vehicle candidate : vehicles) {
      if (candidate == vehicle
          || candidate.getDirection() != vehicle.getDirection()
          || candidate.getProgress() <= vehicle.getProgress()) {
        continue;
      }
      if (closest == null || candidate.getProgress() < closest.getProgress()) {
        closest = candidate;
      }
    }
    return closest;
  }
}
