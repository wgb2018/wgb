package com.microdev.type;

/**
 * @author liutf
 */
public enum UserSex {
    MALE("男", "MALE"), FEMALE("女", "FEMALE"), UNKNOW("未知", "UNKNOW");

    String text;
    String dbtext;

    UserSex(String text, String dbtext) {
        this.text = text;
        this.dbtext = dbtext;
    }

    @Override
    public String toString() {
        return text;
    }

    public String toDBString() {
        return dbtext;
    }
}
