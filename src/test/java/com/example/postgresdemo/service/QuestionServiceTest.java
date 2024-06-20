package com.example.postgresdemo.service;

import com.example.postgresdemo.exception.ResourceNotFoundException;
import com.example.postgresdemo.model.Question;
import com.example.postgresdemo.model.QuestionRequestDTO;
import com.example.postgresdemo.model.QuestionResponseDTO;
import com.example.postgresdemo.repository.QuestionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    QuestionRepository questionRepository;

    @InjectMocks
    QuestionService questionService;

    @Captor
    ArgumentCaptor<Question> questionCaptor;

    @Test
    void testFindAll() {
        Question question1 = new Question();
        question1.setId(1L);
        question1.setTitle("Title1");
        question1.setDescription("Description1");
        Question question2 = new Question();
        question2.setId(2L);
        question2.setTitle("Title2");
        question2.setDescription("Description2");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Question> page = new PageImpl<>(Arrays.asList(question1, question2));
        Mockito.when(questionRepository.findAll(pageable)).thenReturn(page);

        Page<QuestionResponseDTO> result = questionService.findAll(pageable);

        Mockito.verify(questionRepository).findAll(pageable);
        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertEquals(1L, result.getContent().get(0).getId());
        Assertions.assertEquals("Title1\nDescription1", result.getContent().get(0).getBody());
        Assertions.assertEquals(2L, result.getContent().get(1).getId());
        Assertions.assertEquals("Title2\nDescription2", result.getContent().get(1).getBody());
    }

    @ParameterizedTest
    @MethodSource("provideDescriptions")
    void testCreateWithDescriptionVariations(String description, String expectedBody) {
        QuestionRequestDTO request = new QuestionRequestDTO();
        request.setTitle("Title");
        request.setDescription(description);

        Question question = new Question();
        question.setId(1L);
        question.setTitle("Title");
        question.setDescription(description);

        ArgumentCaptor<Question> questionCaptor = ArgumentCaptor.forClass(Question.class);
        Mockito.when(questionRepository.save(questionCaptor.capture())).thenReturn(question);

        QuestionResponseDTO result = questionService.create(request);

        Mockito.verify(questionRepository).save(questionCaptor.capture());

        Question capturedQuestion = questionCaptor.getValue();

        Assertions.assertEquals(request.getTitle(), capturedQuestion.getTitle());
        Assertions.assertEquals(request.getDescription(), capturedQuestion.getDescription());
        Assertions.assertEquals(1L, result.getId());

        Assertions.assertEquals(expectedBody, result.getBody());
    }

    @Test
    void testUpdateExistingQuestion() {
        Long questionId = 123L;
        QuestionRequestDTO request = new QuestionRequestDTO();
        request.setTitle("Title");
        request.setDescription("Description");
        Question questionToUpdate = new Question();
        questionToUpdate.setId(questionId);
        questionToUpdate.setTitle("Old Title");
        questionToUpdate.setDescription("Old Description");

        Mockito.when(questionRepository.findById(questionId)).thenReturn(Optional.of(questionToUpdate));
        Mockito.when(questionRepository.save(questionCaptor.capture())).thenReturn(questionToUpdate);

        QuestionResponseDTO result = questionService.update(questionId, request);

        Mockito.verify(questionRepository).findById(questionId);
        Mockito.verify(questionRepository).save(questionCaptor.capture());

        Question capturedQuestion = questionCaptor.getValue();

        Assertions.assertEquals(request.getTitle(), capturedQuestion.getTitle());
        Assertions.assertEquals(request.getDescription(), capturedQuestion.getDescription());
        Assertions.assertEquals(questionId, result.getId());
    }

    @Test
    void testUpdateNotExistingQuestion() {
        Long questionId = 123L;
        QuestionRequestDTO request = new QuestionRequestDTO();
        request.setTitle("Title");
        request.setDescription("Description");

        Mockito.when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        ResourceNotFoundException resourceNotFoundException = Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            questionService.update(questionId, request);
        });

        Assertions.assertEquals("Question not found with id " + questionId, resourceNotFoundException.getMessage());
        Mockito.verify(questionRepository).findById(questionId);
        Mockito.verifyNoMoreInteractions(questionRepository);
    }

    @Test
    void deleteExistingQuestion() {
        Long questionId = 123L;
        Question questionToDelete = new Question();

        Mockito.when(questionRepository.findById(questionId)).thenReturn(Optional.of(questionToDelete));

        questionService.delete(questionId);

        Mockito.verify(questionRepository).findById(questionId);
        Mockito.verify(questionRepository).delete(questionToDelete);
        Mockito.verifyNoMoreInteractions(questionRepository);
    }

    @Test
    void deleteNonexistentQuestion() {
        Long questionId = 123L;

        Mockito.when(questionRepository.findById(questionId)).thenReturn(Optional.empty());

        ResourceNotFoundException resourceNotFoundException = Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            questionService.delete(questionId);
        });
        Assertions.assertEquals("Question not found with id " + questionId, resourceNotFoundException.getMessage());
        Mockito.verify(questionRepository).findById(questionId);

        Mockito.verifyNoMoreInteractions(questionRepository);
    }

    private static Stream<Arguments> provideDescriptions() {
        return Stream.of(
                Arguments.of(null, "Title\nnull"),
                Arguments.of("", "Title\n"),
                Arguments.of("Some description", "Title\nSome description")
        );
    }
}