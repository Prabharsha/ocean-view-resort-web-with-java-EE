package com.oceanview.util;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[+]?[0-9\\-\\s]{7,20}$");

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return !isNullOrEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return !isNullOrEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidDateRange(LocalDate from, LocalDate to) {
        return from != null && to != null && to.isAfter(from);
    }

    public static boolean isPositive(int value) {
        return value > 0;
    }

    public static boolean isPositive(double value) {
        return value > 0;
    }
}

