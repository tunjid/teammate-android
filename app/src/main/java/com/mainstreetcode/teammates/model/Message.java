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

    private static final String UNKNOWN_ERROR_CODE = "unkown.error";
    private static final String UNAUTHENTICATED_USER_ERROR_CODE = "unauthenticated.user.error";

    final String message;
    final String errorCode;

    public Message(String message) {
        this.message = message;
        this.errorCode = UNKNOWN_ERROR_CODE;
    }

    Message(String message, String errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    public static class GsonAdapter implements com.google.gson.JsonDeserializer<Message> {
        private static final String MESSAGE_KEY = "message";
        private static final String ERROR_CODE_KEY = "errorCode";

        @Override
        public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject messageJson = json.getAsJsonObject();
            String message = messageJson.get(MESSAGE_KEY).getAsString();
            String errorCode = messageJson.get(ERROR_CODE_KEY).getAsString();

            return new Message(message, errorCode);
        }
    }

    public boolean isUnauthorizedUser() {
        return UNAUTHENTICATED_USER_ERROR_CODE.equals(errorCode);
    }
}
