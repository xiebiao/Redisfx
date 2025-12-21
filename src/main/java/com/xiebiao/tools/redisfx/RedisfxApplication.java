package com.xiebiao.tools.redisfx;

import com.xiebiao.tools.redisfx.controller.MainController;
import com.xiebiao.tools.redisfx.utils.Constants;
import com.xiebiao.tools.redisfx.utils.Icons;
import com.xiebiao.tools.redisfx.utils.RedisfxStyles;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RedisfxApplication extends Application {

  private MainController mainController;
  public static final ThreadPoolExecutor mainThreadPool = new ThreadPoolExecutor(
      5, 10, 0L,
      TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue<Runnable>()
  );

  @Override
  public void start(Stage stage) throws IOException {

    FXMLLoader mainView = new FXMLLoader(
        RedisfxApplication.class.getResource("views/main-view.fxml"));
    Parent root = mainView.load();
    mainController = mainView.getController();
    Scene scene = new Scene(root);
    scene.getStylesheets().add(Objects.requireNonNull(RedisfxStyles.styles).toExternalForm());
    stage.setTitle(Constants.appName);
    stage.getIcons().add(Icons.logoImage);
    if (Taskbar.isTaskbarSupported()) {
      Taskbar taskbar = Taskbar.getTaskbar();
      if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
        final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        var dockIcon = defaultToolkit.getImage(getClass().getResource(Constants.logoUri));
        taskbar.setIconImage(dockIcon);
        PopupMenu popupMenu = new PopupMenu();
        popupMenu.add(new MenuItem("Quit"));
        taskbar.setMenu(popupMenu);

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
    //TODO i18n
    Locale.setDefault(new Locale("en", "en_US"));
    launch();
  }
}