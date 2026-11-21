# Requisitos – Armazém de Ingredientes

Este arquivo lista os requisitos do PDF `Desafio_Tecnico_-_Java_Pleno_Vega 1.pdf` (texto completo em `ENUNCIADO.md`).
A coluna "Origem" diz de onde veio cada requisito no PDF.

---

## 1. Tabela de requisitos

### 🧩 Regras e Requisitos

> Os requisitos R1 a R4 vêm da "Descrição Geral", mas foram colocados aqui porque são regras do armazém.

| ID | Requisito | Origem |
|----|-----------|--------|
| R1 | O sistema é uma API RESTful para gerenciar o estoque e o armazenamento de ingredientes de uma fábrica de alimentos (cadastro, movimentação e consulta). | Descrição Geral |
| R2 | O armazém tem exatamente 5 compartimentos. | Descrição Geral |
| R3 | Os compartimentos se chamam C1, C2, C3, C4 e C5. | Descrição Geral |
| R4 | Cada compartimento guarda só um tipo de ingrediente por vez. | Descrição Geral |
| R5 | Existem três tipos de ingrediente: secos, líquidos e refrigerados. | Descrição Geral / Regras |
| R6 | Capacidade máxima de um compartimento com secos: 600 kg. | Regras e Requisitos |
| R7 | Capacidade máxima de um compartimento com líquidos: 500 L. | Regras e Requisitos |
| R8 | Capacidade máxima de um compartimento com refrigerados: 400 kg. | Regras e Requisitos |
| R9 | Um compartimento não pode misturar tipos diferentes no mesmo período. | Regras e Requisitos |
| R10 | Só é possível guardar um ingrediente se houver espaço suficiente no compartimento. | Regras e Requisitos |
| R11 | Toda entrada e toda saída devem ser registradas no histórico. | Regras e Requisitos |
| R12 | Cada registro do histórico tem: data e hora, tipo de operação (ENTRADA ou SAÍDA), quantidade, tipo de ingrediente e responsável. | Regras e Requisitos |

### 🔗 Endpoints Esperados

| ID | Requisito | Origem |
|----|-----------|--------|
| R13 | `POST /ingredientes` cadastra um ingrediente em um compartimento. | Endpoint 1 |
| R14 | O corpo do POST tem os campos `nome`, `tipo`, `quantidade`, `compartimentoId` e `responsavel` (exemplo: "Farinha de Trigo", "SECOS", 400, 1, "pedro"). | Endpoint 1 |
| R15 | O cadastro só é aceito se o tipo e o volume forem válidos para o compartimento. | Endpoint 1 |
| R16 | `GET /ingredientes` lista os ingredientes. | Endpoint 1 |
| R17 | `GET /ingredientes/{id}` busca um ingrediente pelo id. | Endpoint 1 |
| R18 | `GET /ingredientes/volume` retorna o total guardado por tipo (secos, líquidos, refrigerados). | Endpoint 2 |
| R19 | A resposta de volume é uma lista com os campos `type` e `totalQuantity` (exemplo: LIQUIDO 200.000, REFRIGERADO 150.000, SECO 400.000). | Endpoint 2 |
| R20 | `GET /compartimentos/disponiveis?quantidade=...&tipo=...` retorna os compartimentos com espaço livre suficiente para essa quantidade e tipo. | Endpoint 3 |
| R21 | `GET /compartimentos/disponiveis-para-venda?tipo=...` retorna só os compartimentos do tipo pedido com volume maior que 0. | Endpoint 4 |
| R22 | `GET /historico` lista as movimentações. | Endpoint 5 |
| R23 | Por padrão, o histórico vem ordenado por data e compartimento. | Endpoint 5 |
| R24 | A ordenação do histórico pode ser mudada com os parâmetros `sortBy=date` e `order=asc` ou `order=desc`. | Endpoint 5 |

### ⚙️ Regras de Negócio Adicionais

| ID | Requisito | Origem |
|----|-----------|--------|
| R25 | Um compartimento que guardou secos hoje só pode guardar outro tipo amanhã. | Regras Adicionais |
| R26 | Se o cadastro violar o tipo do compartimento, retornar 400 BAD REQUEST com mensagem clara. | Regras Adicionais |
| R27 | Se o cadastro passar da capacidade máxima, retornar 400 BAD REQUEST com mensagem clara. | Regras Adicionais |
| R28 | Nenhuma entrada ou saída pode acontecer sem gerar histórico. | Regras Adicionais |

### 💻 Tecnologias e Boas Práticas

| ID | Requisito | Origem |
|----|-----------|--------|
| R29 | Usar Java + Spring Boot. | Tecnologias |
| R30 | Banco de dados: H2, MySQL ou qualquer banco em memória. | Tecnologias |
| R31 | Usar anotações do Spring (`@RestController`, `@Service`, `@Repository`, `@Transactional` etc.). | Tecnologias |
| R32 | Retornar mensagens de erro claras e status HTTP adequados. | Tecnologias |

### 💻 Etapa Final

| ID | Requisito | Origem |
|----|-----------|--------|
| R33 | Criar um repositório no GitHub (público ou privado). | Etapa Final 1 |
| R34 | Enviar o código completo do projeto para esse repositório. | Etapa Final 2 |
| R35 | Adicionar um `README.md` explicando como rodar o projeto. | Etapa Final 3 |
| R36 | Compartilhar o repositório adicionando como colaborador o e-mail informado na entrevista (ou enviar o link de convite). | Etapa Final 4 |

### 🚀 Diferenciais (opcionais, mas contam pontos)

| ID | Requisito | Origem |
|----|-----------|--------|
| R37 | README com arquitetura, decisões técnicas e como executar. | Diferenciais |
| R38 | Testes unitários e/ou de integração. | Diferenciais |
| R39 | Tratamento de concorrência (entradas e saídas ao mesmo tempo). | Diferenciais |
| R40 | Frontend para ajudar no uso. | Diferenciais |
| R41 | Algoritmo eficiente para calcular o espaço disponível. | Diferenciais |

### 📊 Critérios de Avaliação

| ID | Critério | Origem |
|----|----------|--------|
| R42 | Organização e clareza do código. | Critério 1 |
| R43 | Estrutura de pacotes e responsabilidades. | Critério 2 |
| R44 | Implementação correta das regras de negócio. | Critério 3 |
| R45 | Uso adequado do Spring e boas práticas REST. | Critério 4 |
| R46 | Tratamento de erros e mensagens de resposta. | Critério 5 |
| R47 | Testes automatizados. | Critério 6 |
| R48 | Performance e concorrência. | Critério 7 |
| R49 | Documentação e instruções de execução. | Critério 8 |

---

## 2. Lacunas e ambiguidades

Pontos que o PDF não explica ou explica de forma confusa. Cada um tem uma decisão sugerida.

| # | Lacuna / ambiguidade | Decisão sugerida |
|---|----------------------|------------------|
| L1 | Não existe endpoint de **saída** (retirada/venda). O PDF fala em saída, histórico de SAÍDA e "disponíveis para venda", mas só lista `POST /ingredientes`. | Criar um endpoint de saída, por exemplo `POST /ingredientes/{id}/saida` com `quantidade` e `responsavel`. Explicar no README que ele foi adicionado. |
| L2 | O tipo aparece escrito de formas diferentes: `SECOS` no POST e `SECO`, `LIQUIDO`, `REFRIGERADO` na resposta de volume. | Usar `SECO`, `LIQUIDO`, `REFRIGERADO` como padrão. Na entrada, aceitar também o plural e sem diferenciar maiúscula/minúscula (ex.: `SECOS`, `secos`). |
| L3 | O POST usa `compartimentoId: 1`, mas os compartimentos se chamam C1 a C5. Não está claro se o id é número ou o nome. | `compartimentoId` é número de 1 a 5, e o id 1 corresponde a C1, o 2 a C2, e assim por diante. Opcionalmente aceitar também "C1"... "C5". |
| L4 | Não diz o que acontece se o mesmo ingrediente for cadastrado de novo no mesmo compartimento. | Somar a quantidade no ingrediente que já existe (mesmo nome, ignorando maiúsculas e espaços extras) e gerar um novo registro de ENTRADA no histórico. |
| L5 | "Só um tipo por vez" (R4) e "não misturar no mesmo período" (R9) podem ter sentidos diferentes. Também não diz se ingredientes diferentes do mesmo tipo podem ficar juntos. | Um compartimento guarda um tipo só. Ingredientes diferentes do mesmo tipo podem ficar no mesmo compartimento, e a capacidade vale para a soma deles. |
| L6 | A regra "secos hoje, outro tipo só amanhã" fala só de secos. Não diz se vale para líquidos e refrigerados. | Aplicar a regra para todos os tipos: um compartimento só pode trocar de tipo em um dia diferente do último uso, e só se estiver vazio. |
| L7 | Não diz o que é "hoje" e "amanhã" (fuso horário, dia do calendário ou 24 horas). | Usar o dia do calendário no fuso `America/Sao_Paulo`. "Amanhã" é a partir de 00:00 do dia seguinte. |
| L8 | Não diz se um compartimento com estoque pode trocar de tipo. | Não pode. Para trocar de tipo, o compartimento precisa estar vazio e a regra do dia (L6) precisa ser cumprida. |
| L9 | Não diz quando um compartimento fica "livre" (sem tipo). | Quando a quantidade chega a 0, o compartimento continua marcado com o último tipo até o fim do dia; a partir do dia seguinte aceita qualquer tipo. |
| L10 | Unidades misturadas: kg e L. A resposta de volume soma tudo como número. | Guardar só o número. A unidade é definida pelo tipo (SECO e REFRIGERADO em kg, LIQUIDO em L). Mostrar a unidade no README e, se quiser, em um campo extra da resposta. |
| L11 | O exemplo de volume tem 3 casas decimais (200.000). Não diz se aceita decimais na entrada. | Aceitar quantidade decimal com até 3 casas, sempre maior que 0. |
| L12 | A resposta de volume não diz se tipos com total 0 aparecem. | Sempre retornar os 3 tipos, com 0 quando não houver estoque. |
| L13 | Os nomes da resposta de volume estão em inglês (`type`, `totalQuantity`), e o resto em português. | Manter exatamente `type` e `totalQuantity`, como no exemplo do PDF. |
| L14 | `/compartimentos/disponiveis`: não diz se compartimentos vazios entram no resultado, nem o que fazer se faltar parâmetro. | Retornar compartimentos do mesmo tipo com espaço suficiente e também os vazios que podem receber o tipo hoje (L6). Os dois parâmetros são obrigatórios; se faltar ou for inválido, 400. |
| L15 | Não diz o formato da resposta dos endpoints de compartimentos. | Retornar uma lista com: id, código (C1...), tipo atual, quantidade atual, capacidade máxima e espaço livre. Lista vazia quando não houver nenhum. |
| L16 | `/compartimentos/disponiveis-para-venda`: não diz se `tipo` é obrigatório. | `tipo` é obrigatório; sem ele, 400. |
| L17 | Histórico: "ordenado por data e compartimento", mas o único `sortBy` citado é `date`. | Padrão: data mais recente primeiro (`desc`), depois compartimento C1 a C5. Aceitar `sortBy=date` e também `sortBy=compartimento`. Valor inválido retorna 400. |
| L18 | O histórico deve ter "tipo de ingrediente", mas não fala em nome do ingrediente nem compartimento, e a ordenação usa compartimento. | Guardar no histórico também o compartimento e o nome do ingrediente, além dos campos pedidos. |
| L19 | Não diz se o histórico tem paginação ou filtros. | Sem paginação obrigatória. Paginação e filtros (por tipo ou compartimento) ficam como melhoria opcional. |
| L20 | Não diz quais status usar para "não encontrado" e para sucesso. | 201 para cadastro novo, 200 para consultas e para entrada que soma em ingrediente existente, 404 para id que não existe, 400 para dados inválidos ou regra violada. |
| L21 | Não diz como é a mensagem de erro. | Usar sempre o mesmo formato de erro: status, mensagem clara em português, data e hora, e caminho da requisição. |
| L22 | Não diz o que fazer com saída maior que o estoque. | Recusar com 400 e mensagem clara. Não gravar nada. |
| L23 | Não diz se um ingrediente com quantidade 0 continua existindo. | O ingrediente continua no cadastro com quantidade 0, para manter o histórico ligado a ele. |
| L24 | "Responsável" é só um texto. Não há login ou usuários. | `responsavel` é texto obrigatório e não pode ser vazio. Sem login. |
| L25 | Não diz se os 5 compartimentos são criados pelo sistema ou pela API. | O sistema cria C1 a C5 automaticamente ao iniciar, se ainda não existirem. Não há endpoint para criar ou apagar compartimentos. |
| L26 | Não diz se ingredientes podem ser alterados ou apagados (PUT/DELETE). | Não criar PUT nem DELETE. Mudanças de quantidade só por entrada e saída, para não perder histórico. |
| L27 | Concorrência: não diz como tratar duas operações ao mesmo tempo no mesmo compartimento. | Usar transação com bloqueio do compartimento durante a entrada/saída, para não passar da capacidade nem ficar negativo. |
| L28 | Persistência: "qualquer banco em memória" pode fazer os dados sumirem ao reiniciar. | Usar H2 para testes e MySQL (ou H2 em arquivo) para rodar. Explicar a escolha no README. |
| L29 | O texto do PDF tem erros: "compartilhamento" no lugar de "compartimento", e a frase da Etapa Final 3 está cortada ("na sua"). | Entender "compartilhamento" como "compartimento" e a frase como "como rodar o projeto na sua máquina". |
| L30 | Frontend é diferencial, mas não diz qual tecnologia. | Opcional. Se fizer, uma página simples que use a API. |
