package traffic;

import shared.Config;
import shared.Direction;
import shared.LightColor;

public class TrafficLight {
  private final Direction direction;
  private LightColor color;

  private final double x;
  private final double y;

  public TrafficLight(Direction direction) {
    this.direction = direction;
    this.color = LightColor.RED;

    double cx = Config.CENTER_X;
    double cy = Config.CENTER_Y;

    double half = Config.INTERSECTION_HALF;
    double offset = Config.LANE_WIDTH / 2.0;

    switch (direction) {
      case SOUTH:
        this.x = cx + offset;
        this.y = cy + half + 5;
        break;
      case NORTH:
        this.x = cx - offset;
        this.y = cy - half - 5;
        break;
      case WEST:
        this.x = cx - half - 5;
        this.y = cy + offset;
        break;
      case EAST:
        this.x = cx + half + 5;
        this.y = cy - offset;
        break;
      default:
        this.x = 0;
        this.y = 0;
    }
  }

  public Direction getDirection() {
    return direction;
  }

  public LightColor getColor() {
    return color;
  }

  public void setColor(LightColor c) {
    this.color = c;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }
}
