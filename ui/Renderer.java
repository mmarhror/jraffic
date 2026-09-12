package ui;

import java.io.File;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import shared.Config;
import shared.LightColor;
import shared.Turn;
import simulation.Simulation;
import simulation.Vehicle;
import traffic.LightController;
import traffic.TrafficLight;

public class Renderer {
  private static final Color ROAD = Color.web("#3d4348");
  private static final Color ROAD_EDGE = Color.web("#626a70");
  private static final Color LANE_MARKING = Color.web("#e6e1c8");
  private static final double SPRITE_WIDTH = 25.0;
  private static final double SPRITE_HEIGHT = 38.0;

  private final Image background;
  private final Image blueCar;
  private final Image orangeCar;
  private final Image yellowCar;

  public Renderer() {
    background = loadImage("assets/background.png");
    blueCar = loadImage("assets/car_blue.png");
    orangeCar = loadImage("assets/car_orange.png");
    yellowCar = loadImage("assets/car_yellow.png");
  }

  public void draw(GraphicsContext gc, Simulation simulation, LightController lights) {
    gc.setFill(Color.web("#172126"));
    gc.fillRect(0, 0, Config.WINDOW_W, Config.WINDOW_H);
    drawRoads(gc);
    drawStopLines(gc);
    drawLights(gc, lights);
    drawVehicles(gc, simulation);
    drawLegend(gc);
  }

  private void drawRoads(GraphicsContext gc) {
    if (background != null) {
      gc.drawImage(background, 0, 0, Config.WINDOW_W, Config.WINDOW_H);
      return;
    }

    double center = Config.CENTER_X;
    double roadWidth = Config.LANE_WIDTH * 2;
    gc.setFill(ROAD);
    gc.fillRect(center - roadWidth / 2, 0, roadWidth, Config.WINDOW_H);
    gc.fillRect(0, Config.CENTER_Y - roadWidth / 2, Config.WINDOW_W, roadWidth);

    gc.setStroke(ROAD_EDGE);
    gc.setLineWidth(2);
    gc.strokeRect(center - roadWidth / 2, 0, roadWidth, Config.WINDOW_H);
    gc.strokeRect(0, Config.CENTER_Y - roadWidth / 2, Config.WINDOW_W, roadWidth);

    gc.setStroke(LANE_MARKING);
    gc.setLineWidth(2);
    gc.setLineDashes(12, 10);
    gc.strokeLine(center, 0, center, Config.CENTER_Y - Config.INTERSECTION_HALF);
    gc.strokeLine(center, Config.CENTER_Y + Config.INTERSECTION_HALF, center, Config.WINDOW_H);
    gc.strokeLine(0, Config.CENTER_Y, center - Config.INTERSECTION_HALF, Config.CENTER_Y);
    gc.strokeLine(
        center + Config.INTERSECTION_HALF, Config.CENTER_Y, Config.WINDOW_W, Config.CENTER_Y);
    gc.setLineDashes(null);
  }

  private void drawStopLines(GraphicsContext gc) {
    gc.setStroke(Color.WHITE);
    gc.setLineWidth(4);
    double cx = Config.CENTER_X;
    double cy = Config.CENTER_Y;
    double half = Config.INTERSECTION_HALF;
    gc.strokeLine(cx - Config.LANE_WIDTH, cy + half, cx, cy + half);
    gc.strokeLine(cx, cy - half, cx + Config.LANE_WIDTH, cy - half);
    gc.strokeLine(cx - half, cy - Config.LANE_WIDTH, cx - half, cy);
    gc.strokeLine(cx + half, cy, cx + half, cy + Config.LANE_WIDTH);
  }

  private void drawLights(GraphicsContext gc, LightController lights) {
    for (TrafficLight light : lights.getLights()) {
      gc.setFill(Color.web("#101416"));
      gc.fillOval(light.getX() - 10, light.getY() - 10, 20, 20);
      gc.setFill(
          light.getColor() == LightColor.GREEN ? Color.web("#4ade80") : Color.web("#f05252"));
      gc.fillOval(light.getX() - 7, light.getY() - 7, 14, 14);
    }
  }

  private void drawVehicles(GraphicsContext gc, Simulation simulation) {
    for (Vehicle vehicle : simulation.getVehicles()) {
      Image sprite = spriteFor(vehicle.getTurn());
      if (sprite != null) {
        drawSprite(gc, vehicle, sprite);
        continue;
      }

      gc.save();
      gc.translate(vehicle.getX(), vehicle.getY());
      gc.rotate(vehicle.getAngle());
      gc.setFill(colorFor(vehicle.getTurn()));
      gc.fillRoundRect(-Config.VEHICLE_LENGTH / 2, -10, Config.VEHICLE_LENGTH, 20, 5, 5);
      gc.setFill(Color.rgb(210, 230, 235, 0.8));
      gc.fillRect(0, -7, 8, 14);
      gc.setStroke(Color.rgb(20, 25, 28, 0.8));
      gc.setLineWidth(1);
      gc.strokeRoundRect(-Config.VEHICLE_LENGTH / 2, -10, Config.VEHICLE_LENGTH, 20, 5, 5);
      gc.restore();
    }
  }

  private void drawSprite(GraphicsContext gc, Vehicle vehicle, Image sprite) {
    gc.save();
    gc.translate(vehicle.getX(), vehicle.getY());
    gc.rotate(vehicle.getAngle() + 90.0);
    gc.drawImage(sprite, -SPRITE_WIDTH / 2.0, -SPRITE_HEIGHT / 2.0, SPRITE_WIDTH, SPRITE_HEIGHT);
    gc.restore();
  }

  private Image spriteFor(Turn turn) {
    switch (turn) {
      case LEFT:
        return blueCar;
      case RIGHT:
        return orangeCar;
      default:
        return yellowCar;
    }
  }

  private Image loadImage(String path) {
    File file = new File(path);
    if (!file.isFile()) return null;
    Image image = new Image(file.toURI().toString());
    return image.isError() ? null : image;
  }

  private Color colorFor(Turn turn) {
    switch (turn) {
      case LEFT:
        return Color.web("#60a5fa");
      case RIGHT:
        return Color.web("#f472b6");
      default:
        return Color.web("#facc15");
    }
  }

  private void drawLegend(GraphicsContext gc) {
    double x = 18;
    double y = Config.WINDOW_H - 72;
    gc.setFill(Color.rgb(10, 15, 18, 0.86));
    gc.fillRoundRect(x - 8, y - 18, 185, 58, 8, 8);
    gc.setFill(Color.WHITE);
    gc.fillText("Routes", x, y - 2);
    drawLegendItem(gc, x, y + 14, Color.web("#facc15"), "straight");
    drawLegendItem(gc, x + 80, y + 14, Color.web("#60a5fa"), "left");
    drawLegendItem(gc, x + 135, y + 14, Color.web("#f472b6"), "right");
  }

  private void drawLegendItem(GraphicsContext gc, double x, double y, Color color, String label) {
    gc.setFill(color);
    gc.fillRoundRect(x, y - 8, 12, 12, 3, 3);
    gc.setFill(Color.WHITE);
    gc.fillText(label, x + 17, y + 2);
  }
}
