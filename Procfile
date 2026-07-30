# main app
web: java $JVM_OPTS -jar target/self-destruct.jar --port $PORT

# run db migrations before release
release: java $JVM_OPTS -jar target/self-destruct.jar --migrate
