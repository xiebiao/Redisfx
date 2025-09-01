package com.xiebiao.tools.redisfx.utils;

import javafx.scene.Node;

/**
 * @author Bill Xie
 * @since 2025/8/25 19:28
 **/
public abstract class Utils {
    public static boolean isFastClick(Node node) {
        Object userData = node.getUserData();
        long currentTime = System.currentTimeMillis();
        node.setUserData(currentTime);
        if (userData instanceof Long lastClickTime) {
            return currentTime - lastClickTime <= 500;
        }
        return false;
    }
}
