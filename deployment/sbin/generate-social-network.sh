#!/usr/bin/env bash

# Requires jq, see https://stedolan.github.io/jq/
# Install with
# macOS: brew install jq
# Windows: chocolatey install jq.
# Linux: apt-get install jq

# Wake up user service
echo "--> Wake up user service"
curl -s "http://localhost:9000/user/actuator/info" > /dev/null

echo -n "====> Create users"
ID_MH=`\
curl -s -X "POST" "http://localhost:9000/user/v1/users" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "firstName": "Michael",
  "lastName": "H."
}' | jq .id`

echo -n "."
ID_MS=`\
curl -s -X "POST" "http://localhost:9000/user/v1/users" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "firstName": "Michael",
  "lastName": "S."
}' | jq .id`

echo -n "."
ID_MJ=`\
curl -s -X "POST" "http://localhost:9000/user/v1/users" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "firstName": "Michaela",
  "lastName": "J."
}' | jq .id`

echo -n "."
ID_KB=`\
curl -s -X "POST" "http://localhost:9000/user/v1/users" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "firstName": "Kenny",
  "lastName": "B."
}' | jq .id`

echo -n "."
ID_SH=`\
curl -s -X "POST" "http://localhost:9000/user/v1/users" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "firstName": "Sonja",
  "lastName": "H."
}' | jq .id`

echo -n "."
ID_EE=`\
curl -s -X "POST" "http://localhost:9000/user/v1/users" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "firstName": "Emil",
  "lastName": "E."
}' | jq .id`

echo "done"
sleep 5

# Wake up friend service
echo "--> Wake up friend service"
curl -s "http://localhost:9000/friend/actuator/info" > /dev/null
# Wake up recommendation service
echo "--> Wake up recommendation service"
curl -s "http://localhost:9000/recommendation/actuator/info" > /dev/null


echo -n "====> Create friendships"
curl -s -X "POST" "http://localhost:9000/friend/v1/users/$ID_MH/commands/addFriend?friendId=$ID_MS" | jq
sleep 1
curl -s -X "POST" "http://localhost:9000/friend/v1/users/$ID_MS/commands/addFriend?friendId=$ID_KB" | jq
sleep 1
curl -s -X "POST" "http://localhost:9000/friend/v1/users/$ID_KB/commands/addFriend?friendId=$ID_SH" | jq
sleep 1
curl -s -X "POST" "http://localhost:9000/friend/v1/users/$ID_KB/commands/addFriend?friendId=$ID_EE" | jq
sleep 1
curl -s -X "POST" "http://localhost:9000/friend/v1/users/$ID_MH/commands/addFriend?friendId=$ID_EE" | jq
sleep 1
curl -s -X "POST" "http://localhost:9000/friend/v1/users/$ID_KB/commands/addFriend?friendId=$ID_MJ" | jq
sleep 1

echo "--> Check that $ID_MH has friends"
curl -s "http://localhost:9000/friend/v1/users/$ID_MH/friends" | jq
sleep 1
echo "--> List Mutual friends between $ID_MH and $ID_KB"
curl -s "http://localhost:9000/recommendation/v1/users/$ID_MH/commands/findMutualFriends?friendId=$ID_KB" | jq
sleep 1

echo "--> Recommend friends to $ID_KB"
curl -s "http://localhost:9000/recommendation/v1/users/$ID_KB/commands/recommendFriends" | jq
