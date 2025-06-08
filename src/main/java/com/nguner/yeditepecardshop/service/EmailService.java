package com.nguner.yeditepecardshop.service;

import com.nguner.yeditepecardshop.model.Order;
import com.nguner.yeditepecardshop.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Log4j2
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Send order confirmation email
     */
    public void sendOrderConfirmationEmail(User user, Order order) {
        if (!emailEnabled) {
            log.info("Email sending is disabled");
            return;
        }

        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("user", user);
            context.setVariable("order", order);
            context.setVariable("orderTotal", calculateOrderTotal(order));

            String htmlContent = templateEngine.process("emails/order-confirmation", context);

            sendHtmlEmail(
                user.getEmail(),
                "Order Confirmation - YeditepeCardShop #" + order.getId().substring(0, 8),
                htmlContent
            );

            log.info("Order confirmation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to: {}", user.getEmail(), e);
        }
    }

    /**
     * Send order status update email
     */
    public void sendOrderStatusUpdateEmail(User user, Order order, String oldStatus) {
        if (!emailEnabled) {
            log.info("Email sending is disabled");
            return;
        }

        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("user", user);
            context.setVariable("order", order);
            context.setVariable("oldStatus", oldStatus);
            context.setVariable("newStatus", order.getStatus());

            String htmlContent = templateEngine.process("emails/order-status-update", context);

            sendHtmlEmail(
                user.getEmail(),
                "Order Status Update - YeditepeCardShop #" + order.getId().substring(0, 8),
                htmlContent
            );

            log.info("Order status update email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send order status update email to: {}", user.getEmail(), e);
        }
    }

    /**
     * Send order cancellation email
     */
    public void sendOrderCancellationEmail(User user, Order order) {
        if (!emailEnabled) {
            log.info("Email sending is disabled");
            return;
        }

        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("user", user);
            context.setVariable("order", order);
            context.setVariable("orderTotal", calculateOrderTotal(order));

            String htmlContent = templateEngine.process("emails/order-cancellation", context);

            sendHtmlEmail(
                user.getEmail(),
                "Order Cancelled - YeditepeCardShop #" + order.getId().substring(0, 8),
                htmlContent
            );

            log.info("Order cancellation email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send order cancellation email to: {}", user.getEmail(), e);
        }
    }

    /**
     * Send welcome email for new user registration
     */
    public void sendWelcomeEmail(User user) {
        if (!emailEnabled) {
            log.info("Email sending is disabled");
            return;
        }

        try {
            Context context = new Context(Locale.ENGLISH);
            context.setVariable("user", user);

            String htmlContent = templateEngine.process("emails/welcome", context);

            sendHtmlEmail(
                user.getEmail(),
                "Welcome to YeditepeCardShop!",
                htmlContent
            );

            log.info("Welcome email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }

    /**
     * Send simple text email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        if (!emailEnabled) {
            log.info("Email sending is disabled");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
        }
    }

    /**
     * Send HTML email
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Calculate order total
     */
    private double calculateOrderTotal(Order order) {
        return order.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}
