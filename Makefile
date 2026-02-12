run-console:
	./gradlew -q runConsole --console=plain

run-web:
	docker-compose down
	docker-compose up --build

stop-web:
	docker-compose down

unit-tests-kotlin:
	./gradlew test

unit-tests-web:
	npm --prefix web test

code-check-web:
	npm --prefix web run lint

code-fix-web:
	npm --prefix web run lint -- --fix

code-check-kotlin:
	@echo "Running code check... (placeholder)"

code-fix-kotlin:
	@echo "Running code fix... (placeholder)"

run-all-tests: unit-tests-kotlin unit-tests-web code-check-kotlin code-check-web