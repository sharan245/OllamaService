package com.ollamaService.tools.sendMail;


import com.ollamaService.helper.HelperMethods;
import com.ollamaService.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by: Sharan MH
 * on: 11/08/25
 */
public abstract class SendMail implements Tool {

    @Value("${mcp.tools.sendMail.enabled}")
    private Boolean enabled;
    @Autowired
    protected HelperMethods.MessageToMapConverter messageToMapConverter;

    protected static final Logger log = LoggerFactory.getLogger(SendMail.class);

    public boolean useToolForLLM() {
        if (enabled == null) {
            return false;
        }
        return enabled;
    }
}
