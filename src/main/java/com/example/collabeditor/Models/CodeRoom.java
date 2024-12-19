package com.example.collabeditor.Models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class CodeRoom {
    private List<String> code;

    public CodeRoom(List<String> code) {
        this.code = code;
    }

    public void setCode(List<String> code) {
        this.code = code;
    }

    public List<String> getCode() {
        return code;
    }

    public CodeRoom() {
    }

}
