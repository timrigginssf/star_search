package com.company;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Simulator {

    private String fileDirectory;
    private SimulationRun simulationRun;
    private ArrayList<String[]> fileContents;

    public void main(String fileDirectory) {
        this.fileDirectory = fileDirectory;
        this.fileContents = new ArrayList<>();
        this.simulationRun =  new SimulationRun();

        this.readFile();
        this.printInitialList();
        System.out.println();

        this.initializeSimulation();
        this.runSimulation();
    }

    public void readFile() {
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        try {
            br = new BufferedReader(new FileReader(this.fileDirectory));
            while ((line = br.readLine()) != null) {
                this.fileContents.add(line.split(cvsSplitBy));
            }
        }
        catch (FileNotFoundException e) { e.printStackTrace();  }
        catch (IOException e) { e.printStackTrace(); }
    }

    private void initializeSimulation() {

        String widthString = String.join("", this.fileContents.get(0));
        Integer spaceRegionWidth = Integer.parseInt(widthString);

        String heightString = String.join("", this.fileContents.get(1));
        Integer spaceRegionHeight = Integer.parseInt(heightString);

        String droneCountString = String.join("", this.fileContents.get(2));
        Integer droneCount = Integer.parseInt(droneCountString);

        // we want to skip 0, 1, 2. The the number of drones, then evaluate the next index
        String starCountString = String.join("", this.fileContents.get(2 + droneCount + 1));
        Integer starCount = Integer.parseInt(starCountString);

        // Same idea as above
        // we want to skip 0, 1, 2. The the number of drones, then evaluate the next index, then skip the number of stars, then next index
        String turnsCapString = String.join("", this.fileContents.get(2 + droneCount + 1 + starCount + 1));
        Integer turnsCap = Integer.parseInt(turnsCapString);

        ArrayList<String[]> droneList;
        if(droneCount > 0) {
            droneList = new ArrayList<String[]>(this.fileContents.subList(3, 3 + droneCount));
        } else {
            droneList = new ArrayList<String[]>();
        }

        ArrayList<String[]> starList;
        if(starCount > 0 && droneCount > 0) {
            starList = new ArrayList<String[]>(this.fileContents.subList(2 + droneCount + 2, 2 + droneCount + 2 + starCount));
        } else {
            starList = new ArrayList<String[]>();
        }

        this.simulationRun.main(spaceRegionWidth, spaceRegionHeight, droneCount, starCount, turnsCap, droneList, starList);
    }

    private void printInitialList() {
        for (String[] strings : this.fileContents) {
            for (String s : strings) { System.out.print(s + ","); }
            System.out.println();
        }
    }

    private void runSimulation() {
        this.simulationRun.run();
        this.printResults();
    }

    private void printResults() {
        Boolean isRegionExploredFully = this.simulationRun.isSpaceRegionFullyExplored();
        Integer turnsTaken = this.simulationRun.totalTurns - this.simulationRun.turnsLeft;

        System.out.println();
        System.out.println("Your simulation has Ended");
        if(isRegionExploredFully) {
            System.out.println(
                "Your squadron was able to complete a scan of the space region in " +
                 turnsTaken + " turns!"
            );

        } else if (this.simulationRun.turnsLeft == 0) {
            Integer exploredSquaresCount = this.simulationRun.getExploredCount();
            Integer totalGridSpotsThatCanBeExplored = this.simulationRun.getExplorableGridSpotsCount();
            Integer unxploredCoordsCount = totalGridSpotsThatCanBeExplored - exploredSquaresCount;

            System.out.println(
                "Your squadron was not able to complete a scan of the space region in the " +
                this.simulationRun.totalTurns + " turns allotted." +
                " There were " + unxploredCoordsCount + " coordinates left to explore"
            );
        }
    }

}