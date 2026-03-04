package com.oceanview.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;

public class EmailUtil {
    private static final Properties mailProps = loadProps();

    public static void sendReservationConfirmation(String toEmail, String guestName,
            String reservationNo, String checkIn, String checkOut, double total) {
        String subject = "Reservation Confirmed — " + reservationNo;
        String body = "Dear " + guestName + ",\n\n"
            + "Your reservation at Ocean View Resort has been confirmed.\n\n"
            + "Reservation No : " + reservationNo + "\n"
            + "Check-in       : " + checkIn + "\n"
            + "Check-out      : " + checkOut + "\n"
            + "Total Amount   : LKR " + String.format("%.2f", total) + "\n\n"
            + "We look forward to welcoming you.\n\nOcean View Resort Team";
        sendAsync(toEmail, subject, body);
    }

    public static void sendPaymentReceipt(String toEmail, String guestName,
            String reservationNo, String method, String refNo, double amount) {
        String subject = "Payment Receipt — " + reservationNo;
        String body = "Dear " + guestName + ",\n\n"
            + "Payment received for reservation " + reservationNo + ".\n\n"
            + "Amount         : LKR " + String.format("%.2f", amount) + "\n"
            + "Method         : " + method + "\n"
            + "Reference No   : " + refNo + "\n\n"
            + "Thank you.\n\nOcean View Resort Team";
        sendAsync(toEmail, subject, body);
    }

    private static void sendAsync(String to, String subject, String body) {
        new Thread(() -> {
            try {
                Session mailSession = Session.getInstance(mailProps, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                            mailProps.getProperty("mail.username"),
                            mailProps.getProperty("mail.password"));
                    }
                });
                Message msg = new MimeMessage(mailSession);
                msg.setFrom(new InternetAddress(mailProps.getProperty("mail.from")));
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                msg.setSubject(subject);
                msg.setText(body);
                Transport.send(msg);
            } catch (Exception e) {
                System.err.println("Email failed: " + e.getMessage());
            }
        }).start();
    }

    private static Properties loadProps() {
        Properties p = new Properties();
        try (InputStream is = EmailUtil.class.getClassLoader().getResourceAsStream("email.properties")) {
            p.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }
}

