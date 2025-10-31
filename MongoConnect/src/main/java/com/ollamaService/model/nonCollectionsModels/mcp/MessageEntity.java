package com.ollamaService.model.nonCollectionsModels.mcp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by: Sharan MH
 * on: 25/08/25
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {

    private MessageType messageType;
    private String content;

    @Builder.Default
    private Instant messagedTime=Instant.now();


    public Message toMessage() {
        return switch (messageType) {
            case USER -> new UserMessage(content);
            case ASSISTANT -> new AssistantMessage(content);
            case SYSTEM, TOOL -> new SystemMessage(content);
        };
    }

    public static MessageEntity toMessageEntity(Message message) {
        return new MessageEntity(message.getMessageType(), message.getText(), Instant.now());
    }

    public static List<Message> toMessageList(List<MessageEntity> messageEntities) {
        return messageEntities.stream().map(MessageEntity::toMessage).collect(Collectors.toList());
    }

    public static List<MessageEntity> toMessageEntity(List<Message> messages) {
        return messages.stream().map(MessageEntity::toMessageEntity).collect(Collectors.toList());
    }
}
