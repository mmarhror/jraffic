import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.util.concurrent.ThreadLocalRandom;
import shared.Direction;
import simulation.Simulation;
import traffic.LightController;
import ui.Renderer;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(shared.Config.WINDOW_W, shared.Config.WINDOW_H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);
        root.setFocusTraversable(true);
        Scene scene = new Scene(root, shared.Config.WINDOW_W, shared.Config.WINDOW_H);
        Simulation simulation = new Simulation();
        LightController lights = new LightController();
        Renderer renderer = new Renderer();
        renderer.draw(gc, simulation, lights);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                Platform.exit();
            } else if (event.getCode() == KeyCode.UP) {
                simulation.spawnVehicle(Direction.SOUTH);
            } else if (event.getCode() == KeyCode.DOWN) {
                simulation.spawnVehicle(Direction.NORTH);
            } else if (event.getCode() == KeyCode.RIGHT) {
                simulation.spawnVehicle(Direction.WEST);
            } else if (event.getCode() == KeyCode.LEFT) {
                simulation.spawnVehicle(Direction.EAST);
            } else if (event.getCode() == KeyCode.R) {
                Direction[] directions = Direction.values();
                simulation.spawnVehicle(directions[
                        ThreadLocalRandom.current().nextInt(directions.length)]);
            }
        });

        stage.setTitle("Traffic Simulation");
        stage.setScene(scene);
        stage.show();
        scene.getRoot().requestFocus();

        new AnimationTimer() {
            private long previousNanos;

            @Override
            public void handle(long now) {
                if (previousNanos == 0L) {
                    previousNanos = now;
                    return;
                }
                double dt = Math.min((now - previousNanos) / 1_000_000_000.0, 0.1);
                previousNanos = now;
                simulation.update(dt, lights);
                lights.update(dt, simulation);
                renderer.draw(gc, simulation, lights);
            }
        }.start();
    }

    public static void main(String[] args) {
        launch();
    }
}
