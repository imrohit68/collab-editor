package com.example.collabeditor.Controller;

import com.example.collabeditor.Models.CodeRoom;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.concurrent.ConcurrentHashMap;

@Controller

public class CodeEditorController {
    private final ConcurrentHashMap<String, String> roomCodeMap = new ConcurrentHashMap<>();

    @MessageMapping("/edit/{roomId}")
    @SendTo("/topic/updates/{roomId}")
    public CodeRoom handleCodeEdit(@Payload CodeRoom update, @DestinationVariable String roomId) {
        roomCodeMap.put(roomId, update.getCode());
        return update;
    }

    @MessageMapping("/join/{roomId}")
    @SendTo("/topic/updates/{roomId}")
    public CodeRoom handleRoomJoin(@DestinationVariable String roomId) {
        return new CodeRoom(roomCodeMap.get(roomId));
    }
}
