package simulation;

import java.util.List;

import shared.Direction;

public class Simulation {
    private List<Vehicle> vehicles;
    public List<Vehicle> getVehicles(){
        return  vehicles;
    }   
    public int getQueueLength(Direction dir){
        int len=0;
        for(Vehicle vehicle:vehicles){
            if (vehicle.getDirection().equals(dir)) {
             len++;   
            }
        }
        return len;
    }
    public void spawnVehicle(Direction dir){
        Vehicle vehicle =new Vehicle(null, dir);
        vehicles.add(vehicle);
    }
    public void update(double dt, traffic.LightController lights){
        
    }
}