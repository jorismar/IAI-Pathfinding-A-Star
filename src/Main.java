/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*

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
 *
public class Main extends Application implements Runnable {
    public static LinkedList<GraphNode> graph = new LinkedList<>();
    private static LinkedList<double[]> points;
    public static String routeurl;
    public static String[] argus;
    public static Browser browser;

    public static GraphNode findNeighbors(double lat, double lon) {
        for (GraphNode nd : Main.graph) {
            double coord[] = nd.getCoordinate();

            if (coord[0] == lat && coord[1] == lon) {
                return nd;
            }
        }

        return null;
    }

    public static double calculateCost(GraphNode nd1, GraphNode nd2) {
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

    public static void buildGraph() {
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
                JSONArray districtRoutes = district.getJSONArray("route");

                for (int r = 0; r < districtRoutes.length(); r++) {
                    JSONObject road = districtRoutes.getJSONObject(r);

                    String roadName = road.getString("road");
                    JSONArray points = road.getJSONArray("points");

                    GraphNode neighbor = null;

                    for (int p = 0; p < points.length(); p++) {
                        JSONObject point = points.getJSONObject(p);
                        JSONArray coordinates = point.getJSONArray("coordinates");

                        if (point.getString("cross").equals("yes")) {
                            GraphNode aux_neighbor = Main.findNeighbors(coordinates.getDouble(0), coordinates.getDouble(1));
                            
                            if(aux_neighbor != null) {
                                neighbor = aux_neighbor;
                                continue;
                            }
                        }

                        Main.graph.add(new GraphNode(districtName, roadName, coordinates.getDouble(0), coordinates.getDouble(1)));
                        
                        double cost = Main.calculateCost(neighbor, Main.graph.getLast());

                        Main.graph.getLast().addNeighbors(neighbor, cost);
                        
                        if(neighbor != null)
                            neighbor.addNeighbors(Main.graph.getLast(), cost);
                        
                        neighbor = Main.graph.getLast();
                    }
                }
            }
        } catch (IOException | JSONException ex) {
            System.err.println("JSON read error!");
        }
    }

    public static boolean recursiveReduction(LinkedList<Node> route, Node main, Node node) {
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
    
    public static void main(String[] args) {
        LinkedList<Node> route;
        AStar astar = new AStar();

        System.err.print("Building graph...");
        Main.buildGraph();
        System.err.println("done!");

        Thread t1 = new Thread(new Main());
        
        for(GraphNode nd: graph) {
            System.err.println("Node: " + nd.getRoad() + "[" + nd.getCoordinate()[0] + ", " + nd.getCoordinate()[1] + "]");
            for(Node neigh: nd.getNeighbors())
                System.err.println(" -> [" + + neigh.getCoordinate()[0] + ", " + neigh.getCoordinate()[1] + "] - " + neigh.getCostTo(nd));
        }
        
        while (true) {
            //Scanner scan = new Scanner(System.in);
            
            String id1 = JOptionPane.showInputDialog("Origin ID: ");
            //System.err.println("Origin ID: ");
            //String id1 = scan.nextLine();

            //System.err.println("Destiny ID: ");
            String id2 = JOptionPane.showInputDialog("Destiny ID: ");//scan.nextLine();

            System.err.print("Searching route...");

            JOptionPane.showMessageDialog(null, "Searching route from " + graph.get(Integer.parseInt(id1)).getRoad() + " to " + graph.get(Integer.parseInt(id2)).getRoad());
            route = astar.findRoute(graph.get(Integer.parseInt(id1)), graph.get(Integer.parseInt(id2)));

            System.err.print("Reducing route...");
            recursiveReduction(route, route.getFirst(), route.getFirst());
            
            System.err.println("done!");

            if (route == null) {
                System.err.println("Route not found!");
                return;
            }

            routeurl = "https://www.google.com.br/maps/dir/";

            for (int i = 0; i < route.size(); i++) {
                routeurl += route.get(i).getCoordinate()[0] + "," + route.get(i).getCoordinate()[1] + "/";
            }

            //routeurl += "/data=!3e0";
            
            //showPage(url);
            System.err.println("Route: " + routeurl);
            argus = args;
            
            if(routeurl.length() > 2000){
                System.err.print("The route exceeded the limit we can to display.");
            } else {
                if(!t1.isAlive())
                    t1.start();
                else
                    reload();
            }
        }
    }

    @Override
    public void init() throws Exception {
        // On Mac OS X Chromium engine must be initialized in non-UI thread.
        LoggerProvider.getChromiumProcessLogger().setLevel(Level.OFF);
        LoggerProvider.getIPCLogger().setLevel(Level.OFF);
        LoggerProvider.getBrowserLogger().setLevel(Level.OFF);

        BrowserCore.initialize();
    }

    @Override
    public void start(Stage primaryStage) {
        browser = new Browser();
        BrowserView view = new BrowserView(browser);

        Scene scene = new Scene(new BorderPane(view), 700, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        browser.loadURL(routeurl);

        //browser.executeJavaScript("\nalert('Map loaded!');\n");
    }
    
    public static void reload() {
        browser.loadURL(routeurl);
    }
    
    @Override
    public void run() {
        launch(argus);
    }
}
*/