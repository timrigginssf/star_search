package com.company;

import java.util.ArrayList;

public class SpaceRegion {
    private ArrayList<RegionSquare> grid;

    public void main(
            Integer regionWidth,
            Integer regionHeight,
            ArrayList<String[]> droneList,
            ArrayList<String[]> starList
    ) {
        this.grid = new ArrayList<RegionSquare>();
        this.initializeGrid(regionWidth, regionHeight, droneList, starList);
    }

    private void initializeGrid(
            Integer regionWidth,
            Integer regionHeight,
            ArrayList<String[]> droneList,
            ArrayList<String[]> starList
    ) {
        this.initializeEmptySpacesInGrid(regionWidth, regionHeight);
        this.initializeBarriersInGrid(regionWidth, regionHeight);
        this.initializeDronesInGrid(droneList);
        this.initializeStarsInGrid(starList);
    }

    private void initializeEmptySpacesInGrid(Integer regionWidth, Integer regionHeight) {
        RegionSquare currentSquare;

        for (int i = 0; i < regionWidth + 2; i++) {
            for (int j = 0; j < regionHeight + 2; j++) {
                currentSquare = new RegionSquare();
                currentSquare.main(i, j, "EMPTY");
                this.grid.add(currentSquare);
            }
        }
    }

    private void initializeBarriersInGrid(Integer regionWidth, Integer regionHeight) {

        RegionSquare gridSquare1;
        RegionSquare gridSquare2;

        for (int i = 0; i < regionWidth + 2; i++) {
            // along top
            gridSquare1 = this.findRegionSquareByPosition(i, 0, false);
            gridSquare1.occupancy = "BARRIER";

            // along bottom
            gridSquare2 = this.findRegionSquareByPosition(i, regionHeight + 1, false);
            gridSquare2.occupancy = "BARRIER";
        }

        for (int i = 0; i < regionHeight + 2; i++) {
            // along left
            gridSquare1 = this.findRegionSquareByPosition(0, i, false);
            gridSquare1.occupancy = "BARRIER";

            // along right
            gridSquare2 = this.findRegionSquareByPosition(regionWidth + 1, i, false);
            gridSquare2.occupancy = "BARRIER";
        }
    }

    private void initializeDronesInGrid(ArrayList<String[]> droneList) {
        RegionSquare currentSquare;

        Integer currentIndex = 0;
        for (String[] droneStrings : droneList) {

            // we do this because we are going to add a row and col of barriers
            Integer xLocation = Integer.parseInt(droneStrings[0]) + 1;
            Integer yLocation = Integer.parseInt(droneStrings[1]) + 1;

            currentSquare = this.findRegionSquareByPosition(xLocation, yLocation, false);
            currentSquare.occupancy = "DRONE";
        }
    }

    private void initializeStarsInGrid(ArrayList<String[]> starList) {
        RegionSquare currentSquare;

        Integer currentIndex = 0;
        for (String[] starStrings : starList) {

            // we do this because we are going to add a row and col of barriers
            Integer xLocation = Integer.parseInt(starStrings[0]) + 1;
            Integer yLocation = Integer.parseInt(starStrings[1]) + 1;

            currentSquare = this.findRegionSquareByPosition(xLocation, yLocation, false);
            currentSquare.occupancy = "STAR";
        }
    }

    public ArrayList<RegionSquare> generateScan(Integer xPosition, Integer yPosition) {
        ArrayList<RegionSquare> grid = new ArrayList<RegionSquare>();

        RegionSquare northSquare = this.findRegionSquareByPosition(xPosition, yPosition - 1, true);
        RegionSquare southSquare = this.findRegionSquareByPosition(xPosition, yPosition + 1, true);
        RegionSquare southwestSquare = this.findRegionSquareByPosition(xPosition - 1, yPosition + 1, true);
        RegionSquare southeastSquare = this.findRegionSquareByPosition(xPosition + 1, yPosition + 1, true);
        RegionSquare northwestSquare = this.findRegionSquareByPosition(xPosition - 1, yPosition - 1, true);
        RegionSquare northeastSquare = this.findRegionSquareByPosition(xPosition + 1, yPosition - 1, true);
        RegionSquare westSquare = this.findRegionSquareByPosition(xPosition - 1, yPosition, true);
        RegionSquare eastSquare = this.findRegionSquareByPosition(xPosition + 1, yPosition, true);

        grid.add(northSquare);
        grid.add(southSquare);
        grid.add(southwestSquare);
        grid.add(southeastSquare);
        grid.add(northwestSquare);
        grid.add(northeastSquare);
        grid.add(westSquare);
        grid.add(eastSquare);

        return grid;
    }

    private RegionSquare findRegionSquareByPosition(Integer xPosition, Integer yPosition, Boolean cloneGrid) {
        ArrayList<RegionSquare> grid;
        if(cloneGrid) {
            grid = (ArrayList<RegionSquare>)this.grid.clone();
        } else {
            grid = this.grid;
        }

        for (RegionSquare regionSquare : this.grid) {
            if (regionSquare.xPosition == xPosition && regionSquare.yPosition == yPosition) {
                return regionSquare;
            }
        }
        return null;
    }

    public void moveDrone(Integer xPosition, Integer yPosition, String direction) {
            RegionSquare oldRegionSquare = this.findRegionSquareByPosition(xPosition, yPosition, false);;
            oldRegionSquare.occupancy ="EMPTY";

            if(direction.equals("southeast")) {
                RegionSquare regionSquare = this.findRegionSquareByPosition(xPosition + 1, yPosition + 1, false);
                regionSquare.occupancy = "DRONE";
            }

            if(direction.equals("northeast")) {
                RegionSquare regionSquare = this.findRegionSquareByPosition(xPosition + 1, yPosition - 1, false);
                regionSquare.occupancy = "DRONE";
            }

            if(direction.equals("southwest")) {
                RegionSquare regionSquare = this.findRegionSquareByPosition(xPosition - 1, yPosition + 1, false);
                regionSquare.occupancy = "DRONE";
            }

            if(direction.equals("northwest")) {
                RegionSquare regionSquare = this.findRegionSquareByPosition(xPosition - 1, yPosition - 1, false);
                regionSquare.occupancy = "DRONE";
            }

            if(direction.equals("north")) {
                RegionSquare regionSquare = this.findRegionSquareByPosition(xPosition, yPosition - 1, false);
                regionSquare.occupancy = "DRONE";
            }

            if(direction.equals("south")) {
                RegionSquare regionSquare = this.findRegionSquareByPosition(xPosition, yPosition + 1, false);
                regionSquare.occupancy = "DRONE";
            }

            if(direction.equals("west")) {
                RegionSquare regionSquare = this.findRegionSquareByPosition(xPosition - 1, yPosition, false);
                regionSquare.occupancy = "DRONE";
            }

            if(direction.equals("east")) {
                RegionSquare regionSquare = this.findRegionSquareByPosition(xPosition + 1, yPosition, false);
                regionSquare.occupancy = "DRONE";
            }
    }
}