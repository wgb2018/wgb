package com.microdev.type;

public enum ConstantData {

 
	CATALOG("protocol/"), WORKHRPROTOCOL("/home/micro-worker/wgb/static/11.html"), HRHOTELPROTOCOL("/home/micro-worker/wgb/static/12.html"),TEST("c:/test/12.html"),WORKERTEST("c:/test/11.html");

    String name;

    public String getName() {
        return name;
    }

    private ConstantData(String name) {
        this.name = name;
    }
}
