# API Biblioteca

API REST para gerenciamento de uma biblioteca.

A aplicação será desenvolvida utilizando Java e Spring Boot, com persistência de dados em banco H2.

## Tecnologias

- Java 17+
- Spring Boot
- Maven
- Spring Web
- Spring Data JPA
- H2 Database
- Bean Validation
- Swagger / OpenAPI
- Spring HATEOAS

## Sobre o projeto

A API Biblioteca tem como objetivo disponibilizar os recursos necessários para o gerenciamento de uma biblioteca, incluindo livros, autores, categorias, usuários e empréstimos.

O projeto será desenvolvido seguindo uma arquitetura organizada em camadas, buscando separar as responsabilidades de cada parte da aplicação.

Durante o desenvolvimento serão implementados recursos como:

- Cadastro, consulta, atualização e exclusão de registros
- Relacionamentos entre entidades
- Paginação de resultados
- Consultas personalizadas
- Validação de dados
- Documentação dos endpoints
- HATEOAS
- Tratamento de erros
- Recursos de segurança e controle de acesso

## Configuração inicial

O projeto foi criado utilizando o Spring Initializr, com Maven como gerenciador de dependências.

### Configurações

| Configuração | Valor |
|---|---|
| Group | `com.biblioteca` |
| Artifact | `api-biblioteca` |
| Package | `com.biblioteca.api` |
| Java | `17` |
| Packaging | `Jar` |
| Build | `Maven` |

### Dependências iniciais

Na configuração inicial do projeto foram adicionadas:

- **Spring Web** — desenvolvimento da API REST
- **Spring Data JPA** — persistência e acesso aos dados
- **H2 Database** — banco de dados utilizado pela aplicação

Outras dependências serão adicionadas conforme cada etapa do desenvolvimento.

## Estrutura do projeto

A estrutura inicial do projeto está organizada da seguinte forma:

```text
API Biblioteca
├── .mvn/
│   └── wrapper/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── biblioteca/
│   │   │           └── api/
│   │   │               └── ApiBibliotecaApplication.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── biblioteca/
│                   └── api/
│                       └── ApiBibliotecaApplicationTests.java
│
├── .gitattributes
├── .gitignore
├── HELP.md
├── mvnw
├── mvnw.cmd
└── pom.xml
