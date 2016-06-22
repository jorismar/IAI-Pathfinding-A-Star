
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.BrowserCore;
import com.teamdev.jxbrowser.chromium.LoggerProvider;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import java.util.logging.Level;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jorismar
 */
public class BrowserGUI extends Application implements Runnable {
    private static Browser browser;
    private Scene scene;
    private BrowserView view;
    //private String[] argus;

    public BrowserGUI() {
        //this.argus = null;
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
        this.view = new BrowserView(browser);

        this.scene = new Scene(new BorderPane(view), 700, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        browser.loadURL("https://www.google.com.br/maps/@-7.1611952,-34.8185009,16.5z");
    }
    
    public void reload(String url) {
        browser.loadURL(url);
    }
    
    @Override
    public void run() {
        launch(Gui.argsGUI);
    }
}
