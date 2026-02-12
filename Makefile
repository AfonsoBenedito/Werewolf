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
	@echo "Running web unit tests... (placeholder)"

run-all-tests: unit-tests-kotlin unit-tests-web