package heuristic;
import grafo.Node;

public class AStarNode {
    private int father_id;
    private Node node;
    private double F;
    private double coast;
    private double distance;

    public AStarNode(Node node, int father, double coast, double distance) {
        this.father_id = father;
        this.node = node;
        this.coast = coast / 2.0d;
        this.distance = distance;
        this.F = this.coast + this.distance;
    }

    public int getFatherID() {
        return father_id;
    }

    public void setFatherID(int id) {
        this.father_id = id;
    }

    public double getF() {
        System.err.println("[" + this.node.getCoordinate()[0] + ", " + this.node.getCoordinate()[1] + "] - F=" + this.F + " C=" + this.coast + " D=" + this.distance);
        return F;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public double getCoast() {
        return coast;
    }

    public void setCoast(double coast) {
        this.coast = coast / 2.0d;
        this.F = this.coast + this.distance;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
        this.F = this.coast + this.distance;
    }
}
