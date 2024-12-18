package com.example.collabeditor.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

import java.util.UUID;

@Controller
public class PageController {

    @GetMapping("/")
    public String getHomePage() {
        return "index.html"; // Serve the main page to create and join rooms
    }

    @GetMapping("/createRoom")
    public RedirectView createRoom() {
        // Generate a unique room ID
        String roomId = UUID.randomUUID().toString();
        // Redirect to the room editor page with the generated room ID
        return new RedirectView("/room/" + roomId);
    }

    @GetMapping("/room/{roomId}")
    public RedirectView getEditorRoom(@PathVariable String roomId ) {
        // Redirect to editor.html with the roomId as a query parameter
        return new RedirectView("/editor.html?roomId=" + roomId);
    }

}
