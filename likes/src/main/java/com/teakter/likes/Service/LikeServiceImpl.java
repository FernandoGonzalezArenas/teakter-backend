package com.teakter.likes.Service;

import com.teakter.likes.dto.IdsDto;
import com.teakter.likes.dto.UserPrincipal;
import com.teakter.likes.dto.VideoLikeStatusDto;
import com.teakter.likes.entity.LikeEntity;
import com.teakter.likes.repository.LikeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService{

private final LikeRepository likeRepository;

//agregar o eliminar like de el video
    @Override
    @Transactional
public boolean toggleLike(String videoId){
UserPrincipal principal= (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
String userId= principal.userId();

        if (videoId==null || videoId.isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "videoId invalido o nulo");
        }

            Optional<LikeEntity> existingLike = likeRepository.findByUserIdAndVideoId(userId, videoId);
            if (existingLike.isPresent()) {
                likeRepository.delete(existingLike.get());
                return false;
            } else {
                try {
                    LikeEntity nuevoLike = LikeEntity.builder()
                            .videoId(videoId)
                            .userId(userId)
                            .build();
                    likeRepository.save(nuevoLike);
                    return true;
                }catch (DataIntegrityViolationException e){
                    //solo retornamos true porque el like ya esta registrado
                    return true;
                }
            }
}

@Override
public Boolean hasUserLiked(String videoId){
    if (videoId==null || videoId.isBlank()){
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "videoId invalido o nulo");
    }
var auth=SecurityContextHolder.getContext().getAuthentication();
    if (auth==null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")){
        return false;
    }
UserPrincipal principal=(UserPrincipal) auth.getPrincipal();
String userId= principal.userId();
    return likeRepository.existsByUserIdAndVideoId(userId, videoId);
}

//cuenta los likes de el video
@Override
public  long countLikes(String videoId){
    if (videoId==null || videoId.isBlank()){
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "videoId invalido o nulo");
    }
    return likeRepository.countByVideoId(videoId);
}

@Override
    public VideoLikeStatusDto getVideoLikeStatus(String videoId){
    Long total=countLikes(videoId);
    boolean liked=hasUserLiked(videoId);
    return new VideoLikeStatusDto(total, liked);
}

@Override
    public IdsDto getLikesVideosByUserId(String userId){
        //se obtienen los videos a los que el usuario dio like
    List<LikeEntity> result=likeRepository.findByUserId(userId);

//se crea una lista con los videoId de la lista de entidades obtenida previamente
    List<String> videoIds=result.stream()
            .map(LikeEntity::getVideoId)
            .collect(Collectors.toList());

    //se retorna el DTO de los id obtenidos
    return new IdsDto(videoIds);
}

@Override
    @Transactional
    public void deleteLikesVideo(String videoId){
        likeRepository.deleteByVideoId(videoId);
}

@Override
    @Transactional
    public void deleteLikesByUserId(String userId){
        likeRepository.deleteByUserId(userId);
}

}
