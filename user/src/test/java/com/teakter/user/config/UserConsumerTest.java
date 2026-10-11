package com.teakter.user.config;

import com.teakter.user.dto.UserRabbitDto;
import com.teakter.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class UserConsumerTest {

@Mock
    private UserService service;

@InjectMocks
    private UserConsumer userConsumer;

@Test
    void consumerRegistrationMessage_CallsService(){
    UserRabbitDto userRabbitDto=new UserRabbitDto(1L, "newuser");

    //ejecutamos el metodo de consumo manualmente
    userConsumer.consumeRegistrationMessage(userRabbitDto);

    //verificamos que el servicio fue invocado con los datos correctos
verify(service, times(1)).createInitialProfile(1L, "newuser");
}

}
