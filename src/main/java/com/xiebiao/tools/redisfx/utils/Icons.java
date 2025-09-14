package com.xiebiao.tools.redisfx.utils;

import com.xiebiao.tools.redisfx.RedisfxApplication;
import java.util.Objects;
import javafx.scene.image.Image;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.*;

/**
 * https://kordamp.org/ikonli/cheat-sheet-materialdesign2.html
 *
 * @author Bill Xie
 * @since 2025/8/24 17:33
 **/
public abstract class Icons {

  public static final FontIcon checkIcon = new FontIcon(MaterialDesignC.CHECK);
  public static final FontIcon deleteClockIcon = new FontIcon(MaterialDesignD.DELETE_CLOCK);
  public static final FontIcon checkCircleIcon = new FontIcon(MaterialDesignC.CHECK_CIRCLE);
  public static final FontIcon alterCircleIcon = new FontIcon(MaterialDesignA.ALERT_CIRCLE);
  public static final FontIcon deleteIcon = new FontIcon(MaterialDesignD.DELETE);
  public static final FontIcon serverIcon = new FontIcon(MaterialDesignS.SERVER);
  public static final FontIcon memoryIcon = new FontIcon(MaterialDesignM.MEMORY);
  public static final FontIcon temperatureIcon = new FontIcon(MaterialDesignC.COOLANT_TEMPERATURE);
  public static final FontIcon textSearchIcon = new FontIcon(MaterialDesignT.TEXT_SEARCH_VARIANT);
  public static final Image logoImage = new Image(
      Objects.requireNonNull(RedisfxApplication.class.getResourceAsStream(Constants.logoUri)));
}
