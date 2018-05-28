package com.microdev.common.utils;

import com.microdev.model.Dict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

@Component
public class RedisUtil {

	@Autowired
	private RedisTemplate redisTemplate;
	
	public void setString(String key, String value) {
		ValueOperations<Serializable, String> operation = redisTemplate.opsForValue();
		operation.set(key, value);
	}
	
	public String getString(String key) {
		ValueOperations<Serializable, String> operation = redisTemplate.opsForValue();
		return operation.get(key);
	}
	
	public void setHashInfo(String key, List<Dict> list) {
		HashOperations<String, String, Dict> operation = redisTemplate.opsForHash();
		Iterator<Dict> it = list.iterator();
		Dict d = null;
		while (it.hasNext()) {
			d = it.next();
			operation.put(key, d.getName() + "$" + d.getCode(), d);
		}
	}
	
	public Object getHashInfo(String key, String field) {
		HashOperations<String, String, Dict> operation = redisTemplate.opsForHash();
		if (StringUtils.isEmpty(field)) {
			return operation.values(key);
		} else {
			return operation.get(key, field);
		}
	}
	
	public void setHashInfo(String key, Dict d) {
		HashOperations<String, String, Dict> operation = redisTemplate.opsForHash();
		operation.put(key, d.getName() + "$" + d.getCode(), d);
	}
	
	public void removeHashInfo(String key, String field) {
		HashOperations<String, String, Dict> operation = redisTemplate.opsForHash();
		operation.delete(key, field);
	}
}
