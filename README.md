# API Biblioteca

API REST desenvolvida em Java com Spring Boot para gerenciamento de uma biblioteca.

O projeto foi desenvolvido com o objetivo de aplicar conceitos de desenvolvimento de APIs REST, persistência de dados, modelagem de entidades, relacionamentos entre tabelas, validação de dados, operações CRUD, paginação, consultas personalizadas e documentação de endpoints.

A aplicação permite o gerenciamento de autores, editoras, livros, categorias, leitores e empréstimos.

## Tecnologias utilizadas

- Java 17
- Spring Boot 4.1.1
- Maven
- Spring Web
- Spring Data JPA
- H2 Database
- Bean Validation
- Springdoc OpenAPI
- Swagger UI
- Spring HATEOAS

## Funcionalidades implementadas

Atualmente, a API possui as seguintes funcionalidades:

- Cadastro de autores
- Consulta de autores
- Atualização de autores
- Exclusão de autores
- Cadastro de editoras
- Consulta de editoras
- Atualização de editoras
- Exclusão de editoras
- Cadastro de livros
- Consulta de livros
- Atualização de livros
- Exclusão de livros
- Cadastro de categorias
- Consulta de categorias
- Atualização de categorias
- Exclusão de categorias
- Cadastro de leitores
- Consulta de leitores
- Atualização de leitores
- Exclusão de leitores
- Cadastro de empréstimos
- Consulta de empréstimos
- Atualização de empréstimos
- Exclusão de empréstimos
- Consultas personalizadas
- Paginação dos resultados
- Validação dos dados recebidos
- Relacionamentos entre as entidades
- Controle do status dos empréstimos através de enum
- Documentação dos endpoints com Swagger / OpenAPI

## Objetivo do projeto

A API Biblioteca foi desenvolvida como projeto acadêmico para colocar em prática os principais conceitos utilizados no desenvolvimento de uma API REST utilizando o ecossistema Java e Spring.

A aplicação foi estruturada de forma a separar as responsabilidades entre as diferentes camadas do sistema, facilitando a organização, manutenção e evolução do projeto.

A estrutura utilizada atualmente separa:

- Modelos
- Repositórios
- Serviços
- Controladores
- Configurações

## Configuração inicial

O projeto foi criado utilizando o Spring Initializr.

As configurações utilizadas na criação inicial foram:

| Configuração | Valor |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | 4.1.1 |
| Group | `com.biblioteca` |
| Artifact | `api-biblioteca` |
| Name | `API Biblioteca` |
| Package name | `com.biblioteca.api` |
| Packaging | Jar |
| Java | 17 |

## Dependências

As principais dependências utilizadas no projeto são:

### Spring Web

Responsável pela criação dos endpoints REST da aplicação, permitindo o recebimento e processamento das requisições HTTP.

### Spring Data JPA

Utilizado para realizar a persistência dos dados e o acesso às entidades através do JPA e Hibernate.

### H2 Database

Banco de dados utilizado durante o desenvolvimento da aplicação.

### Bean Validation

Utilizado para validar os dados recebidos nos endpoints de cadastro e atualização.

### Springdoc OpenAPI

Utilizado para gerar a especificação OpenAPI da aplicação e disponibilizar a documentação dos endpoints através do Swagger UI.

### Spring HATEOAS

Dependência adicionada ao projeto para a implementação dos recursos HATEOAS prevista para a próxima etapa do desenvolvimento.

## Arquitetura da aplicação

A aplicação está organizada em camadas.

```text
com.biblioteca.api
│
├── config
│   └── OpenApiConfig
│
├── controller
│   ├── AutorController
│   ├── CategoriaController
│   ├── EditoraController
│   ├── EmprestimoController
│   ├── LeitorController
│   └── LivroController
│
├── model
│   ├── Autor
│   ├── Categoria
│   ├── Editora
│   ├── Emprestimo
│   ├── Leitor
│   ├── Livro
│   └── StatusEmprestimo
│
├── repository
│   ├── AutorRepository
│   ├── CategoriaRepository
│   ├── EditoraRepository
│   ├── EmprestimoRepository
│   ├── LeitorRepository
│   └── LivroRepository
│
├── service
│   ├── AutorService
│   ├── CategoriaService
│   ├── EditoraService
│   ├── EmprestimoService
│   ├── LeitorService
│   └── LivroService
│
└── ApiBibliotecaApplication
