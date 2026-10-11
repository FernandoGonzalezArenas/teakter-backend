package com.teakter.video.service;

import com.teakter.video.client.CommentsClient;
import com.teakter.video.client.LikesClient;
import com.teakter.video.client.SubscriptionsClient;
import com.teakter.video.dto.CommentsDto;
import com.teakter.video.dto.IdsDto;
import com.teakter.video.dto.LikeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class InteractionsServiceTest {

    @Mock
    private CommentsClient commentsClient;

    @Mock
    private LikesClient likesClient;

    @Mock
    private SubscriptionsClient subscriptionsClient;

    @InjectMocks
    private InteractionsService service;

    @Test
    @DisplayName("debe retornar comentarios cuando el cliente responde OK")
    void shouldReturnCommentsOnSuccess(){
        when(commentsClient.getCommentsByVideo(1L, null, 10))
                .thenReturn(List.of(CommentsDto.builder().usernameAuthor("user1").content("hola").build()));
        List<CommentsDto> result=service.getCommentsByVideo(1L, null, 10);

        //validaciones
        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getUsernameAuthor());
    }

    @Test
    @DisplayName("debe retornar fallback manual y retornar lista por defecto ante error")
void shouldExecuteFallbackLogic(){
        List<CommentsDto> result=service.fallbackForComments(1L, null, 10, new RuntimeException("error feign"));

assertEquals(1, result.size());
assertEquals("sistema", result.get(0).getUsernameAuthor());
assertTrue(result.get(0).getContent().contains("temporalmente no disponibles"));
    }

@Test
@DisplayName("debe retornar conteo de likes cuando el cliente responde OK")
void shouldReturnLikesCountOnSuccess(){
    LikeStatus status=new LikeStatus(44L, true);
        when(likesClient.likeStatusInVideo(1L)).thenReturn(status);
        LikeStatus likes=service.likeStatusInVideo(1L);
        assertEquals(44L, likes.getTotalLikes());
}

@Test
    @DisplayName("debe retornar 0 likes al ejecutar fallback por error")
    void shouldReturnZeroLikesOnFallback(){
        LikeStatus likes=service.fallbackForLikes(1L, new RuntimeException("error feign"));
        assertEquals(0L, likes.getTotalLikes());
}

}
