package com.example.studileih.Service;

import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);

    }

    public ResponseEntity<String> sendEmailToOwner(String startDate, String endDate, Product product, User userWhoWantsToRent, User owner) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(owner.getEmail());
        message.setSubject("Neue Ausleianfrage für " + product.getName() + " von " + userWhoWantsToRent.getName());
        message.setText("Hallo " + owner.getName() + ",\n\n" + "der Nutzer " + userWhoWantsToRent.getName() + " aus " + userWhoWantsToRent.getCity() + " möchte dein " + product.getName() + " vom " + startDate + " bis " + endDate + " ausleihen." +
                "\nBitte logge dich bei studileih.de ein, um die Anfrage an/abzulehnen oder einen anderen Termin vorzuschlagen. Oder sende " + userWhoWantsToRent.getName() + " direkt eine Nachricht an " + userWhoWantsToRent.getEmail() + "." +
                "\n\nDon't reply to this email."
        );
        try {
            emailSender.send(message);
            return ResponseEntity.status(HttpStatus.OK).body("Deine Anfrage wurde per Email an " + owner.getName() + " gesendet.");
        } catch (MailException e){
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("Unerwarteter Fehler beim Senden der Anfrage per Email an " + owner.getName());

        }

    }
}