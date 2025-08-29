package com.xiebiao.tools.redisfx.model;

import lombok.Data;
import redis.clients.jedis.params.ScanParams;

/**
 * @author Bill Xie
 * @since 2025/8/16 20:44
 **/
@Data
public class KeyPage {
    private int index;
    public final static int size = 50;
    private boolean isCompleteIteration;
    private String cursor = ScanParams.SCAN_POINTER_START;
    private ScanParams scanParams;

    public KeyPage() {
        this.scanParams = new ScanParams();
        this.scanParams.count(size);
        this.scanParams.match("*");
        this.isCompleteIteration = false;
    }
}
