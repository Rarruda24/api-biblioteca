# API Biblioteca

API REST desenvolvida em Java com Spring Boot para gerenciamento de uma biblioteca.

O projeto foi desenvolvido com o objetivo de aplicar conceitos de desenvolvimento de APIs REST, persistência de dados, modelagem de entidades, relacionamentos entre tabelas, validação de dados, operações CRUD, paginação, consultas personalizadas, HATEOAS e documentação de endpoints.

A aplicação permite o gerenciamento de autores, editoras, livros, categorias, leitores e empréstimos.

## Tecnologias utilizadas

* Java 17
* Spring Boot 4.1.1
* Maven
* Spring Web
* Spring Data JPA
* Hibernate
* H2 Database
* Bean Validation
* Springdoc OpenAPI
* Swagger UI
* Spring HATEOAS

## Funcionalidades implementadas

Atualmente, a API possui as seguintes funcionalidades:

* Cadastro, consulta, atualização e exclusão de autores
* Cadastro, consulta, atualização e exclusão de editoras
* Cadastro, consulta, atualização e exclusão de livros
* Cadastro, consulta, atualização e exclusão de categorias
* Cadastro, consulta, atualização e exclusão de leitores
* Cadastro, consulta, atualização e exclusão de empréstimos
* Consultas personalizadas por nome, título e status
* Paginação dos resultados utilizando `Pageable`
* Ordenação dos resultados utilizando `sort`
* Validação dos dados recebidos
* Controle de conflitos em campos únicos
* Relacionamentos entre as entidades
* Controle do status dos empréstimos através de enum
* HATEOAS para navegação entre os recursos
* Documentação dos endpoints com Swagger / OpenAPI
* Respostas com links de navegação e informações de paginação

## Objetivo do projeto

A API Biblioteca foi desenvolvida como projeto acadêmico para colocar em prática os principais conceitos utilizados no desenvolvimento de uma API REST utilizando o ecossistema Java e Spring.

A aplicação foi estruturada de forma a separar as responsabilidades entre as diferentes camadas do sistema, facilitando a organização, manutenção e evolução do projeto.

A estrutura utilizada separa:

* **Assembler:** criação dos modelos de resposta com links HATEOAS.
* **Config:** configurações gerais da aplicação e documentação.
* **Controller:** recebimento e processamento das requisições HTTP.
* **Model:** representação das entidades e seus relacionamentos.
* **Repository:** acesso e persistência dos dados.
* **Service:** regras de negócio e operações da aplicação.

## Configuração inicial

O projeto foi criado utilizando o Spring Initializr.

As configurações utilizadas na criação inicial foram:

| Configuração | Valor                |
| ------------ | -------------------- |
| Project      | Maven                |
| Language     | Java                 |
| Spring Boot  | 4.1.1                |
| Group        | `com.biblioteca`     |
| Artifact     | `api-biblioteca`     |
| Name         | `API Biblioteca`     |
| Package name | `com.biblioteca.api` |
| Packaging    | Jar                  |
| Java         | 17                   |

## Dependências

As principais dependências utilizadas no projeto são:

### Spring Web

Responsável pela criação dos endpoints REST da aplicação, permitindo o recebimento e processamento das requisições HTTP.

### Spring Data JPA

Utilizado para realizar a persistência dos dados e o acesso às entidades através do JPA e Hibernate.

### H2 Database

Banco de dados utilizado durante o desenvolvimento da aplicação.

### Bean Validation

Utilizado para validar os dados recebidos nos endpoints de cadastro e atualização, de acordo com as regras definidas nas entidades.

### Springdoc OpenAPI

Utilizado para gerar a especificação OpenAPI da aplicação e disponibilizar a documentação dos endpoints através do Swagger UI.

### Spring HATEOAS

Utilizado para implementar HATEOAS na API, adicionando links de navegação aos recursos retornados pelos endpoints. A implementação utiliza `EntityModel`, `PagedModel` e classes assembler para estruturar as respostas.

## Entidades e relacionamentos

A aplicação possui seis entidades principais:

* **Autor:** representa os autores das obras cadastradas.
* **Editora:** representa as editoras responsáveis pelas publicações.
* **Livro:** representa os livros disponíveis no sistema.
* **Categoria:** representa as categorias associadas aos livros.
* **Leitor:** representa os leitores cadastrados na biblioteca.
* **Emprestimo:** representa os empréstimos realizados.

A aplicação utiliza os seguintes relacionamentos:

* **One-to-Many:** um autor pode possuir vários livros.
* **One-to-Many:** uma editora pode possuir vários livros.
* **Many-to-Many:** um livro pode pertencer a várias categorias, e uma categoria pode estar associada a vários livros.
* **One-to-One:** um leitor pode estar associado a um empréstimo.
* **Many-to-One:** um empréstimo está associado a um livro.

O projeto também utiliza o enum `StatusEmprestimo` para representar os estados de um empréstimo:

* `ATIVO`
* `DEVOLVIDO`
* `ATRASADO`

## Paginação e ordenação

As operações de listagem e consulta utilizam `Pageable`, permitindo controlar a paginação e a ordenação dos resultados diretamente pelos parâmetros da requisição.

Exemplo:

```http
GET /api/livros?page=0&size=10&sort=titulo,asc
```

Parâmetros utilizados:

* `page`: número da página, iniciando em 0.
* `size`: quantidade de registros por página.
* `sort`: propriedade utilizada para ordenação e direção da ordenação, podendo ser `asc` ou `desc`.

Também é possível utilizar mais de um critério de ordenação, informando múltiplos parâmetros `sort`.

As respostas paginadas utilizam `PagedModel`, disponibilizando os dados da página e links de navegação.

## HATEOAS

A API utiliza Spring HATEOAS para adicionar links de navegação às respostas dos recursos.

Os modelos assembler são responsáveis por transformar as entidades em respostas que incluem os dados do recurso e seus links relacionados.

Os recursos individuais disponibilizam links como:

* `self`: link para consultar o próprio recurso.
* `delete`: link associado à operação de exclusão do recurso.

Exemplo de resposta:

```json
{
  "id": 1,
  "nome": "Romance",
  "descricao": "Obras literárias de romance.",
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/categorias/1"
    },
    "delete": {
      "href": "http://localhost:8080/api/categorias/1"
    }
  }
}
```

Nas operações de listagem, a API utiliza `PagedModel` para representar as coleções, incluindo informações de paginação e links de navegação.

## Documentação da API

A documentação dos endpoints é disponibilizada através do Springdoc OpenAPI e Swagger UI.

Após iniciar a aplicação, a interface do Swagger pode ser acessada em:

```text
http://localhost:8080/swagger-ui/index.html
```

A especificação OpenAPI pode ser acessada em:

```text
http://localhost:8080/v3/api-docs
```

A documentação apresenta os endpoints disponíveis, parâmetros, exemplos de requisições e respostas, além dos códigos HTTP utilizados pela API.

## Status HTTP utilizados

A API utiliza os códigos de status HTTP de acordo com o resultado de cada operação.

### Sucesso

- `200 OK` — operação realizada com sucesso, como consultas e atualizações.
- `201 Created` — recurso criado com sucesso.
- `204 No Content` — recurso excluído com sucesso.

### Erros do cliente

- `400 Bad Request` — dados enviados são inválidos ou algum parâmetro não pôde ser processado.
- `404 Not Found` — recurso solicitado não foi encontrado.
- `409 Conflict` — conflito com um recurso existente ou com um campo que deve ser único.

### Erros do servidor

- `500 Internal Server Error` — erro interno inesperado durante o processamento da requisição.

### Possíveis códigos HTTP

Dependendo da requisição e do comportamento do Spring Boot, outros códigos HTTP também podem ser retornados:

- `405 Method Not Allowed` — método HTTP utilizado não é permitido para o endpoint.
- `415 Unsupported Media Type` — tipo de conteúdo enviado não é suportado pela API.
- `422 Unprocessable Entity` — requisição está sintaticamente correta, mas os dados não podem ser processados devido a regras de validação ou negócio.

## Arquitetura da aplicação

A aplicação está organizada em camadas, separando as responsabilidades de cada parte do sistema.

```text
com.biblioteca.api
│
├── assembler
│   ├── AutorModelAssembler
│   ├── CategoriaModelAssembler
│   ├── EditoraModelAssembler
│   ├── EmprestimoModelAssembler
│   ├── LeitorModelAssembler
│   └── LivroModelAssembler
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
```

## Fluxo recomendado

Para utilizar a API, recomenda-se seguir a seguinte sequência:

1. Cadastrar um autor.
2. Cadastrar uma editora.
3. Cadastrar uma categoria.
4. Cadastrar um leitor.
5. Cadastrar um livro informando o autor, a editora e a categoria já cadastrados.
6. Cadastrar um empréstimo informando o leitor e o livro já cadastrados.

Após os cadastros, podem ser realizadas as operações de consulta, atualização e exclusão dos recursos.

## Projeto acadêmico

Projeto desenvolvido para fins acadêmicos, aplicando conceitos de desenvolvimento de APIs REST, persistência de dados, relacionamentos entre entidades, validação, paginação, consultas personalizadas, documentação de APIs e HATEOAS.
