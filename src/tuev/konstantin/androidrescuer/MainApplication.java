package tuev.konstantin.androidrescuer;

import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.svg.SVGGlyphLoader;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowException;
import io.datafx.controller.flow.FlowHandler;
import io.datafx.controller.flow.container.DefaultFlowContainer;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import tuev.konstantin.androidrescuer.gui.main.MainController;
import tuev.konstantin.androidrescuer.mycomponenets.SingleInstanceChecker;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class MainApplication extends Application {


    @Override
    public void init() throws Exception {
        if (!SingleInstanceChecker.INSTANCE.isOnlyInstance(MainApplication::otherInstanceTriedToLaunch, true)) {
            System.exit(0);
        }
    }

    private static void otherInstanceTriedToLaunch() {
        primaryStage.setIconified(false);
        primaryStage.toFront();
    }

    //Application.getApplication().setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
    @SuppressWarnings({"JavaReflectionInvocation", "unchecked"})
    private void enableOSXQuitStrategy() {
        try {
            Class application = Class.forName("com.apple.eawt.Application");
            Method getApplication = application.getMethod("getApplication");
            Object instance = getApplication.invoke(application);
            Class strategy = Class.forName("com.apple.eawt.QuitStrategy");
            Enum closeAllWindows = Enum.valueOf(strategy, "CLOSE_ALL_WINDOWS");
            Method method = application.getMethod("setQuitStrategy", strategy);
            method.invoke(instance, closeAllWindows);
        } catch (ClassNotFoundException | NoSuchMethodException |
                SecurityException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException exp) {
            exp.printStackTrace(System.err);
        }
    }

    @FXMLViewFlowContext
    private ViewFlowContext flowContext;

    public static void main(String[] args) {
        launch(args);
    }

    public static Stage primaryStage = null;

    @Override
    public void start(Stage stage) {
        Platform.setImplicitExit(true);
        primaryStage = stage;
        final String os = System.getProperty ("os.name");
        if (os != null && os.startsWith ("Mac")) {
            //menuBar.useSystemMenuBarProperty().set(true);
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", getBundle().getString("appname"));
            enableOSXQuitStrategy();
        }
        new Thread(() -> {
            try {
                InputStream stream = getClass().getResourceAsStream("/fonts/icomoon.svg");
                SVGGlyphLoader.loadGlyphsFont(stream,
                        "icomoon.svg");
            } catch (Exception ioExc) {
                ioExc.printStackTrace();
            }
        }).start();

        Flow flow = new Flow(MainController.class);
        DefaultFlowContainer container = new DefaultFlowContainer();
        flowContext = new ViewFlowContext();
        flowContext.register("Stage", stage);
        FlowHandler handler = flow.createHandler(flowContext);
        handler.getViewConfiguration().setResources(getBundle());
        try {
            handler.start(container);
        } catch (FlowException e) {
            e.printStackTrace();
        }

        JFXDecorator decorator = new JFXDecorator(stage, container.getView(), false, true, true);
        decorator.setCustomMaximize(true);
        decorator.setOnCloseButtonAction(() -> {
            Platform.exit();
            System.exit(0);
        });

        double width = 800;
        double height = 600;
        try {
            Rectangle2D bounds = Screen.getScreens().get(0).getBounds();
            width = bounds.getWidth() / 1.53;
            height = bounds.getHeight() / 1.35;
        }catch (Exception ignored){}

        Scene scene = new Scene(decorator, width, height);
        final ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.addAll(MainApplication.class.getResource("/css/jfoenix-fonts.css").toExternalForm(),
                MainApplication.class.getResource("/css/jfoenix-design.css").toExternalForm(),
                MainApplication.class.getResource("/css/jfoenix-main-demo.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static boolean reset = true;

    private static Preferences prefs = null;

    public static Preferences getPrefs() {
        if (prefs == null) {
            return prefs = Preferences.userNodeForPackage(MainApplication.class);
        }
        return prefs;
    }

    public static Locale getLocale() {
        return getPrefs().get("locale", "en").equals("en") ? Locale.ENGLISH : Locale.GERMAN;
    }

    private static ResourceBundle strings = null;

    public static ResourceBundle getBundle() {
        if (strings == null || reset) {
            reset = false;
            return strings = ResourceBundle.getBundle("strings", getLocale());
        }
        return strings;
    }
}
