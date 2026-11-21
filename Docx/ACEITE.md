# Critérios de aceite

O trabalho só termina quando todos os itens estiverem marcados. "Como verifico" diz onde a evidência aparece.

| ID | Critério | Como verifico | OK |
|---|---|---|---|
| A01 | Existem os compartimentos C1..C5, cada um com `id` (UUID) e `codigo` | Swagger: `GET /compartimentos` | ☐ |
| A02 | Capacidades: SECO 600 kg, LIQUIDO 500 L, REFRIGERADO 400 kg | Teste | ☐ |
| A03 | `POST /ingredientes` válido retorna 201, devolve `SECO` quando a requisição usa `SECOS` e grava uma ENTRADA | Swagger + Teste | ☐ |
| A04 | Capacidade excedida retorna 400 com o espaço livre na mensagem | Swagger + Teste | ☐ |
| A05 | No mesmo dia, um tipo diferente no compartimento retorna 400 | Swagger + Teste | ☐ |
| A06 | Saída reduz o estoque e grava SAIDA; saída maior que o estoque retorna 400 | Swagger + Teste | ☐ |
| A07 | Capacidade excedida: o ingrediente não é criado, nenhuma movimentação é gravada e `quantidade_atual` não muda | Teste | ☐ |
| A08 | `GET /ingredientes/volume` retorna sempre os 3 tipos com `type` e `totalQuantity`; tipo sem estoque aparece com 0 | Swagger + Teste | ☐ |
| A09 | `GET /compartimentos/disponiveis` lista só os compartimentos com espaço para a quantidade e que aceitam o tipo hoje; bloqueados hoje ficam fora | Swagger + Teste | ☐ |
| A10 | `GET /compartimentos/disponiveis-para-venda` lista só os de volume maior que 0 do tipo | Swagger + Teste | ☐ |
| A11 | `GET /historico` ordena por data ou compartimento com `sortBy` e `order`; padrão `date desc`; desempate pelo outro campo; valor inválido → 400 | Swagger + Teste | ☐ |
| A12 | Regra do amanhã (dia do calendário): guardar SECO às 23:59 e LIQUIDO no mesmo dia → 400; LIQUIDO às 00:01 do dia seguinte, com o compartimento vazio → 201 | Teste (relógio simulado) | ☐ |
| A13 | Todos os erros (400, 404 e 409) usam o mesmo JSON: `timestamp, status, error, message, path` | Swagger + Teste | ☐ |
| A14 | Concorrência: 10 entradas simultâneas de 100 kg de SECO em C1 → 6 sucessos (201 ou 200), 4 respostas 400, total 600 kg e 6 ENTRADAS | Teste | ☐ |
| A15 | A aplicação usa `armazem_app` (não root); esse usuário não consegue `CREATE TABLE` | Comando no MySQL | ☐ |
| A16 | `docker compose up` sobe tudo e o Swagger abre em `localhost:8080/swagger-ui.html` | Manual | ☐ |
| A17 | Aceita `SECO` e `SECOS` | Teste | ☐ |
| A18 | Espaço livre calculado sem somar o histórico (O(1)) | Revisão do código | ☐ |
| A19 | Regras de negócio só no Service; controllers finos; código segue as "Regras de código" do PROJETO.md | Revisão do código | ☐ |
| A20 | Frontend em `localhost:3000` faz entrada e saída, mostra volume e histórico, e exibe a mensagem de erro da API | Manual | ☐ |
| A21 | README explica arquitetura, decisões técnicas e como executar o projeto | Revisão do README | ☐ |
| A22 | Cada movimentação grava data e hora, operação, quantidade, tipo e responsável | Teste | ☐ |
| A23 | `POST /ingredientes` com campo faltando, quantidade ≤ 0, mais de 3 casas decimais ou texto com mais de 100 caracteres → 400 | Teste | ☐ |
| A24 | `GET /ingredientes` lista os ingredientes; `GET /ingredientes/{uuid}` retorna um; UUID inexistente → 404; UUID mal formatado → 400 | Swagger + Teste | ☐ |
| A25 | Dois POSTs com o mesmo `nome` no mesmo compartimento: o primeiro retorna 201, o segundo retorna 200; fica 1 ingrediente com a quantidade somada e o histórico tem duas ENTRADAS | Teste | ☐ |
| A26 | Compartimento na resposta tem `id` (UUID), `codigo, tipoAtual, capacidade, quantidadeAtual, espacoLivre` | Swagger + Teste | ☐ |
| A27 | `mvn test` roda com 0 falhas | Saída do Maven | ☐ |
| A28 | Código no GitHub, com o avaliador como colaborador | Manual | ☐ |
