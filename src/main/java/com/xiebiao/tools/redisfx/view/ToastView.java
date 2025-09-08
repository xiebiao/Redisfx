package com.xiebiao.tools.redisfx.view;

import atlantafx.base.theme.Styles;
import com.google.common.base.Strings;
import com.xiebiao.tools.redisfx.utils.RedisfxStyles;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * @author Bill Xie
 * @since 2025/9/8 11:14
 **/
public class ToastView {
    public static void show(String message, Stage ownerStage) {
        show(true, message, ownerStage);
    }

    public static void show(boolean isSuccess, String message, Stage ownerStage) {
        Stage toastStage = new Stage();
        toastStage.initOwner(ownerStage);
        toastStage.initStyle(StageStyle.TRANSPARENT);
        toastStage.setAlwaysOnTop(true);

        Label label = new Label(message);
        label.getStyleClass().add(isSuccess ? RedisfxStyles.TOAST_SUCCESS_CLASS : RedisfxStyles.TOAST_FAILED_CLASS);
        label.setOpacity(0.9);

        StackPane root = new StackPane(label);
        root.setAlignment(Pos.TOP_CENTER);
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(RedisfxStyles.styles.toExternalForm());

        toastStage.setScene(scene);
        //Display at top center.
        double centerX = ownerStage.getX() + ownerStage.getWidth() / 2 ;
        double centerY = ownerStage.getY();
        toastStage.setX(centerX - (double) message.length() / 2);
        toastStage.setY(centerY);
        toastStage.show();
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(2),
                ae -> toastStage.close()));
        timeline.play();
    }
}
