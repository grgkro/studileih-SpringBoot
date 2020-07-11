package com.example.studileih.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="message")
@Data
@NoArgsConstructor
public class Message {
    @Id  // for more details why the ID is generated like this: https://stackoverflow.com/questions/62548985/hibernate-generates-one-id-for-two-tables-entities/62549190?noredirect=1#comment110616128_62549190
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "message_generator")
    @SequenceGenerator(name="message_generator", sequenceName = "message_seq", allocationSize=1)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    private String subject;
    private String text;
    private String sendetAt;   // = createdAt, can be later changed to a Date type instead of String
    private String receivedAt; // = updatedAt



@ManyToOne
    private User sender;

@ManyToOne
    private User receiver;

    public Message(String subject, String text, String sendetAt, User sender, User receiver) {
        this.subject = subject;
        this.text = text;
        this.sendetAt = sendetAt;
        this.sender = sender;
        this.receiver = receiver;
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