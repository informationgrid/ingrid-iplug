package de.ingrid.iplug;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import net.weta.components.communication.messaging.IMessageHandler;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.reflect.ReflectMessage;

import org.apache.log4j.Logger;

public class MessageHandlerCache implements IMessageHandler {

    private final IMessageHandler _messageHandler;

    private CacheManager _cacheManager = null;

    private Cache _cache = null;

    private static final Logger LOG = Logger.getLogger(MessageHandlerCache.class);

    private static final int CACHE_OFF = 0;

    private static final int CACHE_ON = 1;

    public MessageHandlerCache(IMessageHandler messageHandler) throws Exception {
        _messageHandler = messageHandler;
    }

    @Override
    public Message handleMessage(Message message) {
        long start = System.currentTimeMillis();
        int cacheKey = message.hashCode();
        if (message instanceof ReflectMessage) {
            cacheKey = ((ReflectMessage) message).hashCode();
        }
        Message ret = null;
        int status = message.toString().indexOf("cache: false") > -1 ? CACHE_OFF : CACHE_ON;
        switch (status) {
        case CACHE_OFF:
            if (LOG.isDebugEnabled()) {
                LOG.debug("cache option is turned off. searching started...");
            }
            ret = _messageHandler.handleMessage(message);
            getCache().put(new Element(cacheKey, ret));
            break;
        case CACHE_ON:
            if (LOG.isDebugEnabled()) {
                LOG.debug("cache option is turned on. search element in cache...");
            }
            Element element = getFromCache(cacheKey);
            if (element != null) {
                ret = (Message) element.getValue();
                // set new id
                ret.setId(message.getId());
            }
            break;
        }

        if (ret == null) {
            // not found in cache
            ret = _messageHandler.handleMessage(message);
            getCache().put(new Element(cacheKey, ret));
        }
        long end = System.currentTimeMillis();
        if(LOG.isDebugEnabled()) {
            LOG.debug("time to handle message: " + (end - start) + " ms.");
        }
        
        return ret;
    }

    private Cache getCache() {
    	if (_cacheManager == null) {
        	_cacheManager = CacheManager.getInstance();    		
    	}
    	
    	if (_cache == null ||
    		_cache.getStatus() != Status.STATUS_ALIVE) {

    		_cache = _cacheManager.getCache(CacheService.INGRID_CACHE);
            if (_cache == null) {
            	// Use CacheService.DEFAULT_CACHE ???
            	// Guess CacheService was never really coordinated with this MessageHandlerCache or vice versa ...
                if (!_cacheManager.cacheExists("default")) {
                    _cache = new Cache("default", 1000, false, false, 600, 600);
                    _cacheManager.addCache(_cache);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("set up cache: new Cache(\"default\", 1000, false, false, 600, 600)");
                    }            	
                } else {
                    _cache = _cacheManager.getCache("default");
                    if (LOG.isInfoEnabled()) {
                        LOG.info("set up cache: cacheManager.getCache(\"default\")");
                    }            	
                }
            } else {
                if (LOG.isInfoEnabled()) {
                    LOG.info("set up cache: cacheManager.getCache(\"ingrid-cache\")");
                }            	
            }
    	}
        
        return _cache;
    }

    private Element getFromCache(int cacheKey) {
        Element element = null;
        try {
            element = getCache().get(cacheKey);
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
