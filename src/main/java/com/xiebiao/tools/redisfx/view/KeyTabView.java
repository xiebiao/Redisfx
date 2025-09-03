package com.xiebiao.tools.redisfx.view;

import atlantafx.base.layout.InputGroup;
import atlantafx.base.theme.Styles;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventBusService;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventMessasge;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventType;
import com.xiebiao.tools.redisfx.utils.Constants;
import com.xiebiao.tools.redisfx.utils.Icons;
import com.xiebiao.tools.redisfx.utils.RedisKeyTypes;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Objects;

/**
 * @author Bill Xie
 * @since 2025/8/14 19:47
 **/
public class KeyTabView {
    private static Logger logger = LoggerFactory.getLogger(KeyTabView.class);
    private TabPane tabPane;
    private Tab keyTab;
    private TextField key;
    private TextArea value;
    private Jedis jedis;
    private Label tips;
    private Button saveButton;
    private Button deleteKeyButton;
    private ButtonBar bottomButtonBar;
    private String connectionName;
    private Spinner<Integer> ttlSpinner;
    private ComboBox<String> keyTypes;

    public KeyTabView(String connectionName, TabPane tabPane) {
        this.tabPane = tabPane;
        this.connectionName = connectionName;
        RedisfxEventBusService.register(this);
    }

    public Tab create(String title) {
        keyTab = new Tab(connectionName + " | " + title);
        keyTab.setId(Constants.keyInfoTabId);
        keyTab.setContent(createKeyTabVBox());
        tabPane.getTabs().add(keyTab);
        tabPane.getSelectionModel().select(keyTab);

        return keyTab;
    }

    public void setJedis(Jedis jedis) {
        this.jedis = jedis;
    }


    @Subscribe
    public void handleEventMessage(RedisfxEventMessasge eventMessasge) {
        logger.debug("handleEventMessage:{}", eventMessasge);
        if (eventMessasge.getEventType() == RedisfxEventType.DATABASE_CHANGED) {
            jedis = (Jedis) eventMessasge.getData();
        }
        if (eventMessasge.getEventType() == RedisfxEventType.NEW_KEY) {
            if (keyTab == null) {
                keyTab = create(Constants.NEW_KEY_TITLE);
            } else {
                keyTab.setText(connectionName + " | " + Constants.NEW_KEY_TITLE);
            }
            key.setDisable(false);
            key.setText(null);
            value.setText(null);
            keyTypes.setDisable(false);
            boolean isKeyInfoTabAdded = tabPane.getTabs().stream().anyMatch(tab -> tab.getId().equals(Constants.keyInfoTabId));
            if (!isKeyInfoTabAdded) {
                tabPane.getTabs().add(keyTab);
            }
            tabPane.getSelectionModel().select(keyTab);
        }
        if (eventMessasge.getEventType() == RedisfxEventType.KEY_SELECTED) {
            if (keyTab == null) {
                keyTab = create((String) eventMessasge.getData());
            } else {
                keyTab.setText(connectionName + " | " + eventMessasge.getData());
            }
            key.setDisable(true);
            boolean isKeyTabAdded = tabPane.getTabs().stream().anyMatch(tab -> tab.getId().equals(Constants.keyInfoTabId));
            if (!isKeyTabAdded) {
                tabPane.getTabs().add(keyTab);
            }
            String selectedKey = (String) eventMessasge.getData();
            String keyType = jedis.type(selectedKey);
            keyTypes.getSelectionModel().select(keyType);
            keyTypes.setDisable(true);
            key.setText(selectedKey);
            //TODO support other key types
            String selectedKeyValue = jedis.get(selectedKey);
            value.setText(selectedKeyValue);
            tabPane.getSelectionModel().select(keyTab);
        }

    }

    private VBox createKeyTabVBox() {
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(Constants.spacing_10));
        vBox.setSpacing(Constants.spacing_10);

        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(Constants.spacing_10);
        flowPane.setVgap(Constants.spacing_10);

        keyTypes = new ComboBox<>();
        keyTypes.getItems().addAll(RedisKeyTypes.types);
        keyTypes.getSelectionModel().select(0);


        key = new TextField();
        key.textProperty().addListener(observable -> {
            if (key.getText() != null) {
                key.pseudoClassStateChanged(Styles.STATE_DANGER, false);
            }
        });
        key.setPrefWidth(400);
        deleteKeyButton = createDeleteButton();
        flowPane.getChildren().addAll(keyTypes, key, createTTL(), deleteKeyButton);

        value = new TextArea();
        value.textProperty().addListener(observable -> {
            if (value.getText() != null) {
                value.pseudoClassStateChanged(Styles.STATE_DANGER, false);
            }
        });
        value.setEditable(true);

        bottomButtonBar = new ButtonBar();
        tips = new Label();
        tips.setPrefWidth(100);
        saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            if (Strings.isNullOrEmpty(key.getText()) || Strings.isNullOrEmpty(key.getText().trim())) {
                key.pseudoClassStateChanged(Styles.STATE_DANGER, true);
                return;
            }
            if (Strings.isNullOrEmpty(value.getText()) || Strings.isNullOrEmpty(value.getText().trim())) {
                value.pseudoClassStateChanged(Styles.STATE_DANGER, true);
                return;
            }
            saveButton.setDisable(true);
            SetParams setParams = new SetParams();
            if (ttlSpinner.getValue() != -1) {
                setParams.ex(ttlSpinner.getValue());
            }
            try {
                String result = jedis.set(key.getText(), value.getText(), setParams);
                if (result.equals(Constants.SUCCESS)) {
                    RedisfxEventBusService.post(new RedisfxEventMessasge(RedisfxEventType.NEW_KEY_CREATED));

                    tips.setText("Save Success");
                    tips.setVisible(true);
                    tips.pseudoClassStateChanged(Styles.STATE_SUCCESS, true);
                    PauseTransition visiblePause = new PauseTransition(Duration.seconds(2));
                    visiblePause.setOnFinished(event2 -> tips.setVisible(false));
                    visiblePause.play();

                    saveButton.pseudoClassStateChanged(Styles.STATE_SUCCESS, true);
                    logger.debug("Redis set(key={}) Success", key.getText());
                }
            } catch (Exception e) {
                tips.setText(e.getMessage());
            }
            saveButton.setDisable(false);
        });
        bottomButtonBar.getButtons().addAll(tips, saveButton);
        vBox.getChildren().addAll(flowPane, value, bottomButtonBar);
        return vBox;
    }

    private InputGroup createTTL() {
        ttlSpinner = new Spinner<>();
        ttlSpinner.setEditable(true);
        ttlSpinner.getStyleClass().remove("spinner");
        Label ttlLabel = new Label("TTL");
        Button update = new Button();
        update.setGraphic(Icons.checkIcon);
        InputGroup ttlInputGroup = new InputGroup(ttlLabel, ttlSpinner, update);
        ttlSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE));
        return ttlInputGroup;
    }

    private Button createDeleteButton() {
        Button deleteKeyButton = new Button();
        deleteKeyButton.setGraphic(Icons.deleteIcon);
        deleteKeyButton.setOnAction(event -> {
            if (key.getText() == null) {
                key.pseudoClassStateChanged(Styles.STATE_DANGER, true);
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Are you sure to delete key: " + key.getText());
            alert.setContentText("This operation cannot be undone.");
            alert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    if (jedis.del(key.getText()) == Constants.DELETED_ONE_KEY_RESULT) {
                        RedisfxEventBusService.post(new RedisfxEventMessasge(RedisfxEventType.KEY_DELETED, key.getText()));
                    } else {
                        logger.error("Redis del(key={}) Failed", key.getText());
                    }
                }
            });
        });
        return deleteKeyButton;
    }
}
