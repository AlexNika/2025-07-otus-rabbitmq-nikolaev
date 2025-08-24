package ru.otus.rabbitmq.hw.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -1686452662908726121L;

    private Long id;
    private String message;
}
