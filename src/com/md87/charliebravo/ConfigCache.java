/*
 * Copyright (c) 2009-2010 Chris Smith
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
