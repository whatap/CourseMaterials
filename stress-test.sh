#!/bin/bash
# Stress Test Script
# Usage: ./stress-test.sh [concurrency] [delay]

CONCURRENCY=${1:-1}
DELAY=${2:-0.5}
HOST="localhost"
PORT="8080" # Gateway port

echo "Starting stress test with Concurrency: $CONCURRENCY, Delay: $DELAY sec..."
echo "Press [CTRL+C] to stop."

run_worker() {
    local id=$1
    while true; do
        PRODUCT_ID=$((RANDOM % 1000 + 1))
        # echo "[$id] Requesting Product $PRODUCT_ID..."
        
        # Call Product Composite API
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://$HOST:$PORT/product-composite/$PRODUCT_ID)
        
        if [ "$HTTP_CODE" == "200" ]; then
            echo -ne "[$id] OK \r"
        else
            echo "[$id] ERR: $HTTP_CODE"
        fi
        
        sleep $DELAY
    done
}

pids=""

for i in $(seq 1 $CONCURRENCY); do
    run_worker $i &
    pids="$pids $!"
done

cleanup() {
    echo -e "\nStopping workers..."
    for pid in $pids; do
        kill $pid 2>/dev/null
    done
    exit 0
}

trap cleanup SIGINT

wait
