package com.xiebiao.tools.redisfx.model;

import com.xiebiao.tools.redisfx.model.RedisInfo.MemoryInfo;
import com.xiebiao.tools.redisfx.model.RedisInfo.StatsInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author Bill Xie
 * @since 2025/9/2
 */
public class RedisInfoProperty {

  private RedisInfo redisInfo;
  private StatsInfoProperty statsInfoProperty = new StatsInfoProperty();
  private MemoryInfoProperty memoryInfoProperty = new MemoryInfoProperty();

  public RedisInfo getRedisInfo() {
    return redisInfo;
  }

  public void setRedisInfo(RedisInfo redisInfo) {
    this.redisInfo = redisInfo;
    statsInfoProperty.setStatsInfo(redisInfo.getStatsInfo());
    memoryInfoProperty.setMemoryInfo(redisInfo.getMemoryInfo());
  }

  public RedisInfoProperty(RedisInfo redisInfo) {
    this.redisInfo = redisInfo;
    statsInfoProperty = new StatsInfoProperty(redisInfo.getStatsInfo());
    memoryInfoProperty = new MemoryInfoProperty(redisInfo.getMemoryInfo());
  }

  public StatsInfoProperty getStatsInfoProperty() {
    return statsInfoProperty;
  }

  public MemoryInfoProperty getMemoryInfoProperty() {
    return memoryInfoProperty;
  }

  public static class MemoryInfoProperty {

    private StringProperty usedMemoryHuman;
    private StringProperty usedMemoryPeakHuman;
    private StringProperty totalSystemMemoryHuman;
    private MemoryInfo memoryInfo;

    public MemoryInfoProperty() {
      this.usedMemoryHuman = new SimpleStringProperty();
      this.usedMemoryPeakHuman = new SimpleStringProperty();
      this.totalSystemMemoryHuman = new SimpleStringProperty();
    }

    public MemoryInfoProperty(MemoryInfo memoryInfo) {
      this.memoryInfo = memoryInfo;
      usedMemoryHuman = new SimpleStringProperty(memoryInfo.getUsedMemoryHuman());
      usedMemoryPeakHuman = new SimpleStringProperty(memoryInfo.getUsedMemoryPeakHuman());
      totalSystemMemoryHuman = new SimpleStringProperty(memoryInfo.getTotalSystemMemoryHuman());
    }

    public void setMemoryInfo(MemoryInfo memoryInfo) {
      this.memoryInfo = memoryInfo;
      usedMemoryHuman.set(memoryInfo.getUsedMemoryHuman());
      usedMemoryPeakHuman.set(memoryInfo.getUsedMemoryPeakHuman());
      totalSystemMemoryHuman.set(memoryInfo.getTotalSystemMemoryHuman());
    }

    public StringProperty usedMemoryHumanProperty() {
      return usedMemoryHuman;
    }

    public StringProperty usedMemoryPeakHumanProperty() {
      return usedMemoryPeakHuman;
    }

    public StringProperty totalSystemMemoryHumanProperty() {
      return totalSystemMemoryHuman;
    }
  }

  public static class KeyspaceProperty {

  }

  public static class StatsInfoProperty {

    private StringProperty connectedClients;
    private StringProperty totalCommandsProcessed;
    private StringProperty totalConnections;
    private StringProperty keyHits;
    private StringProperty keyMisses;
    private StatsInfo statsInfo;

    public StatsInfoProperty() {
      this.connectedClients = new SimpleStringProperty();
      this.totalCommandsProcessed = new SimpleStringProperty();
      this.totalConnections = new SimpleStringProperty();
      this.keyHits = new SimpleStringProperty();
      this.keyMisses = new SimpleStringProperty();
    }

    public StatsInfoProperty(StatsInfo statsInfo) {
      this.statsInfo = statsInfo;
      this.connectedClients = new SimpleStringProperty(statsInfo.getConnectedClients());
      this.totalCommandsProcessed = new SimpleStringProperty(statsInfo.getTotalCommandsProcessed());
      this.totalConnections = new SimpleStringProperty(statsInfo.getTotalConnections());
      this.keyHits = new SimpleStringProperty(statsInfo.getKeyHits());
      this.keyMisses = new SimpleStringProperty(statsInfo.getKeyMisses());
    }

    public void setStatsInfo(StatsInfo statsInfo) {
      this.statsInfo = statsInfo;
      this.totalConnections.set(statsInfo.getTotalConnections());
      this.keyHits.set(statsInfo.getKeyHits());
      this.keyMisses.set(statsInfo.getKeyMisses());
      this.totalCommandsProcessed.set(statsInfo.getTotalCommandsProcessed());
    }

    public StringProperty getTotalCommandsProcessed() {
      return totalCommandsProcessed;
    }

    public StringProperty getTotalConnections() {
      return totalConnections;
    }

    public StringProperty getKeyMisses() {
      return keyMisses;
    }

    public StringProperty getConnectedClients() {
      return connectedClients;
    }
  }


}
