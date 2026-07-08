package com.sales.leadqualifier.util;

import java.util.regex.Pattern;

/**
 * Utility class containing common validation helpers.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
            
    private static final Pattern PHONE_PATTERN = 
            Pattern.compile("^\\+?[0-9\\-\\s()]{7,20}$");

    private ValidationUtil() {
        // Prevent instantiation
    }

    /**
     * Helper to validate email format manually if needed.
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Helper to validate phone number format manually if needed.
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }
}
