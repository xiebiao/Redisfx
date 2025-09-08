package com.xiebiao.tools.redisfx.utils;

import com.xiebiao.tools.redisfx.RedisfxApplication;

import java.net.URL;

/**
 * @author Bill Xie
 * @since 2025/8/29 16:59
 **/
public abstract class RedisfxStyles {
    public static final String IMPORTANT_INFO_CLASS = "important_info";
    public static final String TOAST_SUCCESS_CLASS = "toast_success";
    public static final String TOAST_FAILED_CLASS = "toast_failed";

    public static final URL styles = RedisfxApplication.class.getResource("styles/custom.css");
}
