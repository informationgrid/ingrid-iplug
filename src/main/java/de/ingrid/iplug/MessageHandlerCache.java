/*
 * **************************************************-
 * ingrid-iplug
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.weta.components.communication.messaging.IMessageHandler;
import net.weta.components.communication.messaging.Message;
import net.weta.components.communication.reflect.ReflectMessage;

import org.apache.log4j.Logger;

public class MessageHandlerCache implements IMessageHandler {

    private final IMessageHandler _messageHandler;

    private CacheManager _cacheManager = null;

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
            Cache cache = getCache();
            if (cache != null)
                cache.put(new Element(cacheKey, ret));
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
            Cache cache = getCache();
            if (cache != null)
                cache.put(new Element(cacheKey, ret));
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
    	
        return _cacheManager.getCache(CacheService.INGRID_CACHE);
    }

    private Element getFromCache(int cacheKey) {
        Element element = null;
        try {
            Cache cache = getCache();
            if (cache == null) return null;
            element = cache.get(cacheKey);
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
