package ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import simulation.Vehicle;
import shared.Config;
import shared.LightColor;
import traffic.TrafficLight;

public class Renderer {
    private static final double VEHICLE_WIDTH = 16.0;

    public void draw(GraphicsContext gc, simulation.Simulation sim, traffic.LightController lights) {
        drawRoads(gc);
        if (lights != null) {
            for (TrafficLight light : lights.getLights()) {
                drawLight(gc, light);
            }
        }
        if (sim != null) {
            for (Vehicle vehicle : sim.getVehicles()) {
                drawVehicle(gc, vehicle);
            }
        }
        drawLegend(gc);
    }

    private void drawRoads(GraphicsContext gc) {
        gc.setFill(Color.rgb(229, 232, 235));
        gc.fillRect(0, 0, Config.WINDOW_W, Config.WINDOW_H);
        gc.setFill(Color.rgb(55, 61, 68));
        gc.fillRect(0, Config.CENTER_Y - Config.LANE_WIDTH, Config.WINDOW_W, Config.LANE_WIDTH * 2.0);
        gc.fillRect(Config.CENTER_X - Config.LANE_WIDTH, 0, Config.LANE_WIDTH * 2.0, Config.WINDOW_H);

        gc.setStroke(Color.rgb(215, 218, 221));
        gc.setLineWidth(2.0);
        for (double position = 0.0; position < Config.WINDOW_W; position += 32.0) {
            gc.strokeLine(position, Config.CENTER_Y, position + 16.0, Config.CENTER_Y);
            gc.strokeLine(Config.CENTER_X, position, Config.CENTER_X, position + 16.0);
        }

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(4.0);
        gc.strokeLine(0, Config.CENTER_Y - Config.INTERSECTION_HALF,
                Config.CENTER_X - Config.INTERSECTION_HALF, Config.CENTER_Y - Config.INTERSECTION_HALF);
        gc.strokeLine(Config.CENTER_X + Config.INTERSECTION_HALF, Config.CENTER_Y - Config.INTERSECTION_HALF,
                Config.WINDOW_W, Config.CENTER_Y - Config.INTERSECTION_HALF);
        gc.strokeLine(Config.CENTER_X - Config.INTERSECTION_HALF, 0,
                Config.CENTER_X - Config.INTERSECTION_HALF, Config.CENTER_Y - Config.INTERSECTION_HALF);
        gc.strokeLine(Config.CENTER_X + Config.INTERSECTION_HALF, 0,
                Config.CENTER_X + Config.INTERSECTION_HALF, Config.CENTER_Y - Config.INTERSECTION_HALF);
        gc.strokeLine(0, Config.CENTER_Y + Config.INTERSECTION_HALF,
                Config.CENTER_X - Config.INTERSECTION_HALF, Config.CENTER_Y + Config.INTERSECTION_HALF);
        gc.strokeLine(Config.CENTER_X + Config.INTERSECTION_HALF, Config.CENTER_Y + Config.INTERSECTION_HALF,
                Config.WINDOW_W, Config.CENTER_Y + Config.INTERSECTION_HALF);
        gc.strokeLine(Config.CENTER_X - Config.INTERSECTION_HALF, Config.CENTER_Y + Config.INTERSECTION_HALF,
                Config.CENTER_X - Config.INTERSECTION_HALF, Config.WINDOW_H);
        gc.strokeLine(Config.CENTER_X + Config.INTERSECTION_HALF, Config.CENTER_Y + Config.INTERSECTION_HALF,
                Config.CENTER_X + Config.INTERSECTION_HALF, Config.WINDOW_H);
    }

    private void drawLight(GraphicsContext gc, TrafficLight light) {
        gc.setFill(Color.rgb(35, 39, 43));
        gc.fillOval(light.getX() - 13.0, light.getY() - 13.0, 26.0, 26.0);
        gc.setFill(light.getColor() == LightColor.GREEN ? Color.LIMEGREEN : Color.CRIMSON);
        gc.fillOval(light.getX() - 8.0, light.getY() - 8.0, 16.0, 16.0);
    }

    private void drawVehicle(GraphicsContext gc, Vehicle vehicle) {
        Color color;
        switch (vehicle.getTurn()) {
            case LEFT:
                color = Color.DODGERBLUE;
                break;
            case RIGHT:
                color = Color.MAGENTA;
                break;
            case STRAIGHT:
            default:
                color = Color.GOLD;
                break;
        }

        gc.save();
        gc.translate(vehicle.getX(), vehicle.getY());
        gc.rotate(vehicle.getAngle());
        gc.setFill(color);
        gc.fillRoundRect(-Config.VEHICLE_LENGTH / 2.0, -VEHICLE_WIDTH / 2.0,
                Config.VEHICLE_LENGTH, VEHICLE_WIDTH, 5.0, 5.0);
        gc.setStroke(Color.rgb(35, 39, 43));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(-Config.VEHICLE_LENGTH / 2.0, -VEHICLE_WIDTH / 2.0,
                Config.VEHICLE_LENGTH, VEHICLE_WIDTH, 5.0, 5.0);
        gc.restore();
    }

    private void drawLegend(GraphicsContext gc) {
        double x = 14.0;
        double y = 14.0;
        gc.setFill(Color.rgb(25, 29, 33, 0.9));
        gc.fillRoundRect(x, y, 154.0, 92.0, 8.0, 8.0);
        gc.setFill(Color.WHITE);
        gc.fillText("Route colors", x + 12.0, y + 20.0);
        drawLegendItem(gc, x + 12.0, y + 39.0, Color.GOLD, "Straight");
        drawLegendItem(gc, x + 12.0, y + 59.0, Color.DODGERBLUE, "Left turn");
        drawLegendItem(gc, x + 12.0, y + 79.0, Color.MAGENTA, "Right turn");
    }

    private void drawLegendItem(GraphicsContext gc, double x, double y, Color color, String label) {
        gc.setFill(color);
        gc.fillRoundRect(x, y - 10.0, 12.0, 12.0, 3.0, 3.0);
        gc.setFill(Color.WHITE);
        gc.fillText(label, x + 20.0, y);
    }
}
