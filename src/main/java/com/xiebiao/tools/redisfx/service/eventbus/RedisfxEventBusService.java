package com.xiebiao.tools.redisfx.service.eventbus;

import com.google.common.eventbus.EventBus;

/**
 * @author Bill Xie
 * @since 2025/8/8 11:01
 **/
public final class RedisfxEventBusService {
    private final static EventBus eventBus = new EventBus();

    public static void post(RedisfxEventMessasge eventMessasge) {
        eventBus.post(eventMessasge);
    }

    public static void register(Object object) {
        eventBus.register(object);

    }

    public static void unregister(Object object) {
        eventBus.unregister(object);

    }
}
