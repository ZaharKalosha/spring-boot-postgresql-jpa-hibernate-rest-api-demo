package com.example.postgresdemo.controller;

import com.example.postgresdemo.model.Question;
import com.example.postgresdemo.repository.QuestionRepository;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;


import java.nio.CharBuffer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest

@AutoConfigureMockMvc
public class QuestionControllerTest {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private QuestionController questionController;

    private void fillQuestions(Integer number) {
        for (int i = 0; i < number; i++) {
            Question question = new Question();
            question.setTitle("Question " + i);
            question.setDescription("Description " + i);
            questionRepository.save(question);
        }
    }

    @BeforeEach
    @AfterEach
    public void deleteQuestions() {
        questionRepository.deleteAll();
    }

    @Test
    public void testGetQuestionsWithAmountLessThanPageSize() throws Exception {
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
    public void testGetQuestionsWithAmountMoreThanPageSize() throws Exception {
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
    public void testCreateCorrectQuestion() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"title\": \"Question 1\",\n" +
                                "    \"description\": \"Description 1\"\n" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.equalTo("Question 1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.equalTo("Description 1")));
    }

    @Test
    public void testCreateQuestionWithoutTitle() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"description\": \"Description\"\n" +
                                "}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testCreateQuestionWithTitleLesThenThreeChars() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"title\": \"Te\",\n" +
                                "    \"description\": \"Description\"\n" +
                                "}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testCreateQuestionWithTitleMoreThenHundredChars() throws Exception {
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
    public void testCreateQuestionWithoutDescription() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\n" +
                                "    \"title\": \"Question 1\"\n" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.equalTo("Question 1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.equalTo(null)));
    }

    @Test
    public void testUpdateQuestion() throws Exception {
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", Matchers.equalTo("Edited Question 1")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.equalTo("Edited Description 1")));
    }

    @Test
    public void testUpdateQuestionWithNonExistingId() throws Exception {
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
    public void testDeleteQuestion() throws Exception {
        fillQuestions(1);
        long questionId = questionRepository.findAll().get(0).getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/questions/" + questionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
