package com.erebelo.springairagdemo.util;

import static com.erebelo.springairagdemo.constant.TokenConstant.CHARS_TO_TOKEN;
import static com.erebelo.springairagdemo.constant.TokenConstant.MAX_DOCUMENT_TOKENS;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

@UtilityClass
public class TokenUtil {

    public static String loadTemplateContent(Resource resource) {
        try {
            return new String(resource.getInputStream().readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error loading template content", e);
        }
    }

    public static String limitTextTokens(String question, int maxTokens) {
        int tokenCount = countTokens(question);
        if (tokenCount > maxTokens) {
            question = question.substring(0, maxTokens * CHARS_TO_TOKEN);
        }
        return question;
    }

    public static List<String> limitDocumentsForPrompt(List<Document> documents) {
        List<String> documentContents = documents.stream().map(Document::getText).collect(Collectors.toList());

        int currentTokenCount = countTokens(String.join("\n", documentContents));

        // Trim documents until the token count fits within the limit
        while (currentTokenCount > MAX_DOCUMENT_TOKENS && !documentContents.isEmpty()) {
            // Remove the last document (or a portion of it) to reduce token usage
            documentContents.removeLast();
            currentTokenCount = countTokens(String.join("\n", documentContents));
        }

        return documentContents;
    }

    private int countTokens(String text) {
        return text.length() / CHARS_TO_TOKEN;
    }

    public static String tokenUsage(ChatResponse chatResponse) {
        if (chatResponse.getMetadata() instanceof ChatResponseMetadata metadata
                && metadata.getUsage() instanceof Usage usage) {
            return String.format("Input Tokens: %d, Output Tokens: %d, Total Tokens: %d", usage.getPromptTokens(),
                    metadata.getUsage().getGenerationTokens(), usage.getTotalTokens());
        }
        return "Token usage information not available";
    }
}
