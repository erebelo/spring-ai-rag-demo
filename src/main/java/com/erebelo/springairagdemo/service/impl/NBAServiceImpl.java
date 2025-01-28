package com.erebelo.springairagdemo.service.impl;

import com.erebelo.springairagdemo.model.Answer;
import com.erebelo.springairagdemo.model.Question;
import com.erebelo.springairagdemo.service.NBAService;
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
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NBAServiceImpl implements NBAService {

    private final SimpleVectorStore simpleVectorStore;
    private final VectorStore vectorStore;
    private final ChatModel chatModel;

    @Value("classpath:/templates/nba-system-message.st")
    private Resource nbaSystemMessageTemplate;

    @Value("classpath:/templates/rag-prompt-template.st")
    private Resource ragPromptTemplate;

    @Override
    public Answer getAnswer(Question question) {
        PromptTemplate systemPromptTemplate = new SystemPromptTemplate(nbaSystemMessageTemplate);
        Message systemPromptMessage = systemPromptTemplate.createMessage();

        List<Document> documents = getDocuments(question.question());
        List<String> documentContents = documents.stream().map(Document::getText).toList();

        PromptTemplate promptTemplate = new PromptTemplate(ragPromptTemplate);
        Message userMessage = promptTemplate
                .createMessage(Map.of("input", question.question(), "documents", String.join("\n", documentContents)));

        ChatResponse chatResponse = chatModel.call(new Prompt(List.of(systemPromptMessage, userMessage)));
        return new Answer(chatResponse.getResult().getOutput().getContent());
    }

    private List<Document> getDocuments(String question) {
        if (simpleVectorStore != null) {
            return simpleVectorStore.similaritySearch(SearchRequest.builder().query(question).topK(5).build());
        }
        return vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(5).build());
    }
}
