# Desafio Técnico – Armazém de Ingredientes

## 📦 Descrição Geral

Crie uma API RESTful responsável por gerenciar o estoque e o armazenamento de ingredientes utilizados em uma fábrica de alimentos. O sistema deve permitir o cadastro, movimentação e consulta de ingredientes, garantindo que as regras de armazenamento e histórico sejam respeitadas.

O armazém possui 5 compartimentos, cada um com capacidade máxima definida por tipo de ingrediente (ex.: secos, líquidos, refrigerados). Cada compartimento só pode armazenar um tipo de ingrediente por vez. Cada compartilhamento deve se chamar C1,C2,C3,C4 e C5.

## 🧩 Regras e Requisitos

- Cada compartimento tem uma capacidade máxima de:
  - 600 kg para ingredientes secos
  - 500 L para ingredientes líquidos
  - 400 kg para ingredientes refrigerados
- Um compartimento não pode misturar tipos diferentes de ingredientes no mesmo período.
- Ingredientes só podem ser armazenados se houver espaço suficiente no compartimento.
- Toda entrada e saída de ingredientes deve ser registrada no histórico, com:
  - Data e hora
  - Tipo de operação (ENTRADA ou SAÍDA)
  - Quantidade
  - Tipo de ingrediente
  - Responsável pela movimentação

## 🔗 Endpoints Esperados

### 1. Cadastro e consulta de ingredientes

- POST /ingredientes
- GET /ingredientes ou /ingredientes/{id}

```json
{
  "nome": "Farinha de Trigo",
  "tipo": "SECOS",
  "quantidade": 400,
  "compartimentoId": 1,
  "responsavel": "pedro"
}
```

- Permitir o cadastro de ingrediente em um compartimento com tipo e volume válidos.

### 2. Consulta de volume total por tipo de ingrediente

- GET /ingredientes/volume
- Retornar o total armazenado por tipo (secos, líquidos, refrigerados).

```json
[{
        "type": "LIQUIDO",
        "totalQuantity": 200.000
    },
    {
        "type": "REFRIGERADO",
        "totalQuantity": 150.000
    },
    {
        "type": "SECO",
        "totalQuantity": 400.000
    }
]
```

### 3. Consulta de compartimentos disponíveis para armazenamento

- GET /compartimentos/disponiveis?quantidade=...&tipo=...
- Calcular quais compartimentos têm espaço livre suficiente.

### 4. Consulta de compartimentos disponíveis para retirada/venda

- GET /compartimentos/disponiveis-para-venda?tipo=...
- Retornar apenas compartimentos com volume > 0 do tipo solicitado.

### 5. Histórico de movimentações

- GET /historico
- Listar movimentações ordenadas por data e compartimento, permitindo alterar a ordenação via parâmetros (sortBy=date, order=asc|desc).

## ⚙️ Regras de Negócio Adicionais

- Um compartimento que armazenou ingredientes secos hoje só pode armazenar outro tipo amanhã.
- Se a tentativa de cadastro violar o tipo do compartimento ou a capacidade máxima, retornar erro coerente (400 BAD REQUEST com mensagem clara).
- Não é possível realizar entrada/saída sem gerar histórico.

## 💻 Tecnologias e Boas Práticas

- Desenvolver em Java + Spring Boot.
- Persistência pode ser feita com H2, MySQL ou qualquer banco em memória.
- Uso de anotações do Spring (@RestController, @Service, @Repository, @Transactional, etc.).
- Retornar mensagens de erro claras e status HTTP adequados.

## 💻 Etapa Final

1. Crie um repositório no GitHub (pode ser público ou privado).
2. Envie o código do projeto completo para esse repositório.
3. Adicione um arquivo `README.md`** explicando como rodar o projeto na sua
4. Compartilhe o repositório adicionando como colaborador o e-mail informado na entrevista (ou envie o link de convite).

## 🚀 Diferenciais

- README explicando arquitetura, decisões técnicas e como executar o projeto.
- Testes unitários e/ou de integração.
- Tratamento de concorrência (transações simultâneas de entrada/saída).
- Frontend para ajudar na usabilidade
- Algoritmo eficiente para cálculo de espaço disponível.

## 📊 Critérios de Avaliação

1. Organização e clareza do código
2. Estrutura de pacotes e responsabilidades
3. Implementação correta das regras de negócio
4. Uso adequado do Spring e boas práticas REST
5. Tratamento de erros e mensagens de resposta
6. Testes automatizados
7. Performance e concorrência
8. Documentação e instruções de execução
