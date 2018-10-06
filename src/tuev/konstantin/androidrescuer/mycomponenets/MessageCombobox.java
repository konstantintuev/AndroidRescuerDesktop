package tuev.konstantin.androidrescuer.mycomponenets;

import com.jfoenix.controls.JFXComboBox;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.prefs.Preferences;

import static tuev.konstantin.androidrescuer.MainApplication.getBundle;

public class MessageCombobox extends JFXComboBox<Label> {

    private static Preferences prefs;

    public boolean isTwoOptions() {
        return twoOptions != null && twoOptions.get();
    }

    public void setTwoOptions(boolean twoOptions) {
        this.twoOptions.set(twoOptions);
    }

    private BooleanProperty twoOptions = new BooleanPropertyBase() {
        @Override
        public Object getBean() {
            return MessageCombobox.this;
        }

        @Override
        public String getName() {
            return "twoOptions";
        }
    };

    public String getThirdField() {
        return thirdField.get();
    }

    public StringProperty thirdFieldProperty() {
        return thirdField;
    }

    public void setThirdField(String thirdField) {
        this.thirdField.set(thirdField);
    }

    private StringProperty thirdField = new StringPropertyBase() {
        @Override
        public Object getBean() {
            return MessageCombobox.this;
        }

        @Override
        public String getName() {
            return "thirdField";
        }
    };

    public MessageCombobox(ObservableList<Label> items) {
        super();
        initIT();
    }

    public MessageCombobox() {
        super();
        initIT();
    }

    private void initIT() {
        new Thread(() -> {
            while (getParent() == null && getId() == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Platform.runLater(() -> {
                setItems(it());
                setMinWidth(130);
                setMaxWidth(130);
                StackPane.setAlignment(MessageCombobox.this, Pos.CENTER_RIGHT);
                StackPane.setAlignment(((StackPane)getParent()).getChildren().get(0), Pos.CENTER_LEFT);
                getSelectionModel().select(getPrefs().getInt(getId(), 0));
            });
        }).start();
        getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            getPrefs().putInt(getId(), newValue.intValue());
        });
    }

    private ObservableList<Label> it() {
        ObservableList<Label> it = FXCollections.observableArrayList();
        if (!isTwoOptions()) {
            it.add(new Label(getBundle().getString("default")));
            it.add(new Label(getBundle().getString("enable")));
            it.add(new Label(getBundle().getString("disable")));
        } else {
            it.add(new Label(getBundle().getString("no")));
            it.add(new Label(getBundle().getString("yes")));
            if (thirdField != null && thirdField.getValue() != null) {
                it.add(new Label(getBundle().getString(thirdField.getValue())));
            }
        }
        return it;
    }


    public static Preferences getPrefs() {
        if (prefs == null) {
            prefs = Preferences.userNodeForPackage(MessageCombobox.class);
        }
        return prefs;
    }
}
