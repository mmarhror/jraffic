package traffic;

import shared.Config;
import shared.Direction;
import shared.LightColor;

public class TrafficLight {

    /*
     * Distance between the road/intersection edge and the center
     * of the traffic-light circle.
     *
     * Increase this value if the circles are still touching the road.
     */
    private static final double LIGHT_MARGIN = 18.0;

    private final Direction direction;
    private LightColor color;

    private final double x;
    private final double y;

    public TrafficLight(Direction direction) {
        if (direction == null) {
            throw new IllegalArgumentException(
                    "Traffic-light direction cannot be null");
        }

        this.direction = direction;
        this.color = LightColor.RED;

        double centerX = Config.CENTER_X;
        double centerY = Config.CENTER_Y;

        /*
         * Putting both coordinates beyond the intersection boundary
         * places each light in a corner outside both roads.
         */
        double outsideOffset =
                Config.INTERSECTION_HALF + LIGHT_MARGIN;

        switch (direction) {
            /*
             * NORTH traffic enters from the top and moves downward.
             * Its light is placed at the upper-left corner.
             */
            case NORTH:
                this.x = centerX - outsideOffset;
                this.y = centerY - outsideOffset;
                break;

            /*
             * SOUTH traffic enters from the bottom and moves upward.
             * Its light is placed at the lower-right corner.
             */
            case SOUTH:
                this.x = centerX + outsideOffset;
                this.y = centerY + outsideOffset;
                break;

            /*
             * EAST traffic enters from the right and moves left.
             * Its light is placed at the upper-right corner.
             */
            case EAST:
                this.x = centerX + outsideOffset;
                this.y = centerY - outsideOffset;
                break;

            /*
             * WEST traffic enters from the left and moves right.
             * Its light is placed at the lower-left corner.
             */
            case WEST:
                this.x = centerX - outsideOffset;
                this.y = centerY + outsideOffset;
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported traffic-light direction: " + direction);
        }
    }

    public Direction getDirection() {
        return direction;
    }

    public LightColor getColor() {
        return color;
    }

    public void setColor(LightColor color) {
        if (color == null) {
            throw new IllegalArgumentException(
                    "Traffic-light color cannot be null");
        }

        this.color = color;
    }

    /**
     * Returns the center X position of the traffic-light circle.
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the center Y position of the traffic-light circle.
     */
    public double getY() {
        return y;
    }
}