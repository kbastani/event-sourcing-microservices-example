#!/usr/bin/env bash

export MY_PATH="$(pwd)"

seq 10 | parallel -j 10 sh generate-social-network.sh