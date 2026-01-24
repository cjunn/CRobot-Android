package com.crobot.core.infra;

import java.io.Serializable;

public class InfraError extends RuntimeException implements Serializable {
    public InfraError(String message) {
        super(message);
    }

    public InfraError(Throwable cause) {
        super(cause);
    }

}
