package com.xiebiao.tools.redisfx.view;

import static com.xiebiao.tools.redisfx.utils.Constants.AUTO_REFRESH_DISABLED;
import static com.xiebiao.tools.redisfx.utils.Constants.AUTO_REFRESH_ENABLED;

import atlantafx.base.controls.ToggleSwitch;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

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
  private redis.clients.jedis.JedisPool jedisPool;
  private int selectedDatabase = 0;
  private Button saveButton;
  private Button deleteKeyButton;
  private ButtonBar bottomButtonBar;
  private String connectionName;
  private Spinner<Integer> ttlSpinner;
  private Button updateTTL;
  private ComboBox<String> keyTypes;
  private volatile boolean autoRefreshPaused = false;
  private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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

  public void setJedisPool(redis.clients.jedis.JedisPool jedisPool, int database) {
    this.jedisPool = jedisPool;
    this.selectedDatabase = database;
  }


  @Subscribe
  public void handleEventMessage(RedisfxEventMessasge eventMessasge) {
    logger.debug("handleEventMessage:{}", eventMessasge);
    if (eventMessasge.getEventType() == RedisfxEventType.DATABASE_CHANGED) {
      jedisPool = (redis.clients.jedis.JedisPool) eventMessasge.getData();
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
      boolean isKeyInfoTabAdded = tabPane.getTabs().stream()
          .anyMatch(tab -> tab.getId().equals(Constants.keyInfoTabId));
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
      boolean isKeyTabAdded = tabPane.getTabs().stream()
          .anyMatch(tab -> tab.getId().equals(Constants.keyInfoTabId));
      if (!isKeyTabAdded) {
        tabPane.getTabs().add(keyTab);
      }
      String selectedKey = (String) eventMessasge.getData();
      try (var jedis = jedisPool.getResource()) {
        jedis.select(selectedDatabase);
        String keyType = jedis.type(selectedKey);
        keyTypes.getSelectionModel().select(keyType);
        keyTypes.setDisable(true);
        key.setText(selectedKey);
        long ttl = jedis.ttl(selectedKey);
        if (ttl > 0) {
          ttlSpinner.getValueFactory().setValue((int) ttl);
        } else {
          ttlSpinner.getValueFactory().setValue(-1);
        }
        //TODO support other key types
        String selectedKeyValue = jedis.get(selectedKey);
        value.setText(selectedKeyValue);
      } catch (Exception e) {
        ToastView.show(false, e.getMessage(), (Stage) tabPane.getScene().getWindow());
        return;
      }
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
    flowPane.getChildren().addAll(keyTypes, key);
    createTTL(flowPane);
    flowPane.getChildren().addAll(deleteKeyButton, createAutoRefreshToggle());

    value = new TextArea();
    value.textProperty().addListener(observable -> {
      if (value.getText() != null) {
        value.pseudoClassStateChanged(Styles.STATE_DANGER, false);
      }
    });
    value.setEditable(true);

    bottomButtonBar = new ButtonBar();
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
      try (var jedis = jedisPool.getResource()) {
        jedis.select(selectedDatabase);
        String result = jedis.set(key.getText(), value.getText(), setParams);
        if (result.equals(Constants.SUCCESS)) {
          RedisfxEventBusService.post(new RedisfxEventMessasge(RedisfxEventType.NEW_KEY_CREATED));
          ToastView.show(true, "Save Success", (Stage) tabPane.getScene().getWindow());
          saveButton.pseudoClassStateChanged(Styles.STATE_SUCCESS, true);
          logger.debug("Redis set(key={}) Success", key.getText());
        }
      } catch (Exception e) {
        ToastView.show(false, e.getMessage(), (Stage) tabPane.getScene().getWindow());
      }
      saveButton.setDisable(false);
    });
    bottomButtonBar.getButtons().addAll(saveButton);
    vBox.getChildren().addAll(flowPane, value, bottomButtonBar);
    return vBox;
  }

  private ToggleSwitch createAutoRefreshToggle() {
    ToggleSwitch autoRefreshToggle = new ToggleSwitch(AUTO_REFRESH_DISABLED);
    autoRefreshToggle.setId("autoRefreshToggle");
    autoRefreshToggle.setTooltip(new Tooltip("Auto refresh every 2 seconds."));
    autoRefreshToggle.setSelected(false);
    autoRefreshToggle.selectedProperty().addListener(
        (obs, old, newVal) -> {
          if (newVal) {
            autoRefreshPaused = false;
            scheduler.scheduleAtFixedRate(() -> {
              if (!autoRefreshPaused) {
                String keyText = this.key.getText();
                try (var jedis = jedisPool.getResource()) {
                  jedis.select(selectedDatabase);
                  long ttl = jedis.ttl(keyText);
                  String val = jedis.get(keyText);
                  Platform.runLater(() -> {
                    ttlSpinner.getValueFactory().setValue((int) ttl);
                    value.setText(val);
                  });
                } catch (Exception e) {
                  logger.error("Auto refresh error", e);
                }
              }
            }, 0, 2, TimeUnit.SECONDS);
          } else {
            autoRefreshPaused = true;
          }
          autoRefreshToggle.setText(newVal ? AUTO_REFRESH_ENABLED : AUTO_REFRESH_DISABLED);
          logger.debug("auto refresh enable:{}", newVal);
        }
    );
    return autoRefreshToggle;

  }

  private void createTTL(Pane parent) {
    ttlSpinner = new Spinner<>();
    ttlSpinner.setEditable(true);
    Tooltip ttlTooltip = new Tooltip("-1 means never expire(seconds)");
    ttlSpinner.setTooltip(ttlTooltip);
    Label ttlLabel = new Label("TTL");
    Button deleteClock = new Button();
    deleteClock.setGraphic(Icons.deleteClockIcon);
    deleteClock.setTooltip(new Tooltip("Delete TTL"));
    deleteClock.setOnAction(event -> {
      try (var jedis = jedisPool.getResource()) {
        jedis.select(selectedDatabase);
        jedis.persist(key.getText());
        ToastView.show(true, "Delete TTL Success", (Stage) tabPane.getScene().getWindow());
      } catch (Exception e) {
        ToastView.show(false, e.getMessage(), (Stage) tabPane.getScene().getWindow());
      }
    });
    updateTTL = new Button();
    updateTTL.setGraphic(Icons.checkIcon);
    updateTTL.setTooltip(new Tooltip("Update TTL"));
    updateTTL.setOnAction(event -> {
      if (ttlSpinner.getValue() != -1) {
        try (var jedis = jedisPool.getResource()) {
          jedis.select(selectedDatabase);
          jedis.expire(key.getText(), ttlSpinner.getValue());
          ToastView.show(true, "Update TTL Success", (Stage) tabPane.getScene().getWindow());
        } catch (Exception e) {
          ToastView.show(false, e.getMessage(), (Stage) tabPane.getScene().getWindow());
        }
      }
    });
    ttlSpinner.setValueFactory(
        new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, Integer.MAX_VALUE));
    InputGroup actions = new InputGroup(deleteClock, updateTTL);
    parent.getChildren().addAll(ttlLabel, ttlSpinner, actions);
  }

  private Button createDeleteButton() {
    Button deleteKeyButton = new Button();
    deleteKeyButton.setTooltip(new Tooltip("Delete Key"));
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
          try (var jedis = jedisPool.getResource()) {
            jedis.select(selectedDatabase);
            if (jedis.del(key.getText()) == Constants.DELETED_ONE_KEY_RESULT) {
              RedisfxEventBusService.post(
                  new RedisfxEventMessasge(RedisfxEventType.KEY_DELETED, key.getText()));
            } else {
              logger.error("Redis del(key={}) Failed", key.getText());
            }
          } catch (Exception e) {
            logger.error("Redis del(key={}) error", key.getText(), e);
            ToastView.show(false, e.getMessage(), (Stage) tabPane.getScene().getWindow());
          }
        }
      });
    });
    return deleteKeyButton;
  }
}
