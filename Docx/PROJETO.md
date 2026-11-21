# PROJETO: Armazém de Ingredientes

Decisões tomadas antes de programar. Cada regra aponta para o critério do `ACEITE.md` que a comprova.

## 1. Stack

| Item | Escolha |
|---|---|
| Backend | Java 17, Spring Boot 3.3 (Web, Data JPA, Validation), Swagger (springdoc), Maven |
| Banco | MySQL 8.4 em Docker; H2 **somente** nos testes |
| Containers | Docker Compose: `mysql`, `app`, `frontend` (nginx, porta 3000) |
| Frontend | Uma página HTML/CSS/JS, sem framework e sem regras de negócio |
| Testes | JUnit 5, AssertJ, MockMvc |

## 2. Arquitetura

`controller` → `service` → `repository` → MySQL

- `@RestController`: só HTTP e validação. `@Service` (`EstoqueService`): **todas** as regras. `@Repository`: acesso ao banco.
- `@Transactional`: cada entrada/saída e o seu histórico são gravados juntos, ou nada é gravado.
- A API usa DTOs (records), nunca as entidades.
- Erros num único `GlobalExceptionHandler`, sempre com o mesmo JSON: `timestamp, status, error, message, path`.
  - 400: regra de negócio, campo inválido ou parâmetro inválido.
  - 404: UUID ou código inexistente (UUID mal formatado → 400).
  - 409: conflito de versão (`@Version`).
- `Clock` injetável (America/Sao_Paulo), para os testes simularem "amanhã".

**Regras de código** (verificadas no A19):
- Nomes claros, em português, iguais aos termos do PDF (`compartimento`, `responsavel`, `quantidadeAtual`).
- Métodos curtos, cada um com uma responsabilidade.
- Sem números mágicos: as capacidades ficam no enum.
- Injeção por construtor (campos `final`), sem `@Autowired` em campo.
- Sem código repetido, sem código morto ou comentado.
- Comentários só onde o código não se explica sozinho.

## 3. Banco de dados

- **Usuários:** `root` só cria o schema (scripts em `docker/mysql/init/`). A aplicação usa `armazem_app`, só com `SELECT, INSERT, UPDATE, DELETE`. `ddl-auto=none`. Senhas no `.env` (fora do Git).
- **Colunas em todas as tabelas:** `id` (BIGINT, só interno: chaves e FKs, nunca sai na API), `uuid` (identificador público usado na API), `ativo` (sempre 1), `data_criacao`, `data_atualizacao`.
- **Identificadores na API:** ingrediente e movimentação pelo `uuid`; compartimento pelo `codigo` (C1..C5). O campo `id` das respostas contém o UUID.
- **Compartimento no POST:** aceita o número do PDF (`compartimentoId: 1`) **ou** o código (`compartimento: "C1"`). O número vira código pela regra `1 → C1 … 5 → C5` (nunca pelo `id` interno do banco).

| Tabela | Colunas |
|---|---|
| `compartimento` | `codigo` (C1..C5), `tipo_atual`, `quantidade_atual`, `data_ultimo_armazenamento`, `version` |
| `ingrediente` | `nome`, `tipo`, `quantidade`, `compartimento_id` |
| `movimentacao` | `data_hora` (com índice), `operacao` (ENTRADA/SAIDA), `quantidade`, `tipo`, `responsavel`, `compartimento_id`, `ingrediente_id` |

`ingrediente`: (`nome`, `compartimento_id`) é único.

Os 5 compartimentos são criados na primeira subida, em ordem: C1 … C5.

## 4. Regras de negócio

| Regra | Como | Aceite |
|---|---|---|
| Capacidade: SECO 600 kg, LIQUIDO 500 L, REFRIGERADO 400 kg | Enum `TipoIngrediente` | A02 |
| Só guarda se houver espaço | Espaço livre = capacidade − `quantidade_atual` (O(1)) | A04, A18 |
| Um tipo por compartimento | O primeiro ingrediente define o tipo | A05 |
| Outro tipo só amanhã | O compartimento só troca de tipo se estiver vazio **e** o último armazenamento foi antes de hoje | A12 |
| Saída | Baixa o ingrediente e o compartimento; maior que o saldo → 400 | A06 |
| Histórico obrigatório | Mesma transação | A07 |
| Concorrência | Lock pessimista (`SELECT ... FOR UPDATE`) no compartimento + `@Version` | A14 |
| `SECOS` na requisição, `SECO` na resposta | O enum aceita os dois | A03, A17 |
| Campos do POST | Todos obrigatórios; `quantidade` > 0 e até 3 casas decimais (ex.: `200.000`), a unidade vem do tipo; textos até 100 caracteres | A13 |
| Mesmo ingrediente no mesmo compartimento | Soma a quantidade no ingrediente existente (nome comparado sem diferença de maiúscula/minúscula e espaços extras) | A03 |

## 5. Endpoints

| Endpoint | Resposta | Aceite |
|---|---|---|
| `POST /ingredientes` | Se já existir ingrediente com o mesmo `nome` no mesmo compartimento, soma a quantidade e devolve 200; senão cria um novo e devolve 201. Sempre grava uma ENTRADA. Corpo: `nome, tipo, quantidade, compartimentoId` (1..5, como no PDF) **ou** `compartimento` (C1..C5), `responsavel`. Falta dos dois, ou os dois diferentes → 400; número fora de 1..5 ou código inexistente → 404 | A03 |
| `GET /ingredientes`, `GET /ingredientes/{uuid}` | Lista / um ingrediente (404 se não existir) | A13 |
| `GET /ingredientes/volume` | Sempre os 3 tipos, em ordem alfabética (LIQUIDO, REFRIGERADO, SECO): `type`, `totalQuantity` (0 se vazio) | A08 |
| `GET /compartimentos/disponiveis?quantidade=&tipo=` | Compartimentos com espaço e que aceitam o tipo hoje. `quantidade` e `tipo` são obrigatórios (falta → 400) | A09 |
| `GET /compartimentos/disponiveis-para-venda?tipo=` | Compartimentos do tipo com estoque > 0 | A10 |
| `GET /historico?sortBy=date\|compartimento&order=asc\|desc` | Lista completa; padrão `date`, `desc`. Desempate: por data, o compartimento (C1..C5); por compartimento, a data | A11 |
| **Extra:** `POST /ingredientes/{uuid}/saida` | 200; o PDF exige histórico de saída, mas não define o endpoint | A06 |
| **Extra:** `GET /compartimentos` | Todos os compartimentos | A01 |

Compartimento na resposta: `id` (UUID), `codigo, tipoAtual, capacidade, quantidadeAtual, espacoLivre`.
Ingrediente e movimentação na resposta: `id` (UUID); o compartimento aparece pelo `codigo`.

- **Sem PUT e sem DELETE:** a quantidade só muda por entrada (POST) e saída, para o histórico ficar sempre correto.
- **Sem login:** `responsavel` é texto livre obrigatório (até 100 caracteres).
- **Tipo na entrada:** aceita singular ou plural, maiúscula ou minúscula, com ou sem acento (`SECOS`, `seco`, `líquido`, `LIQUIDO`...). A resposta usa sempre `SECO`, `LIQUIDO`, `REFRIGERADO`.

## 6. Pastas

```
armazem/
├── README.md            como executar, arquitetura e decisões
├── docker-compose.yml   mysql + app + frontend
├── Dockerfile           2 estágios, usuário não-root
├── .env                 senhas (fora do Git)
├── docker/mysql/init/   schema + usuário da aplicação
├── frontend/            index.html + nginx.conf (proxy /api → app)
└── src/
    ├── main/java/com/vega/armazem/
    │   controller/ service/ repository/ domain/ dto/ exception/ config/
    └── test/            testes + H2
```

## 7. Lacunas do PDF e decisão

| Lacuna | Decisão |
|---|---|
| Sem endpoint de saída | `POST /ingredientes/{uuid}/saida` |
| "Mesmo período" | Mesmo dia do calendário |
| Tipo antes do primeiro uso | O primeiro ingrediente define |
| Compartimento esvaziado | Mantém o tipo; só troca se estiver vazio **e** o último armazenamento foi antes de hoje (mesma regra da seção 4) |
| Tabelas não definidas | Modelo da seção 3 |
| `compartimentoId: 1` × nomes C1..C5 | Aceita os dois; `1 → C1 … 5 → C5` (seção 3) |
| POST repetido do mesmo ingrediente | Soma a quantidade no ingrediente existente do mesmo compartimento |
| Paginação do histórico | Não tem; lista completa |
| "Como rodar o projeto na sua" (frase cortada) | Entendido como "na sua máquina" (seção 9) |

## 8. Testes

O que cada teste prova está no `ACEITE.md` (A01..A28). Resumo:
- **Unitários** (`EstoqueServiceTest`, `TipoIngredienteTest`): capacidade, tipo, regra de "amanhã" com `Clock` fixo, saída maior que o saldo, leitura do tipo (`SECOS`/`seco`/`líquido`), `compartimentoId` → código, POST repetido soma a quantidade e o histórico ganha 2 ENTRADAS.
- **Integração** (`ApiIntegrationTest`, MockMvc + H2): todos os endpoints, status 201/200/400/404 e o JSON de erro.
- **Concorrência** (`ConcorrenciaTest`): várias entradas ao mesmo tempo no mesmo compartimento nunca passam da capacidade.
- Comando: `mvn test` (ou dentro do Docker, sem Maven instalado).

## 9. README

O `README.md` terá, nesta ordem:
1. O que é o projeto (2 linhas).
2. **Como rodar na sua máquina:** pré-requisitos (Docker), copiar `.env.example` para `.env`, `docker compose up -d --build`, endereços da API, do Swagger e do frontend.
3. Como rodar os testes.
4. Endpoints com exemplo de requisição e resposta.
5. Arquitetura (camadas e pastas da seção 2 e 6).
6. Decisões técnicas e lacunas do PDF (seções 4 e 7).

## 10. Entrega (GitHub)

1. Criar um repositório no GitHub (privado).
2. Conferir que `.env` está no `.gitignore` e que só `.env.example` vai para o Git.
3. Enviar o código completo (backend, frontend, Docker, testes, README).
4. Em *Settings → Collaborators*, adicionar o e-mail informado na entrevista (ou enviar o link de convite).
5. Testar com um clone limpo: seguir só o README e ver tudo subir.
