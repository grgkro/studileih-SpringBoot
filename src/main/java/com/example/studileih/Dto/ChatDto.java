package com.example.studileih.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ChatDto {

    private Long id;
    private UserDto user1;
    private UserDto user2;
    private List<MessageDto> messages;

}
