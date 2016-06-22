
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
public class Road {
    private String name;
    private LinkedList<Integer> stops;

    public Road(String name) {
        this.name = name;
        this.stops = new LinkedList<>();
    }

    public void addStop(int stop) {
        this.stops.add(stop);
    }
    
    public String getName() {
        return name;
    }

    public LinkedList<Integer> getStops() {
        return stops;
    }
}
