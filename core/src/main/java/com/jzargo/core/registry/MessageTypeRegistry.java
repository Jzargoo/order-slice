package com.jzargo.core.registry;

import com.jzargo.core.messages.command.BasicCommand;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class MessageTypeRegistry {
    HashMap<String, Class<?>> messageTypes = new HashMap<>();

    public MessageTypeRegistry () {
        String basePackageCommand = BasicCommand.class.getPackageName();
        scanPackage(basePackageCommand);
    }

    private void scanPackage(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        reflections.getTypesAnnotatedWith(MessageType.class)
                .forEach((clazz) -> {
                    MessageType messageTypeAnnotation = clazz.getAnnotation(MessageType.class);
                    if (messageTypeAnnotation != null) {
                        String messageType = messageTypeAnnotation.value();
                        if (!messageTypes.containsKey(messageType)) {
                            messageTypes.put(messageType, clazz);
                        } else {
                            throw new IllegalStateException("Duplicate message type found: " + messageType);
                        }
                    }
                });
    }

    public Class<?> getMessageTypeClass(String messageType) {
        return messageTypes.get(messageType);
    }

    public String getMessageTypeName(Class<?> messageTypeClass) {
        MessageType messageTypeAnnotation = messageTypeClass.getAnnotation(MessageType.class);
        if (messageTypeAnnotation != null) {
            return messageTypeAnnotation.value();
        }
        throw new IllegalArgumentException("Class " + messageTypeClass.getName() + " is not annotated with @MessageType");
    }
}
