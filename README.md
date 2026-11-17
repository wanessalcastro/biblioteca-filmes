# API Biblioteca de Filmes

API RESTful (Spring Boot 4.1.1 · Java 17+ · Maven) para gerenciar **filmes, diretores, gêneros, usuários e avaliações**, aplicando WebServices/REST, Bean Validation, documentação com Swagger (Springdoc OpenAPI) e HATEOAS (**Parte 1**), além de **idempotência, autenticação por chave de API, rate limiting, CORS e versionamento** (**Parte 2**).

## Como executar

Requisitos: Java 17 ou superior. O Maven Wrapper acompanha o projeto.

```bash
./mvnw spring-boot:run        # Linux/macOS
.\mvnw spring-boot:run        # Windows
```

| Recurso | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Console H2 | http://localhost:8080/h2-console |

Console H2: JDBC URL `jdbc:h2:mem:bibliotecafilmes`, usuário `sa`, senha em branco.
Ao subir, `data.sql` carrega 4 diretores, 5 gêneros, 3 usuários, 5 filmes, 6 avaliações e 2 chaves de API de demonstração (veja a Parte 2).

## Modelo de dados

```
Diretor 1───N Filme N───N Genero
                │
                1
                N
            Avaliacao N───1 Usuario 1───N ApiKey
```

`IdempotencyRecord` é uma tabela de apoio (sem relacionamentos) usada pela idempotência.

| Entidade | Principais validações |
|---|---|
| Diretor | `nome` @NotBlank/@Size · `nacionalidade` @NotBlank · `dataNascimento` @NotNull @Past |
| Genero | `nome` @NotBlank @Size (único) · `descricao` @Size |
| Usuario | `nome` @NotBlank · `email` @NotBlank @Email (único) · `dataNascimento` @NotNull @Past |
| Filme | `titulo` @NotBlank · `anoLancamento` @Min(1888) @Max(2100) · `duracaoMinutos` @Min(1) @Max(600) · `classificacao` @NotNull · diretor e ≥1 gênero obrigatórios |
| Avaliacao | `nota` @Min(1) @Max(5) · `comentario` @Size(500) · filme e usuário obrigatórios (um usuário avalia cada filme uma única vez) |
| ApiKey | `descricao` @NotBlank @Size · `nivel` @NotNull (enum `NivelAcesso`) · `usuario` obrigatório · `expiraEm` @Future (no DTO) |

## Endpoints

Todas as listagens são paginadas (`?page=0&size=10&sort=campo,asc`) e retornam `PagedModel` HAL.

| Recurso | CRUD | Consultas personalizadas (paginadas) |
|---|---|---|
| `/diretores` | POST, GET, GET `/{id}`, PUT `/{id}`, DELETE `/{id}` | `GET /diretores/nacionalidade/{nacionalidade}` |
| `/generos` | idem | `GET /generos/filme/{filmeId}` |
| `/usuarios` | idem | `GET /usuarios/buscar?email=` |
| `/filmes` | idem | `GET /filmes/buscar?titulo=` · `/filmes/ano/{ano}` · `/filmes/genero/{generoId}` · `/filmes/diretor/{diretorId}` |
| `/avaliacoes` | idem | `GET /avaliacoes/filme/{filmeId}` · `/avaliacoes/usuario/{usuarioId}` |

Códigos HTTP: `200`, `201` (com header `Location`), `204`, `400` (validação, com lista de campos), `401`/`403` (chave de API), `404`, `405`, `409` (e-mail/nome duplicado, avaliação duplicada, remoção de registro em uso), `415`, `422` (chave de idempotência reutilizada), `429` (rate limit) e `500`.

> A partir da Parte 2, **POST/PUT exigem o cabeçalho `X-API-Key`** e **DELETE exige uma chave ADMIN** (consultas GET continuam públicas).

## HATEOAS

Cada recurso traz `self`, `update`, `delete` e `all`, além de links relacionados:
filme → `diretor`, `generos`, `avaliacoes` · diretor/gênero → `filmes` · usuário → `avaliacoes` · avaliação → `filme`, `usuario`.

## Exemplos de requisição

```bash
# Criar diretor (201 + Location)
curl -i -X POST http://localhost:8080/diretores -H "Content-Type: application/json" -H "X-API-Key: bf_demo_usuario_0002" \
  -d '{"nome":"Steven Spielberg","nacionalidade":"Americano","dataNascimento":"1946-12-18"}'

# Listar filmes paginado e ordenado
curl "http://localhost:8080/filmes?page=0&size=3&sort=titulo,asc"

# Criar filme
curl -i -X POST http://localhost:8080/filmes -H "Content-Type: application/json" -H "X-API-Key: bf_demo_usuario_0002" \
  -d '{"titulo":"Tenet","anoLancamento":2020,"duracaoMinutos":150,"sinopse":"Um agente manipula o fluxo do tempo.","classificacao":"DOZE","diretorId":1,"generoIds":[1,3]}'

# Filmes por gênero / por ano / por título
curl "http://localhost:8080/filmes/genero/1"
curl "http://localhost:8080/filmes/ano/2014"
curl "http://localhost:8080/filmes/buscar?titulo=inter"

# Erro de validação (400 com lista de campos)
curl -i -X POST http://localhost:8080/usuarios -H "Content-Type: application/json" -H "X-API-Key: bf_demo_usuario_0002" \
  -d '{"nome":"","email":"invalido","dataNascimento":"2999-01-01"}'

# Recurso inexistente (404)
curl -i http://localhost:8080/filmes/999
```

## Parte 2 — Recursos avançados

Os recursos transversais são implementados como **filtros** executados nesta ordem (menor = mais externo): `CORS → Rate limit → API Key → Idempotência`. Erros lançados nos filtros passam pelo mesmo `GlobalExceptionHandler`, então **toda resposta de erro tem o mesmo formato** (`ErroResponseDTO`). Apenas as rotas de negócio (`/filmes`, `/diretores`, `/generos`, `/usuarios`, `/avaliacoes`, `/api-keys`) passam pelos filtros; Swagger e console H2 ficam de fora.

### 4.2 Autenticação por chave de API (`X-API-Key`)

| Requisição | Exige |
|---|---|
| `GET` nos recursos | nada (público) |
| `POST`, `PUT` | chave `USUARIO` ou `ADMIN` |
| `DELETE` | chave `ADMIN` |
| qualquer rota de `/api-keys` | chave `ADMIN` |

Sem chave, chave inválida, revogada ou expirada → **401** (com `WWW-Authenticate`). Chave válida com nível insuficiente → **403**.

Chaves ficam na entidade `ApiKey` (ligada a um `Usuario`, nível `USUARIO`/`ADMIN`, ativa, validade opcional). O valor secreto (`bf_...`, 256 bits aleatórios) **é mostrado só uma vez**, na criação; no banco fica apenas o hash SHA-256.

| Endpoint (só ADMIN) | Função |
|---|---|
| `POST /api-keys` | gera uma chave para um usuário (retorna o segredo uma única vez) |
| `GET /api-keys` · `GET /api-keys/{id}` | lista (paginada) / consulta metadados |
| `PUT /api-keys/{id}` | altera descrição, nível, situação (`ativa`) e validade |
| `DELETE /api-keys/{id}` | **revoga** a chave (`ativa=false`); o histórico é mantido |
| `GET /api-keys/usuario/{usuarioId}` | chaves de um usuário (paginada) |

**Chaves de demonstração** (criadas pelo `data.sql`; remova-as fora do ambiente de desenvolvimento):

| Chave | Nível | Usuário |
|---|---|---|
| `bf_demo_admin_0001` | ADMIN | 1 — Maria Silva |
| `bf_demo_usuario_0002` | USUARIO | 2 — João Souza |

No Swagger UI use o botão **Authorize** para informar a chave. Um usuário só pode ser removido se não tiver avaliações nem chaves.

### 4.1 Idempotência (`X-Idempotency-Key`)

Em qualquer `POST`, envie `X-Idempotency-Key` (até 128 caracteres, ex.: um UUID):

- **1ª requisição**: processada normalmente; se der certo (2xx), a resposta é guardada (tabela `idempotency_records`, TTL de 24 h);
- **repetição (mesma chave e mesmo conteúdo)**: devolve **a resposta original** (mesmo status, corpo e `Location`) com `Idempotent-Replayed: true`, **sem criar nada de novo**;
- **mesma chave com outro corpo/rota**: **422**;
- **mesma chave enquanto a 1ª ainda processa**: **409**.

Detalhes de projeto: o cabeçalho é opcional; as chaves são isoladas por chave de API (clientes diferentes podem usar o mesmo valor); respostas de erro não são guardadas, permitindo corrigir e repetir; `POST /api-keys` não usa idempotência para que o segredo nunca seja gravado no banco.

### 4.3 Rate limiting

Janela fixa **por IP**: `app.rate-limit.requests` (padrão 60) a cada `app.rate-limit.window-seconds` (padrão 60). Toda resposta traz `X-RateLimit-Limit`, `X-RateLimit-Remaining` e `X-RateLimit-Reset` (segundos até reiniciar). Ao exceder: **429** com `Retry-After`. O contador fica em memória (instância única). Atrás de proxy reverso, configure `server.forward-headers-strategy` para que o IP real seja visto.

### 4.4 CORS

Configurado em `app.cors.*` (`application.properties`): origens permitidas (padrão `localhost:3000`, `4200` e `5173`), métodos `GET, POST, PUT, PATCH, DELETE, OPTIONS`, cabeçalhos `Content-Type, Accept, X-API-Key, X-Idempotency-Key, X-API-Version` e cabeçalhos expostos ao navegador (`Location`, `Retry-After`, `X-RateLimit-*`, `X-API-Version`, `Idempotent-Replayed`). Implementado como filtro para que também as respostas 401/403/429 tenham cabeçalhos CORS. Origens fora da lista recebem **403** no preflight.

### 4.5 Versionamento (`X-API-Version`)

`GET /filmes/{id}` tem duas versões, escolhidas pelo cabeçalho:

| Cabeçalho | Resposta |
|---|---|
| ausente ou `X-API-Version: 1` | formato original (`FilmeResponseDTO`) |
| `X-API-Version: 2` | formato enriquecido (`FilmeResponseV2DTO`): `duracaoFormatada` (ex.: `2h49min`), `diretor` resumido, nomes dos `generos`, `mediaAvaliacoes` e `totalAvaliacoes` |
| outro valor | **400** informando as versões suportadas |

A resposta devolve `X-API-Version` com a versão efetivamente servida. A v2 reaproveita os mesmos links HATEOAS.

### 5. Validação e tratamento de erros

Além do Bean Validation dos corpos (`@Valid`), os parâmetros de busca (`titulo`, `email`, `nacionalidade`, `ano`) também são validados. O `GlobalExceptionHandler` (`@RestControllerAdvice`) cobre: validação (400, com lista de campos), JSON malformado, tipo de parâmetro inválido, parâmetro obrigatório ausente, `X-API-Version` inválida, 401, 403, 404 (recurso e rota inexistentes), 405, 409, 415, 422, 429 e 500 genérico (sem vazar detalhes internos).

### 6. Documentação (Swagger/OpenAPI)

Todas as operações — inclusive `/api-keys` — estão documentadas, com descrição, exemplos e códigos de resposta. O Swagger traz o esquema de segurança `X-API-Key` (botão **Authorize**), marca as rotas protegidas, e acrescenta automaticamente os cabeçalhos `X-Idempotency-Key` nos POST e as respostas 401/403/422/429. O "Try it out" está habilitado.

### Demonstração (curl)

```bash
# 401 — POST sem chave
curl -i -X POST http://localhost:8080/generos -H "Content-Type: application/json" -d '{"nome":"Terror"}'

# 403 — DELETE com chave USUARIO
curl -i -X DELETE http://localhost:8080/generos/4 -H "X-API-Key: bf_demo_usuario_0002"

# Gerar uma chave nova (ADMIN) — guarde o campo "chave" da resposta
curl -i -X POST http://localhost:8080/api-keys -H "Content-Type: application/json" -H "X-API-Key: bf_demo_admin_0001" \
  -d '{"usuarioId":3,"descricao":"Chave da Ana","nivel":"USUARIO"}'

# Idempotência — rode DUAS vezes: a 2ª devolve a mesma resposta (Idempotent-Replayed: true) e não cria outro gênero
curl -i -X POST http://localhost:8080/generos -H "Content-Type: application/json" \
  -H "X-API-Key: bf_demo_usuario_0002" -H "X-Idempotency-Key: gen-terror-001" -d '{"nome":"Terror"}'
# Mesma chave, corpo diferente -> 422
curl -i -X POST http://localhost:8080/generos -H "Content-Type: application/json" \
  -H "X-API-Key: bf_demo_usuario_0002" -H "X-Idempotency-Key: gen-terror-001" -d '{"nome":"Suspense"}'

# Versionamento
curl -i http://localhost:8080/filmes/1                              # v1
curl -i http://localhost:8080/filmes/1 -H "X-API-Version: 2"        # v2
curl -i http://localhost:8080/filmes/1 -H "X-API-Version: 9"        # 400

# CORS — preflight de origem permitida (200 + Access-Control-*) e de origem não permitida (403)
curl -i -X OPTIONS http://localhost:8080/filmes -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" -H "Access-Control-Request-Headers: x-api-key,content-type"
curl -i -X OPTIONS http://localhost:8080/filmes -H "Origin: http://site-desconhecido.com" -H "Access-Control-Request-Method: POST"

# Rate limiting — com app.rate-limit.requests=5 no application.properties, a 6ª requisição devolve 429 + Retry-After
for i in $(seq 1 7); do curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/filmes; done
```

## Estrutura de pacotes

```
com.exemplo.bibliotecafilmes
├── config       (OpenApiConfig, ExemplosOpenApi, WebConfig)
├── controller   (um por entidade + ApiKeyController)
├── filter       (CORS, rate limit, API Key, idempotência)
├── interceptor  (ApiVersionInterceptor — valida X-API-Version)
├── util         (HashUtil)
├── assembler    (RepresentationModelAssembler por entidade)
├── dto          (request/response)
├── entity       (entidades JPA e enum)
├── exception    (GlobalExceptionHandler e exceções de negócio)
├── repository
└── service
```
# biblioteca-filmes
