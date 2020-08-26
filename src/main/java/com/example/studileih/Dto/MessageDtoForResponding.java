package com.example.studileih.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageDtoForResponding {

    private Long id;
    private String subject;
    private String text;
    private String sendetAt;   // = createdAt
    private String receivedAt;   // = createdAt
    private String sender;         // wenn wir hier User statt String machen würden, würde mit jeder Message auch der komplette Nutzer mitgeschickt -> unnötiges Datenvolumen + unsicherer
    private String receiver;

}
