package simulation;

import java.util.ArrayList;
import java.util.List;
import shared.Config;
import shared.Direction;
import shared.LightColor;
import shared.Turn;

public class Vehicle {
  private final Direction direction;
  private final Turn turn;
  private final List<Point> path;
  private final double stopDistance;
  private double distance;
  private double x;
  private double y;
  private double angle;

  public Vehicle(Direction direction, Turn turn) {
    this.direction = direction;
    this.turn = turn;
    this.path = buildPath(direction, turn);
    this.stopDistance = pathDistanceToStopLine();
    setPosition();
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getAngle() {
    return angle;
  }

  public Turn getTurn() {
    return turn;
  }

  public Direction getDirection() {
    return direction;
  }

  public double getDistance() {
    return distance;
  }

  public boolean isWaiting() {
    return distance < stopDistance && distance > 0.0;
  }

  public boolean isOffScreen() {
    return distance >= totalPathLength()
        || x < -60
        || x > Config.WINDOW_W + 60
        || y < -60
        || y > Config.WINDOW_H + 60;
  }

  public void update(double dt, Vehicle ahead, LightColor light) {
    double desired = Config.VEHICLE_SPEED * Math.max(0.0, dt);
    double available = desired;
    if (distance < stopDistance && light == LightColor.RED) {
      available = Math.min(available, Math.max(0.0, stopDistance - distance));
    }
    if (ahead != null && ahead.distance > distance) {
      double gap = ahead.distance - distance - Config.VEHICLE_LENGTH;
      if (gap < Config.SAFETY_GAP) available = 0.0;
      else available = Math.min(available, gap - Config.SAFETY_GAP);
    }
    distance += Math.max(0.0, available);
    setPosition();
  }

  private List<Point> buildPath(Direction source, Turn route) {
    double cx = Config.CENTER_X;
    double cy = Config.CENTER_Y;
    double lane = Config.LANE_WIDTH / 2.0;
    double edge = Config.INTERSECTION_HALF;
    List<Point> points = new ArrayList<>();
    Point start;
    Point stop;
    Point center;
    Point exit;
    switch (source) {
      case SOUTH:
        start = new Point(cx + lane, Config.WINDOW_H + 40);
        stop = new Point(cx + lane, cy + edge);
        center = new Point(cx + lane, cy);
        exit = new Point(cx + lane, -40);
        break;
      case NORTH:
        start = new Point(cx - lane, -40);
        stop = new Point(cx - lane, cy - edge);
        center = new Point(cx - lane, cy);
        exit = new Point(cx - lane, Config.WINDOW_H + 40);
        break;
      case WEST:
        start = new Point(-40, cy + lane);
        stop = new Point(cx - edge, cy + lane);
        center = new Point(cx, cy + lane);
        exit = new Point(Config.WINDOW_W + 40, cy + lane);
        break;
      case EAST:
      default:
        start = new Point(Config.WINDOW_W + 40, cy - lane);
        stop = new Point(cx + edge, cy - lane);
        center = new Point(cx, cy - lane);
        exit = new Point(-40, cy - lane);
        break;
    }
    points.add(start);
    points.add(stop);
    if (route == Turn.STRAIGHT) {
      points.add(center);
      points.add(exit);
      return points;
    }
    points.add(turnPoint(source, route, cx, cy, edge));
    points.add(outboundPoint(source, route, cx, cy, edge, lane));
    points.add(exitPoint(source, route));
    return points;
  }

  private Point turnPoint(Direction source, Turn route, double cx, double cy, double edge) {
    boolean left = route == Turn.LEFT;
    switch (source) {
      case SOUTH:
        return new Point(cx + (left ? -edge : edge), cy + (left ? -edge : edge));
      case NORTH:
        return new Point(cx + (left ? edge : -edge), cy + (left ? edge : -edge));
      case WEST:
        return new Point(cx + (left ? edge : -edge), cy + (left ? -edge : edge));
      default:
        return new Point(cx + (left ? -edge : edge), cy + (left ? edge : -edge));
    }
  }

  private Point outboundPoint(
      Direction source, Turn route, double cx, double cy, double edge, double lane) {
    boolean left = route == Turn.LEFT;
    switch (source) {
      case SOUTH:
        return new Point(cx + (left ? -lane : lane), cy - edge);
      case NORTH:
        return new Point(cx + (left ? lane : -lane), cy + edge);
      case WEST:
        return new Point(cx + edge, cy + (left ? -lane : lane));
      default:
        return new Point(cx - edge, cy + (left ? lane : -lane));
    }
  }

  private Point exitPoint(Direction source, Turn route) {
    double lane = Config.LANE_WIDTH / 2.0;
    if (source == Direction.SOUTH || source == Direction.NORTH) {
      boolean toWest = source == Direction.SOUTH ? route == Turn.LEFT : route == Turn.RIGHT;
      return new Point(
          toWest ? -40 : Config.WINDOW_W + 40,
          source == Direction.SOUTH ? Config.CENTER_Y - lane : Config.CENTER_Y + lane);
    }
    boolean toNorth = source == Direction.WEST ? route == Turn.LEFT : route == Turn.RIGHT;
    return new Point(
        source == Direction.WEST ? Config.CENTER_X + lane : Config.CENTER_X - lane,
        toNorth ? -40 : Config.WINDOW_H + 40);
  }

  private double pathDistanceToStopLine() {
    return segmentLength(path.get(0), path.get(1));
  }

  private double totalPathLength() {
    double total = 0;
    for (int i = 1; i < path.size(); i++) total += segmentLength(path.get(i - 1), path.get(i));
    return total;
  }

  private void setPosition() {
    double remaining = Math.min(distance, totalPathLength());
    for (int i = 1; i < path.size(); i++) {
      Point from = path.get(i - 1);
      Point to = path.get(i);
      double length = segmentLength(from, to);
      if (remaining <= length) {
        double ratio = length == 0 ? 0 : remaining / length;
        x = from.x + (to.x - from.x) * ratio;
        y = from.y + (to.y - from.y) * ratio;
        angle = Math.toDegrees(Math.atan2(to.y - from.y, to.x - from.x));
        return;
      }
      remaining -= length;
    }
    Point last = path.get(path.size() - 1);
    x = last.x;
    y = last.y;
  }

  private double segmentLength(Point from, Point to) {
    return Math.hypot(to.x - from.x, to.y - from.y);
  }

  private static final class Point {
    private final double x;
    private final double y;

    private Point(double x, double y) {
      this.x = x;
      this.y = y;
    }
  }
}
