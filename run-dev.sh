#!/bin/bash
# Load .env and start the app
set -e
export $(cat .env | grep -v '^#' | xargs)
mvn spring-boot:run
