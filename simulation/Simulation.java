package simulation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import shared.Config;
import shared.Direction;
import shared.Turn;
import traffic.LightController;

public class Simulation {
  private final List<Vehicle> vehicles = new ArrayList<>();
  private final Random random = new Random();

  public void spawnVehicle(Direction direction) {
    for (Vehicle vehicle : vehicles) {
      if (vehicle.getDirection() == direction && vehicle.getDistance() < Config.SPAWN_MIN_GAP)
        return;
    }
    vehicles.add(new Vehicle(direction, Turn.values()[random.nextInt(Turn.values().length)]));
  }

  public void update(double dt, LightController lights) {
    Map<Direction, List<Vehicle>> byDirection = new EnumMap<>(Direction.class);
    for (Direction direction : Direction.values()) byDirection.put(direction, new ArrayList<>());
    for (Vehicle vehicle : vehicles) byDirection.get(vehicle.getDirection()).add(vehicle);
    for (List<Vehicle> lane : byDirection.values()) {
      lane.sort(Comparator.comparingDouble(Vehicle::getDistance).reversed());
      for (int i = 0; i < lane.size(); i++) {
        Vehicle ahead = i == 0 ? null : lane.get(i - 1);
        lane.get(i).update(dt, ahead, lights.getLightFor(lane.get(i).getDirection()));
      }
    }
    vehicles.removeIf(Vehicle::isOffScreen);
  }

  public int getQueueLength(Direction direction) {
    int count = 0;
    for (Vehicle vehicle : vehicles)
      if (vehicle.getDirection() == direction && vehicle.isWaiting()) count++;
    return count;
  }

  public boolean isIntersectionOccupied() {
    for (Vehicle vehicle : vehicles) {
      if (vehicle.getDistance() >= Config.LANE_LENGTH
          && vehicle.getDistance() < Config.LANE_LENGTH + 120) return true;
    }
    return false;
  }

  public List<Vehicle> getVehicles() {
    return new ArrayList<>(vehicles);
  }

  public int getWaitingCount(Direction direction) {
    return getQueueLength(direction);
  }
}
