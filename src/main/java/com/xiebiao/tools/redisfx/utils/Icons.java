package com.xiebiao.tools.redisfx.utils;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignD;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;
import org.kordamp.ikonli.materialdesign2.MaterialDesignT;

/**
 * https://kordamp.org/ikonli/cheat-sheet-materialdesign2.html
 *
 * @author Bill Xie
 * @since 2025/8/24 17:33
 **/
public abstract class Icons {

  public static final FontIcon checkIcon = new FontIcon(MaterialDesignC.CHECK);
  public static final FontIcon deleteIcon = new FontIcon(MaterialDesignD.DELETE);
  public static final FontIcon serverIcon = new FontIcon(MaterialDesignS.SERVER);
  public static final FontIcon memoryIcon = new FontIcon(MaterialDesignM.MEMORY);
  public static final FontIcon temperatureIcon = new FontIcon(MaterialDesignC.COOLANT_TEMPERATURE);
  public static final FontIcon textSearchIcon = new FontIcon(MaterialDesignT.TEXT_SEARCH_VARIANT);
}
