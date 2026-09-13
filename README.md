# JavaFX Traffic Intersection Simulation

This project is a small JavaFX traffic-light simulation that models an urban four-way intersection. Vehicles are spawned from each side of the intersection, follow predefined turn routes, stop at red lights, and interact with a traffic controller that cycles directions and handles congestion.

## Features

- JavaFX-based graphical simulation
- Four-way traffic intersection with lane markings and stop lines
- Vehicles that can go straight, left, or right
- Red/green traffic-light logic with round-robin switching
- Congestion-based green extension logic
- Safe following-distance logic and anti-overlap spawning
- Animated vehicle movement using a frame loop
- Route-color legend on screen

## Controls

- Up Arrow: spawn vehicle from the south side, moving toward the intersection
- Down Arrow: spawn vehicle from the north side, moving toward the intersection
- Right Arrow: spawn vehicle from the west side, moving toward the intersection
- Left Arrow: spawn vehicle from the east side, moving toward the intersection
- R: spawn a vehicle from a random direction
- Esc: exit the application

## Project Structure

```text
jraffic/
├── Main.java
├── pod.xml
├── README.md
├── shared/
│   ├── Config.java
│   ├── Direction.java
│   ├── LightColor.java
│   └── Turn.java
├── simulation/
│   ├── Simulation.java
│   └── Vehicle.java
├── traffic/
│   ├── LightController.java
│   └── TrafficLight.java
└── ui/
    └── Renderer.java
```

## Core Logic Overview

### Shared configuration
The project centralizes intersection dimensions, vehicle behavior, and timing in `shared.Config.java`.

### Vehicle simulation
`simulation/Vehicle.java` stores each vehicle's route, heading, and progress along the path. It applies:

- stop-line red-light behavior
- safe following-distance checks
- route-based movement with a fixed speed
- turn-based route generation

### Traffic controller
`traffic/LightController.java` manages the active green direction and checks the queue to decide when to switch phases or extend the current green period.

### Rendering
`ui/Renderer.java` draws the roads, lane markings, lights, vehicles, and route-color legend.

### Application entry
`Main.java` creates the JavaFX stage, canvas, animation timer, and input listeners.

## Running the Project

This project is a JavaFX application and expects a JavaFX-capable Java runtime/tooling configuration. The project includes JavaFX dependencies in `pod.xml`.

Run it through your JavaFX-enabled IDE or project launcher, or use a JavaFX Maven/Gradle run setup configured for the project.

## Notes

- Vehicles are color-coded by route:
  - Straight: yellow
  - Left turn: blue
  - Right turn: magenta
- The simulation enforces a minimal spawn gap to avoid immediate vehicle overlap in the same lane.
- The traffic controller uses a strict single-green safety rule to prevent conflicting directions from being green at the same time.

## License

This project is provided as a coursework/demo simulation and is intended for educational use.