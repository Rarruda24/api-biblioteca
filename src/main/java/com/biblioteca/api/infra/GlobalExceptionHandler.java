package com.biblioteca.api.infra;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroApi> tratarValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        return ResponseEntity.badRequest().body(erro(400, "Erro de validação", campos, request));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroApi> tratarStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return ResponseEntity.status(status).body(erro(status.value(), ex.getReason() == null ? status.getReasonPhrase() : ex.getReason(), Map.of(), request));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErroApi> tratarCabecalho(MissingRequestHeaderException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(erro(400, "Cabeçalho obrigatório ausente: " + ex.getHeaderName(), Map.of(), request));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroApi> tratarJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(erro(400, "Corpo da requisição inválido ou com formato incorreto.", Map.of(), request));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroApi> tratarIntegridade(DataIntegrityViolationException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro(409, "A operação viola uma restrição de integridade ou um campo único.", Map.of(), request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroApi> tratarErroInterno(Exception ex, HttpServletRequest request) {
        return ResponseEntity.internalServerError().body(erro(500, "Ocorreu um erro interno ao processar a requisição.", Map.of(), request));
    }

    private ErroApi erro(int status, String mensagem, Map<String, String> campos, HttpServletRequest request) {
        return new ErroApi(status, mensagem, campos, LocalDateTime.now(), request.getRequestURI());
    }

    public record ErroApi(int status, String mensagem, Map<String, String> campos, LocalDateTime timestamp, String caminho) { }
}
