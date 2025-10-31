package com.ollamaService.tools.Q_A;


import com.ollamaService.helper.HelperMethods;
import com.ollamaService.tool.AnnotatedTool;
import com.ollamaService.tools.sendMail.SendMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.function.Function;

/**
 * Created by: Sharan MH
 * on: 11/08/25
 */
public abstract class QA implements AnnotatedTool {

    @Value("${mcp.tools.qa.enabled}")
    private Boolean enabled;
    @Autowired
    protected HelperMethods.MessageToMapConverter messageToMapConverter;
    @Autowired
    protected Function<String, String> loadResourceFile;

    protected static final Logger log = LoggerFactory.getLogger(SendMail.class);

    public boolean useToolForLLM() {
        if (enabled == null) {
            return false;
        }
        return enabled;
    }
}
