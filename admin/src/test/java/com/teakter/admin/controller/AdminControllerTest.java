package com.teakter.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teakter.admin.dto.AuditorDto;
import com.teakter.admin.dto.ReportCreateDto;
import com.teakter.admin.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdminControllerTest {

@Autowired
    private MockMvc mockMvc;

@MockBean
    private AdminService adminService;

@Autowired
    private ObjectMapper mapper;

@Test
    void shouldReturnOkWhenDeleteVideo() throws Exception{
    AuditorDto dto=new AuditorDto("contenido inapropiado");

mockMvc.perform(delete("/admin/videos/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(dto)))
        .andExpect(status().isOk());
}

@Test
    void shouldReturnOkWhenBannedUser() throws Exception{
    AuditorDto dto=new AuditorDto("violencia");

    mockMvc.perform(delete("/admin/users/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
}

@Test
    void shouldReturnOkWhenCreatedReport() throws Exception{
    ReportCreateDto dto=new ReportCreateDto("VIOLENCIA", "contenido muy violento");

    mockMvc.perform(post("/admin/reports/VIDEO/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(dto)))
            .andExpect(status().isOk());
}

@Test
    void shouldReturnPendingReports() throws Exception{

    mockMvc.perform(get("/admin/reports/pending"))
            .andExpect(status().isOk());
}

@Test
    void shouldReturnOkWhenMarcDismissReport() throws Exception{
    mockMvc.perform(patch("/admin/reports/1/dismiss")
                    .param("type", "VIDEO"))
            .andExpect(status().isOk());
}

}
