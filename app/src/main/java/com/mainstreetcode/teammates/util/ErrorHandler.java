package com.mainstreetcode.teammates.util;

import com.mainstreetcode.teammates.model.Message;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.functions.Consumer;
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
        Message message = messageMap.containsKey(key)
                ? new Message(messageMap.get(key))
                : isTeammateException(throwable)
                ? new Message(throwable.getMessage())
                : isHttpException(throwable)
                ? new Message((HttpException) throwable)
                : new Message(defaultMessage);

        messageConsumer.accept(message);
        throwable.printStackTrace();
    }

    private boolean isHttpException(Throwable throwable) {
        return throwable instanceof HttpException;
    }

    private boolean isTeammateException(Throwable throwable) {
        return throwable instanceof TeammateException;
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