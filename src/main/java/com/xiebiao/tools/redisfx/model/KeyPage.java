package com.xiebiao.tools.redisfx.model;

import redis.clients.jedis.params.ScanParams;

/**
 * @author Bill Xie
 * @since 2025/8/16 20:44
 **/
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

  public boolean isCompleteIteration() {
    return isCompleteIteration;
  }
  public ScanParams getScanParams() {
    return scanParams;
  }
  public void setCompleteIteration(boolean completeIteration) {
    isCompleteIteration = completeIteration;
  }

  public String getCursor() {
    return cursor;
  }
  public void setCursor(String cursor) {
    this.cursor = cursor;
  }
}
