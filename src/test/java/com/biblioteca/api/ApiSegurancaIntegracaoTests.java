package com.biblioteca.api;

import com.biblioteca.api.model.PlanoAcesso;
import com.biblioteca.api.infra.AuthenticationFailureTracker;
import com.biblioteca.api.repository.AutorRepository;
import com.biblioteca.api.repository.ChaveApiRepository;
import com.biblioteca.api.service.ChaveApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSegurancaIntegracaoTests {
    @Autowired MockMvc mvc;
    @Autowired ChaveApiService chaves;
    @Autowired AutorRepository autores;
    @Autowired ChaveApiRepository chavesRepository;
    @Autowired AuthenticationFailureTracker authenticationFailures;

    @BeforeEach
    void limparFalhasDeAutenticacao() {
        authenticationFailures.clear();
    }

    private String novaChave(PlanoAcesso plano) {
        return chaves.criar("teste-" + UUID.randomUUID(), plano).apiKey();
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor origem(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    @Test
    void geraChaveEProtegeRecursos() throws Exception {
        mvc.perform(post("/api/chaves-api").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"chave teste\",\"plano\":\"BASICO\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.apiKey").isNotEmpty());
        mvc.perform(get("/api/v1/autores")).andExpect(status().isOk());
        mvc.perform(get("/api/v2/autores")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/chaves-api")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v2/autores").header("X-API-Key", "bib_" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
        String falsa = "bib_" + UUID.randomUUID();
        mvc.perform(post("/api/v2/autores").header("X-API-Key", falsa).header("X-Idempotency-Key", falsa)
                        .header("X-API-Version", "2").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Nao autorizada\",\"nacionalidade\":\"Brasil\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v2/autores").header("X-API-Key", novaChave(PlanoAcesso.BASICO)))
                .andExpect(status().isOk());
        String chaveGerada = novaChave(PlanoAcesso.BASICO);
        String hashChave = java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                .digest(chaveGerada.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        var chavePersistida = chavesRepository.findByHashChaveAndAtivaTrue(hashChave).orElseThrow();
        assertThat(chavePersistida.getHashChave()).isNotEqualTo(chaveGerada);
        assertThat(chavePersistida.getPlano()).isEqualTo(PlanoAcesso.BASICO);
        mvc.perform(get("/api/chaves-api").header("X-API-Key", novaChave(PlanoAcesso.BASICO)))
                .andExpect(status().isForbidden());
        String admin = novaChave(PlanoAcesso.PREMIUM);
        mvc.perform(get("/api/chaves-api").header("X-API-Key", admin)).andExpect(status().isOk());
        var revoked = chaves.criar("revogar-" + UUID.randomUUID(), PlanoAcesso.BASICO);
        mvc.perform(delete("/api/chaves-api/" + revoked.id()).header("X-API-Key", novaChave(PlanoAcesso.BASICO)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/chaves-api/" + revoked.id()).header("X-API-Key", admin))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v2/autores").header("X-API-Key", revoked.apiKey())).andExpect(status().isUnauthorized());
    }

    @Test
    void bloqueiaTemporariamenteIpAposCincoFalhasDeAutenticacao() throws Exception {
        String ip = "198.51.100.27";
        for (int tentativa = 1; tentativa < AuthenticationFailureTracker.MAX_FAILURES; tentativa++) {
            mvc.perform(get("/api/v2/autores").with(origem(ip)).header("X-API-Key", "chave-invalida-" + tentativa))
                    .andExpect(status().isUnauthorized());
        }
        mvc.perform(get("/api/v2/autores").with(origem(ip)).header("X-API-Key", "chave-invalida-final"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "900"))
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("falhas de autenticação")));

        mvc.perform(get("/api/v2/autores").with(origem(ip)).header("X-API-Key", novaChave(PlanoAcesso.PREMIUM)))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v2/autores").with(origem(ip)).header("X-API-Key", "outra-chave-invalida"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/v2/autores").with(origem("198.51.100.28"))
                        .header("X-API-Key", novaChave(PlanoAcesso.BASICO)))
                .andExpect(status().isOk());
    }

    @Test
    void idempotenciaReproduzRespostaConflitaComCorpoDiferenteEEvitaDuplicidade() throws Exception {
        String key = novaChave(PlanoAcesso.PROFISSIONAL);
        String idempotency = UUID.randomUUID().toString();
        String body = "{\"nome\":\"Ada Lovelace\",\"nacionalidade\":\"Britânica\"}";
        int antes = (int) autores.count();
        MvcResult first = mvc.perform(post("/api/v2/autores").header("X-API-Key", key)
                        .header("X-Idempotency-Key", idempotency).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").value(org.hamcrest.Matchers.containsString("/api/v2/autores/")))
                .andReturn();
        mvc.perform(post("/api/v2/autores").header("X-API-Key", key)
                        .header("X-Idempotency-Key", idempotency).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(header().string("X-Idempotency-Replayed", "true"))
                .andExpect(content().json(first.getResponse().getContentAsString()));
        mvc.perform(post("/api/v2/autores").header("X-API-Key", key)
                        .header("X-Idempotency-Key", idempotency).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Grace Hopper\",\"nacionalidade\":\"Americana\"}"))
                .andExpect(status().isConflict());
        assertThat(autores.count()).isEqualTo(antes + 1);

        String retryKey = UUID.randomUUID().toString();
        mvc.perform(post("/api/v2/autores").header("X-API-Key", key).header("X-Idempotency-Key", retryKey)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\",\"nacionalidade\":\"Brasil\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v2/autores").header("X-API-Key", key).header("X-Idempotency-Key", retryKey)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Retry Ada\",\"nacionalidade\":\"Britanica\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void validaEntradaVersoesCorsEOpenApi() throws Exception {
        String key = novaChave(PlanoAcesso.PREMIUM);
        for (String path : new String[]{"/api/v1/autores", "/api/v2/autores", "/api/v1/editoras", "/api/v2/editoras",
                "/api/v1/livros", "/api/v2/livros", "/api/v1/categorias", "/api/v2/categorias",
                "/api/v1/leitores", "/api/v2/leitores", "/api/v1/emprestimos", "/api/v2/emprestimos"}) {
            mvc.perform(get(path).header("X-API-Key", path.contains("/v2/") ? key : "")).andExpect(status().isOk());
        }
        mvc.perform(post("/api/v1/autores")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\",\"nacionalidade\":\"Brasil\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.caminho").value("/api/v1/autores"));
        mvc.perform(post("/api/v1/autores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"V1 sem chave " + UUID.randomUUID() + "\",\"nacionalidade\":\"Brasil\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").value(org.hamcrest.Matchers.containsString("/api/v1/autores/")));
        mvc.perform(post("/api/v2/autores").header("X-API-Key", key).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"V2 sem idempotencia\",\"nacionalidade\":\"Brasil\"}"))
                .andExpect(status().isBadRequest());
        String nomeCategoria = "Categoria V1 " + UUID.randomUUID();
        String categoriaBody = "{\"nome\":\"" + nomeCategoria + "\",\"descricao\":\"Teste de duplicidade\"}";
        mvc.perform(post("/api/v1/categorias").contentType(MediaType.APPLICATION_JSON).content(categoriaBody))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/categorias").contentType(MediaType.APPLICATION_JSON).content(categoriaBody))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.mensagem").value("Categoria já cadastrada."));
        mvc.perform(get("/api/v2/autores/999999").header("X-API-Key", key))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.timestamp").exists());
        mvc.perform(options("/api/v2/autores").header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,x-api-key,x-idempotency-key"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        for (String origin : new String[]{"http://localhost:3000", "http://127.0.0.1:5500"}) {
            mvc.perform(options("/api/v2/autores").header("Origin", origin)
                            .header("Access-Control-Request-Method", "DELETE")
                            .header("Access-Control-Request-Headers", "x-api-key,x-api-version"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", origin))
                    .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("DELETE")))
                    .andExpect(header().exists("Access-Control-Allow-Headers"));
        }
        for (String method : new String[]{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"}) {
            mvc.perform(options("/api/v2/autores").header("Origin", "http://localhost:3000")
                            .header("Access-Control-Request-Method", method)
                            .header("Access-Control-Request-Headers", "content-type,x-api-key,x-idempotency-key,x-api-version"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
        }
        mvc.perform(options("/api/v2/autores").header("Origin", "https://malicioso.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/v3/api-docs/V1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.info.version").value("1.0.0"))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("Fluxo recomendado")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("422 Entidade Não Processável")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Contrato original"))))
                .andExpect(jsonPath("$.paths['/api/v1/autores']").exists())
                .andExpect(jsonPath("$.paths['/api/chaves-api']").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/autores'].post.requestBody.content['application/json'].example.nome").exists())
                .andExpect(jsonPath("$.paths['/api/v1/autores'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/autores'].post.responses['401']").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/autores'].post.parameters[?(@.name == 'X-API-Version')]").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/autores'].post.parameters[?(@.name == 'X-Idempotency-Key')] ").doesNotExist());
        mvc.perform(get("/v3/api-docs/V2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.info.version").value("2.0.0"))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("Copie o valor bib_... retornado")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("X-Idempotency-Key")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("5 falhas em até 10 minutos")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("15 minutos")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("1 hora")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("24 horas")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("429 Too Many Requests")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("não contam como falhas de autenticação")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("300")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("Fluxo recomendado")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.containsString("422 Entidade Não Processável")))
                .andExpect(jsonPath("$.info.description").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Contrato evoluído"))))
                .andExpect(jsonPath("$.paths['/api/chaves-api'].post").exists())
                .andExpect(jsonPath("$.paths['/api/chaves-api'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/api/chaves-api'].get.security[0].ApiKeyAuth").exists())
                .andExpect(jsonPath("$.paths['/api/chaves-api'].get.responses['401']").exists())
                .andExpect(jsonPath("$.paths['/api/chaves-api'].get.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/v2/autores']").exists())
                .andExpect(jsonPath("$.paths['/api/v2/autores'].get.security[0].ApiKeyAuth").exists())
                .andExpect(jsonPath("$.paths['/api/v2/autores'].get.responses['429'].description")
                        .value(org.hamcrest.Matchers.containsString("bloqueio temporário")))
                .andExpect(jsonPath("$.paths['/api/v2/autores'].get.parameters[?(@.name == 'X-API-Version')]").exists())
                .andExpect(jsonPath("$.paths['/api/v2/editoras'].get.parameters[?(@.name == 'X-API-Version')]").exists())
                .andExpect(jsonPath("$.paths['/api/v2/livros'].get.parameters[?(@.name == 'X-API-Version')]").exists())
                .andExpect(jsonPath("$.paths['/api/v2/categorias'].get.parameters[?(@.name == 'X-API-Version')]").exists())
                .andExpect(jsonPath("$.paths['/api/v2/leitores'].get.parameters[?(@.name == 'X-API-Version')]").exists())
                .andExpect(jsonPath("$.paths['/api/v2/emprestimos'].get.parameters[?(@.name == 'X-API-Version')]").exists())
                .andExpect(jsonPath("$.paths['/api/v2/autores'].post.parameters[?(@.name == 'X-Idempotency-Key')]").exists())
                .andExpect(jsonPath("$.paths['/api/v2/autores'].post.parameters[1].required").value(true));
        mvc.perform(get("/v3/api-docs/V1"))
                .andExpect(jsonPath("$.paths['/api/v1/emprestimos'].post.responses['422'].description")
                        .value(org.hamcrest.Matchers.containsString("data prevista de")));
        mvc.perform(get("/v3/api-docs/V2"))
                .andExpect(jsonPath("$.paths['/api/v2/emprestimos'].post.responses['422'].description")
                        .value(org.hamcrest.Matchers.containsString("422 Entidade")));
        mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urls[?(@.name == 'V1')]").exists())
                .andExpect(jsonPath("$.urls[?(@.name == 'V2')]").exists())
                .andExpect(jsonPath("$.urls[?(@.name == 'Compatibilidade')]").doesNotExist());
        String openApiV2 = mvc.perform(get("/v3/api-docs/V2")).andReturn().getResponse().getContentAsString();
        for (String resource : new String[]{"autores", "editoras", "livros", "categorias", "leitores", "emprestimos"}) {
            String collection = "/api/v2/" + resource;
            assertJsonPathExists(openApiV2, "$.paths['" + collection + "'].get.responses['200']");
            assertJsonPathExists(openApiV2, "$.paths['" + collection + "'].post.responses['201']");
            assertJsonPathExists(openApiV2, "$.paths['" + collection + "/{id}'].get.responses['200']");
            assertJsonPathExists(openApiV2, "$.paths['" + collection + "/{id}'].put.responses['200']");
            assertJsonPathExists(openApiV2, "$.paths['" + collection + "/{id}'].delete.responses['204']");
            assertJsonPathExists(openApiV2, "$.paths['" + collection + "/buscar'].get.responses['200']");
            assertJsonPathExists(openApiV2, "$.paths['" + collection + "'].post.requestBody.content['application/json']");
            assertJsonPathExists(openApiV2, "$.paths['" + collection + "'].post.requestBody.content['application/json'].example");
        }
        mvc.perform(get("/api/v1/livros").header("X-API-Version", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.versao").value(1)).andExpect(jsonPath("$.livros").exists());
        mvc.perform(get("/api/v2/livros").header("X-API-Key", key).header("X-API-Version", "1"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v2/livros").header("X-API-Key", key).header("X-API-Version", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.page").exists());
        mvc.perform(get("/api/v2/livros").header("X-API-Key", key).header("X-API-Version", "9"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/livros").header("X-API-Version", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.versao").value(1));
        mvc.perform(get("/api/livros").header("X-API-Version", "2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.page").exists());
    }

    @Test
    void rateLimitBasicoRetorna429ESeparaCliente() throws Exception {
        String basic = novaChave(PlanoAcesso.BASICO);
        String other = novaChave(PlanoAcesso.BASICO);
        for (int i = 0; i < 30; i++) {
            mvc.perform(get("/api/v2/autores").header("X-API-Key", basic))
                    .andExpect(status().isOk()).andExpect(header().string("X-RateLimit-Limit", "30"));
        }
        mvc.perform(get("/api/v2/autores").header("X-API-Key", basic))
                .andExpect(status().isTooManyRequests()).andExpect(header().exists("Retry-After"))
                .andExpect(header().exists("X-RateLimit-Reset"));
        mvc.perform(get("/api/v2/autores").header("X-API-Key", other)).andExpect(status().isOk());
    }

    @Test
    void limitesDosPlanosProfissionalEPremium() throws Exception {
        for (PlanoAcesso plano : new PlanoAcesso[]{PlanoAcesso.PROFISSIONAL, PlanoAcesso.PREMIUM}) {
            String key = novaChave(plano);
            int limite = plano.getLimitePorMinuto();
            for (int i = 0; i < limite; i++) {
                mvc.perform(get("/api/v2/autores").header("X-API-Key", key))
                        .andExpect(status().isOk()).andExpect(header().string("X-RateLimit-Limit", String.valueOf(limite)));
            }
            mvc.perform(get("/api/v2/autores").header("X-API-Key", key))
                    .andExpect(status().isTooManyRequests()).andExpect(header().exists("Retry-After"));
        }
    }

    @Test
    void repeticoesConcorrentesCriamUmUnicoRegistro() throws Exception {
        String apiKey = novaChave(PlanoAcesso.PREMIUM);
        String idempotency = UUID.randomUUID().toString();
        String body = "{\"nome\":\"Grace Hopper\",\"nacionalidade\":\"Americana\"}";
        int antes = (int) autores.count();
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first = CompletableFuture.supplyAsync(() -> enviarCriacao(apiKey, idempotency, body), pool);
            var second = CompletableFuture.supplyAsync(() -> enviarCriacao(apiKey, idempotency, body), pool);
            assertThat(first.join()).isEqualTo(201);
            assertThat(second.join()).isEqualTo(201);
        } finally { pool.shutdownNow(); }
        assertThat(autores.count()).isEqualTo(antes + 1);
    }

    @Test
    void crudConsultasPaginacaoValidacaoELinksDosSeisModulosV2() throws Exception {
        String key = novaChave(PlanoAcesso.PREMIUM);
        String sufixo = UUID.randomUUID().toString().replace("-", "");

        String autorBody = "{\"nome\":\"Autor" + sufixo + "\",\"nacionalidade\":\"Brasileira\",\"dataNascimento\":\"1980-01-01\"}";
        MvcResult autorCriado = criarV2(key, "/api/v2/autores", autorBody);
        assertThat(autorCriado.getResponse().getStatus()).isEqualTo(201);
        assertLinksV2(autorCriado);
        long autorId = id(autorCriado);

        String editoraBody = "{\"nome\":\"Editora" + sufixo + "\",\"pais\":\"Brasil\"}";
        MvcResult editoraCriada = criarV2(key, "/api/v2/editoras", editoraBody);
        assertThat(editoraCriada.getResponse().getStatus()).isEqualTo(201);
        assertLinksV2(editoraCriada);
        long editoraId = id(editoraCriada);

        String categoriaBody = "{\"nome\":\"Categoria" + sufixo + "\",\"descricao\":\"Teste integrado\"}";
        MvcResult categoriaCriada = criarV2(key, "/api/v2/categorias", categoriaBody);
        assertThat(categoriaCriada.getResponse().getStatus()).isEqualTo(201);
        assertLinksV2(categoriaCriada);
        long categoriaId = id(categoriaCriada);

        String email = "leitor" + sufixo + "@teste.com";
        String leitorBody = "{\"nome\":\"Leitor" + sufixo + "\",\"email\":\"" + email + "\",\"telefone\":\"11999999999\"}";
        MvcResult leitorCriado = criarV2(key, "/api/v2/leitores", leitorBody);
        assertThat(leitorCriado.getResponse().getStatus()).isEqualTo(201);
        assertLinksV2(leitorCriado);
        long leitorId = id(leitorCriado);

        String isbn = "978" + sufixo.substring(0, 10);
        String titulo = "Livro" + sufixo;
        String livroBody = "{\"titulo\":\"" + titulo + "\",\"isbn\":\"" + isbn + "\",\"anoPublicacao\":2020,\"quantidadePaginas\":200,\"autor\":{\"id\":" + autorId + "},\"editora\":{\"id\":" + editoraId + "},\"categorias\":[{\"id\":" + categoriaId + "}]}";
        MvcResult livroCriado = criarV2(key, "/api/v2/livros", livroBody);
        assertThat(livroCriado.getResponse().getStatus()).isEqualTo(201);
        assertLinksV2(livroCriado);
        long livroId = id(livroCriado);

        String datasInvalidas = "{\"dataEmprestimo\":\"2026-01-08\",\"dataPrevistaDevolucao\":\"2026-01-01\",\"status\":\"ATIVO\",\"leitor\":{\"id\":" + leitorId + "},\"livro\":{\"id\":" + livroId + "}}";
        mvc.perform(post("/api/v2/emprestimos").header("X-API-Key", key).header("X-API-Version", "2")
                        .header("X-Idempotency-Key", UUID.randomUUID().toString()).contentType(MediaType.APPLICATION_JSON).content(datasInvalidas))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.status").value(422));

        String emprestimoBody = "{\"dataEmprestimo\":\"2026-01-01\",\"dataPrevistaDevolucao\":\"2026-01-08\",\"status\":\"ATIVO\",\"leitor\":{\"id\":" + leitorId + "},\"livro\":{\"id\":" + livroId + "}}";
        MvcResult emprestimoCriado = criarV2(key, "/api/v2/emprestimos", emprestimoBody);
        assertThat(emprestimoCriado.getResponse().getStatus()).isEqualTo(201);
        assertLinksV2(emprestimoCriado);
        long emprestimoId = id(emprestimoCriado);

        String[][] resources = {
                {"autores", Long.toString(autorId), "buscar?nome=Autor" + sufixo},
                {"editoras", Long.toString(editoraId), "buscar?nome=Editora" + sufixo},
                {"categorias", Long.toString(categoriaId), "buscar?nome=Categoria" + sufixo},
                {"leitores", Long.toString(leitorId), "buscar?nome=Leitor" + sufixo},
                {"livros", Long.toString(livroId), "buscar?titulo=" + titulo},
                {"emprestimos", Long.toString(emprestimoId), "buscar?status=ATIVO"}
        };
        for (String[] resource : resources) {
            String base = "/api/v2/" + resource[0];
            mvc.perform(get(base).param("page", "0").param("size", "1").header("X-API-Key", key).header("X-API-Version", "2"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.page.size").value(1));
            mvc.perform(get(base + "/" + resource[1]).header("X-API-Key", key).header("X-API-Version", "2"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$._links.self.href").value(org.hamcrest.Matchers.containsString(base + "/" + resource[1])));
            mvc.perform(get(base + "/" + resource[2]).header("X-API-Key", key).header("X-API-Version", "2"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.page").exists());
        }

        mvc.perform(put("/api/v2/autores/" + autorId).header("X-API-Key", key).header("X-API-Version", "2")
                        .contentType(MediaType.APPLICATION_JSON).content(autorBody.replace("Autor" + sufixo, "AutorAtualizado" + sufixo)))
                .andExpect(status().isOk());
        mvc.perform(put("/api/v2/editoras/" + editoraId).header("X-API-Key", key).header("X-API-Version", "2")
                        .contentType(MediaType.APPLICATION_JSON).content(editoraBody.replace("Editora" + sufixo, "EditoraAtualizada" + sufixo)))
                .andExpect(status().isOk());
        mvc.perform(put("/api/v2/categorias/" + categoriaId).header("X-API-Key", key).header("X-API-Version", "2")
                        .contentType(MediaType.APPLICATION_JSON).content(categoriaBody.replace("Categoria" + sufixo, "CategoriaAtualizada" + sufixo)))
                .andExpect(status().isOk());
        mvc.perform(put("/api/v2/leitores/" + leitorId).header("X-API-Key", key).header("X-API-Version", "2")
                        .contentType(MediaType.APPLICATION_JSON).content(leitorBody.replace(email, "atualizado" + sufixo + "@teste.com")))
                .andExpect(status().isOk());
        mvc.perform(put("/api/v2/livros/" + livroId).header("X-API-Key", key).header("X-API-Version", "2")
                        .contentType(MediaType.APPLICATION_JSON).content(livroBody.replace(titulo, "LivroAtualizado" + sufixo)))
                .andExpect(status().isOk());
        mvc.perform(put("/api/v2/emprestimos/" + emprestimoId).header("X-API-Key", key).header("X-API-Version", "2")
                        .contentType(MediaType.APPLICATION_JSON).content(emprestimoBody.replace("ATIVO", "DEVOLVIDO")))
                .andExpect(status().isOk());

        for (String[] invalid : new String[][]{
                {"autores", "{\"nome\":\"\",\"nacionalidade\":\"Brasil\"}"},
                {"editoras", "{\"nome\":\"Editora inv\u00e1lida\",\"pais\":\"\"}"},
                {"categorias", "{\"nome\":\"\"}"},
                {"leitores", "{\"nome\":\"Leitor inv\u00e1lido\",\"email\":\"invalido\",\"telefone\":\"11999999999\"}"},
                {"livros", "{\"titulo\":\"\",\"isbn\":\"123\",\"anoPublicacao\":0,\"quantidadePaginas\":0}"},
                {"emprestimos", "{\"status\":\"ATIVO\"}"}
        }) {
            mvc.perform(post("/api/v2/" + invalid[0]).header("X-API-Key", key).header("X-API-Version", "2")
                            .header("X-Idempotency-Key", UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON).content(invalid[1]))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos").isNotEmpty());
        }
        mvc.perform(post("/api/v2/livros").header("X-API-Key", key).header("X-API-Version", "2")
                        .header("X-Idempotency-Key", UUID.randomUUID().toString()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"Livro sem relacionamentos\",\"isbn\":\"9780000000001\",\"anoPublicacao\":2020,\"quantidadePaginas\":100,\"categorias\":[]}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.autor").exists()).andExpect(jsonPath("$.campos.editora").exists());
        mvc.perform(post("/api/v2/emprestimos").header("X-API-Key", key).header("X-API-Version", "2")
                        .header("X-Idempotency-Key", UUID.randomUUID().toString()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataEmprestimo\":\"2026-01-01\",\"dataPrevistaDevolucao\":\"2026-01-08\",\"status\":\"ATIVO\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.leitor").exists()).andExpect(jsonPath("$.campos.livro").exists());

        for (int i = resources.length - 1; i >= 0; i--) {
            String[] resource = resources[i];
            String base = "/api/v2/" + resource[0] + "/" + resource[1];
            mvc.perform(delete(base).header("X-API-Key", key).header("X-API-Version", "2"))
                    .andExpect(status().isNoContent());
            mvc.perform(get(base).header("X-API-Key", key).header("X-API-Version", "2"))
                    .andExpect(status().isNotFound());
        }
    }

    private MvcResult criarV2(String key, String path, String json) throws Exception {
        return mvc.perform(post(path).header("X-API-Key", key).header("X-API-Version", "2")
                        .header("X-Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andReturn();
    }

    private long id(MvcResult result) throws Exception {
        Number value = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return value.longValue();
    }

    private void assertLinksV2(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        assertThat(com.jayway.jsonpath.JsonPath.read(body, "$._links.self.href").toString()).contains("/api/v2/");
        assertThat(com.jayway.jsonpath.JsonPath.read(body, "$._links.update.href").toString()).contains("/api/v2/");
        assertThat(com.jayway.jsonpath.JsonPath.read(body, "$._links.delete.href").toString()).contains("/api/v2/");
    }

    private void assertJsonPathExists(String json, String expression) {
        Object value = com.jayway.jsonpath.JsonPath.read(json, expression);
        assertThat(value).isNotNull();
    }

    private int enviarCriacao(String apiKey, String idempotency, String body) {
        try {
            return mvc.perform(post("/api/v2/autores").header("X-API-Key", apiKey)
                    .header("X-Idempotency-Key", idempotency).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andReturn().getResponse().getStatus();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
