package com.mainstreetcode.teammates.util;

import com.mainstreetcode.teammates.model.Message;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.functions.Consumer;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

/**
 * An Error handler
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class ErrorHandler implements Consumer<Throwable> {

    private final String defaultMessage;
    private final Consumer<Message> messageConsumer;
    private final Map<String, String> messageMap;

    private ErrorHandler(String defaultMessage,
                         Consumer<Message> messageConsumer,
                         Map<String, String> messageMap) {
        this.defaultMessage = defaultMessage;
        this.messageConsumer = messageConsumer;
        this.messageMap = messageMap;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ErrorHandler EMPTY = new ErrorHandler(null, null, null) {
        @Override
        public void accept(Throwable throwable) throws Exception {
            throwable.printStackTrace();
        }
    };

    @Override
    public void accept(Throwable throwable) throws Exception {
        String key = throwable.getClass().getName();
        Message message = null;

        if (messageMap.containsKey(key)) {
            message = new Message(messageMap.get(key));
        }
        else if (throwable instanceof TeammateException) {
            message = new Message(throwable.getMessage());
        }
        else if (throwable instanceof HttpException) {
            message = getMessage((HttpException) throwable, defaultMessage);
        }

        messageConsumer.accept(message);

        throwable.printStackTrace();
    }

    private Message getMessage(HttpException throwable, String defaultMessage) {
        try {
            ResponseBody errorBody = throwable.response().errorBody();
            if (errorBody != null) {
                String json = errorBody.string();
                return TeammateService.getGson().fromJson(json, Message.class);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return new Message(defaultMessage);
    }

    public static class Builder {
        String defaultMessage;
        Consumer<Message> messageConsumer;
        Map<String, String> messageMap = new HashMap<>();

        public Builder defaultMessage(String errorMessage) {
            defaultMessage = errorMessage;
            return this;
        }

        public Builder add(Consumer<Message> messageConsumer) {
            this.messageConsumer = messageConsumer;
            return this;
        }

        public ErrorHandler build() {
            if (defaultMessage == null) {
                throw new IllegalArgumentException("No default message provided");
            }
            if (messageConsumer == null) {
                throw new IllegalArgumentException("No Consumer provided for error message");
            }
            return new ErrorHandler(defaultMessage, messageConsumer, messageMap);
        }
    }
}