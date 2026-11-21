# Armazém de Ingredientes

API REST para controlar o estoque de ingredientes de uma fábrica em 5 compartimentos (C1 a C5).
Registra entradas e saídas, respeita a capacidade e o tipo de cada compartimento e guarda o histórico de tudo.

## Como rodar na sua máquina

Pré-requisito: **Docker** (com Docker Compose). Não precisa instalar Java nem Maven.

```bash
cp .env.example .env        # troque as senhas no .env
docker compose up -d --build
docker compose ps           # mysql (healthy), app e frontend no ar
```

| O quê | Endereço |
|---|---|
| API | http://localhost:8080 |
| Swagger (documentação e testes da API) | http://localhost:8080/swagger-ui.html |
| Frontend | http://localhost:3000 |

Para parar: `docker compose down` (os dados ficam no volume `armazem-mysql-data`).

## Como rodar os testes

Os testes usam o perfil `test` com H2 em modo MySQL (não precisam do MySQL no ar):

```bash
MSYS_NO_PATHCONV=1 docker run --rm -v "$PWD:/app" -v armazem-m2:/root/.m2 -w /app \
  maven:3.9-eclipse-temurin-17 mvn -B test
```

`MSYS_NO_PATHCONV=1` só é necessário no Git Bash do Windows. Com Maven instalado, basta `mvn test`.
Cada critério do [Docx/ACEITE.md](Docx/ACEITE.md) marcado como "Teste" tem um teste com o ID no nome (ex.: `a12_...`).
O A14 dispara 10 entradas simultâneas de 100 kg de SECO em C1: 6 aceitas, 4 recusadas, total 600 kg.

## Endpoints

| Método e caminho | O que faz |
|---|---|
| `POST /ingredientes` | Entrada. Ingrediente novo → 201; mesmo nome no mesmo compartimento → soma e devolve 200 |
| `POST /ingredientes/{id}/saida` | Saída (retirada/venda) → 200 |
| `GET /ingredientes`, `GET /ingredientes/{id}` | Lista ou busca um ingrediente |
| `GET /ingredientes/volume` | Total por tipo (sempre os 3 tipos) |
| `GET /compartimentos` | Todos os compartimentos com espaço livre |
| `GET /compartimentos/disponiveis?quantidade=&tipo=` | Onde cabe essa quantidade desse tipo hoje |
| `GET /compartimentos/disponiveis-para-venda?tipo=` | Compartimentos com estoque desse tipo |
| `GET /historico?sortBy=date\|compartimento&order=asc\|desc` | Histórico de entradas e saídas (padrão: data, mais recente primeiro) |

Exemplo de entrada:

```bash
curl -X POST http://localhost:8080/ingredientes -H "Content-Type: application/json" \
  -d '{"nome":"Farinha de Trigo","tipo":"SECOS","quantidade":400,"compartimentoId":1,"responsavel":"pedro"}'
```

```json
{"id":"8f1c…","nome":"Farinha de Trigo","tipo":"SECO","quantidade":400.000,"compartimento":"C1"}
```

Exemplo de erro (o formato é sempre o mesmo):

```json
{"timestamp":"2026-11-21T10:00:00","status":400,"error":"Bad Request",
 "message":"Capacidade excedida no compartimento C1: …","path":"/ingredientes"}
```

## Arquitetura

```
navegador ──► frontend (nginx :3000) ──/api/──► app (Spring Boot :8080) ──► MySQL 8.4
```

Camadas do backend:

```
controller  →  service  →  repository  →  banco
 (HTTP,        (TODAS as    (Spring Data JPA,
  validação)    regras)      locks)
```

- **controller:** recebe a requisição, valida os campos (`@Valid`) e devolve o status HTTP.
- **service** (`EstoqueService`): todas as regras de negócio, cada operação numa transação.
- **repository:** acesso ao banco; trava o compartimento durante entrada e saída.
- **exception:** `GlobalExceptionHandler` transforma qualquer erro no JSON padrão acima.

Mapa de pastas:

```
docker-compose.yml        mysql + app + frontend
Dockerfile                build em 2 estágios, cache do Maven, usuário não-root
.env.example              modelo de senhas (o .env real não vai para o Git)
docker/mysql/init/        01-schema.sql (tabelas) e 02-usuario-app.sh (usuário da aplicação)
frontend/                 index.html (HTML/CSS/JS puro) e nginx.conf (proxy /api/ → app)
src/main/java/com/vega/armazem/
  controller/             endpoints HTTP
  service/                EstoqueService (regras de negócio)
  repository/             interfaces Spring Data JPA
  domain/                 entidades e enums (TipoIngrediente, Operacao)
  dto/                    objetos de entrada e saída da API
  exception/              exceções e tratamento de erros
  config/                 relógio, conversores, Swagger, criação de C1..C5
src/test/                 testes (H2, relógio simulado, concorrência)
Docx/                     enunciado, requisitos, projeto e critérios de aceite
```

## Decisões técnicas

- **Aplicação sem root no banco.** A app conecta como `armazem_app`, que só pode `SELECT/INSERT/UPDATE/DELETE`.
  As tabelas são criadas pelo root nos scripts de init, e o Hibernate não altera o schema (`ddl-auto=none`).
  Se a aplicação for atacada, ela não consegue apagar ou mudar tabelas.
- **Espaço livre em O(1).** Cada compartimento guarda sua `quantidade_atual`, atualizada em toda entrada e saída.
  O espaço livre é `capacidade - quantidade_atual`, sem somar os ingredientes a cada consulta.
- **Lock pessimista para concorrência.** Entrada e saída começam com `SELECT … FOR UPDATE` no compartimento:
  duas operações no mesmo compartimento esperam uma pela outra, então nunca passa da capacidade nem fica negativo.
  Há também `@Version` (resposta 409 se houver conflito) e isolamento `READ COMMITTED` para sempre ler o saldo atual.
- **MySQL para rodar, H2 só nos testes.** O PDF permite banco em memória, mas aí os dados sumiriam ao reiniciar.
- **Relógio injetável** (`Clock`, fuso `America/Sao_Paulo`): a regra do "amanhã" pode ser testada sem esperar um dia.
- **IDs públicos em UUID.** A API não expõe o id interno do banco; compartimentos aparecem pelo código (C1..C5).
- **Quantidades** em decimal com até 3 casas. A unidade vem do tipo: SECO e REFRIGERADO em kg, LIQUIDO em litros.

Capacidade de cada compartimento: LIQUIDO 500 L, REFRIGERADO 400 kg, SECO 600 kg.

## Lacunas do PDF e a decisão tomada

| Lacuna no PDF | Decisão |
|---|---|
| Não existe endpoint de saída, mas o histórico precisa de SAÍDA | Criado `POST /ingredientes/{id}/saida` com `quantidade` e `responsavel` |
| Tipo escrito como `SECOS` no POST e `SECO` no volume | Aceita singular ou plural, maiúscula ou minúscula, com ou sem acento; a resposta usa sempre `SECO`, `LIQUIDO`, `REFRIGERADO` |
| `compartimentoId: 1` × nomes C1..C5 | Aceita os dois: `compartimentoId` de 1 a 5 (1 → C1) ou `compartimento` "C1".."C5" |
| Mesmo ingrediente cadastrado de novo | Soma a quantidade no ingrediente existente do mesmo compartimento (200) e grava uma nova ENTRADA |
| "Secos hoje, outro tipo só amanhã" e "mesmo período" | Vale para todos os tipos. Período = dia do calendário em São Paulo. Um compartimento só troca de tipo se estiver vazio **e** o último armazenamento foi antes de hoje |
| Tipo de um compartimento novo | O primeiro ingrediente define |
| Ingredientes diferentes do mesmo tipo | Podem dividir o compartimento; a capacidade vale para a soma |
| Saída maior que o estoque | Recusada com 400; nada é gravado |
| Ingrediente com quantidade 0 | Continua cadastrado, para o histórico continuar ligado a ele |
| Volume por tipo sem estoque | Sempre os 3 tipos, com 0 quando vazio; campos `type` e `totalQuantity` em inglês, como no PDF |
| Parâmetros dos endpoints de compartimentos | `quantidade` e `tipo` são obrigatórios; faltando ou inválidos → 400 |
| Ordenação do histórico | `sortBy=date` (padrão) ou `compartimento`, `order=desc` (padrão) ou `asc`; valor inválido → 400 |
| Paginação do histórico | Não tem; lista completa |
| Status HTTP e formato de erro | 201 cadastro novo, 200 consultas/soma/saída, 400 dado inválido ou regra violada, 404 não encontrado, 409 conflito; erro sempre no mesmo JSON |
| Login e "responsável" | Sem login; `responsavel` é texto obrigatório (até 100 caracteres) |
| Criação dos compartimentos | A aplicação cria C1..C5 ao iniciar, se não existirem |
| PUT e DELETE | Não existem: a quantidade só muda por entrada e saída, para o histórico ficar correto |
| Frontend sem tecnologia definida | Uma página em HTML/CSS/JS puro servida pelo nginx; as regras ficam só na API |
| Texto com erros ("compartilhamento", frase cortada "na sua") | Entendido como "compartimento" e "como rodar na sua máquina" |
