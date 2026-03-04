package com.oceanview.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    private static final int LOG_ROUNDS = 12;

    public static String hash(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(LOG_ROUNDS));
    }

    public static boolean check(String plainText, String hashed) {
        return BCrypt.checkpw(plainText, hashed);
    }

    /**
     * Utility main method for generating BCrypt hashes for seed data.
     */
    public static void main(String[] args) {
        System.out.println("Admin@123  -> " + hash("Admin@123"));
        System.out.println("Manager@123 -> " + hash("Manager@123"));
        System.out.println("Staff@123  -> " + hash("Staff@123"));
    }
}

