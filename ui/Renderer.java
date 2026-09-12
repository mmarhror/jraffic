package ui;
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;

public class Renderer {

    public void draw(
            GraphicsContext gc,
            simulation.Simulation sim,
            traffic.LightController lights) {
          gc.clearRect(0, 0, 800, 800);
          gc.setFill(Color.DARKGRAY);
          gc.fillRect(0, 360, 800, 80);
          gc.fillRect(360, 0, 80, 800);
    }
}
