package de.ingrid.iplug;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.weta.components.communication.messaging.IMessageHandler;
import net.weta.components.communication.messaging.Message;

import org.apache.log4j.Logger;

public class MessageHandlerCache implements IMessageHandler {

    private final IMessageHandler _messageHandler;

    private CacheManager _cacheManager;

    private Cache _cache;

    private static final Logger LOG = Logger.getLogger(MessageHandlerCache.class);

    private static final int CACHE_OFF = 0;

    private static final int CACHE_ON = 1;

    public MessageHandlerCache(IMessageHandler messageHandler) throws Exception {
        _messageHandler = messageHandler;
        _cacheManager = CacheManager.getInstance();
        _cache = _cacheManager.getCache("ingrid-cache");
        if (_cache == null) {
            if (!_cacheManager.cacheExists("default")) {
                _cache = new Cache("default", 1000, false, false, 600, 600);
                _cacheManager.addCache(_cache);
            } else {
                _cache = _cacheManager.getCache("default");
            }
        }
    }

    @Override
    public Message handleMessage(Message message) {
        long start = System.currentTimeMillis();
        String cacheKey = message.toString();
        Message ret = null;
        int status = cacheKey.indexOf("cache: false") > -1 ? CACHE_OFF : CACHE_ON;
        switch (status) {
        case CACHE_OFF:
            if (LOG.isDebugEnabled()) {
                LOG.debug("cache option is turned off. searching started...");
            }
            ret = _messageHandler.handleMessage(message);
            _cache.put(new Element(cacheKey, ret));
            break;
        case CACHE_ON:
            if (LOG.isDebugEnabled()) {
                LOG.debug("cache option is turned on. search element in cache...");
            }
            Element element = getFromCache(cacheKey);
            ret = element != null ? (Message) element.getValue() : null;
            break;
        }

        if (ret == null) {
            // not found in cache
            ret = _messageHandler.handleMessage(message);
            _cache.put(new Element(cacheKey, ret));
        }
        long end = System.currentTimeMillis();
        if(LOG.isDebugEnabled()) {
            LOG.debug("time to handle message: " + (end - start) + " ms.");
        }
        
        return ret;
    }

    private Element getFromCache(String cacheKey) {
        Element element = null;
        try {
            element = _cache.get(cacheKey);
        } catch (Exception e) {
            LOG.error("error while searching in cache", e);
        }
        if (LOG.isDebugEnabled()) {
            if (element != null) {
                LOG.debug("found element in cache, with cacheKey: " + cacheKey);
            } else {
                LOG.debug("dont found element in cache, with cacheKey: " + cacheKey);
            }
        }
        return element;
    }

}
