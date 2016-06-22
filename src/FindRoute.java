/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserCore;
import com.teamdev.jxbrowser.chromium.LoggerProvider;
import com.teamdev.jxbrowser.chromium.events.FailLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.FrameLoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadEvent;
import com.teamdev.jxbrowser.chromium.events.LoadListener;
import com.teamdev.jxbrowser.chromium.events.ProvisionalLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.StartLoadingEvent;
import com.teamdev.jxbrowser.chromium.internal.Environment;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import grafo.*;
import heuristic.AStar;
import java.awt.BorderLayout;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Jorismar
 */
public class FindRoute {
    private LinkedList<GraphNode> graph;
    private LinkedList<double[]> points;
    private String routeurl;
    private String[] argus;
    private BrowserGUI browser;
    private LinkedList<District> districtsList;
    //private Thread t1;
        
    public FindRoute(BrowserGUI browser) {
        this.graph = new LinkedList<>();
        this.browser = browser;
        this.districtsList = new LinkedList<>();
        //this.t1 = new Thread(this.browser);
    }

    public LinkedList<District> getDistrictsList() {
        return districtsList;
    }
    
    public GraphNode findNeighbors(double lat, double lon) {
        for (GraphNode nd : graph) {
            double coord[] = nd.getCoordinate();

            if (coord[0] == lat && coord[1] == lon) {
                return nd;
            }
        }

        return null;
    }

    public double calculateCost(GraphNode nd1, GraphNode nd2) {
        if(nd1 == null || nd2 == null)
            return 0.0d;
        
        double coord1[] = nd1.getCoordinate();
        double coord2[] = nd2.getCoordinate();

        String url = "http://router.project-osrm.org/viaroute?loc=" + coord1[0] + "," + coord1[1] + "&loc=" + coord2[0] + "," + coord2[1];

        try {
            InputStream input = new URL(url).openStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")));

            StringBuilder sb = new StringBuilder();

            int cp = rd.read();

            while (cp != -1) {
                sb.append((char) cp);
                cp = rd.read();
            }

            JSONObject jsonObject = new JSONObject(sb.toString());

            return jsonObject.getJSONObject("route_summary").getDouble("total_distance"); //TEMPO DO PERCUSSO
        } catch (IOException e) {
            return nd1.getDistanceTo(nd2) * 125;    //1km vai demorar 7min
        }
    }

    public void buildGraph() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("bus_stop.json"));

            String line, text = "";

            while ((line = reader.readLine()) != null) {
                text += line + "\n";
            }

            JSONObject jsonRoot = new JSONObject(text);
            JSONArray districts = jsonRoot.getJSONArray("districts");

            for (int d = 0; d < districts.length(); d++) {
                JSONObject district = districts.getJSONObject(d);

                String districtName = district.getString("name");
                this.districtsList.add(new District(districtName));
                JSONArray districtRoutes = district.getJSONArray("route");

                for (int r = 0; r < districtRoutes.length(); r++) {
                    JSONObject road = districtRoutes.getJSONObject(r);

                    String roadName = road.getString("road");
                    this.districtsList.getLast().addRoad(new Road(roadName));
                    JSONArray points = road.getJSONArray("points");

                    GraphNode neighbor = null;

                    for (int p = 0; p < points.length(); p++) {
                        JSONObject point = points.getJSONObject(p);
                        JSONArray coordinates = point.getJSONArray("coordinates");

                        if (point.getString("cross").equals("yes")) {
                            GraphNode aux_neighbor = findNeighbors(coordinates.getDouble(0), coordinates.getDouble(1));
                            
                            if(aux_neighbor != null) {
                                neighbor = aux_neighbor;
                                continue;
                            }
                        }

                        graph.add(new GraphNode(districtName, roadName, coordinates.getDouble(0), coordinates.getDouble(1)));
                        this.districtsList.getLast().getRoads().getLast().addStop(graph.size() - 1);
                        
                        double cost = calculateCost(neighbor, graph.getLast());

                        graph.getLast().addNeighbors(neighbor, cost);
                        
                        if(neighbor != null)
                            neighbor.addNeighbors(graph.getLast(), cost);
                        
                        neighbor = graph.getLast();
                    }
                    
                    if(neighbor != null && neighbor != graph.getLast()) {
                        double cost = calculateCost(neighbor, graph.getLast());

                        graph.getLast().addNeighbors(neighbor, cost);
                        
                        neighbor.addNeighbors(graph.getLast(), cost);
                    }
                }
            }
        } catch (IOException | JSONException ex) {
            System.err.println("JSON read error!");
        }
    }

    public boolean recursiveReduction(LinkedList<Node> route, Node main, Node node) {
        boolean hasNeighborsOnTheRoute = false;
        
        if(!route.contains(node))
            return false;
        
        if(route.getLast() == node)
            return true;
        
        for(int i = 0; i < node.getNeighbors().size(); i++) {
            if(main != node.getNeighbors().get(i))
                hasNeighborsOnTheRoute = recursiveReduction(route, node, node.getNeighbors().get(i));
            
            if(hasNeighborsOnTheRoute)
                break;
        }

        if(!hasNeighborsOnTheRoute) 
            route.remove(node);
        
        return hasNeighborsOnTheRoute;
    }
    
    public String find(int id1, int id2) {
        LinkedList<Node> route;
        AStar astar = new AStar();

        System.err.print("Searching route...");

        route = astar.findRoute(graph.get(id1), graph.get(id2));

        if (route == null) {
            System.err.println("Route not found!");
            return "";
        }
        
        System.err.print("Reducing route...");
        recursiveReduction(route, route.getFirst(), route.getFirst());
        System.err.println("done!");

        routeurl = "https://www.google.com.br/maps/dir/";

        for (int i = 0; i < route.size(); i++)
            routeurl += route.get(i).getCoordinate()[0] + "," + route.get(i).getCoordinate()[1] + "/";

        System.err.println("Route: " + routeurl);

        return routeurl;
    }
}  

