# How to Work 100% in Parallel from Day 1

To work completely in parallel without waiting for anyone, agree on the exact method signatures.

Each person uses a 1-line "dummy" mock while building their own part so their code compiles and runs immediately:

* **Abdelkafy** mocks the light: assumes it's always `GREEN` until Med finishes.
* **Med** mocks the queue: passes a hardcoded number (e.g., `3`) until Abdelkafy finishes.
* **Mimoun** mocks vehicles & lights: puts 2 hardcoded dummy rectangles on screen to test rendering, before hooking up Abdelkafy and Med.

---

## Folder Structure

```text
road-intersection/
├── Main.java                  ← Mimoun (entry point)
├── shared/                    ← ALL (copy-paste on Day 1)
│   ├── Config.java
│   ├── Direction.java
│   ├── Turn.java
│   └── LightColor.java
├── simulation/                ← Abdelkafy
│   ├── Vehicle.java
│   └── Simulation.java
├── traffic/                   ← Med
│   ├── TrafficLight.java
│   └── LightController.java
└── ui/                        ← Mimoun
    └── Renderer.java

```

---

## Exact Class Contracts (No guessing needed)

### Shared Files (create once, commit immediately)

#### `shared/Config.java`

```java
package shared;

public final class Config {
    private Config() {}

    public static final int WINDOW_W = 800;
    public static final int WINDOW_H = 800;
    public static final double CENTER_X = WINDOW_W / 2.0;
    public static final double CENTER_Y = WINDOW_H / 2.0;
    public static final double LANE_WIDTH = 40.0;
    public static final double INTERSECTION_HALF = LANE_WIDTH;

    public static final double VEHICLE_LENGTH = 30.0;
    public static final double SAFETY_GAP = 10.0;
    public static final double VEHICLE_SPEED = 60.0; // px/sec

    public static final double LANE_LENGTH = (WINDOW_W / 2.0) - INTERSECTION_HALF;
    public static final int LANE_CAPACITY = (int) Math.floor(LANE_LENGTH / (VEHICLE_LENGTH + SAFETY_GAP));

    public static final double GREEN_TIME = 5.0;
    public static final double ALL_RED_TIME = 1.0;
    public static final double SPAWN_MIN_GAP = VEHICLE_LENGTH + SAFETY_GAP;
}

```

#### `shared/Direction.java`

```java
package shared;

public enum Direction { NORTH, SOUTH, EAST, WEST }

```

#### `shared/Turn.java`

```java
package shared;

public enum Turn { STRAIGHT, LEFT, RIGHT }

```

#### `shared/LightColor.java`

```java
package shared;

public enum LightColor { RED, GREEN }

```

---

### Abdelkafy — `simulation/`

**Goal:** Physics, movement, safe distance, stop line logic, spawning.

#### 1. `simulation/Vehicle.java`

**Must expose:**

* `double getX()`, `double getY()`, `double getAngle()` (for Mimoun to draw)
* `Turn getTurn()` (to determine color: Straight = Yellow, Left = Blue, Right = Magenta)
* `void update(double dt, Vehicle ahead, LightColor light)`:
* If approaching stop line and `light == RED` → stop.
* If vehicle ahead exists and distance < `SAFETY_GAP` → stop.
* Otherwise → move along predefined path at `VEHICLE_SPEED`.



#### 2. `simulation/Simulation.java`

**Must expose:**

* `void spawnVehicle(Direction dir)`: Pick random `Turn`. If the newest vehicle in that lane is closer than `SPAWN_MIN_GAP` to the spawn point, reject/do nothing (anti-spam rule).
* `void update(double dt, traffic.LightController lights)`: Update all vehicles and remove those off-screen.
* `int getQueueLength(Direction dir)`: Count vehicles waiting before the stop line in that direction (used by Med).
* `List<Vehicle> getVehicles()` (used by Mimoun).

> **How Abdelkafy works alone:** Create a dummy light returning `GREEN` to test vehicle movement and turns before Med is done.

---

### Med — `traffic/`

**Goal:** 2-color traffic lights, non-conflicting cycle, dynamic congestion handling.

#### 1. `traffic/TrafficLight.java`

**Must expose:**

* `Direction getDirection()`
* `LightColor getColor()`
* `double getX()`, `double getY()` (position at stop line for Mimoun to draw)

#### 2. `traffic/LightController.java`

**Must expose:**

* `LightColor getLightFor(Direction dir)` (used by Abdelkafy's vehicles).
* `List<TrafficLight> getLights()` (used by Mimoun to draw).
* `void update(double dt, simulation.Simulation sim)`:
* Cycles phases: (1) N+S Green → (2) All Red → (3) E+W Green → (4) All Red.
* Dynamic Congestion Rule: Check `sim.getQueueLength(dir)`. If a lane approaches capacity (≥ 80% `LANE_CAPACITY`), extend its green phase to clear congestion.



> **How Med works alone:** In `update()`, use a hardcoded value instead of `sim.getQueueLength(dir)` to verify light cycles and extensions in console prints.

---

### Mimoun — `ui/` + `Main.java`

**Goal:** Window, rendering roads/cars/lights, key bindings.

#### 1. `ui/Renderer.java`

**Must expose:**

* `void draw(GraphicsContext gc, simulation.Simulation sim, traffic.LightController lights)`:
* Clear screen & draw 2 crossing roads (gray) + lane markings (white).
* Draw stop lines.
* Draw each traffic light as a Red or Green circle at its position.
* Draw each vehicle as a rectangle at (x, y) rotated by angle. Color based on `Turn` (Straight = Yellow, Left = Blue, Right = Magenta).
* Draw color legend in a corner.



#### 2. `Main.java` (Root)

* Sets up JavaFX Stage, Scene (800×800), and Canvas.
* Runs `AnimationTimer`:
```java
sim.update(dt, lights);
lights.update(dt, sim);
renderer.draw(gc, sim, lights);

```


* Handles `KeyEvent`:
* `UP` → `sim.spawnVehicle(Direction.SOUTH)`
* `DOWN` → `sim.spawnVehicle(Direction.NORTH)`
* `RIGHT` → `sim.spawnVehicle(Direction.WEST)`
* `LEFT` → `sim.spawnVehicle(Direction.EAST)`
* `R` → `sim.spawnVehicle(random Direction)`
* `ESCAPE` → `Platform.exit()`



> **How Mimoun works alone:** Hardcode a static list of 2 dummy vehicles and 4 dummy lights to write and polish all rendering code before Abdelkafy and Med finish.