package grafo;

import java.util.LinkedList;
/**
 *
 * @author Jorismar
 */
public interface Node {
    public String getDistrict();
    public String getRoad();
    public double[] getCoordinate();
    public double getDistanceTo(Node nd);
    public double getCostTo(Node nd);
    public LinkedList<Node> getNeighbors();
}
