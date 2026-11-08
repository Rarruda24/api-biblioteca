package com.biblioteca.api.service;

import com.biblioteca.api.model.ChaveApi;
import com.biblioteca.api.model.PlanoAcesso;
import com.biblioteca.api.repository.ChaveApiRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
public class ChaveApiService {
    private final ChaveApiRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();
    public ChaveApiService(ChaveApiRepository repository) { this.repository = repository; }

    public CriacaoChave criar(String nome, PlanoAcesso plano) {
        byte[] bytes = new byte[32]; secureRandom.nextBytes(bytes);
        String chaveEmTexto = "bib_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        ChaveApi chave = new ChaveApi();
        chave.setNome(nome.trim()); chave.setPlano(plano); chave.setHashChave(sha256(chaveEmTexto));
        repository.save(chave);
        return new CriacaoChave(chave.getId(), chave.getNome(), chaveEmTexto, chave.getPlano(),
                chave.getPlano().getLimitePorMinuto(), chave.getCriadaEm());
    }

    public ChaveApi validar(String chaveEmTexto) {
        return repository.findByHashChaveAndAtivaTrue(sha256(chaveEmTexto))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "X-API-Key inválida ou inativa"));
    }

    public List<ResumoChave> listar() {
        return repository.findAll().stream().map(chave -> new ResumoChave(chave.getId(), chave.getNome(),
                chave.getPlano(), chave.getPlano().getLimitePorMinuto(), chave.isAtiva(), chave.getCriadaEm())).toList();
    }

    public void revogar(Long id) {
        ChaveApi chave = repository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Chave de API não encontrada"));
        chave.setAtiva(false); repository.save(chave);
    }

    private String sha256(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception ex) { throw new IllegalStateException("Não foi possível processar a chave de API", ex); }
    }

    public record CriacaoChave(Long id, String nome, String apiKey, PlanoAcesso plano, int limitePorMinuto,
                               java.time.LocalDateTime criadaEm) {}
    public record ResumoChave(Long id, String nome, PlanoAcesso plano, int limitePorMinuto,
                              boolean ativa, java.time.LocalDateTime criadaEm) {}
}
