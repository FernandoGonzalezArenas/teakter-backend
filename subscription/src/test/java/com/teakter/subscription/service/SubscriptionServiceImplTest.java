package com.teakter.subscription.service;
import com.teakter.subscription.dto.SubscriptionStatusDto;
import com.teakter.subscription.dto.UserPrincipal;
import com.teakter.subscription.entity.SubscriptionEntity;
import com.teakter.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository repository;

    @InjectMocks
    private SubscriptionServiceImpl service;

    private final String userId="bfgihjlikhmflkhjfk";

    @BeforeEach
    void setup(){
        UserPrincipal principal=new UserPrincipal(userId, null);
        UsernamePasswordAuthenticationToken auth=new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void toggleSubscription_NewSubscription_ReturnsTrue(){
        //simulamos que no existe una subscripcion previa
        when(repository.findBySubscriberIdAndChannelId("bfgihjlikhmflkhjfk", "jjitdn58erkjg"))
                .thenReturn(Optional.empty());

//ejecutamos el toggle
        boolean result=service.toggleSubscription("jjitdn58erkjg");

        //verificaciones
        assertTrue(result);
        verify(repository, times(1)).save(any(SubscriptionEntity.class));
    }

    @Test
    void toggleSubscription_existingSubscription_DeleteAndReturnsFalse(){
        //simulamos que ya existe la subscripcion
        SubscriptionEntity existing=new SubscriptionEntity(1L, "ndritujelkgdibueoi", "jfhginutrihflhgnrtgtlk");
        when(repository.findBySubscriberIdAndChannelId(userId, "jfhginutrihflhgnrtgtlk")).thenReturn(Optional.of(existing));

        boolean result=service.toggleSubscription("jfhginutrihflhgnrtgtlk");

        //verificaciones
        assertFalse(result);
        verify(repository, times(1)).delete(existing);
    }

    @Test
    void getChannelStatus_ShouldReturnCorrectStatus_ForAuthenticatedUser(){
        when(repository.countByChannelId("jfhginutrihflhgnrtgtlk")).thenReturn(15L);
        when(repository.existsBySubscriberIdAndChannelId(userId, "jfhginutrihflhgnrtgtlk")).thenReturn(true);

        //llamada a el metodo
        SubscriptionStatusDto status=service.getChannelStatus("jfhginutrihflhgnrtgtlk");

        //validaciones
        assertEquals(15L, status.getTotalSubscriptions());
        assertTrue(status.isSubscriptionByUser());
    }

    @Test
    void getChannelStatus_ShouldReturnFalse_IfGestUser(){
        SecurityContextHolder.clearContext();
        when(repository.countByChannelId("jfhginutrihflhgnrtgtlk")).thenReturn(15L);

        SubscriptionStatusDto status=service.getChannelStatus("jfhginutrihflhgnrtgtlk");

        //validaciones
        assertEquals(15L, status.getTotalSubscriptions());
        assertFalse(status.isSubscriptionByUser());
    }

    @Test
    void shouldReturnThrows_WhenChannelIdIsInvalid(){
        assertThrows(ResponseStatusException.class, () -> service.countSubscriptions(""));
    }

    @Test
    @DisplayName("debe borrar las subscripciones y subscriptores de un usuario")
    void shouldDeleteSubscriptionsOfUser(){
assertDoesNotThrow(() -> service.deleteSubscriptionsOfUser("jfhginutrihflhgnrtgtlk"));
verify(repository).deleteSubscriptionsByChannelId("jfhginutrihflhgnrtgtlk");
verify(repository).deleteSubscriptorsOfChannelId("jfhginutrihflhgnrtgtlk");
    }

}
