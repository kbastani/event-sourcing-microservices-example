#!/usr/bin/env bash

# Requires jq, see https://stedolan.github.io/jq/
# Install with
# macOS: brew install jq
# Windows: chocolatey install jq.
# Linux: apt-get install jq

# Wake up user service
echo -n "--> Wake up user service "
curl -s "http://localhost:9000/user/actuator/health" | jq '.status'

# Wake up friend service
echo -n "--> Wake up friend service "
curl -s "http://localhost:9000/friend/actuator/health" | jq '.status'

# Wake up recommendation service
echo -n "--> Wake up recommendation service "
curl -s "http://localhost:9000/recommendation/actuator/health" | jq '.status'

echo "====> Create users"

IDS=()

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
while read fullname; do
  NAME=( $(IFS=" " echo "${fullname}") )
  ADD_USER=`\
    curl -s -X "POST" "http://localhost:9000/user/v1/users" \
      -H 'Content-Type: application/json; charset=utf-8' \
      -d $"{\"firstName\": \"${NAME[0]}\",\"lastName\": \"${NAME[1]}\"}"`
  echo $ADD_USER | jq
  USER_ID=`echo $ADD_USER | jq .id`
  [[ "$USER_ID" != "null" ]] && IDS+=("${USER_ID}")
  #sleep 1
done <$DIR/names.txt

echo -n "added ids: "
for i in "${IDS[@]}"
do
    echo -n $i
done
echo

echo "====> Create 10 friendships"
for i in {1..10};
do
  friend1=${IDS[$RANDOM % ${#IDS[@]}]}
  friend2=${IDS[$RANDOM % ${#IDS[@]}]}
  echo "$friend1 <3 $friend2"
  curl -s -X "POST" "http://localhost:9000/friend/v1/users/$friend1/commands/addFriend?friendId=$friend2" | jq  
  sleep 1
done

friend1=${IDS[$RANDOM % ${#IDS[@]}]}
friend2=${IDS[$RANDOM % ${#IDS[@]}]}
echo "--> Check that $friend1 has friends"
curl -s "http://localhost:9000/friend/v1/users/$friend1/friends" | jq
sleep 1

friend1=${IDS[$RANDOM % ${#IDS[@]}]}
friend2=${IDS[$RANDOM % ${#IDS[@]}]}
echo "--> List Mutual friends between $friend1 and $friend2"
curl -s "http://localhost:9000/recommendation/v1/users/$friend1/commands/findMutualFriends?friendId=$friend2" | jq
sleep 1

echo "--> Recommend friends to $rec"
rec=${IDS[$RANDOM % ${#IDS[@]}]}
curl -s "http://localhost:9000/recommendation/v1/users/$rec/commands/recommendFriends" | jq
