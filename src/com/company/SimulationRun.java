package com.company;

import java.util.ArrayList;

public class SimulationRun {
    private Integer spaceRegionWidth;
    private Integer spaceRegionHeight;
    private Integer droneCount;
    private Integer starCount;
    public Integer turnsLeft;
    public Integer totalTurns;

    private Squadron squadron;
    private SpaceRegion spaceRegion;

    public void main(
            Integer spaceRegionWidth,
            Integer spaceRegionHeight,
            Integer droneCount,
            Integer starCount,
            Integer turnsCap,
            ArrayList<String[]> droneList,
            ArrayList<String[]> starList
    ) {
        this.spaceRegionWidth = spaceRegionWidth;
        this.spaceRegionHeight = spaceRegionHeight;
        this.droneCount = droneCount;
        this.starCount = starCount;
        this.turnsLeft = turnsCap;
        this.totalTurns = turnsCap;

        this.initializeSquadron(droneList);
        this.initializeSpaceRegion(droneList, starList);
    }

    private void initializeSquadron(ArrayList<String[]> droneList) {
        this.squadron  = new Squadron();
        this.squadron.main(this.spaceRegionWidth, this.spaceRegionHeight, droneList);
    }

    private void initializeSpaceRegion(ArrayList<String[]> droneList, ArrayList<String[]> starList) {
        this.spaceRegion  = new SpaceRegion();
        this.spaceRegion.main(this.spaceRegionWidth, this.spaceRegionHeight, droneList, starList);
    }

    public void run() {
        while(this.canExecuteTurn()) {
            this.executeTurn();
        };
    }

    private boolean canExecuteTurn() {
        if(this.turnsLeft == 0) return false;
        if(this.isSpaceRegionFullyExplored()) return false;
        return true;
    }

    public Integer getExploredCount() {
        Integer exploredSquaresCount = this.squadron.getExploredSquaresCount();
        return exploredSquaresCount;
    }

    public Integer getExplorableGridSpotsCount() {
        Integer totalGridSpots = this.spaceRegionHeight * this.spaceRegionWidth;
        Integer totalGridSpotsThatCanBeExplored = totalGridSpots - this.starCount;
        return totalGridSpotsThatCanBeExplored;
    }

    public boolean isSpaceRegionFullyExplored() {
        Integer exploredSquaresCount = this.getExploredCount();
        Integer totalGridSpotsThatCanBeExplored = this.getExplorableGridSpotsCount();
        if(exploredSquaresCount >= totalGridSpotsThatCanBeExplored) return true;
        return false;
    }

    private void executeTurn() {
        Integer currentTurn =  this.totalTurns - this.turnsLeft + 1;

        TurnRequest turnRequest = this.squadron.requestTurn();
        if(turnRequest.action == "SCAN") {
            ArrayList<RegionSquare> grid = this.spaceRegion.generateScan(turnRequest.xPosition, turnRequest.yPosition);
            this.squadron.executeTurn(turnRequest, grid);
            System.out.println(
                "TURN: " + currentTurn + " -> " +
                "SCAN for Drone with id: " + turnRequest.droneId +
                " from position x: " + turnRequest.xPosition.toString() +
                " and position y: " + turnRequest.yPosition.toString()
            );
        } else if (turnRequest.action == "THRUST") {
            this.squadron.executeTurn(turnRequest, null);
            this.spaceRegion.moveDrone(turnRequest.xPosition, turnRequest.yPosition, turnRequest.direction);
            System.out.println(
                "TURN: " + currentTurn + " -> " +
                "THRUST for Drone with id: " + turnRequest.droneId +
                " from position x: " + turnRequest.xPosition.toString() +
                " and position y: " + turnRequest.yPosition.toString() +
                " in direction of: " + turnRequest.direction
            );
        } else if (turnRequest.action == "STEER") {
            this.squadron.executeTurn(turnRequest, null);
            System.out.println(
                 "TURN: " + currentTurn + " -> " +
                 "STEER for Drone with id: " + turnRequest.droneId +
                 " from position x: " + turnRequest.xPosition.toString() +
                 " and position y: " + turnRequest.yPosition.toString() +
                 " in direction of: " + turnRequest.direction
            );
        } else if (turnRequest.action == "PASS") {
            this.squadron.executeTurn(turnRequest, null);
            System.out.println(
                "TURN: " + currentTurn + " -> " +
                "PASS for Drone with id: " + turnRequest.droneId +
                " from position x: " + turnRequest.xPosition.toString() +
                " and position y: " + turnRequest.yPosition.toString()
            );
        }
        this.turnsLeft--;
    }
}