package com.xiebiao.tools.redisfx.controller;

import atlantafx.base.theme.Styles;
import atlantafx.base.util.IntegerStringConverter;
import com.google.common.eventbus.Subscribe;
import com.xiebiao.tools.redisfx.model.RedisConnectionInfo;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventBusService;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventMessasge;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventType;
import com.xiebiao.tools.redisfx.utils.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;

import java.net.URL;
import java.util.ResourceBundle;

import static com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventType.NEW_CONNECTION_CREATED;

/**
 * @author Bill Xie
 * @since 2025/8/8 10:19
 **/
public class ConnectionInfoController implements Initializable {
    private Logger logger = org.slf4j.LoggerFactory.getLogger(ConnectionInfoController.class);
    @FXML
    private DialogPane newConnectionDialogPane;
    @FXML
    private TextField host;
    @FXML
    private Spinner<Integer> port;
    @FXML
    private PasswordField password;
    @FXML
    private TextField username;
    @FXML
    private TextField connectionName;
    @FXML
    private Text connectionError;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initPortView();
        initHostView();
        Button applyButton = (Button) newConnectionDialogPane.lookupButton(ButtonType.APPLY);
        applyButton.setOnAction(actionEvent -> {
            RedisConnectionInfo redisConnection = new RedisConnectionInfo(
                    host.getText(),
                    port.getValue(),
                    password.getText(),
                    username.getText(),
                    connectionName.getText());
            RedisfxEventBusService.post(new RedisfxEventMessasge(NEW_CONNECTION_CREATED, redisConnection));
        });
        applyButton.addEventFilter(ActionEvent.ACTION, event -> {

            if (!isValidInputs()) {
                connectionError.setText("Please check your inputs!");
                connectionError.pseudoClassStateChanged(Styles.STATE_DANGER, true);
                event.consume();
                return;
            }
            try (Jedis jedis = tryConnect()) {
                // 连接测试成功
            } catch (Exception e) {
                logger.error("Error connecting to Redis", e);
                connectionError.setText(e.getMessage());
                connectionError.pseudoClassStateChanged(Styles.STATE_DANGER, true);
                event.consume();
            }
        });
        RedisfxEventBusService.register(this);
    }

    private void initHostView() {
        host.textProperty().subscribe(text -> {
            if (!text.equals("")) {
                host.pseudoClassStateChanged(Styles.STATE_DANGER, false);
            }
            connectionName.setText(host.getText() + "@" + Constants.defaultPort);
        });
    }

    private void initPortView() {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 65535, 6379);
        port.setValueFactory(valueFactory);
        port.setEditable(true);
        IntegerStringConverter.createFor(port);
        port.valueProperty().subscribe(value -> {
            connectionName.setText(host.getText() + "@" + port.getValue());
        });
    }

    private boolean isValidInputs() {
        if (host.getText().isEmpty()) {
            host.pseudoClassStateChanged(Styles.STATE_DANGER, true);
            return false;
        }
        return true;
    }

    private Jedis tryConnect() throws Exception {
        JedisClientConfig config = DefaultJedisClientConfig.builder()
                .password(password.getText())
                .user(username.getText().isEmpty() ? "default" : username.getText())
                .build();
        Jedis jedis = new Jedis(host.getText(), port.getValue(), config);
        jedis.connect();
        return jedis;
    }

    @Subscribe
    public void handleEventMessage(RedisfxEventMessasge eventMessasge) {
        if (eventMessasge.getEventType() == RedisfxEventType.EDIT_CONNECTION_DIALOG_OPEN) {
            RedisConnectionInfo redisConnectionInfo = (RedisConnectionInfo) eventMessasge.getData();
            this.host.setText(redisConnectionInfo.host());
            this.port.getValueFactory().setValue(redisConnectionInfo.port());
            this.password.setText(redisConnectionInfo.password());
            this.username.setText(redisConnectionInfo.username());
            this.connectionName.setText(redisConnectionInfo.connectionName());
        }
    }

}
