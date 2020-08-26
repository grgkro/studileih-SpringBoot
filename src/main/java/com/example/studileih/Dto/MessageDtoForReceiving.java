package com.example.studileih.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageDtoForReceiving {

    private Long chatId;
    private String subject;
    private String text;
    private String sendetAt;   // = createdAt


}
