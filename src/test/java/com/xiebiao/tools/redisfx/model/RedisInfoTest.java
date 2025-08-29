package com.xiebiao.tools.redisfx.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Bill Xie
 * @since 2025/8/14 16:17
 **/
public class RedisInfoTest {
    private static final String redisInfoText = """
            # Server
            redis_version:7.0.15
            redis_git_sha1:00000000
            redis_git_dirty:0
            redis_build_id:3ec7bf4ec5bfafb8
            redis_mode:standalone
            os:Linux 6.8.0-51-generic x86_64
            arch_bits:64
            monotonic_clock:POSIX clock_gettime
            multiplexing_api:epoll
            atomicvar_api:c11-builtin
            gcc_version:13.3.0
            process_id:954
            process_supervised:systemd
            run_id:7ad2168d71b51fd1a07a1782b69dba67068eeb94
            tcp_port:6379
            server_time_usec:1755161198467108
            uptime_in_seconds:930851
            uptime_in_days:10
            hz:10
            configured_hz:10
            lru_clock:10330734
            executable:/usr/bin/redis-server
            config_file:/etc/redis/redis.conf
            io_threads_active:0
            
            # Clients
            connected_clients:5
            cluster_connections:0
            maxclients:10000
            client_recent_max_input_buffer:20480
            client_recent_max_output_buffer:0
            blocked_clients:0
            tracking_clients:0
            clients_in_timeout_table:0
            
            # Memory
            used_memory:1419592
            used_memory_human:1.35M
            used_memory_rss:14417920
            used_memory_rss_human:13.75M
            used_memory_peak:1428568
            used_memory_peak_human:1.36M
            used_memory_peak_perc:99.37%
            used_memory_overhead:966168
            used_memory_startup:876096
            used_memory_dataset:453424
            used_memory_dataset_perc:83.43%
            allocator_allocated:1889360
            allocator_active:2314240
            allocator_resident:5287936
            total_system_memory:7623856128
            total_system_memory_human:7.10G
            used_memory_lua:31744
            used_memory_vm_eval:31744
            used_memory_lua_human:31.00K
            used_memory_scripts_eval:0
            number_of_cached_scripts:0
            number_of_functions:0
            number_of_libraries:0
            used_memory_vm_functions:32768
            used_memory_vm_total:64512
            used_memory_vm_total_human:63.00K
            used_memory_functions:200
            used_memory_scripts:200
            used_memory_scripts_human:200B
            maxmemory:0
            maxmemory_human:0B
            maxmemory_policy:noeviction
            allocator_frag_ratio:1.22
            allocator_frag_bytes:424880
            allocator_rss_ratio:2.28
            allocator_rss_bytes:2973696
            rss_overhead_ratio:2.73
            rss_overhead_bytes:9129984
            mem_fragmentation_ratio:10.17
            mem_fragmentation_bytes:13000160
            mem_not_counted_for_evict:0
            mem_replication_backlog:0
            mem_total_replication_buffers:0
            mem_clients_slaves:0
            mem_clients_normal:85776
            mem_cluster_links:0
            mem_aof_buffer:0
            mem_allocator:jemalloc-5.3.0
            active_defrag_running:0
            lazyfree_pending_objects:0
            lazyfreed_objects:0
            
            # Persistence
            loading:0
            async_loading:0
            current_cow_peak:0
            current_cow_size:0
            current_cow_size_age:0
            current_fork_perc:0.00
            current_save_keys_processed:0
            current_save_keys_total:0
            rdb_changes_since_last_save:0
            rdb_bgsave_in_progress:0
            rdb_last_save_time:1755078483
            rdb_last_bgsave_status:ok
            rdb_last_bgsave_time_sec:0
            rdb_current_bgsave_time_sec:-1
            rdb_saves:41
            rdb_last_cow_size:782336
            rdb_last_load_keys_expired:0
            rdb_last_load_keys_loaded:24
            aof_enabled:0
            aof_rewrite_in_progress:0
            aof_rewrite_scheduled:0
            aof_last_rewrite_time_sec:-1
            aof_current_rewrite_time_sec:-1
            aof_last_bgrewrite_status:ok
            aof_rewrites:0
            aof_rewrites_consecutive_failures:0
            aof_last_write_status:ok
            aof_last_cow_size:0
            module_fork_in_progress:0
            module_fork_last_cow_size:0
            
            # Stats
            total_connections_received:465238
            total_commands_processed:15483
            instantaneous_ops_per_sec:1
            total_net_input_bytes:14150564
            total_net_output_bytes:16803795
            total_net_repl_input_bytes:0
            total_net_repl_output_bytes:0
            instantaneous_input_kbps:0.21
            instantaneous_output_kbps:0.16
            instantaneous_input_repl_kbps:0.00
            instantaneous_output_repl_kbps:0.00
            rejected_connections:0
            sync_full:0
            sync_partial_ok:0
            sync_partial_err:0
            expired_keys:134
            expired_stale_perc:0.00
            expired_time_cap_reached_count:0
            expire_cycle_cpu_milliseconds:33027
            evicted_keys:0
            evicted_clients:0
            total_eviction_exceeded_time:0
            current_eviction_exceeded_time:0
            keyspace_hits:9941
            keyspace_misses:214
            pubsub_channels:0
            pubsub_patterns:0
            pubsubshard_channels:0
            latest_fork_usec:425
            total_forks:41
            migrate_cached_sockets:0
            slave_expires_tracked_keys:0
            active_defrag_hits:0
            active_defrag_misses:0
            active_defrag_key_hits:0
            active_defrag_key_misses:0
            total_active_defrag_time:0
            current_active_defrag_time:0
            tracking_total_keys:0
            tracking_total_items:0
            tracking_total_prefixes:0
            unexpected_error_replies:0
            total_error_replies:465401
            dump_payload_sanitizations:0
            total_reads_processed:945897
            total_writes_processed:480500
            io_threaded_reads_processed:0
            io_threaded_writes_processed:0
            reply_buffer_shrinks:331
            reply_buffer_expands:44
            
            # Replication
            role:master
            connected_slaves:0
            master_failover_state:no-failover
            master_replid:16b7e917a71d446456981397dbcaa489ae891de0
            master_replid2:0000000000000000000000000000000000000000
            master_repl_offset:0
            second_repl_offset:-1
            repl_backlog_active:0
            repl_backlog_size:1048576
            repl_backlog_first_byte_offset:0
            repl_backlog_histlen:0
            
            # CPU
            used_cpu_sys:662.453104
            used_cpu_user:741.032645
            used_cpu_sys_children:0.080800
            used_cpu_user_children:0.036100
            used_cpu_sys_main_thread:662.437471
            used_cpu_user_main_thread:741.008926
            
            # Modules
            
            # Errorstats
            errorstat_ERR:count=242
            errorstat_NOAUTH:count=464987
            errorstat_WRONGPASS:count=172
            
            # Cluster
            cluster_enabled:0
            
            # Keyspace
            db0:keys=1,expires=0,avg_ttl=0
            db1:keys=51,expires=40,avg_ttl=1957610124
            
            """;

    private String keyspacesText0 = """
             # Keyspace
            db0:keys=1,expires=0,avg_ttl=0
            db1:keys=51,expires=40,avg_ttl=1957610124
            # Keysizes
            db0_distrib_strings_sizes:0=1,4=2
            """;
    private String keyspacesText1 = """
             # Keyspace
            db8:keys=1,expires=0,avg_ttl=0
            db10:keys=51,expires=40,avg_ttl=1957610124
            # Keysizes
            db0_distrib_strings_sizes:0=1,4=2
            """;
    private String keyspacesText3 = """
             # Keyspace
            """;

    @Test
    public void test_redisInfo() {
        RedisInfo redisInfo = new RedisInfo(redisInfoText);
        Assertions.assertEquals("7.0.15", redisInfo.getServerInfo().getRedisVersion());
        Assertions.assertEquals("Linux 6.8.0-51-generic x86_64", redisInfo.getServerInfo().getOs());
        Assertions.assertEquals(954, redisInfo.getServerInfo().getProcessId());


    }

//    @Test
//    public void test_parse_keyspace0() {
//        RedisInfo redisInfo = new RedisInfo(keyspacesText0);
//        System.out.println(redisInfo.getServerInfo().getRedisVersion());
//
//    }

    @Test
    public void test_parse_keyspace3() {
        RedisInfo redisInfo = new RedisInfo(keyspacesText3);
        List<RedisInfo.Keyspace> keyspaces = redisInfo.getKeyspaces();
        Assertions.assertEquals(16, keyspaces.size());
    }

    @Test
    public void test_parse_keyspace1() {
        RedisInfo redisInfo = new RedisInfo(keyspacesText1);
        List<RedisInfo.Keyspace> keyspaces = redisInfo.getKeyspaces();
        Assertions.assertEquals(16, keyspaces.size());
        Assertions.assertEquals(0, keyspaces.getFirst().getIndex());
        Assertions.assertEquals("keys=1", keyspaces.get(8).getKeys());
        Assertions.assertEquals("keys=51", keyspaces.get(10).getKeys());
        Assertions.assertEquals(15, keyspaces.getLast().getIndex());
        keyspaces = redisInfo.getKeyspaces();
        System.out.println(keyspaces);

    }
}
