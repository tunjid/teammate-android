package com.mainstreetcode.teammates.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import lombok.Getter;

/**
 * Messages from the {@link com.mainstreetcode.teammates.rest.TeammateApi}
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Getter
public class Message {

    String message;

    Message(String message) {
        this.message = message;
    }

    public static class JsonDeserializer implements com.google.gson.JsonDeserializer<Message> {
        private static final String MESSAGE_KEY = "message";

        @Override
        public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject messageJson = json.getAsJsonObject();
            return new Message(messageJson.has(MESSAGE_KEY)
                    ? messageJson.get(MESSAGE_KEY).getAsString()
                    : "");
        }
    }
}
