package com.xiebiao.tools.redisfx.view;

import atlantafx.base.theme.Styles;
import com.google.common.eventbus.Subscribe;
import com.xiebiao.tools.redisfx.RedisfxApplication;
import com.xiebiao.tools.redisfx.model.KeyPage;
import com.xiebiao.tools.redisfx.model.RedisConnectionInfo;
import com.xiebiao.tools.redisfx.model.RedisInfo;
import com.xiebiao.tools.redisfx.core.LifeCycle;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventBusService;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventMessasge;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventType;
import com.xiebiao.tools.redisfx.utils.Constants;
import com.xiebiao.tools.redisfx.utils.Utils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.Getter;
import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.ScanResult;

import java.io.IOException;
import java.util.*;

/**
 * @author Bill Xie
 * @since 2025/8/13 23:49
 **/
@Getter
public class ConnectionTitledPane implements LifeCycle {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ConnectionTitledPane.class);
    private JedisPool jedisPool;
    private Jedis jedis;
    private RedisConnectionInfo redisConnectionInfo;
    private TitledPane connectionTitledPane;
    private TabPane tabPane;
    private MenuButton menuButton;
    private ChoiceBox<RedisInfo.Keyspace> databaseChoiceBox;
    private ButtonBar buttonBar;
    private ButtonBar loadButtonBar;
    private Button newKeyButton;
    private ListView<String> keysListView;
    private boolean isCompleteIteration = false;
    private Map<Integer, KeyPage> keyPageMap = new java.util.HashMap<>();

    private Tab serverInfoTab;
    private Tab keyInfoTab;
    private ServerInfoTabView serverInfoTabView;
    private KeyTabView keyTabView;
    private MenuItem closeConnection;
    private MenuItem editConnection;

    public ConnectionTitledPane(RedisConnectionInfo redisConnectionInfo) {
        this.redisConnectionInfo = redisConnectionInfo;
        initJedisPool();
        registerEventBus();
    }

    private void initJedisPool() {
        jedisPool = new JedisPool(
                redisConnectionInfo.host(),
                redisConnectionInfo.port(),
                redisConnectionInfo.username(),
                redisConnectionInfo.password());
    }

    private void registerEventBus() {
        RedisfxEventBusService.register(this);
    }

    public TitledPane create(TabPane tabPane) {
        this.tabPane = tabPane;
        connectionTitledPane = new TitledPane();
        connectionTitledPane.getStyleClass().add(Styles.DENSE);
        connectionTitledPane.setText(redisConnectionInfo.connectionName());

        connectionTitledPane.setCollapsible(true);
        //默认不展开
        connectionTitledPane.setExpanded(false);
        connectionTitledPane.setId(redisConnectionInfo.connectionName());
        connectionTitledPane.setGraphic(createConnectionMenuButton());
        connectionTitledPane.setGraphicTextGap(40);
        connectionTitledPane.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        connectionTitledPane.setContentDisplay(ContentDisplay.RIGHT);

        connectionTitledPane.setContent(createTitledPaneContentVBox());
        connectionTitledPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (Utils.isFastClick(connectionTitledPane)) {
                logger.debug("{} fast click, id:{}",
                        connectionTitledPane.getClass().getSimpleName(),
                        connectionTitledPane.getId());
                return;
            }
            if (newValue) {
                if (jedisPool == null || jedisPool.isClosed()) {
                    initJedisPool();
                }
                serverInfoTabView = new ServerInfoTabView(redisConnectionInfo.connectionName(), tabPane);
                if (serverInfoTab == null) {
                    serverInfoTab = serverInfoTabView.create();
                }
                RedisfxEventBusService.post(new RedisfxEventMessasge(RedisfxEventType.CONNECTION_CONNECTED, jedisPool.getResource()));
                tabPane.getTabs().add(serverInfoTab);
                tabPane.getSelectionModel().select(serverInfoTab);
                enableAllButton();
            }
        });
        keyTabView = new KeyTabView(redisConnectionInfo.connectionName(), tabPane);
        return connectionTitledPane;
    }

    private void enableAllButton() {
        menuButton.setDisable(false);
        buttonBar.setDisable(false);
        loadButtonBar.setDisable(false);
        loadDatabases(databaseChoiceBox);
    }

    private void disableAllButton() {
        menuButton.setDisable(true);
        buttonBar.setDisable(true);
        loadButtonBar.setDisable(true);
        //删除所有tab页
        tabPane.getTabs().removeAll();
        databaseChoiceBox.getItems().clear();
        keysListView.getItems().clear();
        keyPageMap.clear();
        if (jedisPool != null) {
            jedisPool.destroy();
            jedisPool = null;
        }

    }

    private MenuButton createConnectionMenuButton() {
        menuButton = new MenuButton();
        menuButton.setText("Action");
        editConnection = new MenuItem("Edit Connection");
        editConnection.setOnAction(event -> {
            //Close connection before edit
            closeConnection();
            Dialog<DialogPane> dialog = new Dialog<>();
            FXMLLoader fxmlLoader = new FXMLLoader(
                    RedisfxApplication.class.getResource("views/connectioninfo-view.fxml"));
            try {
                DialogPane dialogPane = fxmlLoader.load();
                dialog.setDialogPane(dialogPane);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            RedisfxEventBusService.post(new RedisfxEventMessasge(RedisfxEventType.EDIT_CONNECTION_DIALOG_OPEN, redisConnectionInfo));
            dialog.showAndWait();
        });
        closeConnection = new MenuItem("Close Connection");
        closeConnection.setOnAction(event -> {
            closeConnection();
        });
        menuButton.getItems().addAll(editConnection, closeConnection);
        return menuButton;
    }

    private void closeConnection() {
        destroy();
        int tabSize = tabPane.getTabs().size();
        tabPane.getTabs().remove(0, tabSize);
        disableAllButton();
        connectionTitledPane.setExpanded(false);
    }

    private VBox createTitledPaneContentVBox() {
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        buttonBar = new ButtonBar();
        databaseChoiceBox = new ChoiceBox<>();
        databaseChoiceBox.valueProperty().addListener(new ChangeListener<RedisInfo.Keyspace>() {
            @Override
            public void changed(ObservableValue<? extends RedisInfo.Keyspace> observable, RedisInfo.Keyspace oldValue, RedisInfo.Keyspace newValue) {
                if (connectionTitledPane.isExpanded() && newValue != null) {
                    keyPageMap.remove(newValue.getIndex());
                    loadKeys(false);
                }
            }
        });

        newKeyButton = new Button(Constants.NEW_KEY_TITLE);
        newKeyButton.setOnAction(actionEvent -> {
            boolean isKeyInfoTabCreated = tabPane.getTabs().stream().anyMatch(tab -> tab.getId().equals(Constants.keyInfoTabId));
            if (!isKeyInfoTabCreated) {
                keyInfoTab = keyTabView.create(Constants.NEW_KEY_TITLE);
                if (jedis == null) {
                    jedis = this.jedisPool.getResource();
                }
                jedis.select(databaseChoiceBox.getSelectionModel().getSelectedItem().getIndex());
                keyTabView.setJedis(jedis);
            }
            RedisfxEventBusService.post(new RedisfxEventMessasge(RedisfxEventType.NEW_KEY));
        });
        buttonBar.getButtons().addAll(databaseChoiceBox, newKeyButton);
        //keys
        createKeysListView();
        loadButtonBar = new ButtonBar();
        Button loadMoreButton = new Button("Load More");
        loadMoreButton.setOnAction(event -> {
            loadKeys(true);
        });
        loadButtonBar.getButtons().addAll(loadMoreButton);
        vBox.getChildren().addAll(buttonBar, keysListView, loadButtonBar);
        return vBox;
    }

    private void createKeysListView() {
        keysListView = new ListView<>();
        loadDatabases(databaseChoiceBox);
        if (connectionTitledPane.isExpanded()) {
            loadKeys(false);
        }
        keysListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    boolean isKeyInfoTabCreated = tabPane.getTabs().stream().anyMatch(tab -> tab.getId().equals(Constants.keyInfoTabId));
                    if (!isKeyInfoTabCreated) {
                        keyInfoTab = keyTabView.create(Constants.NEW_KEY_TITLE);
                        if (jedis == null) {
                            jedis = jedisPool.getResource();
                        }
                        jedis.select(databaseChoiceBox.getSelectionModel().getSelectedItem().getIndex());
                        keyTabView.setJedis(jedis);
                    }
                    RedisfxEventBusService.post(new RedisfxEventMessasge(RedisfxEventType.KEY_SELECTED, newValue));
                }
            }
        });

    }

    private void loadKeys(boolean loadMore) {
        if (jedis == null) {
            jedis = jedisPool.getResource();
        }
        int index = databaseChoiceBox.getSelectionModel().getSelectedItem().getIndex();
        jedis.select(index);
        KeyPage keyPage = keyPageMap.getOrDefault(index, new KeyPage());
        if (keyPage.isCompleteIteration()) {
            return;
        }
        ScanResult<String> scanResult = jedis.scan(keyPage.getCursor(), keyPage.getScanParams());
        List<String> keys = scanResult.getResult();
        keyPage.setCompleteIteration(scanResult.isCompleteIteration());
        keyPage.setCursor(scanResult.getCursor());
        keyPageMap.put(index, keyPage);
        if (!loadMore) {
            this.keysListView.getItems().clear();
        }
        Collections.sort(keys);
        this.keysListView.getItems().addAll(uniqKeys(keys));
    }

    private ObservableList<String> uniqKeys(List<String> keys) {
        Set<String> uniqueSet = new LinkedHashSet<>(keys); // 保持顺序
        return FXCollections.observableArrayList(uniqueSet);
    }

    private void loadDatabases(ChoiceBox<RedisInfo.Keyspace> choiceBox) {
        String redisInfoText = this.jedisPool.getResource().info();
        RedisInfo redisInfo = new RedisInfo(redisInfoText);
        choiceBox.getItems().clear();
        choiceBox.getItems().addAll(redisInfo.getKeyspaces());
        choiceBox.getSelectionModel().selectFirst();
        RedisfxEventBusService.post(new RedisfxEventMessasge(RedisfxEventType.DATABASE_CHANGED, jedis));
    }

    @Subscribe
    public void handleEventMessage(RedisfxEventMessasge eventMessasge) {
        if (eventMessasge.getEventType() == RedisfxEventType.KEY_DELETED) {
            this.keysListView.getItems().remove((String) eventMessasge.getData());
        }
    }

    @Override
    public void destroy() {
        if (jedisPool != null) {
            jedisPool.destroy();
            jedisPool = null;
        }
    }

}
