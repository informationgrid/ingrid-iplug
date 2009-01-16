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
	private static final Logger LOG = Logger
			.getLogger(MessageHandlerCache.class);

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
		String cacheKey = message.toString();
		Message ret = null;
		if (cacheKey.indexOf("cache:on") > -1) {
			Element element = getFromCache(cacheKey);
			ret = element != null ? (Message) element.getValue() : null;
		}
		if (ret == null) {
			ret = _messageHandler.handleMessage(message);
			_cache.put(new Element(cacheKey, ret));
		} 
		return ret;
	}

	private Element getFromCache(String cacheKey) {
		Element element = null;
		try {
			element = _cache.get(cacheKey);
			if (LOG.isDebugEnabled()) {
				LOG.debug("found element in cache, with cacheKey: " + cacheKey);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return element;
	}

}
