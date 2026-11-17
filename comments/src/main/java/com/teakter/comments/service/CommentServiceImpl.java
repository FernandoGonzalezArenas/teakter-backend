package com.teakter.comments.service;

import com.teakter.comments.dto.CommentDtoEntrada;
import com.teakter.comments.dto.CommentsDto;
import com.teakter.comments.dto.UpdateCommentDto;
import com.teakter.comments.dto.UserPrincipal;
import com.teakter.comments.entity.CommentEntity;
import com.teakter.comments.repository.CommentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService{

    private final CommentRepository commentRepository;

//guardar un comentario
@Override
    public CommentsDto saveComments(CommentDtoEntrada comment){
    UserPrincipal principal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String userId=principal.userId();
    String username=principal.username();
    CommentEntity comentario=new CommentEntity();
    comentario.setVideoId(comment.getVideoId());
    comentario.setUserId(userId);
    comentario.setUsernameAuthor(username);
    comentario.setContent(comment.getContent());

    CommentEntity savedComment = commentRepository.save(comentario);

    return mapToDto(savedComment);
}

//obtener los comentarios de un video
    @Override
    public List<CommentsDto> getCommentsByVideo(String videoId, Long lastId, Integer limit){
     Pageable pageable= PageRequest.of(0, limit);
        List<CommentEntity> comments= commentRepository.findNextComments(videoId, lastId, pageable);

        return comments.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    //contar los comentarios de un video
    public  Long countCommentsInVideo(String videoId){
    if (videoId==null || videoId.isBlank()){
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "videoId invalido");
    }
    return commentRepository.countByVideoId(videoId);
    }

    //eliminar un comentario
@Override
    public void  deleteComment(Long id){
UserPrincipal principal=(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
String username=principal.username();

    CommentEntity comment= commentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "comentario no encontrado"));

//validacion de autoría
    if (!comment.getUsernameAuthor().equals(username)){
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "no puedes borrar un comentario ajeno");
    }

    commentRepository.deleteById(id);
    }

    //editar un comentario
@Override
    public CommentsDto editComment(Long id, UpdateCommentDto content){
UserPrincipal principal =(UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
String username=principal.username();

    CommentEntity comment=commentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (!comment.getUsernameAuthor().equals(username)){
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "no tienes permiso para modificar este comentario");
    }
    comment.setContent(content.getContent());
    return mapToDto(commentRepository.save(comment));
 }

 @Override
 @Transactional
 public void deleteCommentsWithVideoId(String videoId){
commentRepository.deleteByVideoId(videoId);
 }

 @Override
 @Transactional
 public void deleteCommentsByUserId(String userId){
    commentRepository.deleteByUserId(userId);
 }

 private CommentsDto mapToDto(CommentEntity comment){
return CommentsDto.builder()
        .id(comment.getId())
        .videoId(comment.getVideoId())
        .userId(comment.getUserId())
        .usernameAuthor(comment.getUsernameAuthor())
        .content(comment.getContent())
        .createdAt(comment.getCreatedAt().toString())
        .updatedAt(comment.getUpdatedAt().toString()).build();
 }

}
