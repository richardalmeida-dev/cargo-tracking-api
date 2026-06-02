A CI falhou em ~12s no run 26842720087 — quase certamente mvnw sem bit de executável (Windows), então ./mvnw dá "Permission denied" no runner Linux.

1. CONFIRME a causa: gh run view 26842720087 --log-failed
   Deve mostrar permission denied / cannot execute em ./mvnw. Se for outro erro, me reporte antes de seguir.

2. Conserte o bit do wrapper:
   git update-index --chmod=+x mvnw
   git commit -m "tornar o Maven wrapper executavel"

3. Badges do README: as URLs de CI e CodeQL têm o placeholder <owner>/<repo> (codificado %3Cowner%3E/%3Crepo%3E). Troque os dois por richardalmeida-dev/cargo-tracking-api.

4. ci.yml — tire o dependency-check de todo push (sem NVD_API_KEY ele trava):
   - em on:, adicione schedule (cron '0 6 * * 1') e workflow_dispatch.
   - no job dependency-check, adicione: if: github.event_name == 'schedule' || github.event_name == 'workflow_dispatch'
   Push/PR passa a rodar só o build-test; o scan pesado roda semanal ou sob demanda.

5. Commit e push de tudo:
   git add -A && git commit -m "corrigir badges do README e mover dependency-check para schedule" && git push

6. Metadata do repo (gh já está como richardalmeida-dev):
   gh repo edit richardalmeida-dev/cargo-tracking-api --description "REST API de rastreio de cargas com maquina de estados — Java 21, Spring Boot 3, PostgreSQL, Testcontainers" --add-topic java --add-topic spring-boot --add-topic postgresql --add-topic rest-api --add-topic state-machine --add-topic testcontainers

7. Confirme: gh run list --workflow=ci.yml — o build-test tem que passar rodando os 63 testes (duração bem maior que 12s). Me mostre o status e confirme que os badges resolvem agora.