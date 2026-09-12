package shared;

public final class Config {
    private Config() {}

    public static final int WINDOW_W = 800;
    public static final int WINDOW_H = 800;
    public static final double CENTER_X = WINDOW_W / 2.0;
    public static final double CENTER_Y = WINDOW_H / 2.0;
    public static final double LANE_WIDTH = 40.0;
    public static final double INTERSECTION_HALF = LANE_WIDTH;

    public static final double VEHICLE_LENGTH = 30.0;
    public static final double SAFETY_GAP = 10.0;
    public static final double VEHICLE_SPEED = 60.0;

    public static final double LANE_LENGTH = (WINDOW_W / 2.0) - INTERSECTION_HALF;
    public static final int LANE_CAPACITY = (int) Math.floor(LANE_LENGTH / (VEHICLE_LENGTH + SAFETY_GAP));

    public static final double GREEN_TIME = 5.0;
    public static final double ALL_RED_TIME = 1.0;
    public static final double SPAWN_MIN_GAP = VEHICLE_LENGTH + SAFETY_GAP;
}

