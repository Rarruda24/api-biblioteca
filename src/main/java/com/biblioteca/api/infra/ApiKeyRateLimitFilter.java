
package com.biblioteca.api.infra;

import com.biblioteca.api.model.ChaveApi;
import com.biblioteca.api.model.PlanoAcesso;
import com.biblioteca.api.service.ChaveApiService;
import tools.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class ApiKeyRateLimitFilter extends OncePerRequestFilter {

    private final ChaveApiService chaveApiService;
    private final ObjectMapper objectMapper;
    private final AuthenticationFailureTracker authenticationFailures;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public ApiKeyRateLimitFilter(
            ChaveApiService chaveApiService,
            ObjectMapper objectMapper,
            AuthenticationFailureTracker authenticationFailures
    ) {
        this.chaveApiService = chaveApiService;
        this.objectMapper = objectMapper;
        this.authenticationFailures = authenticationFailures;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();
        boolean gerenciamentoChaves = path.equals("/api/chaves-api") || path.startsWith("/api/chaves-api/");

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || !(path.startsWith("/api/v2/") || gerenciamentoChaves)
                || (path.equals("/api/chaves-api") && "POST".equalsIgnoreCase(request.getMethod()));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/api/v2/")) {
            String versao = request.getHeader("X-API-Version");
            if (versao != null && !versao.isBlank() && !"2".equals(versao.trim())) {
                escreverErro(response, 400, "A rota V2 aceita somente X-API-Version: 2.", request.getRequestURI());
                return;
            }
        }

        String client = request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
        Duration activeLockout = authenticationFailures.remainingLockout(client);
        String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null || apiKey.isBlank()) {
            if (activeLockout != null) {
                escreverBloqueio(response, activeLockout, request.getRequestURI());
                return;
            }
            registrarFalhaAutenticacao(response, client,
                    "Cabeçalho X-API-Key obrigatório. Gere uma chave em POST /api/chaves-api.", request.getRequestURI());
            return;
        }

        ChaveApi chave;

        try {
            chave = chaveApiService.validar(apiKey);
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            if (activeLockout != null) {
                escreverBloqueio(response, activeLockout, request.getRequestURI());
                return;
            }
            registrarFalhaAutenticacao(response, client,
                    ex.getReason() == null ? "Chave de API inválida." : ex.getReason(), request.getRequestURI());
            return;
        }

        authenticationFailures.registerSuccess(client);

        PlanoAcesso plano = chave.getPlano();

        if (request.getRequestURI().startsWith("/api/chaves-api")
                && !"POST".equalsIgnoreCase(request.getMethod())
                && plano != PlanoAcesso.PREMIUM) {
            escreverErro(response, 403, "Apenas chaves do plano PREMIUM podem consultar ou revogar chaves de API.", request.getRequestURI());
            return;
        }

        int limite = plano.getLimitePorMinuto();

        Bucket bucket = buckets.computeIfAbsent(
                "chave-" + chave.getId(),
                ignored -> criarBucket(limite)
        );

        ConsumptionProbe probe =
                bucket.tryConsumeAndReturnRemaining(1);

        response.setHeader(
                "X-RateLimit-Limit",
                String.valueOf(limite)
        );

        response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(probe.getRemainingTokens())
        );
        response.setHeader("X-RateLimit-Reset", String.valueOf(Instant.now().plusSeconds(60).getEpochSecond()));

        if (!probe.isConsumed()) {

            long segundos = Math.max(
                    1,
                    (long) Math.ceil(
                            probe.getNanosToWaitForRefill()
                                    / 1_000_000_000.0
                    )
            );

            response.setHeader(
                    "Retry-After",
                    String.valueOf(segundos)
            );
            response.setHeader("X-RateLimit-Reset", String.valueOf(Instant.now().plusSeconds(segundos).getEpochSecond()));

            escreverErro(
                    response,
                    429,
                    "Limite do plano " + plano
                            + " excedido. Tente novamente em "
                            + segundos + " segundo(s).", request.getRequestURI()
            );

            return;
        }

        request.setAttribute("planoAcesso", plano.name());
        request.setAttribute("idChaveApi", chave.getId());

        filterChain.doFilter(request, response);
    }

    private Bucket criarBucket(int limite) {

        Bandwidth regra = Bandwidth.classic(
                limite,
                Refill.intervally(limite, Duration.ofMinutes(1))
        );

        return Bucket.builder()
                .addLimit(regra)
                .build();
    }

    private void registrarFalhaAutenticacao(HttpServletResponse response, String client,
                                             String mensagem, String path) throws IOException {
        Duration bloqueio = authenticationFailures.registerFailure(client);
        if (bloqueio != null) {
            escreverBloqueio(response, bloqueio, path);
            return;
        }
        escreverErro(response, 401, mensagem, path);
    }

    private void escreverBloqueio(HttpServletResponse response, Duration duracao, String path) throws IOException {
        long segundos = Math.max(1, (long) Math.ceil(duracao.toMillis() / 1000.0));
        response.setHeader("Retry-After", String.valueOf(segundos));
        escreverErro(response, 429,
                "Muitas falhas de autenticação. Acesso temporariamente bloqueado; tente novamente em "
                        + segundos + " segundo(s).", path);
    }

    private void escreverErro(
            HttpServletResponse response,
            int status,
            String mensagem,
            String path
    ) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                Map.of("status", status, "mensagem", mensagem, "campos", Map.of(),
                        "timestamp", LocalDateTime.now(), "caminho", path)
        );
    }
}
