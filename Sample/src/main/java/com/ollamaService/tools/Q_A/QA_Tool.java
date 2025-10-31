package com.ollamaService.tools.Q_A;


import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Created by: Sharan MH
 * on: 11/08/25
 */
@Component
public class QA_Tool extends QA{

    /**
     *MCP Style still works but cannot add dynamic name and desc
     */
    @Tool(
            name = "about_company_and_product",
            description = "use this tool if you want to know all about company ands its products like"
    )
    public String apply(ToolContext toolContext) {
        log.debug("about_company_and_product tool called");

        String response = null;
        try {
            return "";//loadResourceFile.apply("QA/AboutCompanyAndProduct");
        } catch (Exception e) {
            log.error("Exception when executing tool about_company_and_product" , e);
            response = "about_company_and_product , This service is currently facing problem, the issue has already been notified";
        } finally {
            log.debug("about_company_and_product Tool Execution completed result: " + response);
        }
        return response;
    }
}
