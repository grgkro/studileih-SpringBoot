package com.example.studileih.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
public class Message {
    @Id
    // for more details why the ID is generated like this: https://stackoverflow.com/questions/62548985/hibernate-generates-one-id-for-two-tables-entities/62549190?noredirect=1#comment110616128_62549190
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_generator")
    @SequenceGenerator(name = "message_generator", sequenceName = "message_seq", allocationSize = 1)
    @Column(name = "message_id", updatable = false, nullable = false)
    private Long id;

    private String subject;
    @Column
    @Lob                 // wir sagen der DB, dass sie diese Spalte als Datentyp LongText (Large Object = lob) anlegen soll, damit auch längere Texte rein gehen. Sonst legt sie es automatich als VARCHAR an und das ist schnell zu klein.
    private String text;
    private String sendetAt;   // = createdAt, can be later changed to a Date type instead of String
    private String receivedAt; // = updatedAt


    @ManyToOne
    private User sender;

    @ManyToOne
    private User receiver;

    @ManyToOne
    @JoinTable(name = "chat_messages",
            joinColumns = @JoinColumn(name = "message_id",
                    referencedColumnName = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "chat_id",
                    referencedColumnName = "chat_id"))
    private Chat chat;

    public Message(String subject, String text, String sendetAt, User sender, User receiver) {
        this.subject = subject;
        this.text = text;
        this.sendetAt = sendetAt;
        this.sender = sender;
        this.receiver = receiver;
    }

    public Message(String subject, String text, String sendetAt, User sender, User receiver, Chat chat) {
        this.subject = subject;
        this.text = text;
        this.sendetAt = sendetAt;
        this.receivedAt = receivedAt;
        this.sender = sender;
        this.receiver = receiver;
        this.chat = chat;
    }

    // gleiches Problem mit Rekursion wie bei Product.java -> Message enthält User enthält Message enthält...
    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", subject='" + subject + '\'' +
                ", text='" + text + '\'' +
                ", sendetAt='" + sendetAt + '\'' +
                ", receivedAt='" + receivedAt + '\'' +
                ", sender=" + "sender" +
                ", receiver=" + "receiver" +
                "}";
    }
}