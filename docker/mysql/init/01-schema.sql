-- Executado uma unica vez pelo root, quando o volume do MySQL esta vazio.
-- O banco (MYSQL_DATABASE) ja foi criado pela imagem e esta selecionado.

CREATE TABLE compartimento (
    id                        BIGINT        NOT NULL AUTO_INCREMENT,
    uuid                      CHAR(36)      NOT NULL,
    ativo                     BOOLEAN       NOT NULL DEFAULT TRUE,
    data_criacao              DATETIME(6)   NOT NULL,
    data_atualizacao          DATETIME(6)   NOT NULL,
    codigo                    VARCHAR(2)    NOT NULL,
    tipo_atual                VARCHAR(20)   NULL,
    quantidade_atual          DECIMAL(10,3) NOT NULL DEFAULT 0,
    data_ultimo_armazenamento DATETIME(6)   NULL,
    version                   BIGINT        NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_compartimento_uuid   UNIQUE (uuid),
    CONSTRAINT uk_compartimento_codigo UNIQUE (codigo),
    CONSTRAINT ck_compartimento_quantidade CHECK (quantidade_atual >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE ingrediente (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    uuid             CHAR(36)      NOT NULL,
    ativo            BOOLEAN       NOT NULL DEFAULT TRUE,
    data_criacao     DATETIME(6)   NOT NULL,
    data_atualizacao DATETIME(6)   NOT NULL,
    nome             VARCHAR(100)  NOT NULL,
    tipo             VARCHAR(20)   NOT NULL,
    quantidade       DECIMAL(10,3) NOT NULL,
    compartimento_id BIGINT        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_ingrediente_uuid UNIQUE (uuid),
    -- A collation _ci torna a comparacao do nome insensivel a maiusculas.
    CONSTRAINT uk_ingrediente_nome_compartimento UNIQUE (nome, compartimento_id),
    CONSTRAINT fk_ingrediente_compartimento FOREIGN KEY (compartimento_id) REFERENCES compartimento (id),
    CONSTRAINT ck_ingrediente_quantidade CHECK (quantidade >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE movimentacao (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    uuid             CHAR(36)      NOT NULL,
    ativo            BOOLEAN       NOT NULL DEFAULT TRUE,
    data_criacao     DATETIME(6)   NOT NULL,
    data_atualizacao DATETIME(6)   NOT NULL,
    data_hora        DATETIME(6)   NOT NULL,
    operacao         VARCHAR(10)   NOT NULL,
    quantidade       DECIMAL(10,3) NOT NULL,
    tipo             VARCHAR(20)   NOT NULL,
    responsavel      VARCHAR(100)  NOT NULL,
    compartimento_id BIGINT        NOT NULL,
    ingrediente_id   BIGINT        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_movimentacao_uuid UNIQUE (uuid),
    CONSTRAINT fk_movimentacao_compartimento FOREIGN KEY (compartimento_id) REFERENCES compartimento (id),
    CONSTRAINT fk_movimentacao_ingrediente FOREIGN KEY (ingrediente_id) REFERENCES ingrediente (id),
    CONSTRAINT ck_movimentacao_quantidade CHECK (quantidade > 0),
    INDEX idx_movimentacao_data_hora (data_hora, compartimento_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
