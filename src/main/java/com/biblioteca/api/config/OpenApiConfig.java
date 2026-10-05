package com.biblioteca.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@Configuration
@EnableSpringDataWebSupport(
        pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO
)
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliotecaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Biblioteca")
                        .version("1.0.0")
                        .description("""
                                API RESTful para gerenciamento de uma biblioteca.

                                Recursos:
                                - Autores, editoras, livros, categorias, leitores e empréstimos.
                                - Operações de cadastro, consulta, atualização e exclusão.
                                - Consultas personalizadas por nome, título e status.
                                - Paginação e ordenação utilizando "page", "size" e "sort".
                                - Validação dos dados recebidos.
                                - Controle de conflitos em campos únicos.
                                - HATEOAS para navegação entre os recursos.

                                Fluxo recomendado:
                                1. Cadastrar autor e editora.
                                2. Cadastrar categorias e leitor.
                                3. Cadastrar o livro informando autor, editora e categorias.
                                4. Cadastrar o empréstimo informando leitor e livro.
                                5. Consultar, atualizar ou excluir os recursos conforme necessário.

                                Paginação e ordenação:

                                "page" define a página, iniciando em 0.
                                "size" define a quantidade de registros por página.
                                "sort" define o campo e a direção da ordenação.

                                Exemplos:
                                GET /api/livros?page=0&size=10&sort=titulo,asc
                                GET /api/livros?page=0&size=10&sort=titulo,desc

                                Códigos de resposta:

                                - 200 OK: operação realizada com sucesso.
                                - 201 Created: recurso criado com sucesso.
                                - 204 No Content: recurso excluído com sucesso.
                                - 400 Bad Request: dados ou parâmetros inválidos.
                                - 404 Not Found: recurso não encontrado.
                                - 409 Conflict: conflito com recurso ou campo único existente.
                                - 422 Unprocessable Content: dados válidos, mas que violam uma regra de negócio.
                                - 405 Method Not Allowed: método HTTP não permitido.
                                - 415 Unsupported Media Type: tipo de conteúdo não suportado.
                                - 500 Internal Server Error: erro interno inesperado.

                                Exemplos de conflitos:
                                - ISBN de livro já cadastrado.
                                - E-mail de leitor já cadastrado.
                                - Nome de categoria já cadastrado.

                                Regra de negócio (422):
                                A data prevista de devolução não pode ser anterior à data do empréstimo.

                                HATEOAS:

                                Os recursos podem conter links de navegação, como "self" e "delete".
                                As consultas paginadas utilizam PagedModel com informações de paginação e links.

                                Tecnologias:
                                Java 17, Spring Boot, Spring Web, Spring Data JPA,
                                Hibernate, H2, Bean Validation, Springdoc OpenAPI,
                                Swagger UI e Spring HATEOAS.

                                Projeto desenvolvido para fins acadêmicos.
                                """)
                        .contact(new Contact()
                                .name("Rodrigo Arruda")));
    }
}