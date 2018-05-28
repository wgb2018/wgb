package com.microdev.common.utils;

import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liutf
 */
public class StringKit {
    public static boolean isBlank(String str) {
        if (str == null || str.trim().equals("")) {
            return true;
        }

        return false;
    }

    public static String removeEnd(String str, String remove) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(remove)) {
            return str;
        }
        if (str.endsWith(remove)) {
            return str.substring(0, str.length() - remove.length());
        }
        return str;
    }

    public static String templateReplace(String tplStr, Map<String, String> data) {
        Matcher m = Pattern.compile("\\{([\\w\\.]*)\\}").matcher(tplStr);
        while (m.find()) {
            String group = m.group();
            group = group.replaceAll("\\{|\\}", "");
            String value = data.getOrDefault(group, "");
            tplStr = tplStr.replace(m.group(), value);
        }

        return tplStr;
    }

    /**
     * 生成指定长度的数字字符串，用于短信验证码
     */
    public static String numberStrGenerator(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append((int) Math.floor(Math.random() * 9)); //0-9
        }
        return builder.toString();
    }
}
