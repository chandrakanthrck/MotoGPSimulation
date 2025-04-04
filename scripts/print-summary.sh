#!/bin/bash

echo "🏁 MotoGP Race Simulation Summary"
echo "==================================="
echo "🕒 Date: $(date)"
echo ""

# Java Version Info
echo "☕ Java Version:"
java -version 2>&1 | head -n 1
echo ""

# Check for race results
echo "📁 Race Results:"
ls -lh race_results_*.csv 2>/dev/null || echo "No race result CSVs found."
echo ""

# Optional: Show last 5 log lines
echo "📄 Last 5 lines from latest CSV (if exists):"
latest=$(ls -t race_results_*.csv 2>/dev/null | head -n 1)
if [[ -f "$latest" ]]; then
  tail -n 5 "$latest"
else
  echo "No CSV file to preview."
fi
