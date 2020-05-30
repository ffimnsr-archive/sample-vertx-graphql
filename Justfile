set shell := ["pwsh", "-ExecutionPolicy", "Bypass", "-c"]

default:
    cd {{invocation_directory()}}; .\mvnw.cmd compile exec:java

migrate:
    cd {{invocation_directory()}}; .\mvnw.cmd migrate