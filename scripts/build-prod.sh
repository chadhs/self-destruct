#!/bin/sh

echo "Building self-destruct for production..."

# Ensure Java 21 is on PATH. FreeBSD's /usr/local/bin/java can be the javavm
# wrapper, so put OpenJDK 21 first.
export JAVA_HOME="${JAVA_HOME:-/usr/local/openjdk21}"
export PATH="${JAVA_HOME}/bin:/usr/local/bin:$PATH"

if [ ! -x "${JAVA_HOME}/bin/java" ]; then
    echo "Error: Java 21 not found at ${JAVA_HOME}/bin/java"
    echo "Install it with: sudo pkg install openjdk21"
    exit 1
fi

if ! "${JAVA_HOME}/bin/java" -version 2>&1 | grep -q 'version "21'; then
    echo "Error: ${JAVA_HOME}/bin/java is not Java 21"
    "${JAVA_HOME}/bin/java" -version
    exit 1
fi

if [ ! -f "project.clj" ]; then
    echo "Error: Please run this script from the self-destruct project root directory"
    exit 1
fi

if [ ! -f ".env" ]; then
    echo "Error: .env file not found. Please run scripts/create-env.sh first"
    exit 1
fi

if ! command -v lein >/dev/null 2>&1; then
    echo "Error: lein not found on PATH"
    echo "Install Leiningen (e.g. sudo pkg install leiningen) or place lein in PATH"
    exit 1
fi

echo "Loading environment variables..."
while IFS= read -r line || [ -n "$line" ]; do
    case "$line" in
        ''|'#'*) continue ;;
        *'='*)
            key="${line%%=*}"
            value="${line#*=}"
            if echo "$key" | grep -q '^[A-Za-z_][A-Za-z0-9_]*$'; then
                export "$key=$value"
                echo "  Loaded: $key"
            else
                echo "  Skipped invalid key: $key"
            fi
            ;;
        *)
            echo "  Skipped invalid line: $line"
            ;;
    esac
done < .env

echo "Fetching Clojure dependencies..."
lein deps
if [ $? -ne 0 ]; then
    echo "Error: Failed to fetch dependencies"
    exit 1
fi

echo "Building uberjar..."
lein uberjar
if [ $? -ne 0 ]; then
    echo "Error: Failed to build uberjar"
    exit 1
fi

if [ ! -f "target/self-destruct.jar" ]; then
    echo "Error: target/self-destruct.jar was not created"
    exit 1
fi

echo "Running database migrations..."
"${JAVA_HOME}/bin/java" -jar target/self-destruct.jar --migrate
if [ $? -ne 0 ]; then
    echo "Error: Failed to run database migrations"
    exit 1
fi

echo "Production build completed successfully!"
echo ""
echo "Build summary:"
echo "  - Dependencies fetched"
echo "  - Uberjar built: target/self-destruct.jar"
echo "  - Database migrations applied"
echo ""
echo "Next steps:"
echo "  1. Restart the service: sudo service selfdestruct restart"
echo "  2. Check status: sudo service selfdestruct status"
echo "  3. Monitor logs: sudo tail -f /var/log/selfdestruct.log"
