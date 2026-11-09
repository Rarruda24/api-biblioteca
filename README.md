# API Biblioteca

API REST desenvolvida em Java e Spring Boot para gerenciamento de uma biblioteca. O projeto aplica conceitos de modelagem de domínio, persistência relacional, operações CRUD, paginação, validação, consultas personalizadas, HATEOAS e documentação OpenAPI.

A API gerencia autores, editoras, livros, categorias, leitores e empréstimos. Ela disponibiliza os contratos V1 e V2: a V1 preserva o produto original da Parte 1; a V2 mantém os recursos da aplicação e acrescenta os mecanismos avançados de autenticação, idempotência, Rate Limit e CORS.

## Tecnologias

- Java 17
- Spring Boot 4.1.1
- Maven
- Spring WebMVC
- Spring Data JPA e Hibernate
- H2 Database
- Bean Validation
- Spring HATEOAS
- Springdoc OpenAPI 3.1.1 e Swagger UI
- Bucket4j 8.10.1

## Funcionalidades

- CRUD para os seis módulos da biblioteca.
- Listagens e consultas personalizadas paginadas com `Pageable`.
- Ordenação por propriedades do recurso.
- Validação dos dados recebidos em cadastros e atualizações.
- Relacionamentos JPA entre autores, editoras, livros, categorias, leitores e empréstimos.
- Status de empréstimo representado pelo enum `StatusEmprestimo`.
- Respostas HATEOAS para navegação entre recursos.
- Tratamento global de erros com respostas padronizadas.
- Documentação interativa no Swagger para V1 e V2.
- Recursos avançados de segurança e tráfego disponíveis na V2.

## Entidades e relacionamentos

| Entidade | Descrição e principais dados |
|---|---|
| Autor | Nome, nacionalidade e data de nascimento. |
| Editora | Nome e país. |
| Livro | Título, ISBN, ano de publicação, número de páginas, autor, editora e categorias. |
| Categoria | Nome e descrição. |
| Leitor | Nome, e-mail e telefone. |
| Empréstimo | Data do empréstimo, data prevista e efetiva de devolução, status, leitor e livro. |

Relacionamentos implementados:

- Um autor pode possuir vários livros (1:N).
- Uma editora pode publicar vários livros (1:N).
- Um livro pode pertencer a várias categorias, e uma categoria pode estar em vários livros (N:N).
- Um leitor pode estar associado a um empréstimo (1:1).
- Cada empréstimo está associado a um livro (N:1).

O enum `StatusEmprestimo` possui os valores `ATIVO`, `DEVOLVIDO` e `ATRASADO`.

## Organização do projeto

O código está dividido por responsabilidade dentro do pacote `com.biblioteca.api`:

```text
com.biblioteca.api
├── assembler     Montadores de respostas HATEOAS e links versionados
├── config        Configuração OpenAPI e documentação
├── controller    Endpoints REST dos recursos e gerenciamento de API Keys
├── infra         Filtros, CORS, idempotência, Rate Limit e erros globais
├── model         Entidades JPA e enums
├── repository    Repositórios Spring Data JPA
└── service       Regras de negócio e operações da aplicação
```

## V1 e V2

O versionamento dos recursos é feito por rota. Os seis módulos estão disponíveis em ambas as versões:

| Módulo | V1 | V2 |
|---|---|---|
| Autores | `/api/v1/autores` | `/api/v2/autores` |
| Editoras | `/api/v1/editoras` | `/api/v2/editoras` |
| Livros | `/api/v1/livros` | `/api/v2/livros` |
| Categorias | `/api/v1/categorias` | `/api/v2/categorias` |
| Leitores | `/api/v1/leitores` | `/api/v2/leitores` |
| Empréstimos | `/api/v1/emprestimos` | `/api/v2/emprestimos` |

Cada módulo oferece listagem, criação, busca por ID, atualização, exclusão e uma consulta personalizada. Os mesmos métodos HTTP e formatos de corpo são usados nas duas versões, com as diferenças de segurança e tráfego abaixo.

### V1 — contrato original

A V1 preserva o contrato da Parte 1: CRUD, paginação, consultas personalizadas, validação e respostas HATEOAS. As rotas V1 não exigem `X-API-Key`, não exigem `X-Idempotency-Key` e não aplicam o Rate Limit da V2.

### V2 — contrato com recursos avançados

A V2 mantém CRUD, paginação, consultas, validações e HATEOAS e acrescenta:

- Autenticação por API Key.
- Idempotência nos POSTs de recursos.
- Rate Limit por cliente e plano, usando Bucket4j.
- Proteção progressiva contra tentativas repetidas de autenticação.
- Política CORS para as origens configuradas.
- Cabeçalho `X-API-Version: 2` documentado nas operações V2. Quando enviado, o valor deve ser `2`.

As rotas antigas `/api/{recurso}` continuam disponíveis por compatibilidade. O seletor do Swagger apresenta os grupos V1 e V2.

## Endpoints dos módulos

Para cada módulo, use o caminho correspondente da tabela V1/V2 acima:

| Operação | Método e caminho | Resposta de sucesso |
|---|---|---:|
| Listar com paginação | `GET /api/vN/{recurso}` | 200 |
| Criar | `POST /api/vN/{recurso}` | 201 |
| Buscar por ID | `GET /api/vN/{recurso}/{id}` | 200 |
| Atualizar | `PUT /api/vN/{recurso}/{id}` | 200 |
| Excluir | `DELETE /api/vN/{recurso}/{id}` | 204 |
| Consulta personalizada | `GET /api/vN/{recurso}/buscar` | 200 |

`vN` representa `v1` ou `v2`. As consultas personalizadas são:

| Módulo | Rota | Parâmetro |
|---|---|---|
| Autores | `/autores/buscar` | `nome` |
| Editoras | `/editoras/buscar` | `nome` |
| Livros | `/livros/buscar` | `titulo` |
| Categorias | `/categorias/buscar` | `nome` |
| Leitores | `/leitores/buscar` | `nome` |
| Empréstimos | `/emprestimos/buscar` | `status` (`ATIVO`, `DEVOLVIDO` ou `ATRASADO`) |

Todas as rotas de listagem e de busca personalizada aceitam paginação. Por padrão, a página inicial é `0`, o tamanho é `10` e a ordenação é por `id`.

```http
GET /api/v1/livros?page=0&size=10&sort=titulo,asc
```

Parâmetros:

- `page`: número da página, começando em zero.
- `size`: quantidade máxima de itens na página.
- `sort`: propriedade e direção, como `titulo,asc` ou `id,desc`. Podem ser enviados vários parâmetros `sort`.

As listagens são serializadas como `PagedModel`, com conteúdo, metadados da página e links de navegação.

## HATEOAS

As respostas de recursos individuais utilizam `EntityModel` e incluem links relacionados, como `self`, `update` e `delete`. Os links das respostas versionadas apontam para a mesma versão da requisição.

Exemplo resumido de uma categoria:

```json
{
  "id": 1,
  "nome": "Romance",
  "descricao": "Obras literárias de romance.",
  "_links": {
    "self": { "href": "http://localhost:8080/api/v1/categorias/1" },
    "update": { "href": "http://localhost:8080/api/v1/categorias/1" },
    "delete": { "href": "http://localhost:8080/api/v1/categorias/1" }
  }
}
```

## Validação e respostas de erro

As entidades usam Bean Validation para verificar campos obrigatórios, tamanhos, e-mail, números positivos e datas. Livro exige autor e editora existentes; empréstimo exige leitor e livro existentes.

O tratamento global retorna uma estrutura padronizada, sem stack trace:

```json
{
  "status": 400,
  "mensagem": "Erro de validação",
  "campos": { "nome": "O nome do autor é obrigatório" },
  "timestamp": "2026-11-08T12:00:00",
  "caminho": "/api/v1/autores"
}
```

| HTTP | Significado |
|---:|---|
| 200 | Consulta ou atualização concluída. |
| 201 | Recurso criado. |
| 204 | Recurso excluído. |
| 400 | Dados, parâmetros ou JSON inválidos. |
| 401 | Na V2, API Key ausente, inválida ou revogada. |
| 403 | Ação não autorizada para o plano da chave. |
| 404 | Recurso não encontrado. |
| 409 | Conflito, como duplicidade ou reutilização de chave idempotente com outro corpo. |
| 422 | Requisição válida que viola uma regra de negócio. |
| 429 | Rate Limit excedido ou tentativa de autenticação temporariamente bloqueada. |
| 500 | Erro interno; detalhes da aplicação não são expostos. |

Por exemplo, um empréstimo com data prevista de devolução anterior à data do empréstimo retorna 422. A resposta segue o mesmo formato padronizado de erro.

## Recursos de segurança e tráfego da V2

### API Key

A criação de chave é pública e feita por `POST /api/chaves-api`. O segredo completo só é retornado na criação; no banco fica armazenado seu hash SHA-256. A consulta e a revogação de chaves exigem uma chave PREMIUM.

Exemplo de criação:

```http
POST /api/chaves-api
Content-Type: application/json

{"nome":"Aplicação Biblioteca","plano":"BASICO"}
```

Planos e limites:

| Plano | Requisições por minuto |
|---|---:|
| BASICO | 30 |
| PROFISSIONAL | 100 |
| PREMIUM | 300 |

No Swagger, execute `POST /api/chaves-api`, copie o campo `apiKey` da resposta e cole somente o valor retornado no botão Authorize. Gerar uma chave não a aplica automaticamente às requisições.

### Idempotência

Todo POST de recurso V2 exige `X-Idempotency-Key`. Repetir a mesma chave, rota e corpo reproduz a resposta original sem criar outro registro. Usar a mesma chave com outro corpo retorna 409. Falhas não são armazenadas; as chaves idempotentes expiram após 24 horas e requisições simultâneas são protegidas contra duplicidade. A criação de API Key e a V1 não exigem esse cabeçalho.

Exemplo de criação de autor na V2:

```http
POST /api/v2/autores
X-API-Key: bib_sua_chave
X-API-Version: 2
X-Idempotency-Key: cadastro-autor-001
Content-Type: application/json

{
  "nome": "Machado de Assis",
  "nacionalidade": "Brasileira",
  "dataNascimento": "1839-06-21"
}
```

### Rate Limit

O Rate Limit é aplicado por API Key com buckets independentes do Bucket4j. Ao ultrapassar o limite, a V2 retorna 429, `Retry-After`, `X-RateLimit-Limit`, `X-RateLimit-Remaining` e `X-RateLimit-Reset`. Requisições OPTIONS e Swagger/OpenAPI não consomem tokens.

### Bloqueio progressivo de falhas de autenticação

Cinco tentativas sem API Key ou com chave inválida originadas do mesmo IP em até 10 minutos fazem tentativas inválidas desse IP receberem bloqueio por 15 minutos. Novas sequências após o fim do bloqueio elevam o prazo para 1 hora e depois 24 horas. Tentativas bloqueadas recebem 429 e `Retry-After`. Uma chave válida ainda pode autenticar e limpa o histórico. Erros de validação e regras de negócio não são contabilizados. O histórico fica em memória nesta instância e é apagado quando a aplicação reinicia.

### CORS

Origens permitidas:

- `http://localhost:3000`
- `http://localhost:5173`
- `http://127.0.0.1:5500`

Métodos permitidos: GET, POST, PUT, PATCH, DELETE e OPTIONS. A configuração aceita `Content-Type`, `Authorization`, `X-API-Key`, `X-Idempotency-Key`, `X-API-Version` e `Accept`. Os cabeçalhos de Rate Limit e idempotência são expostos para o frontend. Não são aceitas origens arbitrárias.

## Fluxo recomendado de uso

1. Cadastrar um autor.
2. Cadastrar uma editora.
3. Cadastrar uma categoria.
4. Cadastrar um leitor.
5. Cadastrar um livro informando autor, editora e categoria já cadastrados.
6. Cadastrar um empréstimo informando leitor e livro já cadastrados.
7. Consultar, atualizar e excluir os recursos conforme necessário.

Os exemplos completos de requisição e resposta de cada operação estão na documentação Swagger. O frontend local também permite carregar exemplos de POST por módulo. IDs usados nos relacionamentos precisam corresponder a registros existentes.

## Executar localmente

Requisitos: JDK 17 ou superior. O Maven Wrapper acompanha o projeto.

No PowerShell:

```powershell
./mvnw.cmd spring-boot:run
```

A aplicação inicia por padrão em `http://localhost:8080`. O banco H2 é em memória; reiniciar a aplicação limpa os dados. A console H2 está disponível em `http://localhost:8080/h2-console` durante a execução local.

Para executar os testes e empacotar a aplicação:

```powershell
./mvnw.cmd clean verify
```

## Frontend local

Com a aplicação em execução, abra `http://localhost:8080/`. O frontend oferece formulários e um console para enviar requisições à API. É possível escolher o módulo, preencher um exemplo de POST e conferir o método, a rota, o status, o corpo e os cabeçalhos da resposta.

## Documentação Swagger / OpenAPI

Swagger UI local:

```text
http://localhost:8080/swagger-ui/index.html
```

Especificação OpenAPI local:

```text
http://localhost:8080/v3/api-docs
```

Os grupos de documentação são `V1` e `V2`:

- `/v3/api-docs/V1`
- `/v3/api-docs/V2`

Documentação da aplicação publicada no Render:

- Swagger UI: https://api-biblioteca-6xow.onrender.com/swagger-ui/index.html
- OpenAPI: https://api-biblioteca-6xow.onrender.com/v3/api-docs

## Verificação automatizada

Os testes de integração cobrem criação e autenticação de API Keys, respostas 401/403, bloqueio progressivo, replay e conflito de idempotência, concorrência, Rate Limit dos três planos e isolamento entre clientes, CORS, rotas V1/V2, validação, 404, respostas HATEOAS e documentação OpenAPI. A última execução de `clean verify` concluiu com 12 testes aprovados.

## Release notes

As alterações introduzidas especificamente na V2 estão detalhadas em [RELEASE_NOTES_V2.md](RELEASE_NOTES_V2.md).

## Finalidade acadêmica

Projeto acadêmico de desenvolvimento de API REST com Spring Boot, com persistência de dados, relacionamentos entre entidades, validação, paginação, consultas personalizadas, documentação OpenAPI e HATEOAS.
