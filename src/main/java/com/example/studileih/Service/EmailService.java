package com.example.studileih.Service;

import com.example.studileih.Entity.Chat;
import com.example.studileih.Entity.Message;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private MessageService messageService;

    private SimpleDateFormat inputDateFormatter =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");  // this is the format the dates are saved in the database eg. 2020-07-17T17:51:19.349Z
    private SimpleDateFormat finalDateFormat =new SimpleDateFormat("dd.MM.yy HH:mm"); // this is the format, we want to present the dates to the user (without milliseconds etc.) eg. 17.07.20 19:42



    public void sendSimpleMessage(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);

    }

    public ResponseEntity<String> sendEmailToOwner(String startDate, String endDate, Product product, User userWhoWantsToRent, User owner) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(owner.getEmail());
        mail.setSubject("Neue Ausleianfrage für " + product.getName() + " von " + userWhoWantsToRent.getName());
        //create Message with the given infos
        Message message = null;
        try {
            message = messageService.createAusleihanfrage(startDate, endDate, product, userWhoWantsToRent, owner);
        } catch (ParseException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Interner Datenbankfehler beim Erstellen der Nachricht - Datum konnte nicht umgewandelt werden.");
        }
        // add the message to the mail (we only need the text of the message)
        mail.setText(message.getText());
        // send email
        try {
            emailSender.send(mail);
            return ResponseEntity.status(HttpStatus.OK).body("Deine Anfrage wurde per Email an " + owner.getName() + " gesendet.");
        } catch (MailException e){
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("Unerwarteter Fehler beim Senden der Anfrage per Email an " + owner.getName());

        }

    }

    public ResponseEntity<String> sendEmailReply(Message message, Chat chat, User sender, User receiver) throws ParseException {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(receiver.getEmail());
        mail.setSubject("Neue Antwort zu deiner Ausleihanfrage von " + sender.getName());
        // transform the Message into a fitting string, depending on given values
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator()); // https://stackoverflow.com/questions/14534767/how-to-append-a-newline-to-stringbuilder
        sb.append(System.lineSeparator());
        if (!message.getSubject().isEmpty()) {
            sb.append("Betreff: " + message.getSubject());
        } else {
            sb.append("Kein Betreff");
        }
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Nachricht: " + message.getText());
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Gesendet von: " + sender.getName());
        sb.append(System.lineSeparator());
        sb.append(sender.getEmail());
        sb.append(System.lineSeparator());
        sb.append("Gesendet um: " + DateFormat.getDateInstance(DateFormat.SHORT).format(new Date()) + " " + DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date()) );
        sb.append(System.lineSeparator());
        sb.append("Don't reply to this email.");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        // now also append the previous messages (there is at least one, namely the Ausleihanfrage with wich each chat starts.
        sb.append("-----------------------------------------------------------");
        sb.append(System.lineSeparator());
        sb.append("Deine bisherige Nachrichten mit " + sender.getName() + ": ");
        sb.append(System.lineSeparator());
        sb.append("-----------------------------------------------------------");
        // go backwards trough all previous chat messages - therefore we start with int i = getMessages().size() - 1 which is the last sent message.
        for (int i = (chat.getMessages().size() - 1); i >= 0; i-- ) {
            sb.append(System.lineSeparator()); // https://stackoverflow.com/questions/14534767/how-to-append-a-newline-to-stringbuilder
            sb.append(System.lineSeparator());
            if (!message.getSubject().isEmpty()) {
                sb.append("Betreff: " + chat.getMessages().get(i).getSubject());
            } else {
                sb.append("Kein Betreff");
            }
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
            sb.append("Nachricht: " + chat.getMessages().get(i).getText());
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
            sb.append("Gesendet von: " + sender.getName());
            sb.append(System.lineSeparator());
            sb.append("Gesendet um: " + finalDateFormat.format(inputDateFormatter.parse(chat.getMessages().get(i).getSendetAt())));   // https://stackoverflow.com/questions/2009207/java-unparseable-date-exception
            sb.append(System.lineSeparator());
            if (chat.getMessages().get(i).getReceivedAt() != null) sb.append("Gesehen auf Studileih.de um: " + finalDateFormat.format(inputDateFormatter.parse(chat.getMessages().get(i).getReceivedAt())));
        if (i != 0) {
            // if it's not the last message, we append a seperation line again.
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
            sb.append("-----------------------------------------------------------");
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
        }
        }
        // put transformed message into the email:
        mail.setText(sb.toString());
        try {
            emailSender.send(mail);
            return ResponseEntity.status(HttpStatus.OK).body("Deine Antwort wurde per Email an " + receiver.getName() + " gesendet.");
        } catch (MailException e){
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("Unerwarteter Fehler beim Senden der Antwort per Email an " + receiver.getName());
        }
    }
}