package com.erebelo.springairagdemo.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TokenConstant {

    public static final int MAX_SYSTEM_MESSAGE_TOKENS = 250;
    public static final int MAX_RAG_PROMPT_TOKENS = 150;
    public static final int MAX_DOCUMENT_TOKENS = 3000;
    public static final int MAX_QUESTION_TOKENS = 50;
    public static final int CHARS_TO_TOKEN = 4; // Assuming 1 token = 4 characters (for English text)

}
