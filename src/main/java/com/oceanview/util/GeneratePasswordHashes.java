package com.oceanview.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Run this class to generate correct BCrypt password hashes and
 * the SQL UPDATE statements needed to fix the users table.
 *
 * Usage (from project root, after mvn compile):
 *   mvn exec:java -Dexec.mainClass="com.oceanview.util.GeneratePasswordHashes"
 */
public class GeneratePasswordHashes {

    public static void main(String[] args) {
        System.out.println("=== Ocean View Resort - Password Hash Generator ===\n");

        String[][] users = {
            {"admin",   "Admin@123"},
            {"manager", "Manager@123"},
            {"staff1",  "Staff@123"}
        };

        System.out.println("-- Run these SQL statements on your ocean_view_db to fix passwords:");
        System.out.println();

        for (String[] entry : users) {
            String username = entry[0];
            String plain    = entry[1];
            String hash     = BCrypt.hashpw(plain, BCrypt.gensalt(10));
            System.out.printf("UPDATE users SET password = '%s' WHERE username = '%s';%n", hash, username);
        }

        System.out.println();
        System.out.println("-- Verification (should all print 'OK'):");
        for (String[] entry : users) {
            String username = entry[0];
            String plain    = entry[1];
            String hash     = BCrypt.hashpw(plain, BCrypt.gensalt(10));
            boolean ok = BCrypt.checkpw(plain, hash);
            System.out.printf("  %-10s %-15s -> %s%n", username, plain, ok ? "OK" : "FAILED");
        }
    }
}

