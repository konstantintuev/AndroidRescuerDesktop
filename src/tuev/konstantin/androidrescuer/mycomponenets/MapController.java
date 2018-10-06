package tuev.konstantin.androidrescuer.mycomponenets;

import com.jfoenix.controls.*;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.GMapMouseEvent;
import com.lynden.gmapsfx.javascript.event.MouseEventHandler;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import io.datafx.controller.ViewController;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.json.JSONException;
import org.json.JSONObject;
import tuev.konstantin.androidrescuer.gui.main.MainController;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import static tuev.konstantin.androidrescuer.MainApplication.getBundle;

@ViewController(value = "/fxml/map.fxml", title = "Maps")
public class MapController implements Initializable, MapComponentInitializedListener {

    @FXML
    private JFXTextField phone;

    @FXML
    private JFXPasswordField pass;

    @FXML
    private GoogleMapView mapView;

    @FXML
    private StackPane root;

    @FXML
    private JFXButton trace;

    @FXML
    private JFXCheckBox autoRefresh;

    private GoogleMap map;

    @FXMLViewFlowContext
    private ViewFlowContext context;


    LatLong lastLatLong = null;

    /**
     * init fxml when loaded.
     */
    @PostConstruct
    public void init() {
        MainController.toolbarTitle.setText(getBundle().getString("mapTitle"));
        root.heightProperty().addListener((observable, oldValue, newValue) -> mapView.setPrefHeight(root.getHeight()-111));

        final String os = System.getProperty ("os.name");
        KeyCombination.Modifier key[] = new KeyCombination.Modifier[]{KeyCombination.CONTROL_DOWN};
        if (os != null && os.startsWith ("Mac")) {
            key[0] = KeyCombination.META_DOWN;
        }
        final boolean[] sceneDone = {false};
        root.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !sceneDone[0]) {
                sceneDone[0] = true;

                newValue.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {

                    KeyCombination charComboIn = new KeyCharacterCombination("+", key[0]);

                    KeyCombination charComboOut = new KeyCharacterCombination("-", key[0]);

                    public void handle(KeyEvent ke) {
                        if (mapReady.getValue()) {
                            if (charComboIn.match(ke)) {
                                int zoom = map.getZoom();
                                if (zoom <= 18) {
                                    map.setZoom(zoom + 1);
                                }
                                ke.consume(); // <-- stops passing the event to next node
                            }
                            if (charComboOut.match(ke)) {
                                int zoom = map.getZoom();
                                if (zoom >= 5) {
                                    map.setZoom(zoom - 1);
                                }
                                ke.consume(); // <-- stops passing the event to next node
                            }
                        }
                    }
                });
            }
        });
        trace.setOnMouseClicked(event -> search(true));
        phone.textProperty().addListener((observable, oldValue, newValue) -> canRefresh = false);
        pass.textProperty().addListener((observable, oldValue, newValue) -> canRefresh = false);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (canRefreshCheck && canRefresh && !phone.getText().replace(" ", "").isEmpty() && !pass.getText().replace(" ", "").isEmpty()) {
                    Platform.runLater(() -> search(false));
                }
            }
        }, 0, 10000);
        autoRefresh.selectedProperty().addListener((observable, oldValue, newValue) -> {
            canRefreshCheck = newValue;
        });
    }

    boolean canRefresh = false;
    boolean canRefreshCheck = false;

    @Override
    public void mapInitialized() {
        mapReady.setValue(true);
        //Set the initial properties of the map.
        MapOptions mapOptions = new MapOptions();

        mapOptions.center(new LatLong(41.90270080000001,12.496235200000001))
                .mapType(MapTypeIdEnum.ROADMAP)
                .overviewMapControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(true)
                .zoom(5);

        map = mapView.createMap(mapOptions);

        map.addMouseEventHandler(UIEventType.click, mouseEvent -> {
            if (lastLatLong != null && mouseEvent.getLatLong().distanceFrom(lastLatLong) <= 20) {
                if (marker != null) {
                    if (infoWindow != null) {
                        infoWindow.close();
                    }
                    infoWindow = new InfoWindow();
                    infoWindow.setContent(marker.getTitle());
                    infoWindow.open(map, marker);
                }
            } else {
                if (infoWindow != null) {
                    infoWindow.close();
                }
            }
        });
    }

    boolean doingSearch = false;

    InfoWindow infoWindow = null;

    Observer observer;
    private ObservableBoolean mapReady = new ObservableBoolean();
    private Marker marker;

    public void search(boolean showProgress) {
        if (!doingSearch) {
            if (!mapReady.getValue()) {
                observer = (o, arg) -> {
                    if (mapReady.getValue()) {
                        mapReady.deleteObserver(observer);
                        realSearch(showProgress);
                    }
                };
                mapReady.addObserver(observer);
            } else {
                realSearch(showProgress);
            }
        }
    }

    private void realSearch(boolean showProgress) {
        AlertDialog progress = null;
        if (showProgress) {
            progress = new AlertDialog.Builder(context)
                    .setTitle("Loading...")
                    .setProgress()
                    .show();
        }
        boolean valid = phone.validate() && pass.validate();
        String phoneString = this.phone.getText();
        String passString = this.pass.getText();
        System.err.println("valid: "+valid);
        if (valid) {
            doingSearch = true;
            JSONObject json = new JSONObject();
            try {
                json.put("phone", phoneString);
                json.put("pass", passString);
                json.put("myphone", Config.getSerialNumber());
                json.put("test", Config.TEST);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            AlertDialog finalProgress = progress;
            new Config.CallAPI(Config.url.GETUSERLOC.getValue(), json.toString(), out -> {
                try {
                    JSONObject response = new JSONObject(out);
                    String responseText = response.getString(Config.ResponseJson.TEXT.toString());
                    Boolean responseError = response.getBoolean(Config.ResponseJson.ERROR.toString());
                    if (showProgress) {
                        finalProgress.cancel();
                    }
                    if (responseError) {
                        new AlertDialog.Builder(context)
                                .setTitle(getBundle().getString("error"))
                                .setMessage(responseText)
                                .setPositiveButton("OK", JFXDialog::close).show();
                        canRefresh = false;
                        doingSearch = false;
                    } else {
                        canRefresh = true;
                        LatLong latLng = new LatLong(Double.parseDouble(response.optString("lat", "0.0")), Double.parseDouble(response.optString("long", "0.0")));
                        if (lastLatLong == null || lastLatLong.getLatitude() != latLng.getLatitude() || lastLatLong.getLongitude() != latLng.getLongitude()) {
                            lastLatLong = latLng;
                            if (marker == null) {
                                marker = new Marker(new MarkerOptions()
                                        .title("Phone number: " + phoneString)
                                        .position(latLng));
                                map.addMarker(marker);
                                map.setZoom(20);
                            } else {
                                marker.setTitle("Phone number: " + phoneString);
                                marker.setPosition(latLng);
                            }
                            map.setCenter(latLng);
                        }
                        doingSearch = false;
                    }
                } catch (Exception e) {
                    doingSearch = false;
                    if (showProgress) {
                        finalProgress.cancel();
                    }
                    System.err.println("JSON error: "+e.getMessage());
                    new AlertDialog.Builder(context)
                            .setTitle(getBundle().getString("error"))
                            .setPositiveButton("OK", JFXDialog::close).show();
                }
            });
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mapView.addMapInializedListener(this);
    }
}
