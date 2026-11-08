# API Biblioteca V2 — Parte 2 SENAC

API REST para gerenciamento de autores, editoras, livros, categorias, leitores e empréstimos, construída com Java 17 e Spring Boot 4.1.1. Mantém CRUD, consultas paginadas, relacionamentos JPA e respostas HATEOAS.

## Executar

Requer JDK 17 ou superior. Na raiz do projeto:

```powershell
./mvnw.cmd spring-boot:run
```

Testes automatizados:

```powershell
./mvnw.cmd clean test
```

A aplicação usa H2 em memória; reiniciar a aplicação limpa os dados.

## Swagger

- UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI geral: http://localhost:8080/v3/api-docs
- Grupos de versoes no seletor: V1 e V2 (`/v3/api-docs/V1` e `/v3/api-docs/V2`). As rotas legadas `/api/{recurso}` continuam disponiveis para compatibilidade, mas nao aparecem como um terceiro grupo no Swagger.

No botão **Authorize**, informe a chave sem prefixo no esquema `X-API-Key`.
A operação pública `POST /api/chaves-api` está no grupo V2: execute-a, copie `apiKey` da resposta e informe o valor em **Authorize**. O grupo V1 identifica-se como `API Biblioteca 1.0.0`, e o V2 como `API Biblioteca 2.0.0`.

### Diferença entre V1 e V2

- **V1 (`/api/v1/...`)** preserva o contrato da Parte 1: CRUD, paginação e HATEOAS. Não exige `X-API-Key` nem `X-Idempotency-Key` e não aplica Rate Limit.
- **V2 (`/api/v2/...`)** mantém os recursos da Parte 1 e acrescenta autenticação por API Key, idempotência nos POSTs, Rate Limit Bucket4j e CORS configurado.
- As rotas antigas `/api/{recurso}` continuam disponíveis para compatibilidade. Os filtros da Parte 2 são aplicados apenas à V2 e às operações demonstrativas de chave/versionamento.
- `X-API-Version` e documentado em todas as operacoes V2 dos seis modulos. Envie `2` nas rotas `/api/v2/...`; um valor diferente e rejeitado. A V1 preserva o contrato publicado e nao valida esse cabecalho.

### Fluxo recomendado

1. Cadastrar um autor.
2. Cadastrar uma editora.
3. Cadastrar uma categoria.
4. Cadastrar um leitor.
5. Cadastrar um livro informando o autor, a editora e a categoria já cadastrados.
6. Cadastrar um empréstimo informando o leitor e o livro já cadastrados.
7. Após os cadastros, realizar consultas, atualizações e exclusões dos recursos.

Em empréstimos, HTTP 422 (`Unprocessable Entity`) significa que a requisição está sintaticamente correta, mas não pode ser processada por uma regra de negócio. Por exemplo, o cadastro ou atualização será rejeitado quando a data prevista de devolução for anterior à data do empréstimo.

## Frontend e exemplos de POST

Com a aplicação em execução, abra `http://localhost:8080/`. O frontend usa a mesma origem selecionada no seletor **Servidor da API** para montar o link do Swagger. Os formulários enviam os mesmos campos e estruturas JSON dos exemplos de criação documentados para autores, editoras, livros, categorias, leitores e empréstimos.

No **Console da API**, selecione o módulo e clique em **Carregar exemplo POST deste módulo** para preencher método, rota e corpo com o exemplo equivalente ao Swagger. Em livros e empréstimos, os IDs de autor/editora/categoria/leitor/livro precisam apontar para registros que já existam. O painel Request/Response exibe o status, corpo e cabeçalhos reais da última operação; a atualização posterior da lista não substitui esse resultado.

## Autenticação e planos

Gere uma chave sem autenticação inicial:

```http
POST /api/chaves-api
Content-Type: application/json

{"nome":"Aplicação Biblioteca","plano":"BASICO"}
```

O segredo completo é retornado uma única vez e somente seu hash SHA-256 é persistido. Use `X-API-Key` nas demais rotas. Chaves ausentes, inválidas ou revogadas recebem 401. A consulta e revogação de chaves em `/api/chaves-api` exigem plano PREMIUM (403 para os demais planos).

| Plano | Limite por chave |
|---|---:|
| BASICO | 30 requisições/minuto |
| PROFISSIONAL | 100 requisições/minuto |
| PREMIUM | 300 requisições/minuto |

O rate limit usa Bucket4j com bucket independente pelo ID persistido da chave. Ao exceder, a resposta é 429 com `Retry-After`, `X-RateLimit-Limit`, `X-RateLimit-Remaining` e `X-RateLimit-Reset`. OPTIONS e endpoints Swagger/OpenAPI não consomem tokens.

Falhas repetidas de autenticação também recebem proteção progressiva, independente do plano: 5 tentativas sem chave ou com chave inválida em 10 minutos fazem as tentativas inválidas desse IP receberem bloqueio de 15 minutos; novas sequências elevam o bloqueio para 1 hora e depois 24 horas. A resposta é 429 e informa `Retry-After`. Uma chave válida continua podendo autenticar e limpa o histórico do IP; erros 400, 403, 409 e 422 não contam. O bloqueio usa o endereço remoto do cliente, não confia em `X-Forwarded-For`, e o estado fica em memória (reiniciar a aplicação o limpa).

## Idempotência

Todos os POSTs de recursos V2 exigem `X-Idempotency-Key`; a V1 e a geração de chave são excluídas. O filtro vincula a chave idempotente ao cliente, rota e fingerprint SHA-256 do corpo, reproduz a resposta de sucesso sem executar novamente, rejeita outro corpo com 409 e expira entradas após 24 horas. Falhas não são armazenadas, permitindo nova tentativa. A sincronização é por faixa de chave para evitar duplicidade concorrente sem serializar todas as operações globalmente.

Exemplo:

```http
POST /api/v2/autores
X-API-Key: bib_...
X-Idempotency-Key: cadastro-autor-001
Content-Type: application/json

{"nome":"Ada Lovelace","nacionalidade":"Britânica"}
```

## Versionamento

As rotas `/api/v1/{recurso}` e `/api/v2/{recurso}` estao disponiveis para os seis modulos, mantendo `/api/{recurso}` por compatibilidade. A versao por rota funciona nos seis modulos; nas operacoes V2, `X-API-Version: 2` e documentado e um valor diferente retorna 400. Links HATEOAS de entidades em respostas V1 e V2 permanecem na mesma versao da requisicao. A V1 preserva o contrato publicado; V2 acrescenta API Key, idempotencia, Rate Limit e CORS sem remover CRUD, paginacao ou HATEOAS.

## CORS

Origens permitidas: `http://localhost:3000`, `http://localhost:5173` e `http://127.0.0.1:5500`. Métodos GET, POST, PUT, PATCH, DELETE e OPTIONS. Cabeçalhos de autenticação, idempotência e versão autorizados; cabeçalhos de limite expostos para o frontend.

## Erros

As respostas de erro incluem status, mensagem, campos (para validação), timestamp e caminho. Stack traces não são enviados ao cliente. Os status esperados incluem 400, 401, 403, 404, 409, 429 e 500.

## Verificação automatizada

Os testes de integração verificam criação/autenticação de API Key, respostas 401, idempotência/replay/conflito/concorrência, rate limit de cada plano, isolamento entre clientes, CORS, rotas V1/V2, validação, 404 e documentos OpenAPI agrupados.
