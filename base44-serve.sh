#!/bin/sh
set -e

OUTDIR=/app/out
mkdir -p "$OUTDIR"

generate() {
  echo "=== Compiling Java sources ==="
  rm -f "$OUTDIR"/*.class 2>/dev/null || true
  javac -d "$OUTDIR" /app/src/*.java
  echo "=== Running Main ==="
  java -cp "$OUTDIR" Main > /tmp/output.txt 2>&1 || true
  echo "=== Output regenerated ==="
}

generate

# Watch for source changes and recompile/re-run in the background
(
  while inotifywait -q -e modify,create,delete,move -r /app/src 2>/dev/null; do
    generate
  done
) &

# Serve the program output as a simple HTML page on port 3000
exec python3 -c '
import http.server, html, os, time

class Handler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        try:
            with open("/tmp/output.txt") as f:
                output = html.escape(f.read())
        except FileNotFoundError:
            output = "Waiting for program output..."
        body = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Revision - Vehicle Rental System</title>
<style>
  body { background: #1e1e2e; color: #cdd6f4; font-family: "SF Mono", "Cascadia Code", "Courier New", monospace; padding: 2rem; max-width: 800px; margin: 0 auto; line-height: 1.6; }
  h1 { color: #89b4fa; border-bottom: 1px solid #45475a; padding-bottom: .5rem; }
  pre { white-space: pre-wrap; word-wrap: break-word; background: #181825; padding: 1.5rem; border-radius: 8px; border: 1px solid #45475a; }
  .footer { color: #6c7086; font-size: .8rem; margin-top: 2rem; }
</style>
</head>
<body>
  <h1>Revision &mdash; Vehicle Rental System</h1>
  <pre>""" + output + """</pre>
  <p class="footer">Java console application output &mdash; refresh to see recompiled results.</p>
</body>
</html>"""
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.end_headers()
        self.wfile.write(body.encode())

    def log_message(self, *args):
        pass

http.server.HTTPServer(("0.0.0.0", 3000), Handler).serve_forever()
'
