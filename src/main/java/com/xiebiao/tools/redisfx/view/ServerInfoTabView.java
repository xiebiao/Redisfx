package com.xiebiao.tools.redisfx.view;

import static atlantafx.base.theme.Styles.TEXT_BOLD;
import static com.xiebiao.tools.redisfx.utils.Constants.AUTO_REFRESH_DISABLED;
import static com.xiebiao.tools.redisfx.utils.Constants.AUTO_REFRESH_ENABLED;

import atlantafx.base.controls.Card;
import atlantafx.base.controls.CustomTextField;
import atlantafx.base.controls.ToggleSwitch;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Tweaks;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.xiebiao.tools.redisfx.core.LifeCycle;
import com.xiebiao.tools.redisfx.model.DetailInfo;
import com.xiebiao.tools.redisfx.model.RedisInfo;
import com.xiebiao.tools.redisfx.model.RedisInfoProperty;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventBusService;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventMessasge;
import com.xiebiao.tools.redisfx.service.eventbus.RedisfxEventType;
import com.xiebiao.tools.redisfx.utils.Constants;
import com.xiebiao.tools.redisfx.utils.Icons;
import com.xiebiao.tools.redisfx.utils.RedisfxStyles;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * @author Bill Xie
 * @since 2025/8/15 18:43
 **/
public class ServerInfoTabView implements LifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(ServerInfoTabView.class);
    private String connectionName;
    private TabPane tabPane;
    private Jedis jedis;
    private ScrollPane scrollPane;
    private VBox tabContents;
    private HBox cards;
    private RedisInfoProperty redisInfoProperty;
    private StackedBarChart chart;
    private XYChart.Series<String, Number> keys;
    private XYChart.Series<String, Number> expires;
    private XYChart.Series<String, Number> avgTtl;
    private TableView<DetailInfo> detailInfoTable;
    private FilteredList<DetailInfo> filteredData;
    private CustomTextField searchTextField;
    private volatile boolean autoRefreshPaused = false;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public ServerInfoTabView(String connectionName, TabPane tabPane) {
        this.connectionName = connectionName;
        this.tabPane = tabPane;
    }

    public Tab create() {
        Tab serverInfoTab = new Tab(connectionName);
        serverInfoTab.setId(Constants.serverInfoTabId);
        tabPane.widthProperty().addListener(observable -> {
            scrollPane.setFitToWidth(true);
            tabContents.setPrefWidth(tabPane.getWidth());
            cards.getChildren().stream().forEach(node -> {
                Card card = (Card) node;
                card.setPrefWidth(tabPane.getWidth());
            });
            cards.setPrefWidth(tabPane.getWidth());

        });
        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        //Disable horizontal scrolling
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaX() != 0) {
                event.consume();
            }
        });
        serverInfoTab.setContent(scrollPane);
        serverInfoTab.setClosable(false);
        RedisfxEventBusService.register(this);
        return serverInfoTab;
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
                                Platform.runLater(() -> {
                                    RedisInfo redisInfo = new RedisInfo(jedis.info());
                                    redisInfoProperty.setRedisInfo(redisInfo);
                                    createKeyspaceChart(redisInfo);
                                    createDetailInfoTable(redisInfo);
                                });
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

    private void show() {
        String info = jedis.info();
        RedisInfo redisInfo = new RedisInfo(info);
        redisInfoProperty = new RedisInfoProperty(redisInfo);
        cards = new HBox();
        cards.setSpacing(Constants.spacing_20);
        cards.getChildren().addAll(
                createServerInfoCard(redisInfo),
                createMemoryInfoCard(redisInfoProperty),
                createStatsInfoCard(redisInfoProperty)
        );

        tabContents = new VBox();
        tabContents.setAlignment(Pos.CENTER_RIGHT);
        tabContents.setSpacing(Constants.spacing_20);
        tabContents.getChildren().addAll(
                createAutoRefreshToggle(),
                cards,
                createStatisticsInfo(redisInfo),
                createDetailInfo(redisInfo)
        );
        scrollPane.setContent(tabContents);
    }

    private TitledPane createStatisticsInfo(RedisInfo redisInfo) {

        TitledPane statisticsInfo = new TitledPane();
        statisticsInfo.setText("Statistics Info");
        statisticsInfo.setContentDisplay(ContentDisplay.RIGHT);
        statisticsInfo.setExpanded(true);
        statisticsInfo.setCollapsible(false);
        statisticsInfo.setContent(createKeyspaceChart(redisInfo));

        return statisticsInfo;
    }

    private TitledPane createDetailInfo(RedisInfo redisInfo) {
        searchTextField = new CustomTextField();
        searchTextField.setRight(Icons.textSearchIcon);
        searchTextField.setMinWidth(150);
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(detailInfo -> {
                if (Objects.nonNull(detailInfo.getKey()) && detailInfo.getKey().contains(newValue)) {
                    return true;
                }
                return Objects.nonNull(detailInfo.getValue()) && detailInfo.getValue().contains(newValue);
            });
        });
        TitledPane detailInfo = new TitledPane();
        detailInfo.setGraphic(searchTextField);
        detailInfo.setTextAlignment(TextAlignment.LEFT);
        detailInfo.setGraphicTextGap(Constants.spacing_20);
        detailInfo.setText("All Redis Info");
        detailInfo.setContentDisplay(ContentDisplay.RIGHT);
        detailInfo.setExpanded(true);
        detailInfo.setCollapsible(false);
        detailInfo.setContent(createDetailInfoTable(redisInfo));
        return detailInfo;
    }


    private Card createServerInfoCard(RedisInfo redisInfo) {
        Card card = createCard();
        Label label = new Label("Server", Icons.serverIcon);
        label.getStyleClass().addAll(Styles.TITLE_4, TEXT_BOLD);
        card.setHeader(label);

        GridPane body = new GridPane();
        body.setVgap(Constants.spacing_10);
        body.setHgap(Constants.spacing_10);
        body.add(new Label("Redis Version:"), 0, 0);
        Label version = new Label(redisInfo.getServerInfo().getRedisVersion());
        version.getStyleClass().addAll(RedisfxStyles.IMPORTANT_INFO_CLASS);
        body.add(version, 1, 0);

        body.add(new Label("OS:"), 0, 1);
        Label os = new Label(redisInfo.getServerInfo().getOs());
        os.getStyleClass().addAll(RedisfxStyles.IMPORTANT_INFO_CLASS);
        body.add(os, 1, 1);

        body.add(new Label("Process Id:"), 0, 2);
        Label processId = new Label(redisInfo.getServerInfo().getProcessId() + "");
        processId.getStyleClass().addAll(RedisfxStyles.IMPORTANT_INFO_CLASS);
        body.add(processId, 1, 2);

        card.setBody(body);
        return card;
    }

    private TableView<DetailInfo> createDetailInfoTable(RedisInfo redisInfo) {
        Iterable<String> lines = redisInfo.getLines();
        ObservableList<DetailInfo> data = buildDetailInfo(lines);
        if (detailInfoTable == null) {
            detailInfoTable = new TableView<>();
            detailInfoTable.addEventFilter(ScrollEvent.SCROLL, event -> {
                if (event.getDeltaX() != 0) {
                    event.consume();
                }
            });
            TableColumn<DetailInfo, String> key = new TableColumn<>("Key");
            key.setMinWidth(200);
            TableColumn<DetailInfo, String> value = new TableColumn<>("Value");
            key.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey()));
            value.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getValue()));
            value.prefWidthProperty().bind(detailInfoTable.widthProperty().multiply(0.70));
            detailInfoTable.getColumns().addAll(key, value);
            detailInfoTable.setFixedCellSize(30);
            detailInfoTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        }
        if (!Strings.isNullOrEmpty(searchTextField.getText())) {
            filteredData = data.filtered(p -> {
                if (!Strings.isNullOrEmpty(p.getKey()) && p.getKey().contains(searchTextField.getText())) {
                    return true;
                } else return !Strings.isNullOrEmpty(p.getValue()) && p.getValue().contains(searchTextField.getText());
            });
        } else {
            filteredData = new FilteredList<>(data, p -> true);
        }
        detailInfoTable.setPrefHeight(detailInfoTable.getFixedCellSize() * data.size());
        detailInfoTable.setItems(filteredData);
        return detailInfoTable;
    }

    private ObservableList<DetailInfo> buildDetailInfo(Iterable<String> lines) {
        ObservableList<DetailInfo> data = FXCollections.observableArrayList();
        lines.forEach(line -> {
            DetailInfo detailInfo = new DetailInfo();
            Iterator<String> itr = Splitter.on(":").split(line).iterator();
            detailInfo.setKey(itr.next());
            if (itr.hasNext()) {
                detailInfo.setValue(itr.next());
            }
            data.add(detailInfo);
        });
        return data;
    }

    private Card createCard() {
        Card card = new Card();
        card.setPrefWidth(tabPane.getWidth() / 3);
        card.getStyleClass().add(Styles.ELEVATED_3);
        return card;
    }

    private StackedBarChart<String, Number> createKeyspaceChart(RedisInfo redisInfo) {
        if (chart == null) {
            var x = new CategoryAxis();
            x.setLabel("Database");
            var y = new NumberAxis();
            y.setLabel("Count");

            chart = new StackedBarChart<>(x, y);
            chart.setTitle("Keyspace");
            chart.setMinHeight(300);

            keys = new XYChart.Series<String, Number>();
            keys.setName("Keys");
            expires = new XYChart.Series<String, Number>();
            expires.setName("Expires");
            avgTtl = new XYChart.Series<String, Number>();
            avgTtl.setName("Avg TTL");
            redisInfo.getKeyspaces().forEach(keyspace -> {
                keys.getData().add(new XYChart.Data<>(keyspace.getName(), keyspace.getKeyCount()));
                expires.getData().add(new XYChart.Data<>(keyspace.getName(), keyspace.getExpiresCount()));
                avgTtl.getData().add(new XYChart.Data<>(keyspace.getName(), keyspace.getAvgTtlCount()));
            });
            chart.getData().addAll(keys, expires, avgTtl);
        } else {
            //Update
            for (int i = 0; i < redisInfo.getKeyspaces().size(); i++) {
                keys.getData().get(i).setYValue(redisInfo.getKeyspaces().get(i).getKeyCount());
                expires.getData().get(i).setYValue(redisInfo.getKeyspaces().get(i).getExpiresCount());
                avgTtl.getData().get(i).setYValue(redisInfo.getKeyspaces().get(i).getAvgTtlCount());
            }
        }

        return chart;
    }


    private Card createMemoryInfoCard(RedisInfoProperty redisInfoProperty) {
        Card card = createCard();
        Label label = new Label("Memory", Icons.memoryIcon);
        label.getStyleClass().addAll(Styles.TITLE_4, TEXT_BOLD);
        card.setHeader(label);

        GridPane body = new GridPane();
        body.setVgap(Constants.spacing_10);
        body.setHgap(Constants.spacing_10);

        body.add(new Label("Total System Memory:"), 0, 0);
        Label systemMemory = new Label(
                redisInfoProperty.getRedisInfo().getMemoryInfo().getTotalSystemMemoryHuman());
        systemMemory.getStyleClass().addAll(RedisfxStyles.IMPORTANT_INFO_CLASS);
        systemMemory.textProperty()
                .bind(redisInfoProperty.getMemoryInfoProperty().totalSystemMemoryHumanProperty());
        body.add(systemMemory, 1, 0);

        body.add(new Label("Used Memory:"), 0, 1);
        Label usedMemory = new Label(
                redisInfoProperty.getRedisInfo().getMemoryInfo().getUsedMemoryHuman());
        usedMemory.getStyleClass().addAll(RedisfxStyles.IMPORTANT_INFO_CLASS);
        usedMemory.textProperty()
                .bind(redisInfoProperty.getMemoryInfoProperty().usedMemoryHumanProperty());
        body.add(usedMemory, 1, 1);

        body.add(new Label("Used Memory Peak:"), 0, 2);
        Label usedMemoryPeak = new Label(
                redisInfoProperty.getRedisInfo().getMemoryInfo().getUsedMemoryPeakHuman());
        usedMemoryPeak.getStyleClass().addAll(RedisfxStyles.IMPORTANT_INFO_CLASS);
        usedMemoryPeak.textProperty()
                .bind(redisInfoProperty.getMemoryInfoProperty().usedMemoryPeakHumanProperty());
        body.add(usedMemoryPeak, 1, 2);

        card.setBody(body);
        return card;
    }

    private Card createStatsInfoCard(RedisInfoProperty redisInfoProperty) {
        Card card = createCard();
        Label label = new Label("Stats", Icons.temperatureIcon);
        label.getStyleClass().addAll(Styles.TITLE_4, TEXT_BOLD);
        card.setHeader(label);
        GridPane body = new GridPane();
        body.setVgap(Constants.spacing_10);
        body.setHgap(Constants.spacing_10);
        body.add(new Label("Connected Clients:"), 0, 0);
        Label connectedClients = new Label(
                redisInfoProperty.getRedisInfo().getStatsInfo().getConnectedClients());
        connectedClients.getStyleClass().addAll(RedisfxStyles.IMPORTANT_INFO_CLASS);
        connectedClients.textProperty()
                .bind(redisInfoProperty.getStatsInfoProperty().getConnectedClients());
        body.add(connectedClients, 1, 0);

        body.add(new Label("Total Connections:"), 0, 1);
        Label totalConnections = new Label(
                redisInfoProperty.getRedisInfo().getStatsInfo().getTotalConnections());
        totalConnections.getStyleClass().addAll(RedisfxStyles.IMPORTANT_INFO_CLASS);
        totalConnections.textProperty()
                .bind(redisInfoProperty.getStatsInfoProperty().getTotalConnections());
        body.add(totalConnections, 1, 1);

        body.add(new Label("Total Commands Processed:"), 0, 2);
        Label totalCommandsProcessed = new Label(
                redisInfoProperty.getRedisInfo().getStatsInfo().getTotalCommandsProcessed());
        totalCommandsProcessed.getStyleClass().addAll(RedisfxStyles.IMPORTANT_INFO_CLASS);
        totalCommandsProcessed.textProperty()
                .bind(redisInfoProperty.getStatsInfoProperty().getTotalCommandsProcessed());
        body.add(totalCommandsProcessed, 1, 2);

        body.add(new Label("Key Misses:"), 0, 3);
        Label keyMisses = new Label(
                redisInfoProperty.getRedisInfo().getStatsInfo().getKeyMisses());
        keyMisses.getStyleClass().addAll(RedisfxStyles.IMPORTANT_INFO_CLASS);
        keyMisses.textProperty().bind(redisInfoProperty.getStatsInfoProperty().getKeyMisses());
        body.add(keyMisses, 1, 3);

        card.setBody(body);
        return card;
    }

    @Subscribe
    public void handleEventMessage(RedisfxEventMessasge eventMessasge) {
        if (eventMessasge.getEventType() == RedisfxEventType.CONNECTION_CONNECTED) {
            this.jedis = (Jedis) eventMessasge.getData();
            Platform.runLater(() -> {
                show();
            });
        }
    }

    @Override
    public void destroy() {
        scheduler.shutdownNow();
    }
}
