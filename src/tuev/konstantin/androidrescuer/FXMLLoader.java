package tuev.konstantin.androidrescuer;

import java.net.URL;

public class FXMLLoader extends javafx.fxml.FXMLLoader {
    public FXMLLoader(URL location) {
        super(location, MainApplication.getBundle());
    }
}
