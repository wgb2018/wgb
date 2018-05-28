package com.microdev.common.context;

import java.util.HashMap;

/**
 * @author liutf
 */
public class User extends HashMap<String, Object> {
    public String getId() {
        return (String) get("id");
    }

    public User setId(String id) {
        put("id", id);
        return this;
    }

    public User set(String k, Object v) {
        put(k, v);
        return this;
    }

    public String getString(String key) {
        return (String) get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getObj(String key, Class<T> clazz) {
        return (T) get(key);
    }

    public static User me() {
        return new User();
    }

}
