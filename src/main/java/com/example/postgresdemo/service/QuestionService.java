package com.example.postgresdemo.service;

import com.example.postgresdemo.exception.ResourceNotFoundException;
import com.example.postgresdemo.model.Question;
import com.example.postgresdemo.model.QuestionRequestDTO;
import com.example.postgresdemo.model.QuestionResponseDTO;
import com.example.postgresdemo.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;

    public Page<QuestionResponseDTO> findAll(Pageable pageable) {
        return questionRepository.findAll(pageable)
                .map(this::toQuestionResponseDTO);
    }

    public QuestionResponseDTO create(QuestionRequestDTO questionRequest) {
        Question question = toQuestion(questionRequest);
        return toQuestionResponseDTO(questionRepository.save(question));
    }

    public QuestionResponseDTO update(Long questionId, QuestionRequestDTO questionRequest) {
        Question question = toQuestion(questionRequest);
        return questionRepository.findById(questionId)
                .map(foundQuestion -> {
                    foundQuestion.setTitle(question.getTitle());
                    foundQuestion.setDescription(question.getDescription());
                    return toQuestionResponseDTO(questionRepository.save(foundQuestion));
                }).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));

    }

    public ResponseEntity<?> delete(Long questionId) {
        return questionRepository.findById(questionId)
                .map(question -> {
                    questionRepository.delete(question);
                    return ResponseEntity.ok().build();
                }).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
    }

    private QuestionResponseDTO toQuestionResponseDTO(Question question) {
        QuestionResponseDTO questionResponse = new QuestionResponseDTO();
        questionResponse.setId(question.getId());
        questionResponse.setBody(question.getTitle() + "\n" + question.getDescription());


        return questionResponse;
    }

    private Question toQuestion(QuestionRequestDTO questionRequestDTO) {
        Question question = new Question();
        question.setTitle(questionRequestDTO.getTitle());
        question.setDescription(questionRequestDTO.getDescription());
        return question;
    }
}
