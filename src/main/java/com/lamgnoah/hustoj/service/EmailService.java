package com.lamgnoah.hustoj.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
public class EmailService {

  private static final String CONFIRM_YOUR_EMAIL_MSG = "Please confirm your email";
  private static final String FAILED_TO_SEND_EMAIL_MSG = "failed to send email";
  @Autowired
  private JavaMailSender mailSender;
  @Autowired
  private  TemplateEngine templateEngine;
  @Value("${spring.mail.username}")
  private String from;

  @Async("asyncTaskExecutor")
  @Transactional
  public void send(String to, String email) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
      helper.setText(email, true);
      helper.setTo(to);
      helper.setSubject(CONFIRM_YOUR_EMAIL_MSG);
      helper.setFrom(from);
      mailSender.send(mimeMessage);
      log.info("Send mail success");
    } catch (MessagingException e) {
      log.error(FAILED_TO_SEND_EMAIL_MSG, e);
      throw new IllegalStateException(FAILED_TO_SEND_EMAIL_MSG);
    }
  }

  public String buildEmail(String verifyPath , String template) {
    final Context context = new Context();
    context.setVariable("verify_path", verifyPath);
    return templateEngine.process(template, context);
  }
}
