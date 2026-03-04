package com.oceanview.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.util.Properties;

public class EmailUtil {
    private static final Properties mailProps = loadProps();

    // ── Guest: Booking Confirmation ──────────────────────────────────────────
    public static void sendReservationConfirmation(String toEmail, String guestName,
            String reservationNo, String checkIn, String checkOut, double total) {
        String subject = "Booking Confirmed — " + reservationNo + " | Ocean View Resort";
        String body = buildHtml(
            "Booking Confirmed! 🎉",
            "Dear <strong>" + guestName + "</strong>,",
            "<p>Your reservation at <strong>Ocean View Resort</strong> has been confirmed.</p>"
            + "<table style='width:100%;border-collapse:collapse;margin:16px 0'>"
            + row("Reservation No", reservationNo)
            + row("Check-in",  checkIn)
            + row("Check-out", checkOut)
            + row("Total Amount", "LKR " + String.format("%,.2f", total))
            + "</table>"
            + "<p>We look forward to welcoming you to our beachside paradise in Galle, Sri Lanka.</p>",
            "#2d9b5e"
        );
        sendAsync(toEmail, subject, body);
    }

    // ── Guest: Payment Receipt ───────────────────────────────────────────────
    public static void sendPaymentReceipt(String toEmail, String guestName,
            String reservationNo, String method, String refNo, double amount) {
        String subject = "Payment Received — " + reservationNo + " | Ocean View Resort";
        String body = buildHtml(
            "Payment Received ✅",
            "Dear <strong>" + guestName + "</strong>,",
            "<p>We have received your payment for reservation <strong>" + reservationNo + "</strong>.</p>"
            + "<table style='width:100%;border-collapse:collapse;margin:16px 0'>"
            + row("Amount Paid",   "LKR " + String.format("%,.2f", amount))
            + row("Payment Method", method)
            + row("Reference No",  refNo)
            + row("Status",        "COMPLETED")
            + "</table>"
            + "<p>Thank you for choosing Ocean View Resort!</p>",
            "#2e86ab"
        );
        sendAsync(toEmail, subject, body);
    }

    // ── Staff: Welcome / Credentials email ──────────────────────────────────
    public static void sendStaffCredentials(String toEmail, String fullName,
            String username, String tempPassword, String role) {
        String subject = "Welcome to Ocean View Resort — Your Account Details";
        String body = buildHtml(
            "Welcome to Ocean View Resort 🌊",
            "Dear <strong>" + fullName + "</strong>,",
            "<p>Your staff account has been created. Below are your login credentials:</p>"
            + "<table style='width:100%;border-collapse:collapse;margin:16px 0'>"
            + row("Username", username)
            + row("Temporary Password", "<code style='background:#f4f7fb;padding:2px 6px;border-radius:4px'>" + tempPassword + "</code>")
            + row("Role", role)
            + "</table>"
            + "<p><strong>Important:</strong> Please log in and change your password immediately.</p>"
            + "<p><a href='#' style='color:#2e86ab'>Login to Ocean View Resort Management System</a></p>",
            "#1a3c5e"
        );
        sendAsync(toEmail, subject, body);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private static String row(String label, String value) {
        return "<tr>"
             + "<td style='padding:8px 12px;background:#f4f7fb;font-weight:600;font-size:13px;border-radius:4px;width:40%'>" + label + "</td>"
             + "<td style='padding:8px 12px;font-size:13px'>" + value + "</td>"
             + "</tr>";
    }

    private static String buildHtml(String heading, String intro, String content, String accentColor) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#f4f7fb;font-family:Segoe UI,Arial,sans-serif'>"
            + "<div style='max-width:560px;margin:32px auto;background:#fff;border-radius:10px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,.08)'>"
            + "<div style='background:" + accentColor + ";padding:28px 32px'>"
            + "<h1 style='color:#fff;margin:0;font-size:22px'>🌊 Ocean View Resort</h1>"
            + "<p style='color:rgba(255,255,255,.8);margin:6px 0 0;font-size:13px'>Galle, Sri Lanka</p>"
            + "</div>"
            + "<div style='padding:32px'>"
            + "<h2 style='color:#1a3c5e;margin:0 0 12px;font-size:20px'>" + heading + "</h2>"
            + "<p style='color:#2c3e50;margin:0 0 16px'>" + intro + "</p>"
            + content
            + "</div>"
            + "<div style='background:#f4f7fb;padding:16px 32px;text-align:center;font-size:11px;color:#7f8c8d'>"
            + "&copy; 2026 Ocean View Resort, Galle, Sri Lanka. All rights reserved."
            + "</div></div></body></html>";
    }

    private static void sendAsync(String to, String subject, String htmlBody) {
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
                msg.setContent(htmlBody, "text/html; charset=UTF-8");
                Transport.send(msg);
            } catch (Exception e) {
                System.err.println("Email send failed to " + to + ": " + e.getMessage());
            }
        }, "EmailThread-" + System.currentTimeMillis()).start();
    }

    private static Properties loadProps() {
        Properties p = new Properties();
        try (InputStream is = EmailUtil.class.getClassLoader().getResourceAsStream("email.properties")) {
            if (is != null) p.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }
}



