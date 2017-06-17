package com.mainstreetcode.teammates.util;

import android.text.TextUtils;

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

    private final String errorMessage;
    private final Consumer<String> messageConsumer;
    private final Map<String, String> messageMap;

    private ErrorHandler(String errorMessage,
                         Consumer<String> messageConsumer,
                         Map<String, String> messageMap) {
        this.errorMessage = errorMessage;
        this.messageConsumer = messageConsumer;
        this.messageMap = messageMap;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void accept(Throwable throwable) throws Exception {
        String key = throwable.getClass().getName();
        String message = null;

        if (messageMap.containsKey(key)) message = messageMap.get(key);
        else if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            try {
                ResponseBody errorBody = httpException.response().errorBody();
                if (errorBody != null) {
                    String json = errorBody.string();
                    message = TeammateService.getGson().fromJson(json, Message.class).getMessage();
                }
            }
            catch (Exception e) {
                message = errorMessage;
            }
        }

        if (TextUtils.isEmpty(message)) message = errorMessage;
        messageConsumer.accept(message);

        throwable.printStackTrace();
    }

    public static class Builder {
        String defaultMessage;
        Consumer<String> messageConsumer;
        Map<String, String> messageMap = new HashMap<>();

        public Builder defaultMessage(String errorMessage) {
            defaultMessage = errorMessage;
            return this;
        }

        public Builder add(String message, Class<? extends Throwable> exceptionClass) {
            messageMap.put(exceptionClass.getName(), message);
            return this;
        }

        public Builder add(Consumer<String> messageConsumer) {
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