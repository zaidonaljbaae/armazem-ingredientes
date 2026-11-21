# Arquitetura – Armazém de Ingredientes

## Propósito
API REST (desafio técnico Vega) para estoque de ingredientes em 5 compartimentos (C1..C5),
com regras de capacidade, tipo por compartimento, regra do "amanhã" e histórico de movimentações.
Especificação: `Docx/ENUNCIADO.md`, `Docx/REQUISITOS.md`, `Docx/PROJETO.md`, `Docx/ACEITE.md`.

## Stack
- Java 17, Spring Boot 3.3.5 (Web, Data JPA, Validation), springdoc (Swagger), Maven
- MySQL 8.4 em Docker; H2 só nos testes
- Docker Compose: `mysql`, `app`, `frontend` (nginx, HTML/CSS/JS puro)

## Pastas
```
.env / .env.example      senhas (só .env.example vai para o Git)
docker-compose.yml       mysql + app + frontend
Dockerfile               2 estágios, cache Maven, usuário não-root
docker/mysql/init/       01-schema.sql (tabelas), 02-usuario-app.sh (armazem_app só DML)
src/main/java/com/vega/armazem/
  controller/  HTTP + @Valid (Ingrediente, Compartimento, Historico)
  service/     EstoqueService: TODAS as regras de negócio
  repository/  Spring Data JPA (locks FOR UPDATE no compartimento)
  domain/      entidades (BaseEntity, Compartimento, Ingrediente, Movimentacao) e enums
  dto/         records de entrada/saída da API
  exception/   GlobalExceptionHandler + exceções 400/404
  config/      Clock, conversor de tipo, OpenAPI, criação de C1..C5
src/main/resources/application.properties
frontend/                index.html (página única) + nginx.conf (proxy /api/ → app:8080)
Docx/                    PDF e documentos de especificação
```

## Fluxo de dados
controller → EstoqueService (@Transactional) → repository → MySQL.
Entrada/saída: trava o compartimento (`SELECT ... FOR UPDATE`), valida, altera
ingrediente + compartimento e grava a movimentação na mesma transação.

## Decisões-chave
- App conecta como `armazem_app` (SELECT/INSERT/UPDATE/DELETE); `ddl-auto=none`; schema criado pelo root no init.
- API expõe só `uuid` (campo `id`); compartimento pelo `codigo`. POST aceita `compartimentoId` 1..5 ou `compartimento` C1..C5.
- POST repetido (mesmo nome, mesmo compartimento) soma a quantidade → 200; novo → 201.
- Espaço livre O(1) via `quantidade_atual` do compartimento.
- Concorrência: lock pessimista + `@Version` (409) + isolamento READ COMMITTED.
- `Clock` injetável em America/Sao_Paulo para a regra do "amanhã".

## Como rodar
- `cp .env.example .env` (trocar senhas) e `docker compose up -d --build`
- API: http://localhost:8080 — Swagger: http://localhost:8080/swagger-ui.html
- Frontend: http://localhost:3000 (chama `/api/...`, mesma origem, sem CORS)
- Testes: `MSYS_NO_PATHCONV=1 docker run --rm -v "$PWD:/app" -v armazem-m2:/root/.m2 -w /app maven:3.9-eclipse-temurin-17 mvn -B test`
- Maven não precisa estar instalado (build dentro do Docker).
