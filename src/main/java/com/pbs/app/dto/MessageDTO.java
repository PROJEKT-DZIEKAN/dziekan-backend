package com.pbs.app.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class MessageDTO {
    private Long chatId;
    private Long senderId;
    private String content;
    private Instant sentAt;
}
