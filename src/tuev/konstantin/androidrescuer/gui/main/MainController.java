package tuev.konstantin.androidrescuer.gui.main;

import com.jfoenix.controls.*;
import com.jfoenix.controls.JFXPopup.PopupHPosition;
import com.jfoenix.controls.JFXPopup.PopupVPosition;
import io.datafx.controller.ViewController;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.FlowHandler;
import io.datafx.controller.flow.context.FXMLViewFlowContext;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import tuev.konstantin.androidrescuer.FXMLLoader;
import tuev.konstantin.androidrescuer.MainApplication;
import tuev.konstantin.androidrescuer.datafx.ExtendedAnimatedFlowContainer;
import tuev.konstantin.androidrescuer.gui.sidemenu.SideMenuController;
import tuev.konstantin.androidrescuer.mycomponenets.AlertDialog;
import tuev.konstantin.androidrescuer.mycomponenets.Config;
import tuev.konstantin.androidrescuer.mycomponenets.LocaleCombobox;
import tuev.konstantin.androidrescuer.mycomponenets.PhoneController;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.datafx.controller.flow.container.ContainerAnimations.FADE;

@ViewController(value = "/fxml/Main.fxml", title = "Android Rescuer Desktop")
public final class MainController {

    @FXMLViewFlowContext
    private ViewFlowContext context;

    @FXML
    private StackPane root;

    @FXML
    private StackPane titleBurgerContainer;
    @FXML
    private JFXHamburger titleBurger;

    @FXML
    private StackPane optionsBurger;
    @FXML
    private JFXRippler optionsRippler;
    @FXML
    private JFXDrawer drawer;
    @FXML
    private LocaleCombobox localeBox;
    @FXML
    private Label toolbarText;

    private JFXPopup toolbarPopup;

    public static Label toolbarTitle;
    /**
     * init fxml when loaded.
     */
    @PostConstruct
    public void init() throws Exception {
        // init the title hamburger icon
        drawer.setOnDrawerOpening(e -> {
            final Transition animation = titleBurger.getAnimation();
            animation.setRate(1);
            animation.play();
        });
        drawer.setOnDrawerClosing(e -> {
            final Transition animation = titleBurger.getAnimation();
            animation.setRate(-1);
            animation.play();
        });
        titleBurgerContainer.setOnMouseClicked(e -> {
            if (drawer.isHidden() || drawer.isHidding()) {
                drawer.open();
            } else {
                drawer.close();
            }
        });
        toolbarTitle = toolbarText;

        localeBox.setListener(locale -> {
            Preferences prefs = MainApplication.getPrefs();
            prefs.put("locale", locale == Locale.GERMAN ? "de" : "en");
            try {
                prefs.flush();
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
            MainApplication.primaryStage.close();
            MainApplication.reset = true;
            Platform.runLater(() -> {
                try {
                    new MainApplication().start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ui/popup/MainPopup.fxml"));
        loader.setController(new InputController());
        toolbarPopup = new JFXPopup(loader.load());

        optionsBurger.setOnMouseClicked(e -> toolbarPopup.show(optionsBurger,
                                                               PopupVPosition.TOP,
                                                               PopupHPosition.RIGHT,
                                                               -12,
                                                               15));

        // create the inner flow and content
        context = new ViewFlowContext();
        // set the default controller
        Flow innerFlow = new Flow(PhoneController.class);

        final FlowHandler flowHandler = innerFlow.createHandler(context);
        flowHandler.getViewConfiguration().setResources(MainApplication.getBundle());
        context.register("ContentFlowHandler", flowHandler);
        context.register("ContentFlow", innerFlow);
        final Duration containerAnimationDuration = Duration.millis(320);
        drawer.setContent(flowHandler.start(new ExtendedAnimatedFlowContainer(containerAnimationDuration, FADE)));
        context.register("ContentPane", drawer.getContent().get(0));

        // side controller will add links to the content flow
        Flow sideMenuFlow = new Flow(SideMenuController.class);
        final FlowHandler sideMenuFlowHandler = sideMenuFlow.createHandler(context);
        sideMenuFlowHandler.getViewConfiguration().setResources(MainApplication.getBundle());
        drawer.setSidePane(sideMenuFlowHandler.start(new ExtendedAnimatedFlowContainer(containerAnimationDuration,
                                                                                       FADE)));
    }

    private final class InputController {
        /**
         * Unzip it
         * @param zipFile input zip file
         * @param outputFolder zip file output folder
         */
        public void unZipIt(String zipFile, String outputFolder){

            byte[] buffer = new byte[1024];

            try{

                //create output directory is not exists
                File folder = new File(outputFolder);
                if(!folder.exists()){
                    folder.mkdir();
                }

                //get the zip file content
                ZipInputStream zis =
                        new ZipInputStream(new FileInputStream(zipFile));
                //get the zipped file list entry
                ZipEntry ze = zis.getNextEntry();

                while(ze!=null){

                    String fileName = ze.getName();
                    File newFile = new File(outputFolder + File.separator + fileName);

                    System.out.println("file unzip : "+ newFile.getAbsoluteFile());

                    //create all non exists folders
                    //else you will hit FileNotFoundException for compressed folder
                    new File(newFile.getParent()).mkdirs();

                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }

                    fos.close();
                    ze = zis.getNextEntry();
                }

                zis.closeEntry();
                zis.close();

                System.out.println("Done");

            }catch(IOException ex){
                ex.printStackTrace();
            }
        }

        @FXML
        private JFXListView<?> toolbarPopupList;

        // close application
        @FXML
        private void submit() {
            switch (toolbarPopupList.getSelectionModel().getSelectedIndex()) {
                case 0:
                    new AlertDialog.Builder(context)
                            .setTitle(MainApplication.getBundle().getString("info").toUpperCase(Locale.ENGLISH))
                            .setMessage(MainApplication.getBundle().getString("serial_number_desc")+ Config.getSerialNumber())
                            .setPositiveButton("OK", JFXDialog::close).show();
                    break;
                case 1:
                    String OS = (System.getProperty("os.name")).toUpperCase();
                    String adbPath = System.getProperty("user.dir") + File.separator + "adb" + File.separator + "adb" + (OS.contains("WIN") ? "-win.exe" : (OS.startsWith ("MAC") ? "-mac" : "-lin"));

                    System.out.println(adbPath);
                    Runnable toRunAfterFileCheck = () -> {
                        new AlertDialog.Builder(context)
                                .setProgress()
                                .setTitle("What to do?")
                                .setMessage("You can enable remote GPS activation PERMANENTLY and mobile data activation UNTIL THE DEVICE RESTARTS(this will activate adb over LAN).")
                                .setButton("Both", dialog1 -> {
                                    adbGPSandMobileActivate(adbPath, true, true);
                                    dialog1.close();
                                })
                                .setButton("GPS", dialog12 -> {
                                    adbGPSandMobileActivate(adbPath, true, false);
                                    dialog12.close();
                                })
                                .setButton("Mobile Data", dialog13 -> {
                                    adbGPSandMobileActivate(adbPath, false, true);
                                    dialog13.close();
                                }).show();
                    };
                    if (!new File(adbPath).exists()) {
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                .setProgress()
                                .setCancelable(false)
                                .setTitle("Downloading files...").show();
                        new Thread(() -> {
                            try {
                                URL website = new URL(Config.root+"/adb.zip");
                                try (InputStream in = website.openStream()) {
                                    Files.copy(in, new File(System.getProperty("user.dir") + File.separator + "adb.zip").toPath(), StandardCopyOption.REPLACE_EXISTING);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            unZipIt(System.getProperty("user.dir") + File.separator + "adb.zip", System.getProperty("user.dir") + File.separator + "adb");
                            Platform.runLater(() -> {
                                dialog.cancel();
                                toRunAfterFileCheck.run();
                            });
                        }).start();
                    } else {
                        toRunAfterFileCheck.run();
                    }
                    break;
                case 2:
                    Platform.exit();
                    break;
            }
            toolbarPopup.hide();
        }

        private void adbGPSandMobileActivate(String adbPath, boolean gps, boolean mobileData) {
            AlertDialog dialogLoading = new AlertDialog.Builder(context)
                    .setProgress()
                    .setCancelable(false)
                    .setTitle("Running commands...").show();
            StringBuilder cmdReturn = new StringBuilder();
            new Thread(() -> {
                try {
                    String OS = (System.getProperty("os.name")).toUpperCase();
                    if (!OS.contains("WIN")) {
                        StringBuilder cmdReturnChmod = new StringBuilder();
                        Process process3 = Runtime.getRuntime().exec(new String[]{"/bin/chmod", "u+x", adbPath});
                        try (InputStream inputStream = process3.getInputStream()) {
                            int c;
                            while ((c = inputStream.read()) != -1) {
                                cmdReturnChmod.append((char) c);
                            }
                        }
                        cmdReturnChmod.append("\n");
                        try (InputStream inputStream = process3.getErrorStream()) {
                            int c;
                            while ((c = inputStream.read()) != -1) {
                                cmdReturnChmod.append((char) c);
                            }
                        }
                        cmdReturn.append("\n");
                        cmdReturn.append(cmdReturnChmod);
                    }
                    if (gps) {
                        String[] command = {adbPath, "shell", "pm", "grant", "tuev.konstantin.androidrescuer", "android.permission.WRITE_SECURE_SETTINGS"};
                        Process process = Runtime.getRuntime().exec(command);
                        try (InputStream inputStream = process.getInputStream()) {
                            int c;
                            while ((c = inputStream.read()) != -1) {
                                cmdReturn.append((char) c);
                            }
                        }
                        cmdReturn.append("\n");
                        try (InputStream inputStream = process.getErrorStream()) {
                            int c;
                            while ((c = inputStream.read()) != -1) {
                                cmdReturn.append((char) c);
                            }
                        }
                    }
                    StringBuilder cmdReturnData = new StringBuilder();
                    if (mobileData) {
                        String[] command1 = {adbPath, "tcpip", "5555"};

                        Process process1 = Runtime.getRuntime().exec(command1);
                        try (InputStream inputStream = process1.getInputStream()) {
                            int c;
                            while ((c = inputStream.read()) != -1) {
                                cmdReturnData.append((char) c);
                            }
                        }
                        cmdReturnData.append("\n");
                        try (InputStream inputStream = process1.getErrorStream()) {
                            int c;
                            while ((c = inputStream.read()) != -1) {
                                cmdReturnData.append((char) c);
                            }
                        }
                        if (!cmdReturn.toString().trim().equals(cmdReturnData.toString().trim())) {
                            cmdReturn.append("\n");
                            cmdReturn.append(cmdReturnData);
                        }

                    }
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String[] command2 = {adbPath, "shell", "am", "broadcast", "-a", "tuev.konstantin.androidrescuer.ADBStatusChange", "--ez", "writeSecure", String.valueOf(gps), "--ez", "adbLan", String.valueOf(mobileData)};

                    StringBuilder cmdReturnBroadcast = new StringBuilder();
                    Process process2 = Runtime.getRuntime().exec(command2);
                    try (InputStream inputStream = process2.getInputStream()) {
                        int c;
                        while ((c = inputStream.read()) != -1) {
                            cmdReturnBroadcast.append((char) c);
                        }
                    }
                    cmdReturnBroadcast.append("\n");
                    try (InputStream inputStream = process2.getErrorStream()) {
                        int c;
                        while ((c = inputStream.read()) != -1) {
                            cmdReturnBroadcast.append((char) c);
                        }
                    }
                    if (!cmdReturnData.toString().trim().equals(cmdReturnBroadcast.toString().trim())) {
                        cmdReturn.append("\n");
                        cmdReturn.append(cmdReturnBroadcast);
                    }

                } catch (IOException ex) {
                    Logger.getLogger(MainController.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
                Platform.runLater(() -> {
                    dialogLoading.cancel();
                    String out = cmdReturn.toString().trim();
                    System.out.println(out);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    if (out.contains("error")) {
                        dialog.setTitle("Error occurred!");
                        String message = "";
                        if (out.contains("device unauthorized")) {
                            message = "Authorize your computer with the dialog show on your device and Try Again.";
                        } else {
                            message = out.replace("error:", "");
                            message = message.substring(0, 1).toUpperCase()+message.substring(1);
                        }
                        dialog.setMessage(message);
                    } else {
                        dialog.setTitle("Success!\nApp has admin rights!");
                    }
                    dialog.show();
                });
            }).start();

        }
    }
}
