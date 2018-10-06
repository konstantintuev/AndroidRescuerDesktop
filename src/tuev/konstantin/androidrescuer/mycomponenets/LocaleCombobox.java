package tuev.konstantin.androidrescuer.mycomponenets;

import com.jfoenix.controls.JFXComboBox;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import tuev.konstantin.androidrescuer.MainApplication;

import java.util.Locale;
import java.util.ResourceBundle;

import static tuev.konstantin.androidrescuer.MainApplication.getBundle;

public class LocaleCombobox extends JFXComboBox<Label> {

    public interface localeChangeListener {
        void onChange(Locale locale);
    }

    public localeChangeListener getListener() {
        return listener;
    }

    public void setListener(localeChangeListener listener) {
        this.listener = listener;
    }

    private localeChangeListener listener;

    public LocaleCombobox() {
        super();
        prefWidthProperty().setValue(200);
        ResourceBundle res = getBundle();
        Label english = new Label(res.getString("english"));
        Label german = new Label(res.getString("german"));

        StackPane stackPane = new StackPane();
        stackPane.getStyleClass().setAll("custom-jfx-list-view-icon-container");
        FontAwesomeIconView iconView = new FontAwesomeIconView();
        iconView.glyphNameProperty().setValue("FLAG");
        iconView.setSize("1.5em");
        iconView.setStyleClass("custom-jfx-list-view-icon");
        stackPane.getChildren().setAll(iconView);

        StackPane stackPane1 = new StackPane();
        stackPane1.getStyleClass().setAll("custom-jfx-list-view-icon-container");
        FontAwesomeIconView iconView1 = new FontAwesomeIconView();
        iconView1.glyphNameProperty().setValue("FLAG");
        iconView1.setSize("1.5em");
        iconView1.setStyleClass("custom-jfx-list-view-icon");
        stackPane1.getChildren().setAll(iconView1);

        english.setGraphic(stackPane);
        german.setGraphic(stackPane1);
        ObservableList<Label> options = FXCollections.observableArrayList(english, german);
        setItems(options);
        setOnAction(event -> {
            if (listener != null) {
                Locale locale = Locale.ENGLISH;
                if (getSelectionModel().getSelectedIndex() == 1) {
                    locale = Locale.GERMAN;
                }
                listener.onChange(locale);
            }
        });
        int index = 0;
        if (MainApplication.getLocale() == Locale.GERMAN) {
            index = 1;
        }
        getSelectionModel().select(index);
    }
}
