package com.erebelo.springairagdemo.service.impl;

import static com.erebelo.springairagdemo.constant.TokenConstant.MAX_QUESTION_TOKENS;
import static com.erebelo.springairagdemo.constant.TokenConstant.MAX_RAG_PROMPT_TOKENS;
import static com.erebelo.springairagdemo.constant.TokenConstant.MAX_SYSTEM_MESSAGE_TOKENS;
import static com.erebelo.springairagdemo.util.TokenUtil.limitDocumentsForPrompt;
import static com.erebelo.springairagdemo.util.TokenUtil.limitTextTokens;
import static com.erebelo.springairagdemo.util.TokenUtil.loadTemplateContent;
import static com.erebelo.springairagdemo.util.TokenUtil.tokenUsage;

import com.erebelo.springairagdemo.model.Answer;
import com.erebelo.springairagdemo.model.Question;
import com.erebelo.springairagdemo.service.NBAService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
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

    @Override
    public Answer getAnswer(Question question) {
        String limitedQuestion = limitTextTokens(question.question(), MAX_QUESTION_TOKENS);
        log.info("📌 Question (limited to up to {} tokens): {}", MAX_QUESTION_TOKENS, limitedQuestion);

        List<Document> documents = getDocuments(limitedQuestion);
        List<String> documentContents = limitDocumentsForPrompt(documents);

        Message systemPromptMessage = createMessageFromTemplate(nbaSystemMessageTemplate, MAX_SYSTEM_MESSAGE_TOKENS,
                new HashMap<>(), true);
        Message userMessage = createMessageFromTemplate(ragPromptTemplate, MAX_RAG_PROMPT_TOKENS,
                Map.of("input", limitedQuestion, "documents", String.join("\n", documentContents)), false);

        ChatResponse chatResponse = chatModel.call(new Prompt(List.of(systemPromptMessage, userMessage)));
        String responseContent = chatResponse.getResult().getOutput().getContent();
        log.info("💬 Response: {}", responseContent);

        log.info("🧾 Token Usage -> {}", tokenUsage(chatResponse));
        return new Answer(responseContent);
    }

    private List<Document> getDocuments(String question) {
        if (simpleVectorStore != null) {
            return simpleVectorStore.similaritySearch(SearchRequest.builder().query(question).topK(4).build());
        }
        return vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(4).build());
    }

    private Message createMessageFromTemplate(Resource templateResource, int maxTokens, Map<String, Object> model,
            boolean isSystemMessage) {
        String templateContent = loadTemplateContent(templateResource);
        String limitedContent = limitTextTokens(templateContent, maxTokens);

        PromptTemplate promptTemplate = isSystemMessage
                ? new SystemPromptTemplate(limitedContent)
                : new PromptTemplate(limitedContent);

        return promptTemplate.createMessage(model);
    }
}
