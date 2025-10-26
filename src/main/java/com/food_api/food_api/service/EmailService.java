package com.food_api.food_api.service;

import com.food_api.food_api.dto.DonationDTO;
import com.food_api.food_api.entity.User;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${spring.sendgrid.api-key}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");

    public void sendDonationNotificationToNGOs(DonationDTO donation, User ngo) {
        if (ngo.getEmail() == null || ngo.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("NGO email address is required");
        }

        String subject = "New Food Donation Available";
        String emailContent = String.format("""
            New food donation available in your area!
            
            Donation Details:
            - Food Item: %s
            - Quantity: %s
            - Location: %s
            - Area: %s
            - Best Before: %s
            - Posted by: %s
            
            Additional Information:
            %s
            %s
            %s
            
            Log in to claim this donation.
            """,
                donation.getFoodItem(),
                donation.getQuantity(),
                donation.getLocation(),
                donation.getArea(),
                donation.getExpiryTime().format(DATE_FORMATTER),
                donation.getDonorName(),
                donation.getDietaryInfo() != null ? "- Dietary Info: " + donation.getDietaryInfo() : "",
                donation.getStorageInstructions() != null ? "- Storage Instructions: " + donation.getStorageInstructions() : "",
                donation.getServingSize() != null ? "- Serving Size: " + donation.getServingSize() : ""
        );

        sendEmail(ngo.getEmail(), subject, emailContent);
    }

    public void sendDonationClaimedNotification(DonationDTO donation, User claimingUser) {
        if (donation.getDonorEmail() == null || donation.getDonorEmail().trim().isEmpty()) {
            System.err.println("Cannot send claim notification: donor email is missing");
            throw new IllegalArgumentException("Donor email address is required");
        }

        String subject = "Your Food Donation Has Been Claimed";
        String emailContent = String.format("""
            Dear %s,
            
            Great news! Your donation has been claimed.
            
            Donation Details:
            - Food Item: %s
            - Quantity: %s
            - Posted on: %s
            
            Claimed by:
            - Organization: %s
            - Contact Email: %s
            - Contact Phone: %s
            - Claimed at: %s
            
            Thank you for your generous donation and helping make a difference in our community!
            
            Best regards,
            Food Donation Platform Team
            """,
                donation.getDonorName(),
                donation.getFoodItem(),
                donation.getQuantity(),
                donation.getCreatedAt().format(DATE_FORMATTER),
                claimingUser.getOrganization() != null ? claimingUser.getOrganization() : claimingUser.getUsername(),
                claimingUser.getEmail(),
                claimingUser.getPhone(),
                donation.getClaimedAt().format(DATE_FORMATTER)
        );

        sendEmail(donation.getDonorEmail(), subject, emailContent);
    }

    private void sendEmail(String to, String subject, String emailContent) {
        if (to == null || to.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email address is required");
        }

        try {
            SendGrid sg = new SendGrid(sendGridApiKey);
            Email fromEmailObj = new Email(fromEmail);
            Email toEmailObj = new Email(to.trim());
            Content content = new Content("text/plain", emailContent);
            Mail mail = new Mail(fromEmailObj, subject, toEmailObj, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 400) {
                String errorMessage = "Failed to send email. Status Code: " + response.getStatusCode() +
                        ", Body: " + response.getBody();
                System.err.println(errorMessage);
                throw new RuntimeException(errorMessage);
            }

            System.out.println("Email sent successfully to: " + to + ", Status Code: " + response.getStatusCode());

        } catch (IOException e) {
            String errorMessage = "Failed to send email to: " + to;
            System.err.println(errorMessage);
            e.printStackTrace();
            throw new RuntimeException(errorMessage, e);
        }
    }
}