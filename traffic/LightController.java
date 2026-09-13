package traffic;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import shared.Config;
import shared.Direction;
import shared.LightColor;
import simulation.Simulation;

public class LightController {
    private static final double EXTENSION_SECONDS = 3.0;

    private enum Phase {
        NORTH_GREEN,
        ALL_RED_AFTER_NORTH,
        SOUTH_GREEN,
        ALL_RED_AFTER_SOUTH,
        EAST_GREEN,
        ALL_RED_AFTER_EAST,
        WEST_GREEN,
        ALL_RED_AFTER_WEST
    }

    private final Map<Direction, TrafficLight> lights = new EnumMap<>(Direction.class);
    private Phase phase = Phase.NORTH_GREEN;
    private double remaining = Config.GREEN_TIME;
    private boolean extensionUsed;

    public LightController() {
        for (Direction direction : Direction.values()) {
            lights.put(direction, new TrafficLight(direction));
        }
        applyLights();
    }

    public LightColor getLightFor(Direction direction) {
        return lights.get(direction).getColor();
    }

    public List<TrafficLight> getLights() {
        return new ArrayList<>(lights.values());
    }

    public void update(double seconds, Simulation simulation) {
        if (seconds <= 0.0) {
            return;
        }

        remaining -= seconds;
        if (remaining > 0.0) {
            return;
        }

        if (isGreenPhase() && shouldExtend(simulation) && !extensionUsed) {
            remaining = EXTENSION_SECONDS;
            extensionUsed = true;
            return;
        }

        if (isGreenPhase() && simulation != null && simulation.isIntersectionOccupied()) {
            remaining = 0.1;
            return;
        }

        advancePhase();
    }

    private boolean isGreenPhase() {
        return phase == Phase.NORTH_GREEN || phase == Phase.SOUTH_GREEN
                || phase == Phase.EAST_GREEN || phase == Phase.WEST_GREEN;
    }

    private Direction greenDirection() {
        switch (phase) {
            case NORTH_GREEN:
                return Direction.NORTH;
            case SOUTH_GREEN:
                return Direction.SOUTH;
            case EAST_GREEN:
                return Direction.EAST;
            case WEST_GREEN:
                return Direction.WEST;
            default:
                return null;
        }
    }

    private boolean shouldExtend(Simulation simulation) {
        if (simulation == null || greenDirection() == null) {
            return false;
        }
        int threshold = (int) Math.ceil(Config.LANE_CAPACITY * 0.8);
        return simulation.getQueueLength(greenDirection()) >= threshold;
    }

    private void advancePhase() {
        switch (phase) {
            case NORTH_GREEN:
                phase = Phase.ALL_RED_AFTER_NORTH;
                break;
            case ALL_RED_AFTER_NORTH:
                phase = Phase.SOUTH_GREEN;
                break;
            case SOUTH_GREEN:
                phase = Phase.ALL_RED_AFTER_SOUTH;
                break;
            case ALL_RED_AFTER_SOUTH:
                phase = Phase.EAST_GREEN;
                break;
            case EAST_GREEN:
                phase = Phase.ALL_RED_AFTER_EAST;
                break;
            case ALL_RED_AFTER_EAST:
                phase = Phase.WEST_GREEN;
                break;
            case WEST_GREEN:
                phase = Phase.ALL_RED_AFTER_WEST;
                break;
            case ALL_RED_AFTER_WEST:
                phase = Phase.NORTH_GREEN;
                break;
            default:
                break;
        }
        remaining = isGreenPhase() ? Config.GREEN_TIME : Config.ALL_RED_TIME;
        extensionUsed = false;
        applyLights();
    }

    private void applyLights() {
        Direction green = greenDirection();
        for (Direction direction : Direction.values()) {
            lights.get(direction).setColor(direction == green ? LightColor.GREEN : LightColor.RED);
        }
    }
}
