/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.md87.charliebravo;

import com.dmdirc.util.ConfigFile;
import java.util.Map;
import uk.co.md87.evetool.api.io.ApiCache;
import uk.co.md87.evetool.api.io.Downloader;

/**
 *
 * @author chris
 */
public class ConfigCache implements ApiCache {
    
    private static final String DOMAIN = ".eve-cache";

    private final ConfigFile config;

    public ConfigCache(ConfigFile config) {
        this.config = config;
    }

    public void setCache(String method, Map<String, String> args, String data, long cacheUntil) {
        final String url = method + "?" + Downloader.encodeArguments(args);
        config.getKeyDomain(DOMAIN).put(url, cacheUntil + ";"
                + System.currentTimeMillis() + ";" + data);
    }

    public CacheStatus getCacheStatus(String method, Map<String, String> args) {
        final String url = method + "?" + Downloader.encodeArguments(args);

        if (config.getKeyDomain(DOMAIN).containsKey(url)) {
            final String[] data = config.getKeyDomain(DOMAIN).get(url).split(";", 3);
            final long expirary = Long.valueOf(data[0]);
            return expirary < System.currentTimeMillis() ? CacheStatus.EXPIRED : CacheStatus.HIT;
        } else {
            return CacheStatus.MISS;
        }
    }

    public CacheResult getCache(String method, Map<String, String> args) {
        final String url = method + "?" + Downloader.encodeArguments(args);
        final String[] data = config.getKeyDomain(DOMAIN).get(url).split(";", 3);
        final long expirary = Long.valueOf(data[0]);
        final long cachedAt = Long.valueOf(data[1]);

        return new CacheResult(data[2], cachedAt, expirary);
    }

}
