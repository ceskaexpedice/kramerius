package cz.incad.Kramerius.tags;

import static cz.incad.Kramerius.tags.CachedImportSupport.*;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;


import javax.servlet.jsp.JspTagException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class URLContentCache {

    private static URLContentCache _INSTANCE = null;

    public static final String CACHE_ALIAS = "urlContentJSTLTagCache";
    private final CacheManager cacheManager;
    private Cache<String, String> cache;


    private URLContentCache(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        this.init();
    }


    private void init() {
        cache = cacheManager.getCache(CACHE_ALIAS, String.class, String.class);
        if (cache == null) {
            cache = cacheManager.createCache(CACHE_ALIAS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                            ResourcePoolsBuilder.heap(1000).offheap(32, MemoryUnit.MB))
                            .withExpiry(Expirations.timeToLiveExpiration(
                                    Duration.of(KConfiguration.getInstance().getCacheTimeToLiveExpiration(), TimeUnit.SECONDS))).build());
        }

    }

    public static synchronized URLContentCache getInstance(CacheManager cacheManager) {
        if (_INSTANCE == null) {
            _INSTANCE = new URLContentCache(cacheManager);
        }
        return _INSTANCE;
    }

    public Reader getCachedContent(String target, String charEncoding) throws IOException, JspTagException {
        // check target cached
        List<String> solrCachedURLS = KConfiguration.getInstance().getSolrCachedURLS();
        if (solrCachedURLS.contains(target)) {
            String s = cache.get(target);
            if (s == null) {
                Reader r = getRawReader(target, charEncoding);
                s = IOUtils.toString(r);
                cache.put(target, s);
            }
            return new StringReader(s);
        } else {
            return getRawReader(target, charEncoding);
        }
    }


    Reader getRawReader(String target, String charEncoding) throws IOException, JspTagException {
        // handle absolute URLs ourselves, using java.net.URL
        URL u = new URL(target);
        URLConnection uc = u.openConnection();
        InputStream i = uc.getInputStream();

        // okay, we've got a stream; encode it appropriately
        Reader r = null;
        String charSet;
        if (charEncoding != null && !charEncoding.equals("")) {
            charSet = charEncoding;
        } else {
            // charSet extracted according to RFC 2045, section 5.1
            String contentType = uc.getContentType();
            if (contentType != null) {
                charSet = Util.getContentTypeAttribute(contentType, "charset");
                if (charSet == null) charSet = DEFAULT_ENCODING;
            } else {
                charSet = DEFAULT_ENCODING;
            }
        }
        try {
            r = new InputStreamReader(i, charSet);
        } catch (Exception ex) {
            r = new InputStreamReader(i, DEFAULT_ENCODING);
        }

        // check response code for HTTP URLs before returning, per spec,
        // before returning
        if (uc instanceof HttpURLConnection) {
            int status = ((HttpURLConnection) uc).getResponseCode();
            if (status < 200 || status > 299)
                throw new JspTagException(status + " " + target);
        }
        return r;
    }

}
