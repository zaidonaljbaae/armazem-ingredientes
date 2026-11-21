# PROJECT_SUMMARY

## Current state
- Backend completo conforme `Docx/PROJETO.md`; frontend em http://localhost:3000; README pronto.
- `docker compose up -d --build` sobe `armazem-mysql`, `armazem-app` (8080) e `armazem-frontend` (3000).
- Testes (perfil `test`, H2 modo MySQL, `RelogioMutavel`): 19 testes, 19 PASS.
- ACEITE.md alinhado ao PROJETO.md: A25 (POST repetido soma, 201 depois 200) e A14 (6 sucessos 201/200).
- O banco tem dados de teste do smoke test (C1, C2, C3).

## Last 5 changes
1. README.md (rodar, testes, endpoints, arquitetura, decisões, lacunas do PDF).
2. Testes automatizados por critério do ACEITE.md (a02..a26, a14 concorrência).
3. ACEITE.md corrigido (A25, A14) e testes ajustados.
4. Frontend: `frontend/index.html` (entrada, saída, compartimentos, volume, histórico, erro da API).
5. Serviço `frontend` (nginx:1.27-alpine, 3000:80, proxy `/api/` → `app:8080`).

## Next steps
- Verificação manual do A20 no navegador.
- Opcional: `ddl-auto=validate` em teste contra MySQL real (schema SQL não é testado no H2).
- Commit e entrega no GitHub (seção 10).

## Decisions & gotchas
- Volume antigo `armazem-ingredientes_mysql-data` (sessão anterior, outras senhas) NÃO foi apagado;
  o compose usa `armazem-mysql-data`. Pode ser removido manualmente se não for mais útil.
- Init do MySQL só roda com volume vazio; para recriar o schema: `docker compose down -v`.
- Maven não está instalado no host: compilar via Docker (`maven:3.9-eclipse-temurin-17`, volume `armazem-m2`).
- READ COMMITTED é necessário: na saída a primeira leitura é sem lock; em REPEATABLE READ o saldo viria velho.
- Git Bash no Windows manda acentos fora de UTF-8 no curl; usar `--data-binary @arquivo` para testar `líquido`.
