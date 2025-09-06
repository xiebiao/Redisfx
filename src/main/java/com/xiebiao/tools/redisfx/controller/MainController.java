package com.xiebiao.tools.redisfx.controller;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import com.google.common.eventbus.Subscribe;
import com.xiebiao.tools.redisfx.RedisfxApplication;
import com.xiebiao.tools.redisfx.model.RedisConnectionInfo;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventBusService;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventMessasge;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventType;
import com.xiebiao.tools.redisfx.utils.Constants;
import com.xiebiao.tools.redisfx.utils.Icons;
import com.xiebiao.tools.redisfx.view.ConnectionTitledPane;
import com.xiebiao.tools.redisfx.view.MemoryMonitorView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.xiebiao.tools.redisfx.utils.Icons.checkIcon;

public class MainController implements Initializable {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(MainController.class);
    @FXML
    private Button newConnectionButton;
    @FXML
    private SplitPane splitPane;
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private VBox connectionContainer;

    private TabPane tabPane;

    private static Map<String, ConnectionTitledPane> connectionTitledPaneMap = new HashMap<>();
    private Theme primerDarkTheme = new PrimerDark();
    private Theme primerLightTheme = new PrimerLight();
    @FXML
    private Menu themeMenu;
    @FXML
    private MenuItem primerDarkThemeMenuItem;
    @FXML
    private MenuItem primerLightThemeMenuItem;
    @FXML
    private MenuItem primitiveThemeMenuItem;

    private EventHandler<ActionEvent> themeChangeEventHandler = event -> {
        themeMenu.getItems().forEach(menuItem -> menuItem.setGraphic(null));
        if (event.getSource() == primerDarkThemeMenuItem) {
            primerDarkThemeMenuItem.setGraphic(checkIcon);
            Application.setUserAgentStylesheet(this.primerDarkTheme.getUserAgentStylesheet());
        } else if (event.getSource() == primerLightThemeMenuItem) {
            primerLightThemeMenuItem.setGraphic(checkIcon);
            Application.setUserAgentStylesheet(this.primerLightTheme.getUserAgentStylesheet());
        }
        else if (event.getSource() == primitiveThemeMenuItem) {
            primitiveThemeMenuItem.setGraphic(checkIcon);
            Application.setUserAgentStylesheet(null);
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTheme();
        initAboutMenuItem();
        initTabPaneView();
        Platform.runLater(() -> splitPane.setDividerPosition(0, 0.195));
        newConnectionButton.setOnAction(actionEvent -> {
            Dialog<DialogPane> dialog = new Dialog<>();
            FXMLLoader fxmlLoader = new FXMLLoader(
                    RedisfxApplication.class.getResource("views/connectioninfo-view.fxml"));
            try {
                DialogPane dialogPane = fxmlLoader.load();
                dialog.setDialogPane(dialogPane);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            dialog.showAndWait();
        });
        RedisfxEventBusService.register(this);
    }

    private void initTabPaneView() {
        this.tabPane = new TabPane();
        this.tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        this.tabPane.setId(Constants.tabPaneId);
        // 动态计算标签宽度 (考虑预留滚动条/按钮空间)
//        this.tabPane.styleProperty().bind(Bindings.createStringBinding(() -> {
//            int tabCount = this.tabPane.getTabs().size();
//            if (tabCount == 0 || this.tabPane.getWidth() <= 0) return "";
//
//            // 计算可用宽度 (减去滚动按钮等控件占用的空间)
//            double reservedSpace = 50; // 预留空间
//            double availableWidth = Math.max(0, this.tabPane.getWidth() - reservedSpace);
//            double tabWidth = availableWidth / tabCount;
//            // 设置最小/最大宽度相同以实现固定宽度效果
//            return String.format(
//                    "-fx-tab-min-width: %.2fpx; -fx-tab-max-width: %.2fpx;",
//                    tabWidth, tabWidth
//            );
//        }, this.tabPane.widthProperty(), this.tabPane.getTabs()));
        tabPane.setMinWidth(100);
        this.splitPane.getItems().add(tabPane);
    }

    private void initTheme() {
        themeMenu.getItems().forEach(menuItem -> menuItem.setOnAction(themeChangeEventHandler));
        Application.setUserAgentStylesheet(this.primerLightTheme.getUserAgentStylesheet());
        FontIcon themeSelectedIcon = checkIcon;
        primerLightThemeMenuItem.setGraphic(themeSelectedIcon);
    }

    private void initAboutMenuItem() {
        aboutMenuItem.setOnAction(actionEvent -> {
            Stage owner = (Stage) splitPane.getScene().getWindow();
            Stage aboutStage = new Stage();
            aboutStage.initModality(Modality.APPLICATION_MODAL);
            aboutStage.initOwner(owner);
            aboutStage.setTitle("About");

            ImageView logo = new ImageView(Icons.logoImage);
            logo.setFitWidth(84);
            logo.setFitHeight(84);

            Text title = new Text(Constants.appName);
            title.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");

            Text version = new Text(Constants.version);
            Text copyright = new Text(Constants.copyright);
            Hyperlink websiteLink = new Hyperlink(Constants.websiteLink);
            websiteLink.setOnAction(e -> {
                try {
                    Desktop.getDesktop().browse(new URI(Constants.websiteLink));
                } catch (Exception ex) {
                    logger.error("Open website error: {}", ex.getMessage());
                }
            });
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> aboutStage.close());

            // 布局
            VBox layout = new VBox(10);
            layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
            layout.getChildren().addAll(
                    logo,
                    title,
                    version,
                    copyright,
                    websiteLink,
                    closeButton
            );
            Scene dialogScene = new Scene(layout, 300, 300);
            aboutStage.setScene(dialogScene);
            aboutStage.setResizable(false);
            aboutStage.show();
        });
    }

    @Subscribe
    public void handleEventMessage(RedisfxEventMessasge eventMessasge) {
        if (eventMessasge.getEventType() == RedisfxEventType.NEW_CONNECTION_CREATED) {
            RedisConnectionInfo redisConnection = (RedisConnectionInfo) eventMessasge.getData();
            createConnectionTitledPane(redisConnection);
        }
    }

    private void createConnectionTitledPane(RedisConnectionInfo redisConnectionInfo) {
        if (connectionTitledPaneMap.get(redisConnectionInfo.connectionName()) != null) {
            //已经存在一个相同的连接
            return;
        }
        ConnectionTitledPane connectionTitledPane = new ConnectionTitledPane(redisConnectionInfo);
        TitledPane connectionPane = connectionTitledPane.create(this.tabPane);
        connectionTitledPaneMap.put(redisConnectionInfo.connectionName(), connectionTitledPane);
        connectionContainer.getChildren().addAll(connectionPane);
    }

    public void openVmMonitor() {
        MemoryMonitorView memoryMonitorView = new MemoryMonitorView();
        Stage primaryStage = (Stage) splitPane.getScene().getWindow();
        memoryMonitorView.create(primaryStage);
    }

    public void destroy() {
        RedisfxEventBusService.unregister(this);
        connectionTitledPaneMap.values().forEach(ConnectionTitledPane::destroy);
        logger.debug("Destroy MainController");

    }
}