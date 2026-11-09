# Release Notes — API Biblioteca V2

## Versão 2.0.0

A V2 acrescenta recursos de segurança, controle de tráfego e confiabilidade à API Biblioteca. A V1 permanece como o contrato original da Parte 1 e não exige os mecanismos descritos abaixo.

### Novidades em relação à V1

#### Autenticação e gerenciamento de API Keys

- As rotas de recursos em `/api/v2` exigem uma chave válida no cabeçalho `X-API-Key`.
- `POST /api/chaves-api` permite gerar uma chave e escolher um plano de acesso. A chave completa é exibida na criação; o sistema persiste seu hash.
- A consulta e a revogação de chaves exigem uma chave do plano PREMIUM.
- Chave ausente, inválida ou revogada retorna HTTP 401. Falta de permissão retorna HTTP 403.
- Planos disponíveis: BÁSICO (30 requisições por minuto), PROFISSIONAL (100) e PREMIUM (300).

#### Proteção progressiva contra falhas de autenticação

- Cinco tentativas sem chave ou com chave inválida, originadas do mesmo IP em até 10 minutos, fazem novas tentativas inválidas desse IP receberem bloqueio por 15 minutos.
- Reincidências elevam o bloqueio para 1 hora e depois para 24 horas.
- Tentativas bloqueadas retornam HTTP 429 e o cabeçalho `Retry-After` informa os segundos restantes.
- Uma chave válida continua podendo autenticar e limpa o histórico de falhas do IP.
- Erros de validação e regras de negócio não são contabilizados como falhas de autenticação.

#### Idempotência nos POSTs de recursos

- Os POSTs de recursos V2 exigem o cabeçalho `X-Idempotency-Key`.
- Repetir a chave com a mesma rota e corpo reproduz a resposta armazenada, sem executar a criação novamente.
- Reutilizar a chave com corpo diferente retorna HTTP 409.
- Falhas não são armazenadas, permitindo corrigir a requisição e tentar novamente. As entradas idempotentes expiram após 24 horas e requisições concorrentes são protegidas contra duplicidade.

#### Rate Limit por plano

- A V2 aplica limites com Bucket4j, em buckets independentes por chave de API.
- Ao exceder o plano, a resposta é HTTP 429 e inclui `Retry-After`, `X-RateLimit-Limit`, `X-RateLimit-Remaining` e `X-RateLimit-Reset`.
- Requisições OPTIONS e páginas/documentos do Swagger não consomem tokens.

#### CORS

- Origens permitidas: `http://localhost:3000`, `http://localhost:5173` e `http://127.0.0.1:5500`.
- Métodos permitidos: GET, POST, PUT, PATCH, DELETE e OPTIONS.
- A configuração aceita os cabeçalhos de conteúdo, autorização, API Key, idempotência e versão, e expõe ao frontend os cabeçalhos de Rate Limit.

#### Rotas e documentação versionadas

- A V2 está disponível em `/api/v2/autores`, `/api/v2/editoras`, `/api/v2/livros`, `/api/v2/categorias`, `/api/v2/leitores` e `/api/v2/emprestimos`.
- As operações V2 documentam o cabeçalho `X-API-Version: 2`.
- O Swagger apresenta grupos separados V1 e V2. As operações V2 documentam API Key, idempotência, planos, Rate Limit e respostas de erro.

### Compatibilidade

A V1 preserva seu contrato original: continua disponível sem `X-API-Key` e sem `X-Idempotency-Key`. Os requisitos de autenticação, idempotência e Rate Limit são aplicados à V2 e às operações protegidas de gerenciamento de chaves.
