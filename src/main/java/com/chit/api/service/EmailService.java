package com.chit.api.service;

import com.chit.api.config.EmailConfig;
import com.chit.api.constants.EmailConstants;
import com.chit.api.dao.EmailRepo;
import com.chit.api.dao.model.EmailDBModel;
import com.chit.api.enums.EmailEnum;
import com.chit.api.globalexceptions.BadRequestException;
import com.chit.api.globalexceptions.ResourceNotFoundException;
import com.chit.api.request.model.MailRequest;
import com.chit.api.response.model.GetEmailResponse;
import com.chit.api.sequence.SequenceService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class EmailService {

  private final SequenceService sequenceService;
  private final JavaMailSender javaMailSender;
  private final EmailConfig emailConfig;
  private final EmailRepo emailrepo;
  private final MongoTemplate mongoTemplate;

  public EmailService(SequenceService sequenceService, JavaMailSender javaMailSender,
      EmailConfig emailConfig, EmailRepo emailrepo, MongoTemplate mongoTemplate) {
    this.sequenceService = sequenceService;
    this.javaMailSender = javaMailSender;
    this.emailConfig = emailConfig;
    this.emailrepo = emailrepo;
    this.mongoTemplate = mongoTemplate;
  }

  public void sendEmail(MailRequest mailRequest) throws MessagingException {
    log.info("Starting sendEmail process for user: {}", mailRequest.getEmailid());

    validateMailRequest(mailRequest);

    log.info("Sending Thank You email to user: {}", mailRequest.getEmailid());
    sendUserThankYouEmail(mailRequest);

    log.info("Sending New Contact Form Submission email to Admin...");
    sendAdminNotificationEmail(mailRequest);

    log.info("Completed sendEmail process for user: {}", mailRequest.getEmailid());
  }

  private void sendUserThankYouEmail(MailRequest mailRequest) throws MessagingException {
    log.info("Preparing Thank You email for user: {}", mailRequest.getEmailid());

    MimeMessage userMimeMessage = javaMailSender.createMimeMessage();
    MimeMessageHelper userHelper = new MimeMessageHelper(userMimeMessage, true, "UTF-8");

    userHelper.setFrom(emailConfig.getSenderEmail());
    userHelper.setTo(mailRequest.getEmailid());
    userHelper.setSubject("Thank You for Contacting Us!");

    String userHtmlMsg = "<p>Hi " + mailRequest.getName() + ",</p>"
        + "<p>Thank you for reaching out to us!</p>"
        + "<p>We’ve received your request regarding <strong>" + mailRequest.getSelectedRequirenment() + "</strong>. One of our team members will review your message and get back to you within the next 24 hours.</p>"
        + "<p>This is an automated message to confirm your inquiry — No need to reply.</p>"
        + "<p>Warm Regards,</p>"
        + "<p>FoxMerge</p>"
        + "<img src='cid:logoImage' style='width:75px;height:25px;'>";

    userHelper.setText(userHtmlMsg, true);

    ClassPathResource resource = new ClassPathResource("static/Foxmerge.png");
    userHelper.addInline("logoImage", resource);

    javaMailSender.send(userMimeMessage);

    log.info("Successfully sent Thank You email to user: {}", mailRequest.getEmailid());
  }

  private void sendAdminNotificationEmail(MailRequest mailRequest) throws MessagingException {
    log.info("Preparing New Contact Form Submission email for Admin...");
    log.info("From Email: {}, To Email: {}", emailConfig.getSenderEmail(), emailConfig.getSenderEmail());

    MimeMessage adminMimeMessage = javaMailSender.createMimeMessage();
    MimeMessageHelper adminHelper = new MimeMessageHelper(adminMimeMessage, true, "UTF-8");

    adminHelper.setFrom(emailConfig.getSenderEmail());
    adminHelper.setTo(emailConfig.getSenderEmail());
    adminHelper.setSubject("New Contact Form Submission");

    String adminHtmlMsg = "<p>Hi Info Team,</p>"
        + "<p>You’ve received a new inquiry from the website. Here are the details:</p>"
        + "<ul>"
        + "<li><strong>Full Name:</strong> " + mailRequest.getName() + "</li>"
        + "<li><strong>Email Address:</strong> " + mailRequest.getEmailid() + "</li>"
        + "<li><strong>Phone Number:</strong> " + mailRequest.getPhoneno() + "</li>"
        + "<li><strong>Requirement Selected:</strong> " + mailRequest.getSelectedRequirenment() + "</li>"
        + "<li><strong>Message:</strong> " + mailRequest.getMessage() + "</li>"
        + "</ul>"
        + "<p><strong>Note:</strong> Please follow up with the user within 24 hours.</p>"
        + "<p>Warm Regards,</p>"
        + "<p>FoxMerge</p>";

    adminHelper.setText(adminHtmlMsg, true);

    javaMailSender.send(adminMimeMessage);

    log.info("Successfully sent Contact Form Submission email to Admin (From: {}, To: {}).",
             emailConfig.getSenderEmail(), emailConfig.getSenderEmail());
}


  private void validateMailRequest(MailRequest mailRequest) {
    log.info("Validating MailRequest...");

    if (mailRequest == null
        || isEmpty(mailRequest.getName())
        || isEmpty(mailRequest.getEmailid())
        || isEmpty(mailRequest.getMessage())
        || isEmpty(mailRequest.getPhoneno())
        || isEmpty(mailRequest.getSelectedRequirenment())) {
      log.error("Validation failed: Missing required fields.");
      throw new BadRequestException(EmailEnum.EMAIL_EMPTY.getEnumEmailConstant());
    }

    if (!isValidEmail(mailRequest.getEmailid())) {
      log.error("Validation failed: Invalid email format for emailId: {}", mailRequest.getEmailid());
      throw new BadRequestException(EmailEnum.EMAIL_INVALID.getEnumEmailConstant());
    }

    log.info("MailRequest validation passed successfully.");
  }

  private boolean isEmpty(String str) {
    return str == null || str.trim().isEmpty();
  }

  private boolean isValidEmail(String email) {
    String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    Pattern pattern = Pattern.compile(emailRegex);
    return pattern.matcher(email).matches();
  }

  public List<GetEmailResponse> getEmail(String emailId, String phoneNo, String selectedRequest,
      String name) {
    log.info("Entered getEmail method...");
    log.info("Search Parameters: emailId={}, phoneNo={}, selectedRequest={}, name={}",
        emailId, phoneNo, selectedRequest, name);

    Query query = new Query();
    List<Criteria> criteriaList = new ArrayList<>();

    if (!isEmpty(emailId)) {
      log.info("Adding query condition for emailId: {}", emailId);
      criteriaList.add(Criteria.where(EmailConstants.EMAIL_ID).is(emailId));
    }
    if (!isEmpty(phoneNo)) {
      log.info("Adding query condition for phoneNo: {}", phoneNo);
      criteriaList.add(Criteria.where(EmailConstants.PHONE_NO).is(phoneNo));
    }
    if (!isEmpty(selectedRequest)) {
      log.info("Adding query condition for selectedRequest: {}", selectedRequest);
      criteriaList.add(Criteria.where(EmailConstants.SELECTED_REQUEST).is(selectedRequest));
    }
    if (!isEmpty(name)) {
      log.info("Adding query condition for name: {}", name);
      criteriaList.add(Criteria.where(EmailConstants.NAME).is(name));
    }

    if (!criteriaList.isEmpty()) {
      query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
      log.info("Built query: {}", query.toString());
    } else {
      log.error("No valid search parameters provided. Aborting search.");
      throw new BadRequestException("At least one search parameter must be provided.");
    }

    List<EmailDBModel> emailDBModels = mongoTemplate.find(query, EmailDBModel.class);

    if (emailDBModels.isEmpty()) {
      log.error("No email records found for given criteria.");
      throw new ResourceNotFoundException(EmailEnum.EMAIL_NOT_FOUND.getEnumEmailConstant());
    }

    log.info("Email records fetched successfully.");

    List<GetEmailResponse> getEmailResponses = emailDBModels.stream().map(emailDBModel -> {
      GetEmailResponse response = new GetEmailResponse();
      response.setEmailid(emailDBModel.getEmailid());
      response.setPhoneno(emailDBModel.getPhoneno());
      response.setSelectedRequirenment(emailDBModel.getSelectedRequirenment());
      response.setName(emailDBModel.getName());
      response.setMessage(emailDBModel.getMessage());
      return response;
    }).toList();

    return getEmailResponses;
  }
}
