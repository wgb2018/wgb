package com.microdev.param;

import lombok.Data;

@Data
public class AgentAccountResponse {

    private String pid;
    private String name;
    private String password;
    private String powerName;
    private Integer level;
    private String identifer;
    private String parentIdentifer;
}
