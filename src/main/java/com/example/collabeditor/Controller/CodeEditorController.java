package com.example.collabeditor.Controller;

import com.example.collabeditor.Models.CodeRoom;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class CodeEditorController {
    private final ConcurrentHashMap<String, List<String>> roomCodeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> roomDrawingMap = new ConcurrentHashMap<>();

    @MessageMapping("/edit/{roomId}")
    @SendTo("/topic/updates/{roomId}")
    public String handleCodeEdit(@Payload String update, @DestinationVariable String roomId) {
        if(roomCodeMap.containsKey(roomId)) roomCodeMap.get(roomId).add(update);
        else {
            List<String> code = new ArrayList<>();
            code.add(update);
            roomCodeMap.put(roomId,code);
        }
        return update;
    }
    @GetMapping("/codeHistory/{roomId}")
    public ResponseEntity<List<String>> getCodeHistory(@PathVariable String roomId) {
        if(roomCodeMap.containsKey(roomId)){
            List<String> code = roomCodeMap.get(roomId);
            return ResponseEntity.ok(code);
        }else return ResponseEntity.noContent().build();

    }
    @MessageMapping("/draw/{roomId}")
    @SendTo("/topic/drawUpdates/{roomId}")
    public String handleDrawingUpdate(@Payload String update, @DestinationVariable String roomId) {
        if (roomDrawingMap.containsKey(roomId)) {
            roomDrawingMap.get(roomId).add(update);
        } else {
            List<String> code = new ArrayList<>();
            List<String> drawingActions = new ArrayList<>();
            drawingActions.add(update);
            roomDrawingMap.put(roomId,drawingActions);
        }
        return update;
    }

    @GetMapping("/drawingHistory/{roomId}")
    public ResponseEntity<List<String>> getDrawingHistory(@PathVariable String roomId) {
        if (roomDrawingMap.containsKey(roomId)) {
            List<String> drawingActions = roomDrawingMap.get(roomId);
            return ResponseEntity.ok(drawingActions);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    @GetMapping("/roomExists/{roodId}")
    public ResponseEntity<Boolean> checkRoom(@PathVariable String roodId){
        if(roomCodeMap.containsKey(roodId)) return ResponseEntity.ok(true);
        else return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }
}
