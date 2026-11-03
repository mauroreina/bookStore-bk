.PHONY: up down restart logs ps mysql-up

up: mysql-up
	docker compose up -d

down:
	docker compose down
	@docker stop mysql >/dev/null 2>&1 || test "$$(docker inspect --format='{{.State.Running}}' mysql)" = "false"

mysql-up:
	@docker start mysql >/dev/null 2>&1 || test "$$(docker inspect --format='{{.State.Running}}' mysql)" = "true"

restart:
	docker compose restart

logs:
	docker compose logs -f

ps:
	docker compose ps
