package com.example.collabeditor.Models;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class CodeRoom {
    private List<String> code;
    private List<String> drawingActions; // History of drawing actions

    public CodeRoom(List<String> code, List<String> drawingActions) {
        this.code = code;
        this.drawingActions = drawingActions;
    }

    public List<String> getCode() {
        return code;
    }

    public void setCode(List<String> code) {
        this.code = code;
    }

    public List<String> getDrawingActions() {
        return drawingActions;
    }

    public void setDrawingActions(List<String> drawingActions) {
        this.drawingActions = drawingActions;
    }

    public CodeRoom() {
        this.code = new ArrayList<>();
        this.drawingActions = new ArrayList<>();
    }
}

