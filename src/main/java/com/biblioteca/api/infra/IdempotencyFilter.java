package com.biblioteca.api.infra;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(2)
public class IdempotencyFilter extends OncePerRequestFilter {
    private static final Duration TTL = Duration.ofHours(24);
    private final ObjectMapper objectMapper;
    private final Map<String, RespostaArmazenada> respostas = new ConcurrentHashMap<>();
    private final Object[] locks = new Object[256];

    public IdempotencyFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        java.util.Arrays.setAll(locks, ignored -> new Object());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !"POST".equalsIgnoreCase(request.getMethod())
                || !path.startsWith("/api/v2/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String key = request.getHeader("X-Idempotency-Key");
        if (key == null || key.isBlank()) {
            writeError(response, 400, "O cabeçalho X-Idempotency-Key é obrigatório para requisições POST.", request.getRequestURI());
            return;
        }
        if (key.length() > 100) {
            writeError(response, 400, "X-Idempotency-Key deve possuir no máximo 100 caracteres.", request.getRequestURI());
            return;
        }

        byte[] body = request.getInputStream().readAllBytes();
        String fingerprint = sha256(body);
        String clientId = String.valueOf(request.getAttribute("idChaveApi"));
        String id = clientId + ":" + request.getRequestURI() + ":" + key;
        Object lock = locks[Math.floorMod(id.hashCode(), locks.length)];
        synchronized (lock) {
                Instant now = Instant.now();
                respostas.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
                RespostaArmazenada stored = respostas.get(id);
                if (stored != null) {
                    if (!stored.fingerprint().equals(fingerprint)) {
                        writeError(response, 409, "Esta X-Idempotency-Key já foi utilizada com outro conteúdo.", request.getRequestURI());
                        return;
                    }
                    replay(stored, response);
                    return;
                }

                ContentCachingResponseWrapper wrapped = new ContentCachingResponseWrapper(response);
                chain.doFilter(new RepetibleRequest(request, body), wrapped);
                if (wrapped.getStatus() >= 200 && wrapped.getStatus() < 300) {
                    Map<String, java.util.List<String>> headers = new ConcurrentHashMap<>();
                    for (String name : wrapped.getHeaderNames()) headers.put(name, java.util.List.copyOf(wrapped.getHeaders(name)));
                    respostas.put(id, new RespostaArmazenada(fingerprint, wrapped.getStatus(), headers,
                            wrapped.getContentAsByteArray(), Instant.now().plus(TTL)));
                }
                wrapped.copyBodyToResponse();
        }
    }

    private void replay(RespostaArmazenada stored, HttpServletResponse response) throws IOException {
        response.setStatus(stored.status());
        stored.headers().forEach((name, values) -> values.forEach(value -> response.addHeader(name, value)));
        response.setHeader("X-Idempotency-Replayed", "true");
        response.getOutputStream().write(stored.body());
    }

    private String sha256(byte[] value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value)); }
        catch (Exception ex) { throw new IllegalStateException("Falha ao calcular impressão digital da requisição", ex); }
    }

    private void writeError(HttpServletResponse response, int status, String message, String path) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of("status", status, "mensagem", message,
                "campos", Map.of(), "timestamp", LocalDateTime.now(), "caminho", path));
    }

    private record RespostaArmazenada(String fingerprint, int status, Map<String, java.util.List<String>> headers,
                                       byte[] body, Instant expiresAt) { }

    private static class RepetibleRequest extends HttpServletRequestWrapper {
        private final byte[] body;
        RepetibleRequest(HttpServletRequest request, byte[] body) { super(request); this.body = body; }
        @Override public ServletInputStream getInputStream() {
            ByteArrayInputStream input = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override public int read() { return input.read(); }
                @Override public boolean isFinished() { return input.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener listener) { }
            };
        }
        @Override public BufferedReader getReader() { return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8)); }
    }
}
