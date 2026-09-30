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
                                API RESTful desenvolvida para o gerenciamento de uma biblioteca.

                                A API disponibiliza operações para gerenciamento de autores,
                                editoras, livros, categorias, leitores e empréstimos.

                                Principais funcionalidades:
                                - Cadastro, consulta, atualização e exclusão de autores.
                                - Cadastro, consulta, atualização e exclusão de editoras.
                                - Cadastro, consulta, atualização e exclusão de livros.
                                - Cadastro, consulta, atualização e exclusão de categorias.
                                - Cadastro, consulta, atualização e exclusão de leitores.
                                - Cadastro, consulta, atualização e exclusão de empréstimos.
                                - Consultas personalizadas por nome, título e status.
                                - Paginação nas operações de consulta de recursos.
                                - Validação dos dados recebidos pela API.

                                Fluxo recomendado de utilização:

                                1. Cadastrar um autor.
                                2. Cadastrar uma editora.
                                3. Cadastrar uma categoria.
                                4. Cadastrar um leitor.
                                5. Cadastrar um livro informando o autor, a editora e a categoria já cadastrados.
                                6. Cadastrar um empréstimo informando o leitor e o livro já cadastrados.

                                Relacionamentos entre os recursos:

                                - Um autor pode possuir vários livros.
                                - Uma editora pode possuir vários livros.
                                - Um livro pode pertencer a várias categorias.
                                - Um leitor pode possuir um empréstimo.
                                - Um empréstimo está associado a um livro.

                                Paginação:

                                As operações de listagem e consulta utilizam paginação.
                                O parâmetro "page" representa o número da página, iniciando em 0.
                                O parâmetro "size" representa a quantidade de registros por página.

                                Validação:

                                Os dados enviados para cadastro e atualização são validados
                                conforme as regras definidas em cada entidade.

                                Códigos de resposta utilizados:

                                - 200: Operação realizada com sucesso.
                                - 201: Recurso criado com sucesso.
                                - 204: Recurso excluído com sucesso.
                                - 400: Dados enviados são inválidos.
                                - 404: Recurso não encontrado.

                                Após os cadastros, podem ser realizadas as operações de consulta,
                                atualização e exclusão dos recursos conforme necessário.

                                Tecnologias utilizadas:
                                - Java 17
                                - Spring Boot
                                - Spring Data JPA
                                - H2 Database
                                - Spring Validation
                                - Springdoc OpenAPI

                                Projeto desenvolvido para fins acadêmicos, aplicando conceitos
                                de desenvolvimento de APIs REST, persistência de dados,
                                relacionamentos entre entidades, validação e documentação
                                de endpoints.
                                """)
                        .contact(new Contact()
                                .name("Rodrigo Arruda")));
    }
}