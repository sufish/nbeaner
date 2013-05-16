package com.sufish.nbeaner.handlers.callback;

import com.sufish.nbeaner.protocol.BeanstalkResponse;

import java.util.Objects;

public class CallbackUtil {

    public static void checkResponse(BeanstalkResponse response, String expectedStatusText) throws FailedResponseException {
        if (!Objects.equals(response.getResponseText(), expectedStatusText)) {
            throw new FailedResponseException(response.getResponseText());
        }
    }
}
