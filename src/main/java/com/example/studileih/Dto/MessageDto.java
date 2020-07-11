package com.example.studileih.Dto;

import com.example.studileih.Entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.ManyToOne;

@Data
@NoArgsConstructor
public class MessageDto {

    private Long id;
    private String subject;
    private String text;
    private String sendetAt;   // = createdAt
    private String receivedAt; // = updatedAt
    private User sender;
    private User receiver;

}
