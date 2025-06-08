package com.nguner.yeditepecardshop.exception;

public class AddressNotFoundException extends RuntimeException {
    public AddressNotFoundException(int idx) {
        super("No address at index: " + idx);
    }
    public AddressNotFoundException() {
        super("Address not found");
    }
    public AddressNotFoundException(String message) {
        super(message);
    }
}

