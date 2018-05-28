package com.microdev.param;

import com.microdev.model.Message;
import lombok.Data;

import java.util.List;

@Data
public class MessageShowDTO {

    private int systemNum;

    private int hrNum;

    private int companyNum;

    private int workerNum;

    private List<Message> systemList;

    private List<Message> hrList;

    private List<Message> companyList;

    private List<Message> workerList;
}
