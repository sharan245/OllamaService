package com.ollamaService.tools.sendMail;


import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Created by: Sharan MH
 * on: 11/08/25
 */
@Component
public class SendMailTool extends SendMail {

    /**
     * custom too can add dynamic name and description
     */

    @Override
    public String getName() {
        return "send_mail";
    }

    @Override
    public String getDescription() {
        return """
                all 3 inputs for this tool are mandatory
                note:
                 1. for every mail attach'
                   Regards,
                   Ollama Service
                  'as signature
                 2. file attachments to email are not currently supported
                 3. from address cannot be changed from:"""
                + "test.gmail.com"; // can be changes dynamically

    }


    @Call
    public String apply(
            @ToolParam(required = true)
            String toAddress,
            @ToolParam(required = true, description = "Keep it short")
            String mailSubject,
            @ToolParam(required = true, description = "No HTML Tags are allowed")
            String mailBody,
            ToolContext toolContext) {

        Request request = new Request(toAddress, mailSubject, mailBody);
        log.debug("SendMailTool API called Input: " + request);

        String response = null;

        try {
            if ((response = request.validate()) == null) {
                //emailService.sendMail(request.getEmailObj());
                response = "Email send successfully";
            }
        } catch (Exception e) {
            log.error("Exception when executing tool {}" , getName(), e);
            response = "Email not sent, This service is currently facing problem, the issue has already been notified";
        } finally {
            log.debug("SendMailTool Tool Execution completed result: " + response);
        }
        return response;
    }

    public record Request(String toAddress, String mailSubject, String mailBody) {
        public String validate() {
            if (toAddress == null || toAddress.isBlank()) {
                return "toAddress is missing";
            } else if (mailSubject == null || mailSubject.isBlank()) {
                return "mailSubject is missing if not given by user create your self";
            } else if (mailBody == null || mailBody.isBlank()) {
                return "mailBody is missing";
            }
            return null;
        }
    }
}
