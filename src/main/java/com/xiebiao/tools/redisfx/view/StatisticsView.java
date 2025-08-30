package com.xiebiao.tools.redisfx.view;

import javafx.scene.control.TitledPane;

/**
 * @author Bill Xie
 * @since 2025/8/30
 */
public class StatisticsView {

  public TitledPane create() {
    TitledPane statistics = new TitledPane();
    statistics.setText("Statistics");
    return statistics;
  }


}
