package com.group.campus.models;

import java.util.Date;

public class Messages {

    private String prompt, response;

    private Date createTime;

    public Messages() {
    }

    public Messages(String prompt, String response, Date createTime) {
        this.prompt = prompt;
        this.response = response;
        this.createTime = createTime;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
