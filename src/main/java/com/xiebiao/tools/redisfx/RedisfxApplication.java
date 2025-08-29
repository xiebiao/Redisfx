package com.xiebiao.tools.redisfx;

import atlantafx.base.theme.PrimerLight;
import com.xiebiao.tools.redisfx.controller.MainController;
import com.xiebiao.tools.redisfx.utils.Constants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class RedisfxApplication extends Application {
    private MainController mainController;

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader mainView = new FXMLLoader(RedisfxApplication.class.getResource("views/main-view.fxml"));
        Parent root = mainView.load();
        mainController = mainView.getController();
        Scene scene = new Scene(root);
        URL css = RedisfxApplication.class.getResource("styles/custom.css");
        scene.getStylesheets().add(Objects.requireNonNull(css).toExternalForm());
        stage.setTitle(Constants.appName);
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(Constants.logoUri)));
        stage.getIcons().add(image);
        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
                var dockIcon = defaultToolkit.getImage(getClass().getResource(Constants.logoUri));
                taskbar.setIconImage(dockIcon);
            }
        }
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        mainController.destroy();

    }

    public static void main(String[] args) {
        launch();
    }
}