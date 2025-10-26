package com.food_api.food_api.repository;


import com.food_api.food_api.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    // You can add custom queries if needed
}