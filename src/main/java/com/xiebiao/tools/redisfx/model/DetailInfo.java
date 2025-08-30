package com.xiebiao.tools.redisfx.model;

/**
 * @author Bill Xie
 * @since 2025/8/24 21:30
 **/
public class DetailInfo {

  private String key;
  private String value;

  public DetailInfo() {
  }

  public DetailInfo(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }
  public void setKey(String key) {
    this.key = key;
  }
}
