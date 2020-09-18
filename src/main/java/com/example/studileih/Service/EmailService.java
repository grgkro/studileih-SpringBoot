package com.example.studileih.Service;

import com.example.studileih.Entity.Chat;
import com.example.studileih.Entity.Message;
import com.example.studileih.Entity.Product;
import com.example.studileih.Entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;

@Component
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Value("${spring.mail.username}")
    private String studiEmail;

    private SimpleDateFormat inputDateFormatter =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");  // this is the format the dates are saved in the database eg. 2020-07-17T17:51:19.349Z
    private SimpleDateFormat finalDateFormat =new SimpleDateFormat("dd.MM.yy HH:mm"); // this is the format, we want to present the dates to the user (without milliseconds etc.) eg. 17.07.20 19:42

    /**
     * Die Funktion wird direkt nach Start aufgerufen und speichert 1 Beispielwohnheim/Adresse/2 Pro in die DB -> Kann später auskommentiert/gelöscht werden
     */
    @PostConstruct
    public void createBaseDataset() {
        // sends an email from studileih@gmail.com. I think you need to be on a Windows PC that this works! else go to the application.properties and uncomment your system password (Linux, Mac)... (https://www.baeldung.com/spring-email)
       sendSimpleMessage("georgkromer@pm.me", "server started", "yolo");
    }

    public void sendSimpleMessage(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);

    }

    public ResponseEntity prepareSendingEmailToOwner(String startDate, String endDate, String pickUpTime, String returnTime, Product product, User userWhoWantsToRent, User owner) {
        try {
            // sends an email from studileih@gmail.com. I think you need to be on a Windows PC that this works! else go to the application.properties and uncomment your system password (Linux, Mac)... (https://www.baeldung.com/spring-email)
            return sendEmailToOwner(startDate, endDate, pickUpTime, returnTime, product, userWhoWantsToRent, owner);
        } catch (NoSuchElementException e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Interner Datenbankfehler - Produkt, Produktbesitzer oder Anfragender User konnte nicht geladen werden.");
        }
    }

    public ResponseEntity<String> sendEmailToOwner(String startDate, String endDate, String pickUpTime, String returnTime, Product product, User userWhoWantsToRent, User owner) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(owner.getEmail());
        mail.setCc(userWhoWantsToRent.getEmail());
        mail.setReplyTo(userWhoWantsToRent.getEmail());
        mail.setFrom(userWhoWantsToRent.getEmail());
        mail.setSubject("Neue Ausleianfrage für " + product.getDescription() + " von " + userWhoWantsToRent.getName());
        //create Message with the given infos
        Message message = null;
        try {
            message = messageService.createAusleihanfrage(startDate, endDate, pickUpTime, returnTime, product, userWhoWantsToRent, owner);
        } catch (ParseException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Interner Datenbankfehler beim Erstellen der Nachricht - Datum konnte nicht umgewandelt werden.");
        }
        // create the actual email text with small header + message + small footer
        StringBuilder sb = new StringBuilder();
        sb.append("Dies ist eine Kopie der auf Studileih.de an " + owner.getName() + " gesendeten Nachricht:");

        sb.append(message.getText());

        sb.append(System.lineSeparator()); // https://stackoverflow.com/questions/14534767/how-to-append-a-newline-to-stringbuilder
        sb.append(System.lineSeparator());
        sb.append("Du kannst direkt auf diese Email Antworten.");
        sb.append(System.lineSeparator());
        // add the message to the mail (we only need the text of the message)
        mail.setText(message.getText());
        // send email to Owner + copy to userWhoWantsToRent
        try {
            emailSender.send(mail);
            return ResponseEntity.status(HttpStatus.OK).body("Deine Anfrage wurde per Email an " + owner.getName() + " gesendet.");
        } catch (MailException e){
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("Unerwarteter Fehler beim Senden der Anfrage per Email an " + owner.getName());

        }

    }

    public ResponseEntity prepareEmailReply(String subject, String messageText, String sendetAt, Chat chat, User sender) {
        User receiver;
        try {
            // get the chat and the two users
            if (chat.getUser1().getId() == sender.getId()) {
                receiver = chat.getUser2();
            } else {
                receiver = chat.getUser1();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Interner Datenbankfehler - Chat oder einer der User konnten nicht geladen werden.");
        }
        // create the message String subject, String text, String sendetAt, User sender, User receiver, Chat chat
        Message message = new Message(subject, messageText, sendetAt, sender, receiver, chat);

        // sends an email from studileih@gmail.com. I think you need to be on a Windows PC that this works! else go to the application.properties and uncomment your system password (Linux, Mac)... (https://www.baeldung.com/spring-email)
        try {
            return sendEmailReply(message, chat, sender, receiver);
        } catch (ParseException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Interner Datenbankfehler beim Erstellen der Nachricht - Datum konnte nicht umgewandelt werden.");
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
        sb.append("Du kannst direkt auf diese Email antworten.");
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

    public ResponseEntity<String> sendEmailToAdmin(String name, String city, String street, String houseNumber, Principal userDetails) {
        User user = userService.getActiveUserByName(userDetails.getName());
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo("grg.kro@gmail.com");
        mail.setCc(user.getEmail());
        mail.setReplyTo(user.getEmail());
        mail.setFrom(studiEmail);
        mail.setSubject("Bitte neues Wohnheim " + name + " prüfen");
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator()); // https://stackoverflow.com/questions/14534767/how-to-append-a-newline-to-stringbuilder
        sb.append(System.lineSeparator());
        sb.append("Hallo Adi,");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("der Nutzer " + user.getName() + " mit ID: " + user.getId());
        if (user.getCity() != null) sb.append(" aus " + user.getCity());
        sb.append(" möchte gern folgendes Wohnheim in die Liste aufnehmen: ");
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Name: " + name);
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Stadt: " + city);
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Straße: " + street);
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Hausnummer: " + houseNumber);
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("Beste Grüße, dein Server xoxo");
        mail.setText(sb.toString());
        try {
            emailSender.send(mail);
            return ResponseEntity.status(HttpStatus.OK).body("Dein Wohnheim wird in den nächsten Tagen von unserem Admin hinzugefügt.");
        } catch (MailException e){
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body("Unerwarteter Fehler, deine Wohnheim Anfrage konnte nicht verarbeitet werden.");
        }
    }
}