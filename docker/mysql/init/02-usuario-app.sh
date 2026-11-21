#!/bin/bash
# Cria o usuario da aplicacao com permissao apenas de DML (sem CREATE/ALTER/DROP).
# Em .sh (e nao .sql) para ler a senha do ambiente, sem gravar a senha no Git.
set -euo pipefail

mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" <<-SQL
    CREATE USER IF NOT EXISTS '${MYSQL_APP_USER}'@'%' IDENTIFIED BY '${MYSQL_APP_PASSWORD}';
    GRANT SELECT, INSERT, UPDATE, DELETE ON \`${MYSQL_DATABASE}\`.* TO '${MYSQL_APP_USER}'@'%';
    FLUSH PRIVILEGES;
SQL
