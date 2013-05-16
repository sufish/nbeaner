package com.sufish.nbeaner.handlers.callback;

import com.sufish.nbeaner.protocol.BeanstalkResponse;
import com.sufish.nbeaner.protocol.OperationFailureException;

import java.util.Objects;

public class CallbackUtil {

    public static void checkResponse(BeanstalkResponse response, String expectedStatusText) throws OperationFailureException {
        if (!Objects.equals(response.getResponseText(), expectedStatusText)) {
            throw new OperationFailureException(response.getResponseText());
        }
    }
}
