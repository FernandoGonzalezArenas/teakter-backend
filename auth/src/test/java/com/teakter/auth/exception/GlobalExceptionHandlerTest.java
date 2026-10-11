package com.teakter.auth.exception;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class GlobalExceptionHandlerTest {

@Test
void  handleValidationError_devuelveMapaDeErrores(){
    //mockeamos el error y el binding
    MethodArgumentNotValidException ex=mock(MethodArgumentNotValidException.class);
    BindingResult br=mock(BindingResult.class);

    //devolvemos una lista con un FieldError
    when(ex.getBindingResult()).thenReturn(br);
    when(br.getFieldErrors()).thenReturn(java.util.List.of(new FieldError("obj", "username", "requerido")));

    GlobalExceptionHandler handler=new GlobalExceptionHandler();
    ResponseEntity<Object> resp=handler.handleValidationError(ex);

    assertEquals(400, resp.getStatusCode().value());
    Map<?, ?> body=(Map<?, ?>) resp.getBody();
    assertEquals("validacion fallida", body.get("message"));

//el mapa fieldErrors contiene la clave
Map<?, ?> errorsMap=(Map<?, ?>) body.get("fieldErrors");
assertNotNull(errorsMap, "el mapa fieldErrors no deberia ser nulo");
assertEquals("requerido", errorsMap.get("username"));
}

@Test
void  handleRuntime_401(){
    GlobalExceptionHandler handler=new GlobalExceptionHandler();
    var resp=handler.handleRuntimeException(new RuntimeException("x"));
    assertEquals(401, resp.getStatusCode().value());
    assertTrue(((Map<?, ?>) resp.getBody()).get("message").toString()
    .contains("acceso no autorizado"));
}

@Test
void  handleGeneral_500(){
    GlobalExceptionHandler handler=new GlobalExceptionHandler();
    var resp=handler.handleGeneralException(new Exception("boom"));
    assertEquals(500, resp.getStatusCode().value());
    assertTrue(((Map<?, ?>) resp.getBody()).get("message").toString()
    .contains("error interno"));
}

}
