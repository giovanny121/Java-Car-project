# Base44 Dev Environment

## Project Overview
This is a **Java console application** — a simple vehicle rental system. There is no web server, database, or external dependencies. The app compiles `.java` files in `src/` and runs `Main`, which prints rental activity to stdout.

## How It Runs Here
Since the preview expects a web server on port 3000, the setup compiles and runs the Java program, captures stdout, and serves it as an HTML page:
- `Dockerfile.base44` — based on `eclipse-temurin:22-jdk`, adds `python3` and `inotify-tools`.
- `base44-serve.sh` — compiles `src/*.java`, runs `Main`, captures output to `/tmp/output.txt`, then serves it via a Python HTTP server on port 3000. A background `inotifywait` watcher recompiles and re-runs on source changes.
- `docker-compose.base44.yml` — builds the image, bind-mounts the repo at `/app`, runs the serve script.

## Verification
- `docker compose -f docker-compose.base44.yml up -d --build` starts the service.
- Curl `http://localhost:3000` returns an HTML page with the program output.
- Edit any `.java` file in `src/` and the watcher recompiles; refresh the preview to see new output.

## No Secrets Required
This project has no external service dependencies. No credentials are needed.
