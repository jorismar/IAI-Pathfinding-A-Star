/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grafo;

import java.util.LinkedList;

/**
 *
 * @author Jorismar
 */
public class GraphNode implements Node {
    private String district;
    private String road;
    private double coordinate[];
    private LinkedList<Node> neighbors;
    private LinkedList<Double> neighbors_cost;

    public GraphNode(String district, String road, double lat, double lon) {
        this.district = district;
        this.road = road;
        this.neighbors = new LinkedList<>();
        this.neighbors_cost = new LinkedList<>();
        this.coordinate = new double[2];
        this.coordinate[0] = lat;
        this.coordinate[1] = lon;
    }

    public void addNeighbors(Node node, double cost) {
        if(node == null)
            return;
        
        this.neighbors.add(node);
        this.neighbors_cost.add(cost);
    }

    @Override
    public LinkedList<Node> getNeighbors() {
        return neighbors;
    }

    @Override
   public double getDistanceTo(Node nd) {
        double coord[] = nd.getCoordinate();
        
        final double lat = this.deg2rad(coord[0] - this.coordinate[0]);
        final double lon = this.deg2rad(coord[1] - this.coordinate[1]);
        final double a = Math.sin(lat / 2) * Math.sin(lat / 2) + Math.cos(this.deg2rad(this.coordinate[0])) * Math.cos(this.deg2rad(coord[0])) * Math.sin(lon / 2) * Math.sin(lon / 2);
        
        return (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))) * 6371 * 1000;
    }
    
    @Override
    public double getCostTo(Node nd) {
        return this.neighbors_cost.get(this.neighbors.indexOf(nd));
    }

    @Override
    public String getDistrict() {
        return district;
    }

    @Override
    public String getRoad() {
        return road;
    }

    @Override
    public double[] getCoordinate() {
        return coordinate;
    }
    
    private double deg2rad(double deg) {
        return deg * (Math.PI / 180);
    }

}
