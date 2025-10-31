package com.ollamaService.token;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;

/**
 * Created by: Sharan MH
 * on: 20/08/25
 */

public class TokenManager {

    private static final Encoding encoding = Encodings.newDefaultEncodingRegistry()
            .getEncoding(EncodingType.CL100K_BASE); // GPT-3.5/4 encoding

    //accurate slower
    public static int accurateTokens(String text) {
        return encoding.encode(text).size();
    }


    // Rough estimate: ~0.75 tokens per word fater
    public static int estimateTokens(String text) {
        String[] words = text.trim().split("\\s+");
        return (int) (words.length * 0.75);
    }

}
