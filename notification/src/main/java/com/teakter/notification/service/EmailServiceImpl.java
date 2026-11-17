package com.teakter.notification.service;

import com.teakter.notification.entity.EmailLogEntity;
import com.teakter.notification.entity.EmailStatus;
import com.teakter.notification.repository.EmailLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements  EmailService {

private final JavaMailSender mailSender;
private final EmailLogRepository emailLogRepository;

@Value("${app.frontend-url}")
    private String frontendUrl;

@Value("${app.mail.from}")
    private String mailFrom;

    @Override
    public void sendVerificationEmail(String toEmail, String username, String token) {

        String subject = "Verifica tu cuenta en Teakter";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String verificationLink = frontendUrl + "/verify-email?token=" + token;
            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            // Se usa String.format o interpolación limpia garantizando que no haya errores de sustitución en el href
            String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e4e4e7; border-radius: 8px;">
                <h2 style="color: #4f46e5; text-align: center;">¡Bienvenido a Teakter, %s!</h2>
                <p style="color: #3f3f46; font-size: 16px;">
                    Gracias por registrarte. Para completar la creación de tu cuenta y poder iniciar sesión, por favor confirma tu correo electrónico haciendo clic en el siguiente enlace:
                </p>
                <div style="text-align: center; margin: 30px 0;">
                    <a href="%s" target="_blank" style="background-color: #4f46e5; color: #ffffff; padding: 12px 24px; text-decoration: none; font-weight: bold; border-radius: 6px; display: inline-block;">
                        Verificar mi cuenta
                    </a>
                </div>
                <p style="color: #71717a; font-size: 14px;">Si el botón no funciona, copia y pega el siguiente enlace en tu navegador:</p>
                <p style="color: #4f46e5; font-size: 12px; word-break: break-all;">
                    <a href="%s" target="_blank" style="color: #4f46e5;">%s</a>
                </p>
                <hr style="border: none; border-top: 1px solid #e4e4e7; margin: 20px 0;"/>
                <p style="color: #a1a1aa; font-size: 12px; text-align: center;">Si no creaste esta cuenta, puedes ignorar este mensaje.</p>
            </div>
            """.formatted(username, verificationLink, verificationLink, verificationLink);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            saveLog(toEmail, subject, "VERIFICATION", EmailStatus.SENT, null);
        } catch (MessagingException e) {
            saveLog(toEmail, subject, "VERIFICATION", EmailStatus.FAILED, e.getMessage());
            log.error("error al enviar el correo de verificacion a {}: {}", toEmail, e.getMessage());
        }
    }

    private void saveLog(String recipient, String subject, String type, EmailStatus status, String error){
    EmailLogEntity logEntity=EmailLogEntity.builder()
            .recipientEmail(recipient)
            .subject(subject)
            .emailType(type).status(status)
            .errorMessage(error)
            .build();
    emailLogRepository.save(logEntity);
}

}
