package com.mainstreetcode.teammates.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.lang.reflect.Type;
import java.nio.charset.Charset;

import lombok.Getter;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.HttpException;

/**
 * Messages from the {@link com.mainstreetcode.teammates.rest.TeammateApi}
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Getter
public class Message {

    private static final String UNKNOWN_ERROR_CODE = "unknown.error";
    private static final String UNAUTHENTICATED_USER_ERROR_CODE = "unauthenticated.user.error";
    private static final String INVALID_OBJECT_REFERENCE_ERROR_CODE = "invalid.object.reference.error";

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

    public Message(HttpException exception) {
        Message parsed = getMessage(exception);
        this.message = parsed.message;
        this.errorCode = parsed.errorCode;
    }

    public boolean isInvalidObject() { return INVALID_OBJECT_REFERENCE_ERROR_CODE.equals(errorCode);}

    public boolean isUnauthorizedUser() {
        return UNAUTHENTICATED_USER_ERROR_CODE.equals(errorCode);
    }

    private Message getMessage(HttpException throwable) {
        try {
            ResponseBody errorBody = throwable.response().errorBody();
            if (errorBody != null) {
                BufferedSource source = errorBody.source();
                source.request(Long.MAX_VALUE); // request the entire body.
                Buffer buffer = source.buffer();
                // clone buffer before reading from it
                String json = buffer.clone().readString(Charset.forName("UTF-8"));
                return TeammateService.getGson().fromJson(json, Message.class);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new Message("");
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
}
