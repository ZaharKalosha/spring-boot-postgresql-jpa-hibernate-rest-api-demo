package com.example.postgresdemo.controller;

import com.example.postgresdemo.model.Question;
import com.example.postgresdemo.repository.QuestionRepository;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;


import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
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

    @Test
    public void testCreateMockMvc() {
        assertNotNull(mockMvc);
    }

    private boolean fillQuestions(Integer number) {
        try {
            for (int i = 0; i < number; i++) {
                Question question = new Question();
                question.setTitle("Question " + i);
                question.setDescription("Description " + i);
                questionRepository.save(question);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean deleteQuestions() {
        try {
            questionRepository.deleteAll();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    public void testGetQuestions() throws Exception {
        for (int assertionNumber = 0; assertionNumber < 100; assertionNumber++) {
            int pageSize = 20;

            if (!fillQuestions(assertionNumber)) {
                throw new Exception("Failed to fill questions");
            }

            mockMvc.perform(get("/questions")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()", Matchers.equalTo(Math.min(assertionNumber, pageSize))))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", Matchers.equalTo(assertionNumber))) // Assert total elements
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.equalTo((int) Math.ceil(assertionNumber / (double) pageSize)))); // Assert total pages for 10 items per page

            if (!deleteQuestions()) {
                throw new Exception("Failed to delete questions");
            }
        }
    }

}
