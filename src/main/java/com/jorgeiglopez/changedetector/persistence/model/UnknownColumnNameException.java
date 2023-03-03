package com.jorgeiglopez.changedetector.persistence.model;

public class UnknownColumnNameException extends Exception {

    public UnknownColumnNameException(final String columnName) {
        super(String.format("Unknown column name: %s", columnName));
    }
}
