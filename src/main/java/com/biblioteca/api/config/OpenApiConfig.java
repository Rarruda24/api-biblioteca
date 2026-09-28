package com.biblioteca.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliotecaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Biblioteca")
                        .version("1.0.0")
                        .description("""
                                API REST desenvolvida para o gerenciamento de uma biblioteca.

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

                                Tecnologias utilizadas:
                                - Java 17
                                - Spring Boot
                                - Spring Data JPA
                                - H2 Database
                                - Spring Validation
                                - Springdoc OpenAPI
                                - Spring HATEOAS

                                Projeto desenvolvido para fins acadêmicos, aplicando conceitos
                                de desenvolvimento de APIs REST, persistência de dados,
                                relacionamentos entre entidades, validação, documentação de
                                endpoints e navegação entre recursos.
                                """)
                        .contact(new Contact()
                                .name("Rodrigo Arruda")));
    }
}