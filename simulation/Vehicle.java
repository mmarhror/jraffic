package simulation;

import java.util.ArrayList;
import java.util.List;
import shared.Config;
import shared.Direction;
import shared.LightColor;
import shared.Turn;

public class Vehicle {
    private static final double HALF_LANE = Config.LANE_WIDTH / 2.0;

    // Off-screen targets (separate X/Y so non-square windows work correctly)
    private static final double OFF_LEFT = -Config.VEHICLE_LENGTH;
    private static final double OFF_RIGHT = Config.WINDOW_W + Config.VEHICLE_LENGTH;
    private static final double OFF_TOP = -Config.VEHICLE_LENGTH;
    private static final double OFF_BOTTOM = Config.WINDOW_H + Config.VEHICLE_LENGTH;

    private final Direction direction;
    private final Turn turn;
    private final List<Point> route;
    private final double stopProgress;
    private double progress;
    private double x;
    private double y;
    private double angle;

    public Vehicle(Turn turn_in, Direction direction_in) {
        direction = direction_in;
        turn = turn_in == null ? Turn.STRAIGHT : turn_in;
        route = createRoute();
        stopProgress = route.get(1).distance;
        progress = 0.0;
        updatePosition();
    }

    public Turn getTurn() {
        return turn;
    }

    public Direction getDirection() {
        return direction;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getProgress() {
        return progress;
    }

    public boolean isBeforeStopLine() {
        return progress <= stopProgress;
    }

    public void setTurn(Turn turn) {
        throw new IllegalStateException("A vehicle route cannot change after spawning");
    }

    public void update(double dt, Vehicle ahead, LightColor light) {
        if (dt <= 0.0) {
            return;
        }

        double nextProgress = progress + Config.VEHICLE_SPEED * dt;
        if (light != LightColor.GREEN && progress <= stopProgress) {
            nextProgress = Math.min(nextProgress, stopProgress);
        }

        if (ahead != null && ahead.direction == direction) {
            double maximumProgress = ahead.progress - Config.VEHICLE_LENGTH - Config.SAFETY_GAP;
            if (ahead.progress > progress) {
                nextProgress = Math.min(nextProgress, maximumProgress);
            }
        }

        progress = Math.max(progress, nextProgress);
        updatePosition();
    }

    public boolean isInIntersection() {
        return x >= Config.CENTER_X - Config.INTERSECTION_HALF
            && x <= Config.CENTER_X + Config.INTERSECTION_HALF
            && y >= Config.CENTER_Y - Config.INTERSECTION_HALF
            && y <= Config.CENTER_Y + Config.INTERSECTION_HALF;
    }

    public boolean isOffScreen() {
        double margin = Config.VEHICLE_LENGTH;
        return x < -margin || x > Config.WINDOW_W + margin
                || y < -margin || y > Config.WINDOW_H + margin;
    }

    public double getAngle() {
        return angle;
    }

    /**
     * Lane convention (right-hand driving):
     *   moving SOUTH -> x = cx - HALF_LANE
     *   moving NORTH -> x = cx + HALF_LANE
     *   moving WEST  -> y = cy - HALF_LANE
     *   moving EAST  -> y = cy + HALF_LANE
     *
     * Each turn is a quarter circle: it starts at the stop-line edge,
     * bends around the lane corner (used as the Bezier control point),
     * and ends on the exit-lane edge. This makes right turns tight
     * (they curve BEFORE the center) and left turns sweep wide.
     */
    private List<Point> createRoute() {
        double cx = Config.CENTER_X;
        double cy = Config.CENTER_Y;
        double edge = Config.INTERSECTION_HALF;
        double stopOffset = Config.VEHICLE_LENGTH / 2.0;

        double southLane = cx - HALF_LANE; // traffic moving south
        double northLane = cx + HALF_LANE; // traffic moving north
        double westLane = cy - HALF_LANE;  // traffic moving west
        double eastLane = cy + HALF_LANE;  // traffic moving east

        List<double[]> coordinates = new ArrayList<>();

        switch (direction) {
            case NORTH: {
                double laneX = southLane; // spawns at top, moves south
                coordinates.add(new double[] {laneX, OFF_TOP});
                coordinates.add(new double[] {laneX, cy - edge - stopOffset});
                if (turn == Turn.STRAIGHT) {
                    coordinates.add(new double[] {laneX, OFF_BOTTOM});
                } else if (turn == Turn.RIGHT) {          // -> moving west
                    coordinates.add(new double[] {laneX, cy - edge});
                    addQuarterTurn(coordinates, laneX, cy - edge,
                            laneX, westLane,
                            cx - edge, westLane);
                    coordinates.add(new double[] {OFF_LEFT, westLane});
                } else {                                   // LEFT -> moving east
                    coordinates.add(new double[] {laneX, cy - edge});
                    addQuarterTurn(coordinates, laneX, cy - edge,
                            laneX, eastLane,
                            cx + edge, eastLane);
                    coordinates.add(new double[] {OFF_RIGHT, eastLane});
                }
                break;
            }
            case SOUTH: {
                double laneX = northLane; // spawns at bottom, moves north
                coordinates.add(new double[] {laneX, OFF_BOTTOM});
                coordinates.add(new double[] {laneX, cy + edge + stopOffset});
                if (turn == Turn.STRAIGHT) {
                    coordinates.add(new double[] {laneX, OFF_TOP});
                } else if (turn == Turn.RIGHT) {          // -> moving east
                    coordinates.add(new double[] {laneX, cy + edge});
                    addQuarterTurn(coordinates, laneX, cy + edge,
                            laneX, eastLane,
                            cx + edge, eastLane);
                    coordinates.add(new double[] {OFF_RIGHT, eastLane});
                } else {                                   // LEFT -> moving west
                    coordinates.add(new double[] {laneX, cy + edge});
                    addQuarterTurn(coordinates, laneX, cy + edge,
                            laneX, westLane,
                            cx - edge, westLane);
                    coordinates.add(new double[] {OFF_LEFT, westLane});
                }
                break;
            }
            case EAST: {
                double laneY = westLane; // spawns at right, moves west
                coordinates.add(new double[] {OFF_RIGHT, laneY});
                coordinates.add(new double[] {cx + edge + stopOffset, laneY});
                if (turn == Turn.STRAIGHT) {
                    coordinates.add(new double[] {OFF_LEFT, laneY});
                } else if (turn == Turn.RIGHT) {          // -> moving north
                    coordinates.add(new double[] {cx + edge, laneY});
                    addQuarterTurn(coordinates, cx + edge, laneY,
                            northLane, laneY,
                            northLane, cy - edge);
                    coordinates.add(new double[] {northLane, OFF_TOP});
                } else {                                   // LEFT -> moving south
                    coordinates.add(new double[] {cx + edge, laneY});
                    addQuarterTurn(coordinates, cx + edge, laneY,
                            southLane, laneY,
                            southLane, cy + edge);
                    coordinates.add(new double[] {southLane, OFF_BOTTOM});
                }
                break;
            }
            case WEST: {
                double laneY = eastLane; // spawns at left, moves east
                coordinates.add(new double[] {OFF_LEFT, laneY});
                coordinates.add(new double[] {cx - edge - stopOffset, laneY});
                if (turn == Turn.STRAIGHT) {
                    coordinates.add(new double[] {OFF_RIGHT, laneY});
                } else if (turn == Turn.RIGHT) {          // -> moving south
                    coordinates.add(new double[] {cx - edge, laneY});
                    addQuarterTurn(coordinates, cx - edge, laneY,
                            southLane, laneY,
                            southLane, cy + edge);
                    coordinates.add(new double[] {southLane, OFF_BOTTOM});
                } else {                                   // LEFT -> moving north
                    coordinates.add(new double[] {cx - edge, laneY});
                    addQuarterTurn(coordinates, cx - edge, laneY,
                            northLane, laneY,
                            northLane, cy - edge);
                    coordinates.add(new double[] {northLane, OFF_TOP});
                }
                break;
            }
            default:
                break;
        }

        return withDistances(coordinates);
    }

    private void addQuarterTurn(List<double[]> route, double startX, double startY,
            double controlX, double controlY, double endX, double endY) {
        final int curveSteps = 12;
        for (int step = 1; step <= curveSteps; step++) {
            double t = step / (double) curveSteps;
            double inverse = 1.0 - t;
            double px = inverse * inverse * startX + 2.0 * inverse * t * controlX
                    + t * t * endX;
            double py = inverse * inverse * startY + 2.0 * inverse * t * controlY
                    + t * t * endY;
            route.add(new double[] {px, py});
        }
    }

    private List<Point> withDistances(List<double[]> coordinates) {
        List<Point> points = new ArrayList<>();
        double distance = 0.0;
        for (int index = 0; index < coordinates.size(); index++) {
            double[] coordinate = coordinates.get(index);
            if (index > 0) {
                double[] previous = coordinates.get(index - 1);
                distance += Math.hypot(coordinate[0] - previous[0], coordinate[1] - previous[1]);
            }
            points.add(new Point(coordinate[0], coordinate[1], distance));
        }
        return points;
    }

    private void updatePosition() {
        Point start = route.get(route.size() - 2);
        Point end = route.get(route.size() - 1);
        for (int index = 1; index < route.size(); index++) {
            if (progress <= route.get(index).distance) {
                start = route.get(index - 1);
                end = route.get(index);
                break;
            }
        }

        double segmentLength = end.distance - start.distance;
        double segmentProgress = segmentLength == 0.0
                ? 1.0 : (progress - start.distance) / segmentLength;
        segmentProgress = Math.max(0.0, Math.min(1.0, segmentProgress));
        x = start.x + (end.x - start.x) * segmentProgress;
        y = start.y + (end.y - start.y) * segmentProgress;
        angle = Math.toDegrees(Math.atan2(end.y - start.y, end.x - start.x));
    }

    private static final class Point {
        private final double x;
        private final double y;
        private final double distance;

        private Point(double x, double y, double distance) {
            this.x = x;
            this.y = y;
            this.distance = distance;
        }
    }
}