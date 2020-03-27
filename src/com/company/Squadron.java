package com.company;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Random;

public class Squadron {
    private ArrayList<Drone> droneList;
    private ArrayList<SquareState> collectiveDroneScan;
    public Drone currentDroneForTurn;

    // I am cheating here. The squadron should not know how big the space region is.
    // I will be using these in hasExploredRegionFully method.
    // But with proper logic, you could figure out if the "BARRIER" square states make a complete closure.
    // Then you wouldn't need to know the size of the space region.
    private Integer spaceRegionWidth;
    private Integer spaceRegionHeight;


    public void main(Integer spaceRegionWidth, Integer spaceRegionHeight, ArrayList<String[]> droneList) {
        this.spaceRegionWidth = spaceRegionWidth;
        this.spaceRegionHeight = spaceRegionHeight;

        this.collectiveDroneScan = new ArrayList<SquareState>();

        this.initializeDroneList(droneList);
        this.initializeCollectiveScan(droneList);

        if(this.droneList.size() > 0) {
            this.currentDroneForTurn = this.droneList.get(0);
        } else {
            this.currentDroneForTurn = null;
        }
    }

    private void initializeDroneList(ArrayList<String[]> droneStringList) {
        Drone currentDrone;
        ArrayList<Drone> droneList = new ArrayList<Drone>();

        Integer currentIndex = 0;
        for (String[] droneStrings : droneStringList) {
            Integer droneId = currentIndex + 1;

            // we do this because we are going to add a row and col of barriers
            Integer xLocation = Integer.parseInt(droneStrings[0]) + 1;
            Integer yLocation = Integer.parseInt(droneStrings[1]) + 1;

            String orientation = droneStrings[2];

            currentDrone = new Drone();
            currentDrone.main(droneId, xLocation, yLocation, orientation);
            droneList.add(currentDrone);
            currentIndex++;
        }

        this.droneList = droneList;
    }

    private void initializeCollectiveScan(ArrayList<String[]> droneList) {
        SquareState currentSquare;

        for (String[] droneStrings : droneList) {
            currentSquare = new SquareState();

            // we do this because we are going to add a row and col of barriers
            Integer xLocation = Integer.parseInt(droneStrings[0]) + 1;
            Integer yLocation = Integer.parseInt(droneStrings[1]) + 1;

            currentSquare.main(xLocation, yLocation, "DRONE");
            this.collectiveDroneScan.add(currentSquare);
        }
    }

    // this can for sure be refactored.
    public Integer getExploredSquaresCount() {
        // We have an issue here. I ran into cases where the drone closes out the board but it still hadn't recorded all barriers.
        // You can even imagine a grid where 4 stars are clustered in a corner. A drone will never be able to scan that corner.
        // So, what we have to do is just return the number of explored squares + squares drones are currently in.
        // Then evaluate with the space region to see if the whole grid has been explored

//        Integer expectedBarrierCountForClosure = this.spaceRegionHeight * 2 + this.spaceRegionWidth * 2 + 4;
//        ArrayList<SquareState> collectiveDroneScanForBarrier = (ArrayList<SquareState>)this.collectiveDroneScan.clone();
//        collectiveDroneScanForBarrier.removeIf(ds -> (ds.status != "BARRIER"));
//        if(collectiveDroneScanForBarrier.size() < expectedBarrierCountForClosure) { return false; }
//
//        ArrayList<SquareState> collectiveDroneScanForNonBarrierAndNonEmpty = (ArrayList<SquareState>)this.collectiveDroneScan.clone();
//        collectiveDroneScanForNonBarrierAndNonEmpty.removeIf(ds -> (ds.status == "BARRIER" && ds.status == "EMPTY"));
//        if(collectiveDroneScanForNonBarrierAndNonEmpty.size() < this.spaceRegionHeight * this.spaceRegionWidth) { return false; }

//        return true;

        ArrayList<SquareState> exploredSquares = (ArrayList<SquareState>)this.collectiveDroneScan.clone();
        exploredSquares.removeIf(ds -> (ds.status != "EXPLORED" && ds.status != "DRONE"));
        Integer exploredSquaresCount = exploredSquares.size();
        return exploredSquaresCount;
    }

    public boolean hasLiveDrones() {
        ArrayList<Drone> droneList = (ArrayList<Drone>)this.droneList.clone();

        // drone positions get set to null if they crash
        droneList.removeIf(d -> (d.xPosition == null));
        if(droneList.size() == 0) return false;

        return true;
    }

    public TurnRequest requestTurn() {
        TurnRequest turnRequest = new TurnRequest();
        ArrayList<SquareState> emptySquaresThanCanBeMovedTo = this.getEmptySquaresThanCanBeMovedTo();
        if(emptySquaresThanCanBeMovedTo.size() > 0) {
            ArrayList<SquareState> emptySquaresThanCanBeMovedToWithCurrentDirection = this.getEmptySquaresThanCanBeMovedToWithCurrentDirection(emptySquaresThanCanBeMovedTo);
            //thrust
            if(emptySquaresThanCanBeMovedToWithCurrentDirection.size() > 0) {
                turnRequest.main(
                        this.currentDroneForTurn.droneId,
                        this.currentDroneForTurn.xPosition,
                        this.currentDroneForTurn.yPosition,
                        "THRUST",
                        this.currentDroneForTurn.orientation
                );
            }  else {
                Integer randomIndex = new Random().nextInt(emptySquaresThanCanBeMovedTo.size());
                SquareState randomSquareState = emptySquaresThanCanBeMovedTo.get(randomIndex);

                String newDirection = this.getNewDirection(
                        this.currentDroneForTurn.xPosition,
                        this.currentDroneForTurn.yPosition,
                        randomSquareState.xPosition,
                        randomSquareState.yPosition
                );

                turnRequest.main(
                        this.currentDroneForTurn.droneId,
                        this.currentDroneForTurn.xPosition,
                        this.currentDroneForTurn.yPosition,
                        "STEER",
                        newDirection
                );
            }
        } else {
            if(this.hasScanPotential()) {
                turnRequest.main(
                        this.currentDroneForTurn.droneId,
                        this.currentDroneForTurn.xPosition,
                        this.currentDroneForTurn.yPosition,
                        "SCAN",
                        null
                );
            } else {
                turnRequest.main(
                        this.currentDroneForTurn.droneId,
                        this.currentDroneForTurn.xPosition,
                        this.currentDroneForTurn.yPosition,
                        "PASS",
                        null
                );
            }
        }
        return turnRequest;
    }

    public void executeTurn(TurnRequest turnRequest, ArrayList<RegionSquare> grid) {
        if(turnRequest.action == "SCAN") {
            this.updateCollectiveScan(grid);
        } else if (turnRequest.action == "THRUST") {
            this.executeThrust(turnRequest);
        } else if (turnRequest.action == "STEER") {
            this.executeSteer(turnRequest);
        }
        this.moveUpNextDrone();
    }

    private void moveUpNextDrone() {
        Integer currentDroneIndex = this.droneList.indexOf(this.currentDroneForTurn);
        if(currentDroneIndex + 1 == this.droneList.size()) {
            this.currentDroneForTurn = this.droneList.get(0);
        } else {
            this.currentDroneForTurn = this.droneList.get(currentDroneIndex + 1);
        }
    }

    private void updateCollectiveScan(ArrayList<RegionSquare> grid) {
        SquareState currentSquare;

        for (RegionSquare regionSquare : grid) {

            // remove old square
            this.collectiveDroneScan.removeIf(ds -> (
                    ds.xPosition == regionSquare.xPosition &&
                    ds.yPosition == regionSquare.yPosition
            ));

            currentSquare = new SquareState();

            // we do this because we are going to add a row and col of barriers
            Integer xLocation = regionSquare.xPosition;
            Integer yLocation = regionSquare.yPosition;
            String status = regionSquare.occupancy;

            currentSquare.main(xLocation, yLocation, status);
            // might not need this conditional in the end
            if (!this.collectiveDroneScan.contains((currentSquare))) {
                this.collectiveDroneScan.add(currentSquare);
            }

        }
    }

    private ArrayList<SquareState> getEmptySquaresThanCanBeMovedTo() {
        // these are all brute force and gross I know. Refactor later
        ArrayList<SquareState> clearPathRight = this.getClearPathRightToExploreArea();
        ArrayList<SquareState> clearPathLeft = this.getClearPathLeftToExploreArea();
        ArrayList<SquareState> clearPathUp = this.getClearPathUpToExploreArea();
        ArrayList<SquareState> clearPathDown = this.getClearPathDownToExploreArea();

        ArrayList<SquareState> clearPathEmptySquareStates = new ArrayList<SquareState>();
        clearPathEmptySquareStates.addAll(clearPathRight);
        clearPathEmptySquareStates.addAll(clearPathLeft);
        clearPathEmptySquareStates.addAll(clearPathUp);
        clearPathEmptySquareStates.addAll(clearPathDown);

        // remove duplicates
        ArrayList<SquareState> clearPathEmptySquareStatesNoDup = new ArrayList<SquareState>(new LinkedHashSet<SquareState>(clearPathEmptySquareStates));
        return clearPathEmptySquareStatesNoDup;
    }

    private ArrayList<SquareState> getClearPathRightToExploreArea() {
        ArrayList<SquareState> collectiveDroneScan = (ArrayList<SquareState>)this.collectiveDroneScan.clone();
        Integer currentDroneXPosition = this.currentDroneForTurn.xPosition;
        Integer currentDroneYPosition = this.currentDroneForTurn.yPosition;

        collectiveDroneScan.removeIf(ss -> (ss.xPosition != currentDroneXPosition + 1));
        collectiveDroneScan.removeIf(ss -> (ss.status != "EMPTY"));
        collectiveDroneScan.removeIf(ss -> (
                ss.yPosition != currentDroneYPosition &&
                        ss.yPosition != currentDroneYPosition + 1 &&
                        ss.yPosition != currentDroneYPosition - 1
        ));

        return collectiveDroneScan;
    }

    private ArrayList<SquareState> getClearPathLeftToExploreArea() {
        ArrayList<SquareState> collectiveDroneScan = (ArrayList<SquareState>)this.collectiveDroneScan.clone();
        Integer currentDroneXPosition = this.currentDroneForTurn.xPosition;
        Integer currentDroneYPosition = this.currentDroneForTurn.yPosition;

        collectiveDroneScan.removeIf(ss -> (ss.xPosition != currentDroneXPosition - 1));
        collectiveDroneScan.removeIf(ss -> (ss.status != "EMPTY"));
        collectiveDroneScan.removeIf(ss -> (
                ss.yPosition != currentDroneYPosition &&
                ss.yPosition != currentDroneYPosition + 1 &&
                ss.yPosition != currentDroneYPosition - 1
        ));

        return collectiveDroneScan;
    }

    private ArrayList<SquareState> getClearPathUpToExploreArea() {
        ArrayList<SquareState> collectiveDroneScan = (ArrayList<SquareState>)this.collectiveDroneScan.clone();
        Integer currentDroneXPosition = this.currentDroneForTurn.xPosition;
        Integer currentDroneYPosition = this.currentDroneForTurn.yPosition;

        collectiveDroneScan.removeIf(ss -> (ss.yPosition != currentDroneYPosition - 1));
        collectiveDroneScan.removeIf(ss -> (ss.status != "EMPTY"));

        collectiveDroneScan.removeIf(ss -> (
                ss.xPosition != currentDroneXPosition &&
                ss.xPosition != currentDroneXPosition + 1 &&
                ss.xPosition != currentDroneXPosition - 1
        ));

        return collectiveDroneScan;
    }

    private ArrayList<SquareState> getClearPathDownToExploreArea() {
        ArrayList<SquareState> collectiveDroneScan = (ArrayList<SquareState>)this.collectiveDroneScan.clone();
        Integer currentDroneXPosition = this.currentDroneForTurn.xPosition;
        Integer currentDroneYPosition = this.currentDroneForTurn.yPosition;

        collectiveDroneScan.removeIf(ss -> (ss.yPosition != currentDroneYPosition + 1));
        collectiveDroneScan.removeIf(ss -> (ss.status != "EMPTY"));
        collectiveDroneScan.removeIf(ss -> (
                ss.xPosition != currentDroneXPosition &&
                        ss.xPosition != currentDroneXPosition + 1 &&
                        ss.xPosition != currentDroneXPosition - 1
        ));

        return collectiveDroneScan;
    }

    private ArrayList<SquareState> getEmptySquaresThanCanBeMovedToWithCurrentDirection(ArrayList<SquareState> emptySquaresThanCanBeMovedTo) {
        ArrayList<SquareState> emptySquaresThanCanBeMovedToWithCurrentDirection = (ArrayList<SquareState>)emptySquaresThanCanBeMovedTo.clone();
        Integer currentDroneXPosition = this.currentDroneForTurn.xPosition;
        Integer currentDroneYPosition = this.currentDroneForTurn.yPosition;
        String currentDroneOrientation = this.currentDroneForTurn.orientation;


        emptySquaresThanCanBeMovedToWithCurrentDirection.removeIf(ss -> (
                ss.xPosition == currentDroneXPosition + 1 &&
                ss.yPosition == currentDroneYPosition + 1 &&
                !currentDroneOrientation.equals("southeast")
        ));

        emptySquaresThanCanBeMovedToWithCurrentDirection.removeIf(ss -> (
                ss.xPosition == currentDroneXPosition + 1 &&
                ss.yPosition == currentDroneYPosition - 1 &&
                        !currentDroneOrientation.equals("northeast")
        ));

        emptySquaresThanCanBeMovedToWithCurrentDirection.removeIf(ss -> (
                ss.xPosition == currentDroneXPosition - 1 &&
                ss.yPosition == currentDroneYPosition + 1 &&
                !currentDroneOrientation.equals("southwest")
        ));

        emptySquaresThanCanBeMovedToWithCurrentDirection.removeIf(ss -> (
                ss.xPosition == currentDroneXPosition - 1 &&
                ss.yPosition == currentDroneYPosition - 1 &&
                !currentDroneOrientation.equals("northwest")
        ));

        emptySquaresThanCanBeMovedToWithCurrentDirection.removeIf(ss -> (
                ss.xPosition == currentDroneXPosition &&
                ss.yPosition == currentDroneYPosition - 1 &&
                 !currentDroneOrientation.equals("north")
        ));

        emptySquaresThanCanBeMovedToWithCurrentDirection.removeIf(ss -> (
                ss.xPosition == currentDroneXPosition &&
                ss.yPosition == currentDroneYPosition + 1 &&
                !currentDroneOrientation.equals("south")
        ));

        emptySquaresThanCanBeMovedToWithCurrentDirection.removeIf(ss -> (
                ss.xPosition == currentDroneXPosition - 1 &&
                ss.yPosition == currentDroneYPosition &&
                !currentDroneOrientation.equals("west")
        ));

        emptySquaresThanCanBeMovedToWithCurrentDirection.removeIf(ss -> (
                ss.xPosition == currentDroneXPosition + 1 &&
                ss.yPosition == currentDroneYPosition &&
                !currentDroneOrientation.equals("east")
        ));

        return emptySquaresThanCanBeMovedToWithCurrentDirection;
    }

    private boolean hasScanPotential() {
        ArrayList<SquareState> collectiveDroneScan = (ArrayList<SquareState>)this.collectiveDroneScan.clone();
        Integer currentDroneXPosition = this.currentDroneForTurn.xPosition;
        Integer currentDroneYPosition = this.currentDroneForTurn.yPosition;

        // remove left and right
        collectiveDroneScan.removeIf(ss -> (
                ss.yPosition == currentDroneYPosition &&
                (
                        ss.xPosition != currentDroneXPosition - 1 ||
                        ss.xPosition != currentDroneXPosition - 1
                )
        ));

        // remove up and down
        collectiveDroneScan.removeIf(ss -> (
                ss.xPosition == currentDroneXPosition &&
                (
                       ss.yPosition != currentDroneYPosition - 1 ||
                       ss.yPosition != currentDroneYPosition - 1
                )
        ));

        //remove top left and top right
        collectiveDroneScan.removeIf(ss -> (
                ss.xPosition == currentDroneXPosition - 1 &&
                (
                       ss.yPosition != currentDroneYPosition - 1 ||
                       ss.yPosition != currentDroneYPosition - 1
                )
        ));

        //remove bottom left and top right
        collectiveDroneScan.removeIf(ss -> (
                ss.xPosition == currentDroneXPosition - 1 &&
                 (
                        ss.yPosition != currentDroneYPosition - 1 ||
                        ss.yPosition != currentDroneYPosition - 1
                 )
        ));

        if(collectiveDroneScan.size() < 8) { return true; }
        return false;
    }

    private void executeThrust(TurnRequest turnRequest) {
        ArrayList<RegionSquare> newScan = new ArrayList<RegionSquare>();

        RegionSquare oldSquareState = new RegionSquare();
        oldSquareState.main(this.currentDroneForTurn.xPosition, this.currentDroneForTurn.yPosition, "EXPLORED");
        newScan.add(oldSquareState);

        Integer newXPosition = null;
        Integer newYPosition = null;
        if(turnRequest.direction.equals("southeast")) {
            newXPosition = this.currentDroneForTurn.xPosition + 1;
            newYPosition = this.currentDroneForTurn.yPosition + 1;
        }

        if(turnRequest.direction.equals("northeast")) {
            newXPosition = this.currentDroneForTurn.xPosition + 1;
            newYPosition = this.currentDroneForTurn.yPosition - 1;
        }

        if(turnRequest.direction.equals("southwest")) {
            newXPosition = this.currentDroneForTurn.xPosition - 1;
            newYPosition = this.currentDroneForTurn.yPosition + 1;
        }

        if(turnRequest.direction.equals("northwest")) {
            newXPosition = this.currentDroneForTurn.xPosition - 1;
            newYPosition = this.currentDroneForTurn.yPosition - 1;
        }

        if(turnRequest.direction.equals("north")) {
            newXPosition = this.currentDroneForTurn.xPosition;
            newYPosition = this.currentDroneForTurn.yPosition - 1;
        }

        if(turnRequest.direction.equals("south")) {
            newXPosition = this.currentDroneForTurn.xPosition;
            newYPosition = this.currentDroneForTurn.yPosition + 1;
        }

        if(turnRequest.direction.equals("west")) {
            newXPosition = this.currentDroneForTurn.xPosition - 1;
            newYPosition = this.currentDroneForTurn.yPosition;
        }

        if(turnRequest.direction.equals("east")) {
            newXPosition = this.currentDroneForTurn.xPosition + 1;
            newYPosition = this.currentDroneForTurn.yPosition;
        }

        RegionSquare newSquareState = new RegionSquare();
        newSquareState.main(newXPosition, newYPosition, "DRONE");
        newScan.add(newSquareState);

        this.currentDroneForTurn.thrust(newXPosition, newYPosition);
        this.updateCollectiveScan(newScan);
    }

    private void executeSteer(TurnRequest turnRequest) {
        this.currentDroneForTurn.steer(turnRequest.direction);
    }

    public String getNewDirection(Integer oldX, Integer oldY, Integer newX, Integer newY) {
        String verticalDirection = "";

        // south
        if(oldY < newY) {
            verticalDirection = "south";
        // north
        } else if (oldY > newY) {
            verticalDirection = "north";
        }

        String lateralDirection = "";
        // west
        if(oldX < newX) {
            lateralDirection = "east";
        // east
        } else if (oldX > newX) {
            lateralDirection = "west";
        }
        return verticalDirection + lateralDirection;
    }
}