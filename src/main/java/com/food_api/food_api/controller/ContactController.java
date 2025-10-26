package com.food_api.food_api.controller;

import com.food_api.food_api.entity.Contact;
import com.food_api.food_api.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> addContact(@RequestBody Contact newContact) {
        // Validate the input data
        if (newContact == null || newContact.getName() == null || newContact.getEmail() == null || newContact.getMessage() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid input. Please provide all required fields."
            ));
        }

        try {
            // Call the service to add the contact
            Contact savedContact = contactService.addContact(newContact);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Message sent successfully"
            ));
        } catch (Exception e) {
            // Handle errors during the process
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Server error"
            ));
        }
    }


}
