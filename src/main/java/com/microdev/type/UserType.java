package com.microdev.type;



/**
 * @author liutf
 */
public enum UserType {
    platform("0"), worker("1"), hotel("2"), hr("3"), agant("4");

    String code;

    UserType(String code) {
        this.code = code;
    }
}
