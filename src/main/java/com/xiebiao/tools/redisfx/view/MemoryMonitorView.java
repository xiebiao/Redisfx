package com.xiebiao.tools.redisfx.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.lang.management.ManagementFactory;

/**
 * @author Bill Xie
 * @since 2025/8/15 18:01
 **/
public class MemoryMonitorView {
    private Label heapMemoryLabel = new Label();
    private Label nonHeapMemoryLabel = new Label();
    private Label maxMemoryLabel = new Label();
    private ProgressBar heapProgressBar = new ProgressBar();
    private Button gcButton = new Button("GC");

    public void create(Stage primaryStage) {
        // 创建主布局
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // 设置进度条样式
        heapProgressBar.setMinWidth(300);
        heapProgressBar.setStyle("-fx-accent: #2E8B57;");

        // 添加组件
        root.getChildren().addAll(
                createInfoBox("Heap: ", heapMemoryLabel),
                createProgressBox("Heap Progress: ", heapProgressBar),
                createInfoBox("No-Heap", nonHeapMemoryLabel),
                createInfoBox("Max Memory", maxMemoryLabel),
                createGcButtonBox()
        );

        // 设置定时更新
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> updateMemoryInfo())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        Stage stage = new Stage();
        // 设置场景
        Scene scene = new Scene(root, 450, 300);
        stage.setTitle("Memory Monitor");
        stage.setScene(scene);
        stage.initOwner(primaryStage);
        stage.show();
    }

    private HBox createInfoBox(String title, Label valueLabel) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(new Label(title), valueLabel);
        return box;
    }

    private HBox createProgressBox(String title, ProgressBar progressBar) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().addAll(new Label(title), progressBar);
        return box;
    }

    private HBox createGcButtonBox() {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_RIGHT);
        gcButton.setOnAction(e -> System.gc());
        box.getChildren().add(gcButton);
        return box;
    }

    private void updateMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();

        // 堆内存信息
        long usedHeap = runtime.totalMemory() - runtime.freeMemory();
        long maxHeap = runtime.maxMemory();
        double heapUsage = (double) usedHeap / maxHeap;

        heapMemoryLabel.setText(formatBytes(usedHeap) + " / " + formatBytes(maxHeap));
        heapProgressBar.setProgress(heapUsage);

        // 非堆内存信息（示例，实际需要更复杂的监控）
        long nonHeapUsed = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
        nonHeapMemoryLabel.setText(formatBytes(nonHeapUsed));

        // 最大内存
        maxMemoryLabel.setText(formatBytes(maxHeap));
    }

    private String formatBytes(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f %s", bytes / Math.pow(unit, exp), pre);
    }
}
