package com.xiebiao.tools.redisfx.core;

/**
 * @author Bill Xie
 * @since 2025/8/23 23:14
 **/
public interface LifeCycle {
    default void init() {
    }


    default boolean isReady() {
        return true;
    }

    void destroy();
}
