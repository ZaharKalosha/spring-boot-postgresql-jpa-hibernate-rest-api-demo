package com.example.postgresdemo.controller;

import com.example.postgresdemo.model.Question;
import com.example.postgresdemo.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

//@SpringBootTest
//@WebMvcTest
@WebMvcTest(QuestionController.class)
public class QuestionControllerTest {
//    private QuestionRepository questionRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateMockMvc() {
        assertNotNull(mockMvc);
    }

    @Test
    public void testGetQuestions() throws Exception {
//        when(questionRepository.findAll()).thenReturn(List.of(new Question()));

        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/questions"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
