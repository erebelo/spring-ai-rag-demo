package com.erebelo.springairagdemo.controller;

import static com.erebelo.springairagdemo.constant.BusinessConstant.NBA_PATH;
import static com.erebelo.springairagdemo.constant.BusinessConstant.NBA_STANDINGS_ASK_PATH;

import com.erebelo.springairagdemo.model.Answer;
import com.erebelo.springairagdemo.model.Question;
import com.erebelo.springairagdemo.service.NBAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(NBA_PATH)
public class NBAController {

    private final NBAService service;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = NBA_STANDINGS_ASK_PATH, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Answer askQuestions(@RequestBody Question question) {
        log.info("GET {}", NBA_PATH + NBA_STANDINGS_ASK_PATH);
        return service.getAnswer(question);
    }
}
