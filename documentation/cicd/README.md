# Creating a CI/CD Pipeline for the Social Network example application

This document attempts to demonstrate running a full CI/CD system for this Social Network example application by utilizing opensource tools for each step of the pipeline running on Kubernetes.

Any of these parts could fairly trivially be switched out for a hosted service or for a different tool.

## Test and Build - drone.io

* Create a new [github oauth application](https://github.com/settings/applications) for drone.  Take note of the Client ID and Client Secret.

* Create a secret in kubernetes for that application:

```bash
kubectl create secret generic drone-server-secrets \
      --namespace=cicd-drone \
      --from-literal=DRONE_GITHUB_CLIENT_SECRET="github-oauth2-client-secret"
```

* Deploy drone with the secret and the client ID:

```bash
helm install --namespace cicd-drone --name drone \
      --set 'server.env.DRONE_GITHUB_SERVER=https://github.com' \
      --set 'server.env.DRONE_ORGS=paulczar' \
      --set 'server.env.DRONE_GITHUB_CLIENT_ID=2a89bce8256d7258af5e' \
      --set 'server.envSecrets.drone-server-secrets[0]=DRONE_GITHUB_CLIENT_SECRET' \
      --set 'server.host=http://drone.pivlab.gcp.paulczar.wtf:8000' \
      --set 'server.env.DRONE_USER_CREATE=username:paulczar\,admin:true' \
      --set 'adminUser=paulczar' \
      stable/drone
```

* Point DNS A record matching the github settings to your services IP.

## Store Images - harbor registry

## Deploy - Spinnaker
