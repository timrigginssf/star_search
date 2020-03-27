package com.company;

public class TurnRequest {
    public Integer xPosition;
    public Integer yPosition;
    public String action;
    public String direction;
    public Integer droneId;

    public void main(Integer droneId, Integer xPosition, Integer yPosition, String action, String direction) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.action = action;
        this.droneId = droneId;
        this.direction = direction;

    }
}
