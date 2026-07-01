package com.renzo.todo_api.common.exceptions;

import lombok.Getter;

@Getter
public abstract class NotFoundException extends RuntimeException {
    private final String resourceName;
    private final Object resourceId;

    protected NotFoundException(String resourceName, Object resourceId) {
        super(resourceName + " with id " + resourceId + " was not found.");
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }
}
