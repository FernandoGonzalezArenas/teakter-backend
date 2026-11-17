package com.teakter.video.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teakter.video.dto.*;
import com.teakter.video.service.VideoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoController.class)
public class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoService videoService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @DisplayName("POST/save-metadata deve validar entrada y retornar 400 si titulo vacío")
    @WithMockUser(username = "123")
    void shouldReturnBadRequestWhenTitleIsBlank() throws  Exception{
        VideoDtoEntrada entradaInvalida=VideoDtoEntrada.builder()
                .title("")
                .description("descripcion valida")
                .duration(38L)
                .build();

        mockMvc.perform(post("/videos/save-metadata")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(entradaInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    @DisplayName("GET/upload-url debe verificar token y retornar url firmada")
    @WithMockUser(username = "100")
    void shouldGetUploadUrlWithAuth() throws  Exception{
        when(videoService.generateUploadUrl(anyString()))
                .thenReturn(new UploadUrlResponse("http://minio/url", "uuid-video.mp4"));

        mockMvc.perform(get("/videos/upload-url")
                        .param("filename", "mivideo.mp4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").value("http://minio/url"));
    }

    @Test
    @DisplayName("GET /upload-url - debe retornar 401 si el usuario no esta autenticado")
    void shouldReturn401WhenAnonymous() throws Exception{
        mockMvc.perform(get("/videos/upload-url")
                .param("filename", "mivideo.mp4"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /interactions/{videoId} debe retornar interacciones con paginacion")
    @WithMockUser(username = "1")
    void shouldGetInteractionsWithPagination() throws Exception{
        InteractionsDto interacciones = InteractionsDto.builder()
                        .comments(Collections.emptyList())
                                .totalComments(37L)
                                        .status(new LikeStatus(5L, true))
                                                .build();

        when(videoService.getInteractions("fskuhi7whhjsguyewg", 100L, 5))
                .thenReturn(interacciones);

        mockMvc.perform(get("/videos/interactions/fskuhi7whhjsguyewg")
                .param("lastId", "100")
                .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalComments").value(37));
    }

@Test
    @DisplayName("GET /videos/{videoId}/play debe retornar 200 y el DTO de reproduccion")
    @WithMockUser
    void shouldReturnPlaybackData() throws Exception{
    VideoPlaybackDto playback=VideoPlaybackDto.builder().videoUrl("http://signed.com").duration(83L).build();
    when(videoService.getVideoForPlayback("fskuhi7whhjsguyewg")).thenReturn(playback);

    mockMvc.perform(get("/videos/fskuhi7whhjsguyewg/play"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.videoUrl").value("http://signed.com"));
    }

    @Test
    @DisplayName("POST videos/list-likes debe retornar lista de videos por IDs")
    @WithMockUser
    void shouldGetVideosBySpecificIds() throws Exception {
        IdsDto ids = new IdsDto(List.of("fskuhi7whhjsguyewg", "fshhi7wyywy7hd"));
        when(videoService.getVideosByIds(eq("fskuhi7whhjsguyewg"), any(Instant.class), anyInt())).thenReturn(Collections.emptyList());

mockMvc.perform(get("/videos/list-likes/fskuhi7whhjsguyewg")
        .with(csrf())
                .param("lastCreatedAt", "2026-04-27T16:47:37.264Z")
                .param("limit", "5"))
        .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /videos/my-feed-subs debe retornar feed de subscripciones")
    @WithMockUser
    void shouldReturnsSubsFeed() throws Exception{
        when(videoService.getSubscriptionsFeed(eq("fskuhi7whhjsguyewg"), any(Instant.class), anyInt())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/videos/my-feed-subs/fskuhi7whhjsguyewg")
                        .param("lastCreatedAt", "2025-11-30T09:37:58.538Z")
                        .param("limit", "5"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /videos/internal/{id}, debe retornar 200 al eliminar")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnOkOnInternalDelete() throws Exception{
mockMvc.perform(delete("/videos/internal/1")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().string("video eliminado exitosamente por el administrador"));
    }

    @Test
    @DisplayName("GET /videos/internal/details/{id}, debe retornar detalles de el video")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnVideoDetailsForAdmin() throws Exception{
        VideoDto dto1=VideoDto.builder()
                .title("Admin View").duration(59L).build();
        VideoDto dto2=VideoDto.builder()
                .title("Admin View2").duration(69L).build();

        when(videoService.videoInternalDetails(List.of("fskuhi7whhjsguyewg", "sjdfhi7whhjeds"))).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/videos/internal/details")
                        .param("ids", "fskuhi7whhjsguyewg,sjdfhi7whhjeds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Admin View"));
    }

    @Test
    @WithMockUser
    void shouldGetUploadUrlThumb() throws Exception{
        when(videoService.generateUploadUrlThumb(anyString())).thenReturn(new UploadUrlResponse("http://storage/thumb", "uuid-thumb.jpg"));

        mockMvc.perform(get("/videos/upload-tu")
                .param("filename", "portada.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").value("http://storage/thumb"));
    }

    @Test
    @WithMockUser
    void  shouldSearchVideos() throws  Exception{
        Page<VideoDto> emptyPage=new PageImpl<>(Collections.emptyList());
        when(videoService.searchVideosByTitle(anyString(), anyString(), anyInt(), anyInt())).thenReturn(emptyPage);

        mockMvc.perform(get("/videos/search")
                .param("keyword", "spring")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }
}
