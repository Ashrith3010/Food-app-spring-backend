package com.food_api.food_api.service;

import com.food_api.food_api.entity.Contact;
import com.food_api.food_api.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public Contact addContact(Contact newContact) {
        // Set the creation time and default response status
        newContact.setCreatedAt(Instant.now());
        newContact.setResponded(false); // Default value

        // Save to the database and return the saved contact
        return contactRepository.save(newContact);
    }
}
