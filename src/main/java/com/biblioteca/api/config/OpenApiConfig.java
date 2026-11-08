package com.biblioteca.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class OpenApiConfig {
    private static final String ERRO_422 = "422 Entidade N\u00e3o Process\u00e1vel - a requisi\u00e7\u00e3o \u00e9 sintaticamente correta, mas n\u00e3o pode ser processada devido a uma regra de neg\u00f3cio. Exemplo: no cadastro ou atualiza\u00e7\u00e3o de empr\u00e9stimo, a data prevista de devolu\u00e7\u00e3o \u00e9 anterior \u00e0 data do empr\u00e9stimo.";
    private static final String FLUXO_RECOMENDADO = "Fluxo recomendado:\n"
            + "1. Cadastrar um autor.\n2. Cadastrar uma editora.\n3. Cadastrar uma categoria.\n"
            + "4. Cadastrar um leitor.\n5. Cadastrar um livro com autor, editora e categoria j\u00e1 cadastrados.\n"
            + "6. Cadastrar um empr\u00e9stimo com leitor e livro j\u00e1 cadastrados.\n"
            + "7. Depois, consultar, atualizar e excluir os recursos.";
    private static final String ORIENTACOES_V2 = """
            Como começar:

            1. Gere uma chave em POST /api/chaves-api.
            2. Copie o valor bib_... retornado.
            3. Clique em Authorize e cole somente esse valor. A chave não é aplicada automaticamente ao ser gerada.
            4. Envie X-API-Version: 2 nas rotas V2. Todo POST de recurso também exige X-Idempotency-Key.

            Limites por plano:

            - BÁSICO: 30 requisições por minuto.
            - PROFISSIONAL: 100 requisições por minuto.
            - PREMIUM: 300 requisições por minuto.

            Bloqueio progressivo por falhas de autenticação:

            As tentativas sem X-API-Key ou com chave inválida são contadas por endereço IP nas rotas protegidas da V2 e no gerenciamento de chaves. A regra é:

            1. 5 falhas em até 10 minutos: tentativas inválidas desse IP recebem bloqueio por 15 minutos.
            2. Se ocorrer outra sequência de 5 falhas em até 10 minutos após o bloqueio terminar, o próximo bloqueio dura 1 hora.
            3. Uma nova sequência após esse bloqueio aumenta a duração para 24 horas. Reincidências continuam com bloqueio de 24 horas.

            Durante o bloqueio, tentativas sem chave ou com chave inválida recebem 429 Too Many Requests e o cabeçalho Retry-After informa quantos segundos faltam. Uma chave válida ainda pode autenticar, mesmo a partir do mesmo IP, e limpa o histórico de falhas.

            Erros de validação e regras de negócio (400, 403, 409 e 422) não contam como falhas de autenticação. A geração pública em POST /api/chaves-api, as requisições OPTIONS e a documentação Swagger não entram nessa contagem. O histórico fica em memória nesta instância e é apagado quando a aplicação reinicia.

            """;
    private static final String DESCRICAO_V1 = FLUXO_RECOMENDADO + "\n\n" + ERRO_422;
    private static final String DESCRICAO_V2 = FLUXO_RECOMENDADO + "\n\n" + ERRO_422 + "\n\n" + ORIENTACOES_V2;

    @Bean
    public OpenAPI bibliotecaOpenAPI() {
        return new OpenAPI().info(new Info().title("API Biblioteca").version("2.0.0")
                        .description("""
                                API REST da biblioteca. Gere a chave por POST /api/chaves-api, copie o valor bib_... retornado, clique em Authorize e cole somente esse valor. A chave nao e aplicada automaticamente ao gerar.
                                Planos: BASICO 30, PROFISSIONAL 100 e PREMIUM 300 requisicoes por minuto. POST de recurso requer X-Idempotency-Key.
                                Use os grupos V1 e V2 no seletor do Swagger. As rotas V2 dos seis modulos documentam X-API-Version: 2.

                                Fluxo recomendado:
                                1. Cadastrar um autor.
                                2. Cadastrar uma editora.
                                3. Cadastrar uma categoria.
                                4. Cadastrar um leitor.
                                5. Cadastrar um livro com o autor, a editora e a categoria previamente cadastrados.
                                6. Cadastrar um emprestimo com o leitor e o livro previamente cadastrados.
                                7. Depois, consultar, atualizar e excluir os recursos.

                                """ + ERRO_422))
                .components(new Components().addSecuritySchemes("ApiKeyAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("X-API-Key")
                        .description("Chave gerada em POST /api/chaves-api.")))
                .addSecurityItem(new SecurityRequirement().addList("ApiKeyAuth"));
    }

    @Bean
    public GlobalOpenApiCustomizer bibliotecaOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) return;
            openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
                if (operation.getParameters() == null) operation.setParameters(new ArrayList<>());
                boolean v2 = path.startsWith("/api/v2/") || path.equals("/api/chaves-api");
                if (path.startsWith("/api/v2/")) addHeader(operation, "X-API-Version", "Versao do contrato. Envie 2 nas rotas V2.", false, List.of("2"));
                boolean post = pathItem.getPost() == operation;
                boolean put = pathItem.getPut() == operation;
                boolean delete = pathItem.getDelete() == operation;
                if (v2 && post && !path.equals("/api/chaves-api")) {
                    addHeader(operation, "X-Idempotency-Key", "Chave única para repetir POST sem duplicar a operação.", true, null);
                }
                if (path.equals("/api/chaves-api") && post) operation.setSecurity(new ArrayList<>());
                if (!v2) {
                    operation.setSecurity(new ArrayList<>());
                } else if (!(path.equals("/api/chaves-api") && post)) {
                    operation.setSecurity(List.of(new SecurityRequirement().addList("ApiKeyAuth")));
                }
                documentarErros(operation, path, post, put, delete, v2);
            }));
        };
    }

    private void documentarErros(io.swagger.v3.oas.models.Operation operation, String path,
                                  boolean post, boolean put, boolean delete, boolean v2) {
        boolean criarChave = path.equals("/api/chaves-api") && post;
        if (post || put) adicionarErro(operation, "400", "Dados inválidos ou corpo JSON malformado.");
        if (v2 && post && !criarChave) adicionarErro(operation, "409", "Conflito de idempotência ou registro duplicado.");
        if (v2 && !criarChave) adicionarErro(operation, "401", "X-API-Key ausente, inválida ou revogada.");
        if (v2 && path.startsWith("/api/chaves-api") && !post) adicionarErro(operation, "403", "Apenas chaves PREMIUM podem gerenciar chaves.");
        if (delete || put || path.matches(".*/\\{id}.*")) adicionarErro(operation, "404", "Registro não encontrado.");
        if (v2 && (post || put) && path.matches(".*/emprestimos(?:/\\{id})?")) adicionarErro(operation, "422", ERRO_422);
        if (v2 && !criarChave) adicionarErro(operation, "429", "Limite do plano excedido ou bloqueio tempor\u00e1rio ap\u00f3s falhas repetidas de autentica\u00e7\u00e3o; aguarde o tempo indicado em Retry-After.");
        adicionarErro(operation, "500", "Erro interno. A resposta não expõe detalhes da aplicação.");
        io.swagger.v3.oas.models.responses.ApiResponse limite = operation.getResponses().get("429");
        if (limite != null) {
            limite.addHeaderObject("Retry-After", new Header().description("Segundos até a renovação do limite ou o fim do bloqueio temporário de autenticação.").schema(new IntegerSchema()));
            limite.addHeaderObject("X-RateLimit-Limit", new Header().description("Limite do plano por minuto.").schema(new IntegerSchema()));
            limite.addHeaderObject("X-RateLimit-Remaining", new Header().description("Tokens restantes.").schema(new IntegerSchema()));
            limite.addHeaderObject("X-RateLimit-Reset", new Header().description("Instante Unix da próxima renovação.").schema(new IntegerSchema()));
        }
    }

    private void adicionarErro(io.swagger.v3.oas.models.Operation operation, String status, String descricao) {
        ObjectSchema schema = new ObjectSchema();
        schema.addProperty("status", new IntegerSchema());
        schema.addProperty("mensagem", new StringSchema());
        schema.addProperty("campos", new ObjectSchema());
        schema.addProperty("timestamp", new StringSchema().format("date-time"));
        schema.addProperty("caminho", new StringSchema());
        MediaType json = new MediaType().schema(schema).addExamples("resposta", new Example()
                .summary("Formato padronizado de erro")
                .value(Map.of("status", Integer.parseInt(status), "mensagem", descricao,
                        "campos", Map.of(), "timestamp", "2026-11-07T12:00:00", "caminho", "/api/recurso")));
        io.swagger.v3.oas.models.responses.ApiResponse response = operation.getResponses().get(status);
        if (response == null) response = new io.swagger.v3.oas.models.responses.ApiResponse();
        response.description(descricao).content(new Content().addMediaType("application/json", json));
        operation.getResponses().addApiResponse(status, response);
    }

    private void addHeader(io.swagger.v3.oas.models.Operation operation, String name, String description,
                           boolean required, List<String> values) {
        boolean exists = operation.getParameters().stream().anyMatch(p -> "header".equals(p.getIn()) && name.equals(p.getName()));
        if (exists) return;
        StringSchema schema = new StringSchema();
        if (values != null) schema._enum(values);
        operation.addParametersItem(new Parameter().in("header").name(name).description(description)
                .required(required).schema(schema));
    }

    @Bean
    public GroupedOpenApi apiV1() {
        return GroupedOpenApi.builder().group("V1").pathsToMatch("/api/v1/**")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info().title("API Biblioteca").version("1.0.0")
                        .description(DESCRICAO_V1)))
                .build();
    }

    @Bean
    public GroupedOpenApi apiV2() {
        return GroupedOpenApi.builder().group("V2").pathsToMatch("/api/v2/**", "/api/chaves-api")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info().title("API Biblioteca").version("2.0.0")
                        .description(DESCRICAO_V2)))
                .build();
    }
}
