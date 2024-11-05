#!/usr/bin/env bash

# Function to display usage
usage() {
  echo "Usage: $0 [test-spec | all]"
  echo "Example: $0 UserService     # Runs tests matching 'UserService' in their name"
  echo "         $0 all             # Runs all tests"
  echo "         $0                 # Runs all tests by default"
  echo "Options:"
  echo "  -h, --help                Show this help message"
}

# Check if the user requested help
if [[ "$1" == "-h" || "$1" == "--help" ]]; then
  usage
  exit 0
fi

# Set default test spec to "all" if not provided
TESTSPEC=${1:-all}

# Run the tests based on the specified test spec
if [ "$TESTSPEC" = "all" ]; then
  echo "Running all it tests"
  sbt clearCaches clean it/test
else
  echo "Running it tests matching '$TESTSPEC'"
  sbt "clearCaches clean it/testOnly *$TESTSPEC*"
fi
