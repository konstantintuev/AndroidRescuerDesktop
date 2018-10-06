package tuev.konstantin.androidrescuer.mycomponenets;

import com.jfoenix.controls.JFXTextField;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.prefs.Preferences;

public class ValueSaveTextField extends JFXTextField {
    public ValueSaveTextField() {
        super();
        init();
    }

    public ValueSaveTextField(String text) {
        super(text);
        init();
    }

    private void init() {
        idProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String id) {
                if (id != null) {
                    Preferences prefs = Preferences.userNodeForPackage(ValueSaveTextField.class);
                    setText(prefs.get(id, ""));
                    textProperty().addListener((observable1, oldValue1, newValue1) -> prefs.put(id, newValue1));

                    idProperty().removeListener(this);
                }
            }
        });
    }
}
