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
          gc.setStroke(Color.WHITE);
          gc.setLineWidth(3);
          gc.strokeLine(0, 400, 340, 400);
          gc.strokeLine(460, 400, 800, 400);
          gc.strokeLine(400, 0, 400, 340);
          gc.strokeLine(400, 460, 400, 800);
    }
}
