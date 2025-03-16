package com.example.collabeditor.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.UUID;

@Controller
public class PageController {

    @GetMapping("/")
    public String getHomePage() {
        return "index.html"; // Serve the main page to create and join rooms
    }

    @GetMapping("/createRoom")
    public RedirectView createRoom() {
        String roomId = UUID.randomUUID().toString();
        CodeEditorController.roomCodeMap.put(roomId, new ArrayList<>());
        CodeEditorController.roomDrawingMap.put(roomId, new ArrayList<>());
        return new RedirectView("/room/" + roomId);
    }

    @GetMapping("/room/{roomId}")
    public RedirectView getEditorRoom(@PathVariable String roomId ) {
        return new RedirectView("/editor.html?roomId=" + roomId);
    }

}
