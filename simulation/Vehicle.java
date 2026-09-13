package simulation;

import java.util.ArrayList;
import java.util.List;
import shared.Config;
import shared.Direction;
import shared.LightColor;
import shared.Turn;

public class Vehicle {
    private static final double HALF_LANE = Config.LANE_WIDTH / 2.0;
    private static final double ROUTE_END = Config.WINDOW_W + Config.VEHICLE_LENGTH / 2.0;

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

    private List<Point> createRoute() {
        double centerX = Config.CENTER_X;
        double centerY = Config.CENTER_Y;
        double leftLane = centerX - HALF_LANE;
        double rightLane = centerX + HALF_LANE;
        double topLane = centerY - HALF_LANE;
        double bottomLane = centerY + HALF_LANE;
        double half = Config.INTERSECTION_HALF;
        double stopOffset = Config.VEHICLE_LENGTH / 2.0;
        double stopNear = centerX - half - stopOffset;
        double stopFar = centerX + half + stopOffset;
        double end = ROUTE_END;
        List<double[]> coordinates = new ArrayList<>();

        switch (direction) {
            case NORTH:
                coordinates.add(new double[] {leftLane, -stopOffset});
                coordinates.add(new double[] {leftLane, stopNear});
                addTurn(coordinates, turn, leftLane, centerY, end, true);
                break;
            case SOUTH:
                coordinates.add(new double[] {rightLane, Config.WINDOW_H + stopOffset});
                coordinates.add(new double[] {rightLane, stopFar});
                addTurn(coordinates, turn, rightLane, centerY, -stopOffset, false);
                break;
            case EAST:
                coordinates.add(new double[] {Config.WINDOW_W + stopOffset, topLane});
                coordinates.add(new double[] {stopFar, topLane});
                addTurn(coordinates, turn, centerX, topLane, -stopOffset, false);
                break;
            case WEST:
                coordinates.add(new double[] {-stopOffset, bottomLane});
                coordinates.add(new double[] {stopNear, bottomLane});
                addTurn(coordinates, turn, centerX, bottomLane, end, true);
                break;
            default:
                break;
        }

        return withDistances(coordinates);
    }

    private void addTurn(List<double[]> coordinates, Turn selectedTurn, double entryX,
            double entryY, double exitEnd, boolean positiveExit) {
        if (selectedTurn == Turn.STRAIGHT) {
            if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                coordinates.add(new double[] {entryX, exitEnd});
            } else {
                coordinates.add(new double[] {exitEnd, entryY});
            }
            return;
        }

        boolean turnLeft = selectedTurn == Turn.LEFT;
        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            boolean exitPositive = direction == Direction.NORTH ? turnLeft : !turnLeft;
            double horizontalExit = exitPositive
                    ? Config.CENTER_X + Config.INTERSECTION_HALF
                    : Config.CENTER_X - Config.INTERSECTION_HALF;
            double exitY = exitPositive
                ? Config.CENTER_Y + Config.LANE_WIDTH / 2.0
                : Config.CENTER_Y - Config.LANE_WIDTH / 2.0;
            double exitX = exitPositive ? ROUTE_END : -Config.VEHICLE_LENGTH / 2.0;
            coordinates.add(new double[] {entryX, exitY});
            coordinates.add(new double[] {horizontalExit, exitY});
            coordinates.add(new double[] {
                exitX,
                exitY
            });
        } else {
            double verticalExit = (direction == Direction.WEST) == turnLeft
                    ? Config.CENTER_Y - Config.INTERSECTION_HALF
                    : Config.CENTER_Y + Config.INTERSECTION_HALF;
            boolean exitPositive = direction == Direction.WEST ? !turnLeft : turnLeft;
            double exitY = exitPositive ? ROUTE_END : -Config.VEHICLE_LENGTH / 2.0;
            double exitX = verticalExit == Config.CENTER_Y - Config.INTERSECTION_HALF
                ? Config.CENTER_X + Config.LANE_WIDTH / 2.0
                : Config.CENTER_X - Config.LANE_WIDTH / 2.0;
            coordinates.add(new double[] {Config.CENTER_X, entryY});
            coordinates.add(new double[] {Config.CENTER_X, verticalExit});
            coordinates.add(new double[] {
                exitX,
                exitY
            });
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