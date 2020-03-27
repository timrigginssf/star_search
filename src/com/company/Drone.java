package com.company;

import java.util.ArrayList;

public class Drone {
    public Integer droneId;
    public Integer xPosition;
    public Integer yPosition;
    public Integer maxThrust = 1;
    public String orientation;

    public void main(Integer droneId, Integer xPosition, Integer yPosition, String orientation) {
        this.droneId = droneId;

        this.xPosition = xPosition;
        this.yPosition = yPosition;

        this.orientation = orientation;
    }

    public void thrust(Integer xPosition, Integer yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
    }

    public void steer(String newOrientation) {
        this.orientation = newOrientation;
    }
}
