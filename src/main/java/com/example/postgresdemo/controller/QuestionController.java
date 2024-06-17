package com.example.postgresdemo.controller;

import com.example.postgresdemo.model.QuestionRequestDTO;
import com.example.postgresdemo.model.QuestionResponseDTO;
import com.example.postgresdemo.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController

public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @GetMapping("/questions")
    public Page<QuestionResponseDTO> getQuestions(Pageable pageable) {
        return questionService.findAll(pageable);
    }

    @PostMapping("/questions")
    public QuestionResponseDTO createQuestion(@Valid @RequestBody QuestionRequestDTO question) {
        return questionService.create(question);
    }

    @PutMapping("/questions/{questionId}")
    public QuestionResponseDTO updateQuestion(@PathVariable Long questionId,
                                              @Valid @RequestBody QuestionRequestDTO questionRequest) {
        return questionService.update(questionId, questionRequest);
    }

    @DeleteMapping("/questions/{questionId}")
    public void deleteQuestion(@PathVariable Long questionId) {
        questionService.delete(questionId);
    }
}
