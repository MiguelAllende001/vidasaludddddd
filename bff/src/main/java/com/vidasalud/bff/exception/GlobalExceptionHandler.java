package com.vidasalud.bff.exception;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Cuando appointments/catalog devuelven 4xx/5xx, RestClient lo lanza
    // como excepción. Acá lo transformamos de vuelta en la misma
    // respuesta (mismo status, mismo body) para que el frontend la vea
    // igual que si hubiera llamado al microservicio directo.
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<String> handleDownstreamError(RestClientResponseException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ex.getResponseBodyAsString());
    }

    @ExceptionHandler(DownstreamException.class)
    public ResponseEntity<String> handleDownstreamException(DownstreamException ex) {
        return ResponseEntity.status(ex.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ex.getBody());
    }
}
