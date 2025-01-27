package com.erebelo.springairagdemo.service;

import com.erebelo.springairagdemo.model.Answer;
import com.erebelo.springairagdemo.model.Question;

public interface NBAService {

    Answer getAnswer(Question question);

}
