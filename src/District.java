
import java.util.LinkedList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jorismar
 */
public class District {
    private String name;
    private LinkedList<Road> roads;

    public District(String name) {
        this.name = name;
        this.roads = roads = new LinkedList<>();
    }
    
    public void addRoad(Road road) {
        this.roads.add(road);
    }

    public String getName() {
        return name;
    }

    public LinkedList<Road> getRoads() {
        return roads;
    }
}
