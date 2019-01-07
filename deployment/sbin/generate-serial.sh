#!/usr/bin/env bash

# Requires jq, see https://stedolan.github.io/jq/
# Install with
# macOS: brew install jq
# Windows: chocolatey install jq.
# Linux: apt-get install jq

set -e

export EDGE_URI=${1:-localhost:9000}
USER_STATUS="DOWN"
FRIEND_STATUS="DOWN"
RECOMMENDATION_STATUS="DOWN"
IDS=()

echo "Using edge-service URI: $EDGE_URI"

# Wake up user service
echo -n "--> Wake up user service... "
while [[ "$USER_STATUS" = "DOWN" ]]; do
    USER_STATUS=`curl -s "http://$EDGE_URI/user/actuator/health" | jq .status`;
    [[ $USER_STATUS = "" ]] && USER_STATUS="DOWN";
    echo ${USER_STATUS};
    sleep 1;
done


# Wake up friend service
echo -n "--> Wake up friend service..."
while [[ "$FRIEND_STATUS" = "DOWN" ]]; do
    FRIEND_STATUS=`curl -s "http://$EDGE_URI/friend/actuator/health" | jq .status`;
    [[ $FRIEND_STATUS = "" ]] && FRIEND_STATUS="DOWN";
    echo ${FRIEND_STATUS};
    sleep 1;
done

# Wake up recommendation service
echo -n "--> Wake up recommendation service..."
while [[ "$RECOMMENDATION_STATUS" = "DOWN" ]]; do
    RECOMMENDATION_STATUS=`curl -s "http://$EDGE_URI/recommendation/actuator/health" | jq .status`;
    [[ $RECOMMENDATION_STATUS = "" ]] && RECOMMENDATION_STATUS="DOWN";
    echo ${RECOMMENDATION_STATUS};
    sleep 1;
done

echo "====> Create users"


DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

while read fullname; do
  NAME=( $(IFS=" " echo "${fullname}") )
  ADD_USER=`\
    curl -s -X "POST" "http://$EDGE_URI/user/v1/users" \
      -H 'Content-Type: application/json; charset=utf-8' \
      -d $"{\"firstName\": \"${NAME[0]}\",\"lastName\": \"${NAME[1]}\"}"`
  echo $ADD_USER | jq
  USER_ID=`echo $ADD_USER | jq .id`
  [[ "$USER_ID" != "null" ]] && IDS+=("${USER_ID}")
done <$DIR/names-15.txt

while read id; do
  echo ${id} | jq .;
  IDS+=($(echo ${id} | jq -r .id));
done <./temp.txt

echo -n "added ids: "
for i in "${IDS[@]}"
do
    echo -n $i
done
echo

echo "====> Create 100 friendships..."
for i in {1..100};
do
  friend1=${IDS[$RANDOM % ${#IDS[@]}]};
  friend2=${IDS[$RANDOM % ${#IDS[@]}]};
  echo "$friend1 ❤ $friend2";
  FRIEND_RESULT=`curl -s -X "POST" \
   "http://$EDGE_URI/friend/v1/users/$friend1/commands/addFriend?friendId=$friend2" \
    -H 'Content-Type: application/json; charset=utf-8' -d ""`
  echo ${FRIEND_RESULT} | jq
done

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
