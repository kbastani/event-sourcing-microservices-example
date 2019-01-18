#!/bin/bash

# Requires jq, see https://stedolan.github.io/jq/
# Install with
# macOS: brew install jq
# Windows: chocolatey install jq.
# Linux: apt-get install jq

# Requires parallel
# Install with
# macOS: brew install parallel
# Windows: chocolatey install parallel.
# Linux: apt-get install parallel

set -e

echo
usage="$(basename "$0") [-h] [-s hostname] -- high-performance asynchronous program to generate a randomized social network between fake users

where:
    -h  show this help text
    [hostname]  set the edge-service hostname (default: localhost:9000)"

export EDGE_URI=${1:-localhost:9000}
USER_STATUS="DOWN"
FRIEND_STATUS="DOWN"
RECOMMENDATION_STATUS="DOWN"
IDS=()
export IDS

while getopts ':hs:' option; do
  case "$option" in
    h) echo "$usage"
       exit
       ;;
    s) seed=$OPTARG
       ;;
    :) printf "missing argument for -%s\n" "$OPTARG" >&2
       echo "$usage" >&2
       exit 1
       ;;
   \?) printf "illegal option: -%s\n" "$OPTARG" >&2
       echo "$usage" >&2
       exit 1
       ;;
  esac
done

echo "Using edge-service URI: $EDGE_URI"
echo
echo "[Use -h for usage examples]"
echo

# Wake up user service
echo "--> Trying to wake up the user-service... "
while [[ "$USER_STATUS" = "DOWN" ]]; do
    USER_STATUS=`curl -s "http://$EDGE_URI/user/actuator/health" | jq .status`;
    [[ $USER_STATUS = "" ]] && USER_STATUS="DOWN" || USER_STATUS="UP";
    [[ $USER_STATUS = "DOWN" ]] && INDICATOR="\033[1;31mDOWN\033[0m" || INDICATOR="\033[1;32mUP\033[0m"
    echo "$INDICATOR"
    [[ $USER_STATUS = "DOWN" ]] && sleep 4;
done


# Wake up friend service
echo "--> Trying to wake up the friend-service... "
while [[ "$FRIEND_STATUS" = "DOWN" ]]; do
    FRIEND_STATUS=`curl -s "http://$EDGE_URI/friend/actuator/health" | jq .status`;
    [[ $FRIEND_STATUS = "" ]] && FRIEND_STATUS="DOWN" || FRIEND_STATUS="UP";
    [[ $FRIEND_STATUS = "DOWN" ]] && INDICATOR="\033[1;31mDOWN\033[0m" || INDICATOR="\033[1;32mUP\033[0m"
    echo "$INDICATOR"
    [[ $FRIEND_STATUS = "DOWN" ]] && sleep 4;
done

# Wake up recommendation service
echo "--> Trying to wake up the recommendation-service... "
while [[ "$RECOMMENDATION_STATUS" = "DOWN" ]]; do
    RECOMMENDATION_STATUS=`curl -s "http://$EDGE_URI/recommendation/actuator/health" | jq .status`;
    [[ $RECOMMENDATION_STATUS = "" ]] && RECOMMENDATION_STATUS="DOWN" || RECOMMENDATION_STATUS="UP";
    [[ $RECOMMENDATION_STATUS = "DOWN" ]] && INDICATOR="\033[1;31mDOWN\033[0m" || INDICATOR="\033[1;32mUP\033[0m"
    echo "$INDICATOR"
    [[ $RECOMMENDATION_STATUS = "DOWN" ]] && sleep 4;
done

echo "====> Create users"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

read_name() {
  NAME=( $(IFS=" " echo "$1") )
  ADD_USER=`curl -s -X "POST" "http://$EDGE_URI/user/v1/users" \
      -H 'Content-Type: application/json; charset=utf-8' \
      -d $"{\"firstName\": \"${NAME[0]}\",\"lastName\": \"${NAME[1]}\"}"`
  echo "$ADD_USER"
}

export -f read_name

# Generate random text file name
export FILE_NAME="$(cat /dev/urandom | env LC_CTYPE=C tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1).txt"

parallel \
    'read_name {1} | jq -c . >> $FILE_NAME' :::+<$DIR/names-100.txt

while read id; do
  echo ${id} | jq .;
  IDS+=($(echo ${id} | jq -r .id));
done <./$FILE_NAME

rm ./$FILE_NAME

echo "====> Create 2000 friendships..."

add_friend() {
  RANGE=600
  friend1=$RANDOM
  friend2=$RANDOM
  let "friend1 %= $RANGE"
  let "friend2 %= $RANGE"
  let "friend1 += $1"
  let "friend2 += $1"
  echo "$friend1 ❤ $friend2";
  FRIEND_RESULT=`curl -s -X "POST" \
   "http://$EDGE_URI/friend/v1/users/$friend1/commands/addFriend?friendId=$friend2" \
    -H 'Content-Type: application/json; charset=utf-8' -d ""`
  echo ${FRIEND_RESULT} | jq .
}

add_relationship() {
  RANGE=$2
  friend1=$1
  friend2=$1
  let "friend2 += 1"
  let "friend1 += $RANGE"
  let "friend2 += $RANGE"

  echo "$friend1 ❤ $friend2";
  FRIEND_RESULT=`curl -s -X "POST" \
   "http://$EDGE_URI/friend/v1/users/$friend1/commands/addFriend?friendId=$friend2" \
    -H 'Content-Type: application/json; charset=utf-8' -d ""`
  echo ${FRIEND_RESULT} | jq .
}

export -f add_friend
export -f add_relationship
export LOWER_BOUND="${IDS[0]}"

echo -n "added ids: "
for i in "${IDS[@]}"
do
    echo -n $i
done
echo

seq 99 | parallel \
    'add_relationship {1} $LOWER_BOUND'

seq 2000 | parallel \
    'add_friend $LOWER_BOUND'

friend1=${IDS[$RANDOM % ${#IDS[@]}]}
friend2=${IDS[$RANDOM % ${#IDS[@]}]}
echo "--> Check that $friend1 has friends"
curl -s "http://$EDGE_URI/friend/v1/users/$friend1/friends" | jq

friend1=${IDS[$RANDOM % ${#IDS[@]}]}
friend2=${IDS[$RANDOM % ${#IDS[@]}]}
echo "--> List Mutual friends between $friend1 and $friend2"
curl -s "http://$EDGE_URI/recommendation/v1/users/$friend1/commands/findMutualFriends?friendId=$friend2" | jq

rec=${IDS[$RANDOM % ${#IDS[@]}]}
echo "--> Recommend friends to $rec"
curl -s "http://$EDGE_URI/recommendation/v1/users/$rec/commands/recommendFriends" | jq
