package com.mainstreetcode.teammate.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Message;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Consumer;

/**
 * A Error handler
 */

public class ErrorHandler implements Consumer<Throwable> {

    private static final String TAG = "ErrorHandler";
    private static final String ERROR_HANDLER_RECEIVED_ERROR = "Received Error";
    private static final String ERROR_HANDLER_DISPATCH_ERROR = "Dispatch Error to callback";

    private final String defaultMessage;
    private final Consumer<Message> messageConsumer;
    private final Map<String, String> messageMap;
    private final List<Runnable> actions;

    private ErrorHandler(String defaultMessage,
                         Consumer<Message> messageConsumer,
                         Map<String, String> messageMap) {
        this.defaultMessage = defaultMessage;
        this.messageConsumer = messageConsumer;
        this.messageMap = messageMap;
        actions = new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ErrorHandler EMPTY = new ErrorHandler(null, null, null) {
        @Override
        public void accept(Throwable throwable) {
            Logger.log(TAG, ERROR_HANDLER_RECEIVED_ERROR, throwable);
        }
    };

    @Override
    public void accept(Throwable throwable) {
        Logger.log(TAG, ERROR_HANDLER_RECEIVED_ERROR, throwable);

        String key = throwable.getClass().getName();
        Message message = messageMap.containsKey(key)
                ? new Message(messageMap.get(key))
                : isTeammateException(throwable)
                ? new Message(throwable.getMessage())
                : parseMessage(throwable);

        try {
            messageConsumer.accept(message);
            for (Runnable action : actions) action.run();
        }
        catch (Exception e) {
            Logger.log(TAG, ERROR_HANDLER_DISPATCH_ERROR, e);
        }
    }

    public void addAction(Runnable action) {
        actions.add(action);
    }

    private boolean isTeammateException(Throwable throwable) {
        return throwable instanceof TeammateException;
    }

    private boolean isOffline() {
        App app = App.getInstance();
        if (app == null) return true;

        ConnectivityManager connectivityManager = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return true;

        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return netInfo == null || !netInfo.isConnectedOrConnecting();
    }

    private Message parseMessage(Throwable throwable) {
        Message message = Message.fromThrowable(throwable);
        if (message != null) return message;

        App app = App.getInstance();

        if ((isOffline() || throwable instanceof UnknownHostException) && app != null)
            return new Message(app.getString(R.string.error_network));

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