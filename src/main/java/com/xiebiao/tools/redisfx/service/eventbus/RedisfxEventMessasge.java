package com.xiebiao.tools.redisfx.service.eventbus;

/**
 * @author Bill Xie
 * @since 2025/8/8 11:18
 **/
public class RedisfxEventMessasge {
    private RedisfxEventType eventType;
    private Object data;

    public RedisfxEventMessasge(RedisfxEventType eventType) {
        this.eventType = eventType;
    }

    public RedisfxEventMessasge(RedisfxEventType eventType, Object data) {
        this.eventType = eventType;
        this.data = data;
    }

    public RedisfxEventType getEventType() {
        return eventType;
    }

    public Object getData() {
        return data;
    }

    public String toString() {
        return eventType.toString();
    }
}
