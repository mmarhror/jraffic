package simulation;

import shared.LightColor;

public class Vehicle {
    private double x;
    private double y;
    private double angle;
    private shared.Turn turn;
    private  shared.Direction direction;
    public Vehicle(shared.Turn turn_in,shared.Direction direction_in) {
        direction=direction_in;
        turn = turn_in;
    }

    public shared.Turn getTurn() {
        return turn;
    }
  public shared.Direction getDirection() {
        return direction;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setTurn(shared.Turn turn) {
        this.turn = turn;
    }

    public void update(double dt, Vehicle ahead, LightColor light) {

    }

    public double getAngle() {
        return angle;
    }
}