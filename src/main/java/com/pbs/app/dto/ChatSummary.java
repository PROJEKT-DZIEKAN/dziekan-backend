package com.pbs.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatSummary {
    private Long id;
    private Long userAId;
    private Long userBId;
}
