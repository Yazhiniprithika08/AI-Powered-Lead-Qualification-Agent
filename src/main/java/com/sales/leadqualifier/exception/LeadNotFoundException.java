package com.sales.leadqualifier.exception;

/**
 * Custom exception class thrown when a Lead document cannot be found in MongoDB.
 */
public class LeadNotFoundException extends RuntimeException {
    
    public LeadNotFoundException(String id) {
        super("Lead with ID '" + id + "' not found.");
    }
}
