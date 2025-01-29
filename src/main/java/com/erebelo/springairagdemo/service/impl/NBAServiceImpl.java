package com.erebelo.springairagdemo.service.impl;

import com.erebelo.springairagdemo.model.Answer;
import com.erebelo.springairagdemo.model.Question;
import com.erebelo.springairagdemo.service.NBAService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NBAServiceImpl implements NBAService {

    @Nullable
    private final SimpleVectorStore simpleVectorStore;

    @Nullable
    private final VectorStore vectorStore;

    private final ChatModel chatModel;

    @Value("classpath:/templates/nba-system-message.st")
    private Resource nbaSystemMessageTemplate;

    @Value("classpath:/templates/rag-prompt-template.st")
    private Resource ragPromptTemplate;

    private static final int MAX_DOCUMENT_TOKENS = 3000;
    private static final int MAX_QUESTION_TOKENS = 50;
    private static final int CHARS_TO_TOKEN = 4; // Assuming 1 token = 4 characters (for English text)

    @Override
    public Answer getAnswer(Question question) {
        PromptTemplate systemPromptTemplate = new SystemPromptTemplate(nbaSystemMessageTemplate);
        Message systemPromptMessage = systemPromptTemplate.createMessage();

        String limitedQuestion = limitTextTokens(question.question(), MAX_QUESTION_TOKENS);
        log.info("📌 Question (limited to up to {} tokens): {}", MAX_QUESTION_TOKENS, limitedQuestion);

        List<Document> documents = getDocuments(limitedQuestion);
        List<String> documentContents = prepareDocumentsForPrompt(documents);

        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);
        Message userMessage = promptTemplate
                .createMessage(Map.of("input", limitedQuestion, "documents", String.join("\n", documentContents)));

        ChatResponse chatResponse = chatModel.call(new Prompt(List.of(systemPromptMessage, userMessage)));
        String responseContent = chatResponse.getResult().getOutput().getContent();
        log.info("💬 Response: {}", responseContent);

        tokenUsage(chatResponse);
        return new Answer(responseContent);
    }

    private List<Document> getDocuments(String question) {
        if (simpleVectorStore != null) {
            return simpleVectorStore.similaritySearch(SearchRequest.builder().query(question).topK(4).build());
        }
        return vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(4).build());
    }

    private String limitTextTokens(String question, int maxTokens) {
        int tokenCount = countTokens(question);
        if (tokenCount > maxTokens) {
            question = question.substring(0, maxTokens * CHARS_TO_TOKEN);
        }
        return question;
    }

    private List<String> prepareDocumentsForPrompt(List<Document> documents) {
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

    private void tokenUsage(ChatResponse chatResponse) {
        if (chatResponse.getMetadata() instanceof ChatResponseMetadata metadata
                && metadata.getUsage() instanceof Usage usage) {
            log.info("🧾 Token Usage -> Input Tokens: {}, Output Tokens: {}, Total Tokens: {}", usage.getPromptTokens(),
                    metadata.getUsage().getGenerationTokens(), usage.getTotalTokens());
        }
    }
}
