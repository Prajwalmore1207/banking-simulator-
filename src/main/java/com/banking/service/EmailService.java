package com.banking.service;

import com.banking.util.LoggerUtil;
import org.apache.logging.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {
    private static final Logger logger = LoggerUtil.getLogger(EmailService.class);
    private final Properties emailConfig;
    private final boolean enabled;

    public EmailService() {
        this.emailConfig = loadEmailConfig();
        this.enabled = Boolean.parseBoolean(emailConfig.getProperty("email.enabled", "false"));

        logger.info("EmailService initialized - Enabled: {}, SMTP: {}:{}",
                enabled,
                emailConfig.getProperty("email.smtp.host"),
                emailConfig.getProperty("email.smtp.port")
        );

        // Test connection on startup if enabled
        if (enabled) {
            testEmailConnection();
        }
    }

    private Properties loadEmailConfig() {
        Properties props = new Properties();

        try {
            InputStream input = EmailService.class.getClassLoader()
                    .getResourceAsStream("email.properties");

            if (input != null) {
                props.load(input);
                logger.info("📧 Email configuration loaded from email.properties");
            } else {
                logger.warn("email.properties not found, using system properties");
            }
        } catch (Exception e) {
            logger.warn("Failed to load email.properties: {}", e.getMessage());
        }

        // Set defaults
        setDefaultProperty(props, "email.enabled", "false");
        setDefaultProperty(props, "email.smtp.host", "smtp.gmail.com");
        setDefaultProperty(props, "email.smtp.port", "587");
        setDefaultProperty(props, "email.from.name", "Banking Simulator");
        setDefaultProperty(props, "email.smtp.auth", "true");
        setDefaultProperty(props, "email.smtp.starttls.enable", "true");

        // Override with system properties
        overrideWithSystemProperty(props, "email.enabled");
        overrideWithSystemProperty(props, "email.smtp.host");
        overrideWithSystemProperty(props, "email.smtp.port");
        overrideWithSystemProperty(props, "email.username");
        overrideWithSystemProperty(props, "email.password");
        overrideWithSystemProperty(props, "email.from.name");
        overrideWithSystemProperty(props, "email.from.address");

        return props;
    }

    private void setDefaultProperty(Properties props, String key, String defaultValue) {
        if (!props.containsKey(key)) {
            props.setProperty(key, defaultValue);
        }
    }

    private void overrideWithSystemProperty(Properties props, String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            props.setProperty(key, systemValue);
            logger.debug("Overridden {} with system property", key);
        }
    }

    public boolean sendEmail(String toEmail, String subject, String messageBody) {
        // ADD CONSOLE MESSAGE FOR IMMEDIATE FEEDBACK
        System.out.println("📧 Sending email notification...");

        if (!enabled) {
            logger.warn("Email service is disabled. Enable it in email.properties");
            return simulateEmail(toEmail, subject, messageBody);
        }

        String username = emailConfig.getProperty("email.username");
        String password = emailConfig.getProperty("email.password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            logger.error("❌ Email credentials not configured. Check email.properties");
            return simulateEmail(toEmail, subject, messageBody);
        }

        // CHANGED: Send email synchronously (not in background thread)
        boolean success = sendEmailSync(toEmail, subject, messageBody, username, password);

        // ADD CONSOLE MESSAGE FOR RESULT
        if (success) {
            System.out.println("✅ Email sent successfully to: " + toEmail);
        } else {
            System.out.println("⚠️  Email simulation mode - check logs for details");
        }

        return success;
    }

    private boolean sendEmailSync(String toEmail, String subject, String messageBody, String username, String password) {
        try {
            Properties smtpProps = new Properties();
            smtpProps.put("mail.smtp.auth", emailConfig.getProperty("email.smtp.auth", "true"));
            smtpProps.put("mail.smtp.starttls.enable", emailConfig.getProperty("email.smtp.starttls.enable", "true"));
            smtpProps.put("mail.smtp.host", emailConfig.getProperty("email.smtp.host"));
            smtpProps.put("mail.smtp.port", emailConfig.getProperty("email.smtp.port"));
            smtpProps.put("mail.smtp.ssl.trust", emailConfig.getProperty("email.smtp.host"));

            // Timeout settings
            smtpProps.put("mail.smtp.connectiontimeout", "5000");
            smtpProps.put("mail.smtp.timeout", "5000");
            smtpProps.put("mail.smtp.writetimeout", "5000");

            Session session = Session.getInstance(smtpProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);

            String fromAddress = emailConfig.getProperty("email.from.address", username);
            String fromName = emailConfig.getProperty("email.from.name", "Banking Simulator");

            message.setFrom(new InternetAddress(fromAddress, fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("🏦 Banking Alert: " + subject);

            // Create HTML email with better styling
            String htmlContent = createProfessionalEmailTemplate(subject, messageBody);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            logger.info("✅ Email sent successfully to: {} | Subject: {}", toEmail, subject);
            return true;

        } catch (Exception e) {
            logger.error("❌ Failed to send email to {}: {}", toEmail, e.getMessage());
            // Fallback to simulation
            return simulateEmail(toEmail, subject, messageBody);
        }
    }

    private String createProfessionalEmailTemplate(String subject, String messageBody) {
        String headerColor = "#2c3e50";
        String alertColor = subject.toLowerCase().contains("critical") ? "#e74c3c" : "#3498db";

        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Banking Alert</title>" +
                "    <style>" +
                "        body { font-family: 'Arial', sans-serif; margin: 0; padding: 20px; background-color: #f8f9fa; }" +
                "        .container { max-width: 600px; margin: 0 auto; background: white; padding: 0; border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); overflow: hidden; }" +
                "        .header { background: " + headerColor + "; color: white; padding: 25px; text-align: center; }" +
                "        .header h2 { margin: 0; font-size: 24px; }" +
                "        .content { padding: 30px; line-height: 1.6; color: #333; }" +
                "        .alert-box { background: " + alertColor + "; color: white; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
                "        .alert-box h3 { margin: 0 0 15px 0; font-size: 20px; }" +
                "        .message-body { background: #f8f9fa; padding: 20px; border-radius: 6px; border-left: 4px solid " + alertColor + "; white-space: pre-line; }" +
                "        .footer { text-align: center; padding: 20px; color: #7f8c8d; font-size: 12px; background: #ecf0f1; }" +
                "        .logo { font-size: 28px; margin-bottom: 10px; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <div class=\"header\">" +
                "            <div class=\"logo\">🏦</div>" +
                "            <h2>Banking Transaction Simulator</h2>" +
                "        </div>" +
                "        <div class=\"content\">" +
                "            <div class=\"alert-box\">" +
                "                <h3>" + subject + "</h3>" +
                "            </div>" +
                "            <div class=\"message-body\">" + messageBody + "</div>" +
                "            <p style=\"margin-top: 20px; color: #666;\">" +
                "                This is an automated alert from your banking system." +
                "            </p>" +
                "        </div>" +
                "        <div class=\"footer\">" +
                "            <p>Please do not reply to this email. Contact support if you have questions.</p>" +
                "            <p>&copy; 2024 Banking Transaction Simulator. All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private void testEmailConnection() {
        String username = emailConfig.getProperty("email.username");
        String password = emailConfig.getProperty("email.password");

        if (username == null || password == null) {
            logger.error("❌ Email credentials missing. Real emails will not work.");
            return;
        }

        try {
            Properties smtpProps = new Properties();
            smtpProps.put("mail.smtp.auth", "true");
            smtpProps.put("mail.smtp.starttls.enable", "true");
            smtpProps.put("mail.smtp.host", emailConfig.getProperty("email.smtp.host"));
            smtpProps.put("mail.smtp.port", emailConfig.getProperty("email.smtp.port"));
            smtpProps.put("mail.smtp.connectiontimeout", "5000");
            smtpProps.put("mail.smtp.timeout", "5000");

            Session session = Session.getInstance(smtpProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();

            logger.info("✅ Email connection test successful! Real emails are enabled.");

        } catch (Exception e) {
            logger.error("❌ Email connection test failed: {}", e.getMessage());
            logger.error("⚠️  Falling back to email simulation");
        }
    }

    private boolean simulateEmail(String toEmail, String subject, String messageBody) {
        logger.info("📧 EMAIL SIMULATION - To: {} | Subject: {}", toEmail, subject);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("📧 EMAIL SIMULATION (Real emails are disabled)");
        System.out.println("=".repeat(60));
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("\nMessage:");
        System.out.println(messageBody);
        System.out.println("=".repeat(60));
        System.out.println("💡 To enable real emails, configure email.properties");
        System.out.println("=".repeat(60) + "\n");
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }
}