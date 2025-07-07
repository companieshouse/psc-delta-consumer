#!/bin/bash

# Start script for psc-delta-consumer

PORT=8080
exec java -jar -Dserver.port="${PORT}" -XX:MaxRAMPercentage=80 "psc-delta-consumer.jar"
