package tuev.konstantin.androidrescuer.mycomponenets;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXProgressBar;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class AlertDialog {

    public void cancel(){}

    public interface OnClickListener {
        void onClick(JFXDialog dialog);
    }
    public static class Builder {
        private ViewFlowContext context;
        private JFXDialog mainDialog;
        private JFXDialogLayout mainDialogLayout;
        private boolean cancelable = true;

        public Builder(ViewFlowContext context) {
            this.context = context;
            mainDialogLayout = new JFXDialogLayout();
        }
        public Builder setTitle(String title) {
            mainDialogLayout.setHeading(new Label(title));
            return this;
        }
        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }
        public Builder setMessage(String message) {
            mainDialogLayout.setBody(new Label(message));
            return this;
        }
        public Builder setView(Node view) {
            mainDialogLayout.setBody(view);
            return this;
        }
        public Builder setProgress() {
            JFXProgressBar progressBar = new JFXProgressBar();
            progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            progressBar.getStyleClass().setAll("jfx-progress-bar");
            mainDialogLayout.setBody(progressBar);
            return this;
        }
        public Builder setPositiveButton(String text, OnClickListener listener) {
            setButton(text, listener);
            return this;
        }
        public Builder setButton(String text, OnClickListener listener) {
            JFXButton ok = new JFXButton();
            ok.setText(text);
            ok.getStyleClass().setAll("dialog-accept");
            mainDialogLayout.getActions().add(ok);
            ok.setOnMouseClicked(event -> listener.onClick(mainDialog));
            return this;
        }
        public AlertDialog show() {
            Runnable toRun = () -> {
                mainDialog = new JFXDialog((StackPane) context.getRegisteredObject("ContentPane"), mainDialogLayout, JFXDialog.DialogTransition.CENTER);
                mainDialog.overlayCloseProperty().setValue(cancelable);
                mainDialog.show();
            };
            if (Platform.isFxApplicationThread()) {
                toRun.run();
            } else {
                Platform.runLater(toRun);
            }
            return new AlertDialog(){
                @Override
                public void cancel() {
                    if (Platform.isFxApplicationThread()) {
                        mainDialog.close();
                    } else {
                        Platform.runLater(() -> mainDialog.close());
                    }
                }
            };
        }
    }
}
