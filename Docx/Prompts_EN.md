## Step 1 — PDF to Markdown, requirements and gaps

```
Convert the PDF in this folder to ENUNCIADO.md, faithful to the text, without summarizing or inventing anything.
Then create REQUISITOS.md with:
1) A table of numbered requirements (R1, R2...), grouped by the same sections as the PDF: Regras e Requisitos, Endpoints Esperados, Regras de Negócio Adicionais, Tecnologias e Boas Práticas, Etapa Final, Diferenciais and Critérios de Avaliação.
2) A list of gaps or ambiguities in the PDF, each with the decision you suggest.
Write both files in Portuguese, in simple words. Do not write code.
```

## Step 2 — Compare PROJETO.md with the requirements

```
Compare PROJETO.md with REQUISITOS.md.
Produce a table: Requirement | Covered in PROJETO.md? (yes/no/partial) | Note.
Answer in Portuguese, in simple words. Do not change any file and do not write code.
```

## Step 3 — Check the acceptance criteria

```
Read ACEITE.md. Is each criterion A01..A28 verifiable? Is any requirement in REQUISITOS.md left without a criterion? Answer in a short list, in Portuguese, in simple words. Do not change any file.
```

## Step 4 — Implementation

```
Implement the project exactly as described in PROJETO.md, in this order, without pausing, but showing a short summary after each stage:
1) .gitignore (target/, .env, IDE files), .env, docker-compose.yml (mysql service) and docker/mysql/init (schema and application user). Start mysql and prove that armazem_app cannot run CREATE TABLE.
2) Spring Boot backend (pom.xml, domain, repository, dto, exception, service, controller, config, application.properties).
3) Dockerfile (2 stages, non-root user, Maven cache) and the app service in docker-compose.yml.
Rules: no frontend yet (it comes in a later step); business rules only in EstoqueService; the application connects as armazem_app, never root; comments only where the code is not self-explanatory. At the end run docker compose up -d --build and show docker compose ps.
```

## Step 5 — Automated tests (paste first, then open Swagger)

```
Write the automated tests based on ACEITE.md: one test per criterion marked as "Teste", with the ID in the method name (e.g. a12_tipoDiferenteSoAmanha).
- H2 in MySQL mode only for tests, profile "test".
- Simulated clock (mutable Clock) for criterion A12.
- A14: 10 threads x 100 kg of SECO into C1 at the same time.
Do not change the production code: if a test reveals a bug, stop and show it. Run with:
MSYS_NO_PATHCONV=1 docker run --rm -v "$PWD:/app" -v armazem-m2:/root/.m2 -w /app maven:3.9-eclipse-temurin-17 mvn -B test (if the volume path fails on Windows, fix the path and run again)
Deliver: a table ID | test | result (PASS/FAIL), totals, the concurrency result (successes, rejections, final total) and risks found. Show the real Maven output.
```

## Step 6 — Simple frontend

```
Create a simple frontend to help usability, as listed in the PDF "Diferenciais":
- One page: frontend/index.html with plain HTML, CSS and JavaScript (no framework, no build step).
- Forms: entrada (POST /ingredientes) and saída (POST /ingredientes/{id}/saida), both with the field "responsavel".
- Tables: compartimentos (GET /compartimentos), volume por tipo (GET /ingredientes/volume) and histórico (GET /historico with sortBy and order).
- Show the API error message on screen when a request fails.
- Serve it with nginx in a "frontend" service in docker-compose.yml on port 3000, with a proxy from /api/ to the app (so no CORS is needed).
- All text on the page in Portuguese. No business rules in the frontend: the API decides.
At the end run docker compose up -d --build frontend and show docker compose ps.
```

## Step 7 — README

```
Create README.md in Portuguese, short and simple, explaining the architecture, the technical decisions and how to run the project (as the PDF "Diferenciais" ask):
- What the project is.
- How to run it (docker compose up -d --build), the Swagger URL (localhost:8080/swagger-ui.html) and the frontend URL (localhost:3000).
- How to run the tests.
- The architecture in layers and the folder map.
- The main decisions: app user without root, O(1) free space, pessimistic lock for concurrency, and the gaps of the PDF with my decision for each.
Do not run git commands.
```
