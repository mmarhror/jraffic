import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import shared.Config;
import shared.Direction;
import simulation.Simulation;
import traffic.LightController;
import ui.Renderer;

public class Main extends Application {
  @Override
  public void start(Stage stage) {
    Simulation simulation = new Simulation();
    LightController lights = new LightController();
    Renderer renderer = new Renderer();
    Canvas canvas = new Canvas(Config.WINDOW_W, Config.WINDOW_H);
    GraphicsContext graphics = canvas.getGraphicsContext2D();

    StackPane root = new StackPane(canvas);
    Scene scene = new Scene(root, Config.WINDOW_W, Config.WINDOW_H);
    scene.setOnKeyPressed(
        event -> {
          switch (event.getCode()) {
            case UP:
              simulation.spawnVehicle(Direction.SOUTH);
              break;
            case DOWN:
              simulation.spawnVehicle(Direction.NORTH);
              break;
            case RIGHT:
              simulation.spawnVehicle(Direction.WEST);
              break;
            case LEFT:
              simulation.spawnVehicle(Direction.EAST);
              break;
            case R:
              simulation.spawnVehicle(
                  Direction.values()[(int) (Math.random() * Direction.values().length)]);
              break;
            case ESCAPE:
              Platform.exit();
              break;
            default:
              break;
          }
        });

    stage.setTitle("Jraffic | Adaptive Intersection");
    stage.setScene(scene);
    stage.setResizable(false);
    stage.show();
    canvas.requestFocus();

    new AnimationTimer() {
      private long lastNanos = -1;

      @Override
      public void handle(long now) {
        if (lastNanos < 0) lastNanos = now;
        double dt = Math.min((now - lastNanos) / 1_000_000_000.0, 0.1);
        lastNanos = now;
        simulation.update(dt, lights);
        lights.update(dt, simulation);
        renderer.draw(graphics, simulation, lights);
      }
    }.start();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
