package com.ollamaService.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Created by: Sharan MH
 * on: 13/08/25
 */

@Data
@Builder
@AllArgsConstructor
public class TokenCounter {
    private int currentTokens;
    private int tokenLimit;

    public boolean addTokens(int tokens) {
        if (currentTokens + tokens > tokenLimit) {
            return false; // Limit exceeded
        }
        currentTokens += tokens;
        return true;
    }

    public boolean addEstimateTokens(String text) {
        return addTokens(TokenManager.estimateTokens(text));
    }

    public boolean addAccurateTokens(String text) {
        return addTokens(TokenManager.accurateTokens(text));
    }

    public boolean isLimitReached() {
        return currentTokens >= tokenLimit;
    }

    public int getRemainingTokens() {
        return tokenLimit - currentTokens;
    }
}
