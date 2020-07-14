package com.example.studileih.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="chats")
@Data
@NoArgsConstructor
public class Chat {
    @Id  // for more details why the ID is generated like this: https://stackoverflow.com/questions/62548985/hibernate-generates-one-id-for-two-tables-entities/62549190?noredirect=1#comment110616128_62549190
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chat_generator")
    @SequenceGenerator(name="chat_generator", sequenceName = "chat_seq", allocationSize=1)
    @Column(name = "chat_id", updatable = false, nullable = false)
    private Long id;

@ManyToOne
    private User user1;

@ManyToOne
    private User user2;

@OneToMany(mappedBy="chat")
private List<Message> messages;

    public Chat(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

    public Chat(User user1, User user2, List<Message> messages) {
        this.user1 = user1;
        this.user2 = user2;
        this.messages = messages;
    }

    // gleiches Problem mit Rekursion wie bei Product.java -> Message enthält User enthält Message enthält...
    @Override
    public String toString() {
        return "Chat{" +
                "id=" + id +
                ", user1=" + "user1" +
                ", user2=" + "user2" +
                ", messages=" + "messages" +
                '}';
    }
}