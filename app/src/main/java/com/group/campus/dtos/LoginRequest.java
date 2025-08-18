package com.group.campus.dtos;



public class LoginRequest {
    private String regNo;

    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String regNo, String password) {
        this.regNo = regNo;
        this.password = password;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
