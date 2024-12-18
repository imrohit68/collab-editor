package com.example.collabeditor.Models;

import lombok.Getter;

public class CodeRoom {
    private String code;

    public CodeRoom(String code) {
        this.code = code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public CodeRoom() {
    }

}
