package com.example.postgresdemo.controller;

import com.example.postgresdemo.model.Question;
import com.example.postgresdemo.repository.QuestionRepository;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


import java.nio.CharBuffer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest

@AutoConfigureMockMvc
public class QuestionControllerTest {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void deleteQuestions() {
        questionRepository.deleteAll();
    }

    @Test
    void testGetQuestionsWithAmountLessThanPageSize() throws Exception {
        int assertionNumber = 10;
        int pageSize = 20;

        fillQuestions(assertionNumber);

        mockMvc.perform(get("/questions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.equalTo(assertionNumber)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.equalTo(assertionNumber)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.equalTo(1)));
    }

    @Test
    void testGetQuestionsWithAmountMoreThanPageSize() throws Exception {
        int assertionNumber = 30;
        int pageSize = 20;
        int totalPages = (int) Math.ceil(assertionNumber / (double) pageSize);

        fillQuestions(assertionNumber);

        mockMvc.perform(get("/questions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.equalTo(pageSize)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.equalTo(assertionNumber)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.equalTo(totalPages)));
    }

    @Test
    void testCreateCorrectQuestion() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"title\": \"Question 1\",\n" +
                                "    \"description\": \"Description 1\"\n" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body", Matchers.equalTo("Question 1\nDescription 1")));
    }

    @Test
    void testCreateQuestionWithoutTitle() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"body\": \"\\nDescription\"\n" +
                                "}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCreateQuestionWithTitleLesThenThreeChars() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"title\": \"Te\",\n" +
                                "    \"description\": \"Description\"\n" +
                                "}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCreateQuestionWithTitleMoreThenHundredChars() throws Exception {
        int numberOfChars = 101;
        String title = CharBuffer.allocate(numberOfChars).toString().replace('\0', 'T');

        mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"title\": \"" + title + "\",\n" +
                                "    \"description\": \"Description\"\n" +
                                "}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCreateQuestionWithoutDescription() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"title\": \"Question 1\"\n" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body", Matchers.equalTo("Question 1\nnull")));}

    @Test
    void testUpdateQuestion() throws Exception {
        fillQuestions(1);
        long questionId = questionRepository.findAll().get(0).getId();

        mockMvc.perform(MockMvcRequestBuilders.put("/questions/" + questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"title\": \"Edited Question 1\",\n" +
                                "    \"description\": \"Edited Description 1\"\n" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.body", Matchers.equalTo("Edited Question 1\nEdited Description 1")));
    }

    @Test
    void testUpdateQuestionWithNonExistingId() throws Exception {
        fillQuestions(1);
        long questionId = questionRepository.findAll().get(0).getId();

        mockMvc.perform(MockMvcRequestBuilders.put("/questions/" + (questionId + 1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"title\": \"Edited Question 1\",\n" +
                                "    \"description\": \"Edited Description 1\"\n" +
                                "}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testDeleteQuestion() throws Exception {
        fillQuestions(1);
        long questionId = questionRepository.findAll().get(0).getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/questions/" + questionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private void fillQuestions(Integer number) {
        for (int i = 0; i < number; i++) {
            Question question = new Question();
            question.setTitle("Question " + i);
            question.setDescription("Description " + i);
            questionRepository.save(question);
        }
    }
}
