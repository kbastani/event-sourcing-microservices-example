#!/usr/bin/env bash

# Requires jq, see https://stedolan.github.io/jq/
# Install with
# macOS: brew install jq
# Windows: chocolatey install jq.
# Linux: apt-get install jq

# Wake up user service
curl -i "http://localhost:9000/user/actuator/info" | jq

ID_MH=`\
curl -X "POST" "http://localhost:9000/user/v1/users" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "firstName": "Michael",
  "lastName": "H."
}' | jq .id`

ID_MS=`\
curl -X "POST" "http://localhost:9000/user/v1/users" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "firstName": "Michael",
  "lastName": "S."
}' | jq .id`

ID_MJ=`\
curl -X "POST" "http://localhost:9000/user/v1/users" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "firstName": "Michaela",
  "lastName": "J."
}' | jq .id`

ID_KB=`\
curl -X "POST" "http://localhost:9000/user/v1/users" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "firstName": "Kenny",
  "lastName": "B."
}' | jq .id`

ID_SH=`\
curl -X "POST" "http://localhost:9000/user/v1/users" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "firstName": "Sonja",
  "lastName": "H."
}' | jq .id`

ID_EE=`\
curl -X "POST" "http://localhost:9000/user/v1/users" \
     -H 'Content-Type: application/json; charset=utf-8' \
     -d $'{
  "firstName": "Emil",
  "lastName": "E."
}' | jq .id`

# Wake up friend service
curl -i "http://localhost:9000/friend/actuator/info" | jq

# Wake up recommendation service
curl -i "http://localhost:9000/recommendation/actuator/info" | jq

curl -X "POST" "http://localhost:9000/friend/v1/users/$ID_MH/commands/addFriend?friendId=$ID_MS" | jq
curl -X "POST" "http://localhost:9000/friend/v1/users/$ID_MS/commands/addFriend?friendId=$ID_KB" | jq
curl -X "POST" "http://localhost:9000/friend/v1/users/$ID_KB/commands/addFriend?friendId=$ID_SH" | jq
curl -X "POST" "http://localhost:9000/friend/v1/users/$ID_KB/commands/addFriend?friendId=$ID_EE" | jq
curl -X "POST" "http://localhost:9000/friend/v1/users/$ID_MH/commands/addFriend?friendId=$ID_EE" | jq
curl -X "POST" "http://localhost:9000/friend/v1/users/$ID_KB/commands/addFriend?friendId=$ID_MJ" | jq

curl "http://localhost:9000/friend/v1/users/$ID_MH/friends" | jq
curl "http://localhost:9000/recommendation/v1/users/$ID_MH/commands/findMutualFriends?friendId=$ID_KB" | jq
curl "http://localhost:9000/recommendation/v1/users/$ID_KB/commands/recommendFriends" | jq
