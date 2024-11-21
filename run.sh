#!/bin/bash
# Convert input to lowercase
input=$(echo "$2" | tr '[:upper:]' '[:lower:]')
port=${3:-12345}  # Default port to 12345 if not provided

if [ "$input" = "server" ]; then
    java $1.Server $port
elif [ "$input" = "client" ]; then
    java $1.Client $1 $port
    # In Milestone3 changes Client to ClientUI
elif [ "$input" = "ui" ]; then
    java $1.ClientUI $1 $port
    # Milestone 3's new entry point
else
    echo "Must specify client or server"
fi