package tuev.konstantin.androidrescuer.mycomponenets;

import com.jfoenix.controls.*;
import io.datafx.controller.ViewController;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.json.JSONObject;
import tuev.konstantin.androidrescuer.MainApplication;
import tuev.konstantin.androidrescuer.gui.main.MainController;

import javax.annotation.PostConstruct;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;

import static tuev.konstantin.androidrescuer.MainApplication.getBundle;

@ViewController(value = "/fxml/phone.fxml", title = "Control Message")
public class PhoneController {

    @FXMLViewFlowContext
    private ViewFlowContext context;

    @FXML
    private ListView listOptions;

    @FXML
    private JFXButton send;

    @FXML
    private JFXTextField phone;

    @FXML
    private JFXPasswordField pass;

    @FXML
    private StackPane root;


    /**
     * init fxml when loaded.
     */
    @PostConstruct
    public void init() {
        listOptions.getStyleClass().setAll("jfx-list-view");

        MainController.toolbarTitle.setText(getBundle().getString("messageTitle"));

        send.setOnMouseClicked(event -> {
            boolean valid = phone.validate() && pass.validate();
            String phone = this.phone.getText();
            String pass = this.pass.getText();
            try {
            List<String> keys = Arrays.asList(MessageCombobox.getPrefs().keys());
            JSONObject json = new JSONObject();
            if (valid) {
                AlertDialog progress = new AlertDialog.Builder(context)
                        .setTitle("Loading...")
                        .setProgress()
                        .show();
                boolean realtimeLoc = false;
                if (keys.contains("realTimeLoc")) {
                    realtimeLoc =  MessageCombobox.getPrefs().getInt("realTimeLoc", 0) == 1;
                }
                json.put("phone", phone);
                json.put("pass", pass);
                JSONObject dataJ = new JSONObject();
                for (String key : keys) {
                    boolean equal = key.equals("wrongPassLocation");
                    if (!equal || (equal && !realtimeLoc)) {
                        int item = MessageCombobox.getPrefs().getInt(key, 0);
                        getTag(key, item, dataJ);
                    }
                }
                String dataStr = dataJ.toString();
                dataStr = dataStr.substring(dataStr.indexOf('{')+1, dataStr.lastIndexOf('}'));
                json.put("data", dataStr);
                json.put("test", Config.TEST);
                String out = json.toString();
                System.out.println("out: \n" + out);

                new Config.CallAPI(Config.url.SENDCONTROLSMS.getValue(), out, out1 -> {
                    JSONObject response = new JSONObject(out1);
                    String responseText = response.getString(Config.ResponseJson.TEXT.toString());
                    Boolean responseError = response.getBoolean(Config.ResponseJson.ERROR.toString());
                    progress.cancel();
                    if (responseError) {
                        new AlertDialog.Builder(context)
                                .setTitle(getBundle().getString("error"))
                                .setMessage(responseText)
                                .setPositiveButton("OK", JFXDialog::close).show();
                    } else {
                        new AlertDialog.Builder(context)
                                .setTitle(MainApplication.getBundle().getString("success_message"))
                                .setPositiveButton("OK", JFXDialog::close).show();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        });
    }

    private String getTag(String tag, int item, String target) {
        if (item == 1) {
            target += tag + ": 1\n";
        }
        if (item == 2) {
            target += tag + ": 0\n";
        }
        return target;
    }
    private void getTag(String tag, int item, JSONObject target) {
        if (item == 1) {
            target.put(tag, "1");
        }
        if (item == 2) {
            target.put(tag, "0");
        }
    }
}
