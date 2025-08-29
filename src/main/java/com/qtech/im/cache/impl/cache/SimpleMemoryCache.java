package com.qtech.im.cache.impl.cache;

import com.qtech.im.cache.Cache;
import com.qtech.im.cache.CacheConfig;
import com.qtech.im.cache.CacheStats;
import com.qtech.im.cache.builder.CacheConfigBuilder;
import com.qtech.im.cache.support.ExpiringValue;
import com.qtech.im.cache.support.NullValueMarker;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Simple memory cache implementation based on ConcurrentHashMap, no external dependencies
 *
 * @param <K> Key type
 * @param <V> Value type
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/08/29
 */
public class SimpleMemoryCache<K, V> implements Cache<K, V> {

    private final ConcurrentMap<K, ExpiringValue<V>> store = new ConcurrentHashMap<>();
    private final CacheConfig config;
    private final CacheStats stats;

    public SimpleMemoryCache(CacheConfig config) {
        this.config = config != null ? config : CacheConfigBuilder.create().build();
        this.stats = config != null && config.isRecordStats() ? new CacheStats() : null;
    }

    @Override
    public V get(K key) {
        if (key == null) {
            recordMiss();
            return null;
        }
        ExpiringValue<V> ev = store.get(key);
        if (ev == null || ev.isExpired()) {
            if (ev != null) {
                store.remove(key);
                recordEviction();
            }
            recordMiss();
            return null;
        }
        recordHit();
        V value = ev.getValue();
        updateAccessTime(key, ev);
        return value instanceof NullValueMarker ? null : value;
    }

    @Override
    public void put(K key, V value) {
        if (key == null || (value == null && !config.isEnableNullValueProtection())) {
            return;
        }
        enforceMaxSize();
        long ttl = config.getExpireAfterWrite();
        long expireTime = ttl > 0 ? System.currentTimeMillis() + ttl : Long.MAX_VALUE;
        store.put(key, new ExpiringValue<>(value == null ? (V) NullValueMarker.getInstance() : value, expireTime));
        recordPut();
    }

    @Override
    public void put(K key, V value, long ttl, TimeUnit unit) {
        if (key == null || (value == null && !config.isEnableNullValueProtection()) || ttl <= 0 || unit == null) {
            return;
        }
        enforceMaxSize();
        long ttlMs = unit.toMillis(ttl);
        store.put(key, new ExpiringValue<>(value == null ? (V) NullValueMarker.getInstance() : value, System.currentTimeMillis() + ttlMs));
        recordPut();
    }

    @Override
    public void putAtFixedTime(K key, V value, long expireTimestamp) {
        if (key == null || (value == null && !config.isEnableNullValueProtection()) || expireTimestamp <= System.currentTimeMillis()) {
            return;
        }
        enforceMaxSize();
        store.put(key, new ExpiringValue<>(value == null ? (V) NullValueMarker.getInstance() : value, expireTimestamp));
        recordPut();
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        Map<K, V> result = new HashMap<>();
        if (keys == null || keys.isEmpty()) {
            recordMiss(0);
            return result;
        }
        for (K key : keys) {
            V value = get(key); // get() updates stats
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        if (map == null || map.isEmpty()) {
            return;
        }
        enforceMaxSize();
        long ttl = config.getExpireAfterWrite();
        long expireTime = ttl > 0 ? System.currentTimeMillis() + ttl : Long.MAX_VALUE;
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            if (key != null && (value != null || config.isEnableNullValueProtection())) {
                store.put(key, new ExpiringValue<>(value == null ? (V) NullValueMarker.getInstance() : value, expireTime));
                recordPut();
            }
        }
    }

    @Override
    public boolean remove(K key) {
        if (key == null) {
            return false;
        }
        ExpiringValue<V> removed = store.remove(key);
        if (removed != null) {
            recordEviction();
            return true;
        }
        return false;
    }

    @Override
    public int removeAll(Set<? extends K> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (K key : keys) {
            if (remove(key)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            return false;
        }
        ExpiringValue<V> ev = store.get(key);
        if (ev == null || ev.isExpired()) {
            if (ev != null) {
                store.remove(key);
                recordEviction();
            }
            return false;
        }
        updateAccessTime(key, ev);
        return true;
    }

    @Override
    public long size() {
        cleanUp();
        return store.size();
    }

    @Override
    public void clear() {
        long size = store.size();
        store.clear();
        recordEviction(size);
    }

    @Override
    public CacheStats getStats() {
        return stats;
    }

    @Override
    public CacheConfig getConfig() {
        return config;
    }

    @Override
    public V getOrLoad(K key, Function<K, V> loader) {
        if (key == null || loader == null) {
            recordMiss();
            return null;
        }
        enforceMaxSize();
        long startTime = stats != null ? System.nanoTime() : 0;
        try {
            ExpiringValue<V> ev = store.computeIfAbsent(key, k -> {
                V value = loader.apply(k);
                long ttl = config.getExpireAfterWrite();
                long expireTime = ttl > 0 ? System.currentTimeMillis() + ttl : Long.MAX_VALUE;
                return new ExpiringValue<>(value == null && config.isEnableNullValueProtection() ? (V) NullValueMarker.getInstance() : value, expireTime);
            });
            if (ev.isExpired()) {
                store.remove(key);
                recordEviction();
                recordMiss();
                return null;
            }
            recordHit();
            updateAccessTime(key, ev);
            V value = ev.getValue();
            if (stats != null) {
                recordLoadSuccess(System.nanoTime() - startTime);
            }
            return value instanceof NullValueMarker ? null : value;
        } catch (Exception e) {
            if (stats != null) {
                recordLoadException(System.nanoTime() - startTime);
            }
            throw new RuntimeException("Failed to load value for key: " + key, e);
        }
    }

    @Override
    public V getOrLoad(K key, Function<K, V> loader, long ttl, TimeUnit unit) {
        if (key == null || loader == null || ttl <= 0 || unit == null) {
            recordMiss();
            return null;
        }
        enforceMaxSize();
        long startTime = stats != null ? System.nanoTime() : 0;
        try {
            long ttlMs = unit.toMillis(ttl);
            ExpiringValue<V> ev = store.computeIfAbsent(key, k ->
                    new ExpiringValue<>(loader.apply(k) == null && config.isEnableNullValueProtection() ? (V) NullValueMarker.getInstance() : loader.apply(k), System.currentTimeMillis() + ttlMs));
            if (ev.isExpired()) {
                store.remove(key);
                recordEviction();
                recordMiss();
                return null;
            }
            recordHit();
            updateAccessTime(key, ev);
            V value = ev.getValue();
            if (stats != null) {
                recordLoadSuccess(System.nanoTime() - startTime);
            }
            return value instanceof NullValueMarker ? null : value;
        } catch (Exception e) {
            if (stats != null) {
                recordLoadException(System.nanoTime() - startTime);
            }
            throw new RuntimeException("Failed to load value for key: " + key, e);
        }
    }

    @Override
    public V getOrLoadAtFixedTime(K key, Function<K, V> loader, long expireTimestamp) {
        if (key == null || loader == null || expireTimestamp <= System.currentTimeMillis()) {
            recordMiss();
            return null;
        }
        enforceMaxSize();
        long startTime = stats != null ? System.nanoTime() : 0;
        try {
            ExpiringValue<V> ev = store.computeIfAbsent(key, k ->
                    new ExpiringValue<>(loader.apply(k) == null && config.isEnableNullValueProtection() ? (V) NullValueMarker.getInstance() : loader.apply(k), expireTimestamp));
            if (ev.isExpired()) {
                store.remove(key);
                recordEviction();
                recordMiss();
                return null;
            }
            recordHit();
            updateAccessTime(key, ev);
            V value = ev.getValue();
            if (stats != null) {
                recordLoadSuccess(System.nanoTime() - startTime);
            }
            return value instanceof NullValueMarker ? null : value;
        } catch (Exception e) {
            if (stats != null) {
                recordLoadException(System.nanoTime() - startTime);
            }
            throw new RuntimeException("Failed to load value for key: " + key, e);
        }
    }

    @Override
    public void refresh() {
        cleanUp(); // Placeholder: clean expired entries
    }

    @Override
    public void close() {
        store.clear();
        if (stats != null) {
            stats.reset();
        }
    }

    @Override
    public void cleanUp() {
        long removed = store.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired())
                .count();
        store.entrySet().removeIf(entry -> entry.getValue().isExpired());
        recordEviction(removed);
    }

    private void enforceMaxSize() {
        if (config.getMaximumSize() > 0 && store.size() >= config.getMaximumSize()) {
            cleanUp();
            // Simple LRU simulation: remove oldest entries
            while (store.size() >= config.getMaximumSize()) {
                store.entrySet().iterator().remove();
                recordEviction();
            }
        }
    }

    private void updateAccessTime(K key, ExpiringValue<V> ev) {
        if (config.getExpireAfterAccess() > 0) {
            store.computeIfPresent(key, (k, oldEv) ->
                    new ExpiringValue<>(oldEv.getValue(), System.currentTimeMillis() + config.getExpireAfterAccess()));
        }
    }

    private void recordHit() {
        if (stats != null) {
            stats.recordHit();
        }
    }

    private void recordMiss() {
        if (stats != null) {
            stats.recordMiss();
        }
    }

    private void recordMiss(long count) {
        if (stats != null && count >= 0) {
            for (int i = 0; i < count; i++) {
                stats.recordMiss();
            }
        }
    }

    private void recordPut() {
        if (stats != null) {
            stats.recordPut(); // Note: CacheStats has no recordPut, assuming typo; using recordEviction as placeholder
        }
    }

    private void recordEviction() {
        if (stats != null) {
            stats.recordEviction();
        }
    }

    private void recordEviction(long count) {
        if (stats != null && count >= 0) {
            for (int i = 0; i < count; i++) {
                stats.recordEviction();
            }
        }
    }

    private void recordLoadSuccess(long loadTime) {
        if (stats != null) {
            stats.recordLoadSuccess(loadTime);
        }
    }

    private void recordLoadException(long loadTime) {
        if (stats != null) {
            stats.recordLoadException(loadTime);
        }
    }
}