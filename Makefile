run-console:
	./gradlew -q runConsole --console=plain

run-web:
	docker-compose -f docker/docker-compose.yml down
	docker-compose -f docker/docker-compose.yml up --build

stop-web:
	docker-compose -f docker/docker-compose.yml down

run-frontend:
	cd web && npm run dev

run-backend:
	docker-compose -f docker/docker-compose.yml down
	docker-compose -f docker/docker-compose.yml up --build redis backend

stop-backend:
	docker-compose -f docker/docker-compose.yml stop backend redis

unit-tests-kotlin:
	./gradlew test

unit-tests-web:
	cd web && npm test

code-check-web:
	cd web && npx tsc --noEmit && npm run lint

code-fix-web:
	cd web && npx eslint . --fix

code-check-kotlin:
	./gradlew detekt

code-fix-kotlin:
	./gradlew detekt -PdetektAutoCorrect

run-all-tests: unit-tests-kotlin unit-tests-web code-check-kotlin code-check-web