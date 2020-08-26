package com.example.studileih.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatDtoForResponding {

    private Long id;
    private String user1;
    private String user2;
    private List<MessageDtoForResponding> messages;



}
