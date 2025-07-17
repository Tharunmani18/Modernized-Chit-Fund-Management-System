package com.chit.api.controller;

import com.chit.api.enums.EmailEnum;
import com.chit.api.request.model.MailRequest;
import com.chit.api.response.model.GetEmailResponse;
import com.chit.api.response.model.MailResponse;
import com.chit.api.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.mail.MessagingException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin
@RestController
@RequestMapping("/email")
@Slf4j
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Operation(
        summary = "Email Service",
        description = "Sending emails to users",
        tags = {"Email API"},
        responses = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email id or number")
        }
    )
    @PostMapping(path = "/sendEmail", produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<MailResponse> sendEmail(
    @RequestHeader(value = HttpHeaders.ORIGIN, required = true) String origin,
    @RequestBody MailRequest mailRequest
) throws MessagingException {

    log.info("===== Incoming /sendEmail Request =====");
    log.info("Received Origin header: {}", origin);
    log.info("Received MailRequest - Name: {}, Email ID: {}, Phone No: {}, Selected Requirement: {}",
             mailRequest.getName(), mailRequest.getEmailid(), mailRequest.getPhoneno(), mailRequest.getSelectedRequirenment());

    if (origin == null) {
        log.warn("Blocked Request: Missing Origin header (possibly direct tool like Postman or Curl)");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    if (!origin.equals("https://www.foxmerge.com")&&
        !origin.equals("https://foxmerge.com")) {
        log.warn("Blocked Request: Unauthorized Origin '{}' not allowed to access /sendEmail", origin);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    log.info("Origin '{}' is authorized. Proceeding to send email.", origin);

    emailService.sendEmail(mailRequest);

    MailResponse mailResponse = new MailResponse();
    mailResponse.setMessage(EmailEnum.MAIL_SENT.getEnumEmailConstant());

    log.info("Email sent successfully to user: {}", mailRequest.getEmailid());
    log.info("===== Completed /sendEmail Request =====");

    return new ResponseEntity<>(mailResponse, HttpStatus.OK);
}

    @GetMapping("/getEmail")
    public ResponseEntity<List<GetEmailResponse>> getEmail(
        @RequestParam(required = false) String emailId,
        @RequestParam(required = false) String phoneNo,
        @RequestParam(required = false) String selectedRequest,
        @RequestParam(required = false) String name
    ) {
        log.info("Received getEmail request with params - emailId: {}, phoneNo: {}, selectedRequest: {}, name: {}", 
                 emailId, phoneNo, selectedRequest, name);

        List<GetEmailResponse> getEmailResponses = emailService.getEmail(emailId, phoneNo, selectedRequest, name);

        return new ResponseEntity<>(getEmailResponses, HttpStatus.OK);
    }
}
