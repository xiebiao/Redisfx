package com.xiebiao.tools.redisfx.model;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Bill Xie
 * @since 2025/8/14 16:06
 **/
@Getter
public class RedisInfo {
    private final String info;
    private ServerInfo serverInfo;
    private static Set<Integer> defaultKeyspaceIndex = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
    private Iterable<String> lines;
    private List<Keyspace> keyspaces;
    private MemoryInfo memoryInfo;
    private StatsInfo statsInfo;


    public RedisInfo(String info) {
        this.info = info;
        parseRedisInfo();
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
                Keyspace keyspace = new Keyspace(item, null);
                newKeyspaces.add(keyspace);
            });
            return newKeyspaces;
        }

        Set<Integer> indexSet = this.keyspaces.stream().map(Keyspace::getIndex).collect(Collectors.toSet());
        Set<Integer> difference = Sets.difference(defaultKeyspaceIndex, indexSet);
        newKeyspaces.addAll(this.keyspaces);
        difference.forEach(item -> {
            Keyspace keyspace = new Keyspace(item, null);
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
                keyspaces.add(keyspace);
            }
        }
        return keyspaces;
    }

    @Data
    static public class MemoryInfo {
        private String usedMemoryHuman;
        private String usedMemoryPeakHuman;
    }

    @Data
    static public class StatsInfo {
        private String connectedClients;
        private String totalCommandsProcessed;
        private String totalConnections;
    }

    @Data
    static public class ServerInfo {
        private String redisVersion;
        private String os;
        private int processId;
    }

    @Data
    static public class Keyspace implements Comparable<Keyspace> {
        private static final int MAX_INDEX = 15;
        private int index;
        private String keys;

        public Keyspace() {
        }

        public Keyspace(int index, String keys) {
            this.index = index;
            this.keys = keys;
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

        public String toString() {
            if (keys == null) {
                return getName();
            }
            return getName() + "[" + keys + "]";
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
