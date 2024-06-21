package com.example.postgresdemo.service;

import com.example.postgresdemo.exception.ResourceNotFoundException;
import com.example.postgresdemo.model.Question;
import com.example.postgresdemo.model.QuestionRequestDTO;
import com.example.postgresdemo.model.QuestionResponseDTO;
import com.example.postgresdemo.model.User;
import com.example.postgresdemo.repository.QuestionRepository;
import com.example.postgresdemo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;


    public QuestionService(QuestionRepository questionRepository, UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    public Page<QuestionResponseDTO> findAll(Pageable pageable) {
        return questionRepository.findAll(pageable)
                .map(this::toQuestionResponseDTO);
    }

    public QuestionResponseDTO create(QuestionRequestDTO questionRequest) {
        User user = userRepository.findById(questionRequest.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + questionRequest.getAuthorId()));
        Question question = toQuestion(questionRequest, user);
        return toQuestionResponseDTO(questionRepository.save(question));
    }

    public QuestionResponseDTO update(Long questionId, QuestionRequestDTO questionRequest) {
        User user = userRepository.findById(questionRequest.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + questionRequest.getAuthorId()));
        Question question = toQuestion(questionRequest, user);
        return questionRepository.findById(questionId)
                .map(foundQuestion -> {
                    foundQuestion.setTitle(question.getTitle());
                    foundQuestion.setDescription(question.getDescription());
                    foundQuestion.setUser(user);
                    return toQuestionResponseDTO(questionRepository.save(foundQuestion));
                }).orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
    }

    public void delete(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + questionId));
        questionRepository.delete(question);
    }

    private QuestionResponseDTO toQuestionResponseDTO(Question question) {
        QuestionResponseDTO questionResponse = new QuestionResponseDTO();
        questionResponse.setId(question.getId());
        questionResponse.setBody(String.join("\n", question.getTitle(), question.getDescription()));


        return questionResponse;
    }

    private Question toQuestion(QuestionRequestDTO questionRequestDTO, User user) {
        Question question = new Question();
        question.setTitle(questionRequestDTO.getTitle());
        question.setDescription(questionRequestDTO.getDescription());
        question.setUser(user);
        return question;
    }
}
