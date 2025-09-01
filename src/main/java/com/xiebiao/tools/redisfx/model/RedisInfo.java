package com.xiebiao.tools.redisfx.model;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UnknownFormatFlagsException;
import java.util.stream.Collectors;


/**
 * @author Bill Xie
 * @since 2025/8/14 16:06
 **/
public class RedisInfo {

  private final String info;
  private ServerInfo serverInfo;
  private static Set<Integer> defaultKeyspaceIndex = new HashSet<>(
      Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
  private Iterable<String> lines;
  private List<Keyspace> keyspaces;
  private MemoryInfo memoryInfo;
  private StatsInfo statsInfo;


  public RedisInfo(String info) {
    this.info = info;
    parseRedisInfo();
  }

  public MemoryInfo getMemoryInfo() {
    return memoryInfo;
  }

  public StatsInfo getStatsInfo() {
    return statsInfo;
  }

  public Iterable<String> getLines() {
    return lines;
  }

  public ServerInfo getServerInfo() {
    return serverInfo;
  }

  private void parseRedisInfo() {

    if (this.info.contains("\r\n")) {
      lines = Splitter.on("\r\n").omitEmptyStrings().split(this.info);
    } else if (this.info.contains("\n")) {
      lines = Splitter.on("\n").omitEmptyStrings().split(this.info);
    } else if (this.info.contains("\r")) {
      lines = Splitter.on("\r").omitEmptyStrings().split(this.info);
    } else {
      throw new UnknownFormatFlagsException("Unknown format flags");
    }
    serverInfo = parseServerInfo(lines);
    keyspaces = parseKeyspaces(lines);
    memoryInfo = parseMemoryInfo(lines);
    statsInfo = parseStatsInfo(lines);
  }

  public List<Keyspace> getKeyspaces() {
    List<Keyspace> newKeyspaces = new ArrayList<>();
    if (Objects.isNull(this.keyspaces) || this.keyspaces.isEmpty()) {
      defaultKeyspaceIndex.forEach(item -> {
        Keyspace keyspace = new Keyspace(item);
        newKeyspaces.add(keyspace);
      });
      return newKeyspaces;
    }

    Set<Integer> indexSet = this.keyspaces.stream().map(Keyspace::getIndex)
        .collect(Collectors.toSet());
    Set<Integer> difference = Sets.difference(defaultKeyspaceIndex, indexSet);
    newKeyspaces.addAll(this.keyspaces);
    difference.forEach(item -> {
      Keyspace keyspace = new Keyspace(item);
      newKeyspaces.add(keyspace);
    });
    Collections.sort(newKeyspaces);
    return newKeyspaces;
  }

  private ServerInfo parseServerInfo(Iterable<String> lines) {
    ServerInfo serverInfo = new ServerInfo();
    for (String line : lines) {
      if (line.startsWith("redis_version")) {
        String[] values = line.split(":");
        serverInfo.setRedisVersion(values[1]);
      } else if (line.startsWith("os")) {
        String[] values = line.split(":");
        serverInfo.setOs(values[1]);
      } else if (line.startsWith("process_id")) {
        String[] values = line.split(":");
        serverInfo.setProcessId(Integer.parseInt(values[1]));
      }
    }
    return serverInfo;
  }

  private StatsInfo parseStatsInfo(Iterable<String> lines) {
    StatsInfo statsInfo = new StatsInfo();
    for (String line : lines) {
      if (line.startsWith("connected_clients")) {
        String[] values = line.split(":");
        statsInfo.setConnectedClients(values[1]);
      } else if (line.startsWith("total_commands_processed")) {
        String[] values = line.split(":");
        statsInfo.setTotalCommandsProcessed(values[1]);
      } else if (line.startsWith("total_connections")) {
        String[] values = line.split(":");
        statsInfo.setTotalConnections(values[1]);
      } else if (line.startsWith("keyspace_hits")) {
        String[] values = line.split(":");
        statsInfo.setKeyHits(values[1]);
      } else if (line.startsWith("keyspace_misses")) {
        String[] values = line.split(":");
        statsInfo.setKeyMisses(values[1]);
      }
    }
    return statsInfo;
  }

  private MemoryInfo parseMemoryInfo(Iterable<String> lines) {
    MemoryInfo memoryInfo = new MemoryInfo();
    for (String line : lines) {
      if (line.startsWith("used_memory_human")) {
        String[] values = line.split(":");
        memoryInfo.setUsedMemoryHuman(values[1]);
      } else if (line.startsWith("used_memory_peak_human")) {
        String[] values = line.split(":");
        memoryInfo.setUsedMemoryPeakHuman(values[1]);
      } else if (line.startsWith("total_system_memory_human")) {
        String[] values = line.split(":");
        memoryInfo.setTotalSystemMemoryHuman(values[1]);
      }
    }
    return memoryInfo;
  }

  private List<Keyspace> parseKeyspaces(Iterable<String> strings) {
    List<Keyspace> keyspaces = Lists.newArrayList();
    for (String string : strings) {
      Keyspace keyspace = new Keyspace();
      //TODO ugly
      if (string.startsWith("db") && string.contains("keys=")) {
        String[] v = string.split(":");
        keyspace.setIndex(Integer.parseInt(v[0].replace("db", "")));
        String[] v2 = v[1].split(",");
        keyspace.setKeys(v2[0]);
        keyspace.setExpires(v2[1]);
        keyspace.setAvgTtl(v2[2]);
        keyspaces.add(keyspace);
      }
    }
    return keyspaces;
  }

  static public class MemoryInfo {

    private String usedMemoryHuman;
    private String usedMemoryPeakHuman;
    private String totalSystemMemoryHuman;

    public String getUsedMemoryHuman() {
      return usedMemoryHuman;
    }

    public void setUsedMemoryHuman(String usedMemoryHuman) {
      this.usedMemoryHuman = usedMemoryHuman;
    }

    public String getUsedMemoryPeakHuman() {
      return usedMemoryPeakHuman;
    }

    public void setUsedMemoryPeakHuman(String usedMemoryPeakHuman) {
      this.usedMemoryPeakHuman = usedMemoryPeakHuman;
    }

    public String getTotalSystemMemoryHuman() {
      return totalSystemMemoryHuman;
    }

    public void setTotalSystemMemoryHuman(String totalSystemMemoryHuman) {
      this.totalSystemMemoryHuman = totalSystemMemoryHuman;
    }
  }

  static public class StatsInfo {

    private String connectedClients;
    private String totalCommandsProcessed;
    private String totalConnections;
    private String keyHits;
    private String keyMisses;

    public String getConnectedClients() {
      return connectedClients;
    }

    public void setConnectedClients(String connectedClients) {
      this.connectedClients = connectedClients;
    }

    public String getTotalCommandsProcessed() {
      return totalCommandsProcessed;
    }

    public void setTotalCommandsProcessed(String totalCommandsProcessed) {
      this.totalCommandsProcessed = totalCommandsProcessed;
    }

    public String getTotalConnections() {
      return totalConnections;
    }

    public void setTotalConnections(String totalConnections) {
      this.totalConnections = totalConnections;
    }

    public String getKeyHits() {
      return keyHits;
    }

    public void setKeyHits(String keyHits) {
      this.keyHits = keyHits;
    }

    public String getKeyMisses() {
      return keyMisses;
    }

    public void setKeyMisses(String keyMisses) {
      this.keyMisses = keyMisses;
    }
  }

  static public class ServerInfo {

    private String redisVersion;
    private String os;
    private int processId;

    public String getRedisVersion() {
      return redisVersion;
    }

    public void setRedisVersion(String redisVersion) {
      this.redisVersion = redisVersion;
    }

    public String getOs() {
      return os;
    }

    public void setOs(String os) {
      this.os = os;
    }

    public int getProcessId() {
      return processId;
    }

    public void setProcessId(int processId) {
      this.processId = processId;
    }
  }

  static public class Keyspace implements Comparable<Keyspace> {

    private static final int MAX_INDEX = 15;
    private int index;
    private int keyCount = 0;
    private String keys;
    private String expires;
    private int expiresCount;
    private String avgTtl;
    private int avgTtlCount;

    public Keyspace() {
    }

    public Keyspace(int index) {
      this.index = index;
    }

    public Keyspace(int index, String keys) {
      this.index = index;
      this.keys = keys;
      parseKeyCount();
    }

    public void setIndex(int index) {
      this.index = index;
    }

    public void setAvgTtl(String avgTtl) {
      this.avgTtl = avgTtl;
      this.avgTtlCount = Integer.parseInt(avgTtl.replace("avg_ttl=", ""));
    }

    public void setExpires(String expires) {
      this.expires = expires;
      this.expiresCount = Integer.parseInt(expires.replace("expires=", ""));
    }

    public void setKeys(String keys) {
      this.keys = keys;
      parseKeyCount();
    }

    public String getName() {
      return "DB" + index;
    }

    public int getIndex() {
      return this.index;
    }

    public String getKeys() {
      return keys;
    }

    public void parseKeyCount() {
      if (keys != null) {
        this.keyCount = Integer.parseInt(keys.replace("keys=", ""));
      }
    }

    public String toString() {
      if (keys == null) {
        return getName();
      }
      return getName() + "[" + keys + "]";
    }

    public int getKeyCount() {
      if (keys == null) {
        return 0;
      }
      return Integer.parseInt(keys.replace("keys=", ""));
    }

    public int getExpiresCount() {
      return this.expiresCount;
    }

    public int getAvgTtlCount() {
      return this.avgTtlCount;
    }

    @Override
    public int compareTo(Keyspace keyspace) {
      if (keyspace == null) {
        return 0;
      }
      return this.index > keyspace.index ? 1 : -1;
    }
  }
}
