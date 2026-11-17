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
    when(service.toggleLike(eq("gksjdjo834uter8"))).thenReturn(true);

    mockMvc.perform(post("/like/gksjdjo834uter8/toggle").with(csrf()))
            .andExpect(status().isCreated());
}

@Test
    @WithMockUser
    void toggleLike_ShouldReturnNoContent_WhenFalse() throws Exception{
    when(service.toggleLike(eq("gksjdjo834uter8"))).thenReturn(false);

    mockMvc.perform(post("/like/gsdjto8eujlkdk/toggle").with(csrf()))
            .andExpect(status().isNoContent());
}

@Test
@WithMockUser
    void getLikeStatus_ShouldReturnOkWithJson() throws Exception{
    VideoLikeStatusDto status=new VideoLikeStatusDto(10L, true);
    when(service.getVideoLikeStatus(eq("gksjdjo834uter8"))).thenReturn(status);

    mockMvc.perform(get("/like/gksjdjo834uter8/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLikes").value(10L))
            .andExpect(jsonPath("$.isLikedByUser").value(true));
}

@Test
    @WithMockUser
    void getLikesVideosByUserId_ShouldReturnOk() throws Exception{
    IdsDto ids=new IdsDto(List.of("gksjdjo834uter8", "gksjdjo834utjite4d"));
    when(service.getLikesVideosByUserId("fskdjrr84ehkdhkj")).thenReturn(ids);

mockMvc.perform(get("/like/user-likes/fskdjrr84ehkdhkj"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ids").isArray())
        .andExpect(jsonPath("$.ids[0]").value("gksjdjo834uter8"))
        .andExpect(jsonPath("$.ids[1]").value("gksjdjo834utjite4d"));
}

}
