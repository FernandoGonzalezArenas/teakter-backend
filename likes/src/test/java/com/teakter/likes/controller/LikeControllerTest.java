package com.teakter.likes.controller;

import com.teakter.likes.Service.LikeService;
import com.teakter.likes.dto.IdsDto;
import com.teakter.likes.dto.VideoLikeStatusDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LikeController.class)
public class LikeControllerTest {

@Autowired
    private MockMvc mockMvc;
@MockBean
    private LikeService service;

@Test
    @WithMockUser
    void toggleLike_ShouldReturnCreated_WhenTrue() throws Exception{
    when(service.toggleLike(eq(1L))).thenReturn(true);

    mockMvc.perform(post("/like/1/toggle").with(csrf()))
            .andExpect(status().isCreated());
}

@Test
    @WithMockUser
    void toggleLike_ShouldReturnNoContent_WhenFalse() throws Exception{
    when(service.toggleLike(eq(1L))).thenReturn(false);

    mockMvc.perform(post("/like/1/toggle").with(csrf()))
            .andExpect(status().isNoContent());
}

@Test
@WithMockUser
    void getLikeStatus_ShouldReturnOkWithJson() throws Exception{
    VideoLikeStatusDto status=new VideoLikeStatusDto(10L, true);
    when(service.getVideoLikeStatus(eq(1L))).thenReturn(status);

    mockMvc.perform(get("/like/1/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLikes").value(10))
            .andExpect(jsonPath("$.isLikedByUser").value(true));
}

@Test
    @WithMockUser
    void getLikesVideosByUserId_ShouldReturnOk() throws Exception{
    IdsDto ids=new IdsDto(List.of(10L, 20L));
    when(service.getLikesVideosByUserId(1L)).thenReturn(ids);

mockMvc.perform(get("/like/user-likes/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ids").isArray())
        .andExpect(jsonPath("$.ids[0]").value(10))
        .andExpect(jsonPath("$.ids[1]").value(20));
}

}
