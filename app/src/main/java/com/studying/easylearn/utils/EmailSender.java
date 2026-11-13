package com.studying.easylearn.utils;

import java.util.Properties;
import java.util.Random;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    // Thay thế bằng email và Mật khẩu ứng dụng 16 ký tự (Google App Password) của bạn
    private static final String SENDER_EMAIL = "anhtuan220903@gmail.com";
    private static final String SENDER_APP_PASSWORD = "uvlm laka aaem fful";

    // Hàm tạo ngẫu nhiên mã OTP 6 chữ số
    public static String generateOtp() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // Gửi mail trên luồng ngầm qua SMTP
    public static boolean sendOtpEmail(String recipientEmail, String otpCode) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "EasyLearn Security"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("EasyLearn - Mã xác thực đổi mật khẩu");
            message.setText("Xin chào,\n\nMã xác nhận đặt lại mật khẩu EasyLearn của bạn là: "
                    + otpCode + "\n\nMã này có hiệu lực trong 1 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.");

            Transport.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}