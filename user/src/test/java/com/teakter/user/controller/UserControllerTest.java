package com.teakter.user.controller;

import com.teakter.user.dto.UploadUrlResponse;
import com.teakter.user.dto.UserDto;
import com.teakter.user.dto.UserDtoEntrada;
import com.teakter.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

@Autowired
    private MockMvc mockMvc;

@MockBean
    private UserService userService;

@Test
    void getPublicProfile_ReturnsUser() throws Exception{
    UserDto user=UserDto.builder()
            .username("pedro")
            .bio("hola")
            .privacyLikes(true)
            .privacySubs(false)
            .build();
when(userService.getProfile(1L)).thenReturn(user);

mockMvc.perform(get("/users/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("pedro"));
}

@Test
    void updateMyProfile_ReturnsUpdatedUsser() throws Exception{
    UserDto update=UserDto.builder()
            .username("raul")
            .bio("nueva bio")
            .privacySubs(true)
            .privacyLikes(false)
            .build();
    when(userService.updateProfile(any())).thenReturn(update);

mockMvc.perform(put("/users/me")
        .contentType("application/json")
        .content("{\"bio\": \"nueva bio\", \"profilePictureUrl\": \"http://foto.jpg\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.bio").value("nueva bio"));
verify(userService, times(1)).updateProfile(any(UserDtoEntrada.class));
}

@Test
    @DisplayName("debe retornar 200 cuando se elimine la cuenta de el usuario")
@WithMockUser(roles = "ADMIN")
    void shouldReturnOk_WhenUserWillBeDeleted() throws Exception{
mockMvc.perform(delete("/users/internal/1")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().string("la cuenta fue borrada correctamente por un administrador"));
}

@Test
    @DisplayName("debe generar la solicitud GET y resultar como respuesta la URL firmada de la foto de perfil y el nombre unico de el archivo")
    void shouldGenerateRequestForUploadUrlForProfilePicture() throws Exception{
    when(userService.getUploadUrl(anyString())).thenReturn(new UploadUrlResponse("http://storage/profile.jpg", "UUID-profile-jpg"));

    mockMvc.perform(get("/users/upload-ppu")
            .param("filename", "profile.jpg"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uploadUrl").value("http://storage/profile.jpg"));
}

}
