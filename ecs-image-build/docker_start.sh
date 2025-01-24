#!/bin/bash

# Start script for psc-delta-consumer

PORT=8080
exec java -jar -Dserver.port="${PORT}" "psc-delta-consumer.jar"
