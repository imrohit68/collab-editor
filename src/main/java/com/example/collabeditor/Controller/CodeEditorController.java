package com.example.collabeditor.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class CodeEditorController {
    private final ConcurrentHashMap<String, List<String>> roomCodeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> roomDrawingMap = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger("CodeEditorController");

    @MessageMapping("/edit/{roomId}")
    @SendTo("/topic/updates/{roomId}")
    public String handleCodeEdit(@Payload String update, @DestinationVariable String roomId) {
        roomCodeMap.get(roomId).add(update);
        return update;
    }
    @GetMapping("/codeHistory/{roomId}")
    public ResponseEntity<List<String>> getCodeHistory(@PathVariable String roomId) {
        List<String> code = roomCodeMap.get(roomId);
        return ResponseEntity.ok(code);
    }
    @MessageMapping("/draw/{roomId}")
    @SendTo("/topic/drawUpdates/{roomId}")
    public String handleDrawingUpdate(@Payload String update, @DestinationVariable String roomId) {
        roomDrawingMap.get(roomId).add(update);
        return update;
    }

    @GetMapping("/drawingHistory/{roomId}")
    public ResponseEntity<List<String>> getDrawingHistory(@PathVariable String roomId) {
        List<String> drawingActions = roomDrawingMap.get(roomId);
        return ResponseEntity.ok(drawingActions);
    }
    @MessageMapping("/signal/{roomId}")
    @SendTo("/topic/signal/{roomId}")
    public String handleSignal(@Payload String signal, @DestinationVariable String roomId) {
        logger.info("Received signal: " + signal);
        return signal;
    }
    @GetMapping("/roomExists/{roodId}")
    public ResponseEntity<Boolean> checkRoom(@PathVariable String roodId){
        if(roomCodeMap.containsKey(roodId)) return ResponseEntity.ok(true);
        else return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
    }
    @GetMapping("/createRoom")
    public RedirectView createRoom() {
        String roomId = UUID.randomUUID().toString();
        roomCodeMap.put(roomId,new ArrayList<>());
        roomDrawingMap.put(roomId,new ArrayList<>());
        return new RedirectView("/room/" + roomId);
    }

    @GetMapping("/room/{roomId}")
    public RedirectView getEditorRoom(@PathVariable String roomId ) {
        return new RedirectView("/editor.html?roomId=" + roomId);
    }
}
