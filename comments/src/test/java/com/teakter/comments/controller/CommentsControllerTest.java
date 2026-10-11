package com.teakter.comments.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teakter.comments.dto.CommentDtoEntrada;
import com.teakter.comments.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentsController.class)
public class CommentsControllerTest {

@Autowired
    private MockMvc mockMvc;
@MockBean
    private CommentService commentService;
@Autowired
    private ObjectMapper mapper;

@Test
    @WithMockUser
    void createComment_ShouldReturnBadRequest_WhenContentIsTooLong() throws  Exception{
    //un comentario de mas de 1200 caracteres
    String longContent="a".repeat(1202);
    CommentDtoEntrada entrada=new CommentDtoEntrada(1L, longContent);
    mockMvc.perform(post("/comentarios")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(entrada)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("validacion fallida"))
            .andExpect(jsonPath("$.fieldErrors.content").exists());
}

}
