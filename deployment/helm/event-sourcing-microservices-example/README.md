# Deploy Event Sourcing Microservices Example (ESME) via Helm

## Prerequisites

> add instructions for downloading and setting up helm here

Add the bitnami helm repository which contains the `kafka` and `zookeeper` charts:

```bash
helm repo add bitnami https://charts.bitnami.com
```

## Deploy

Once Helm is set up, deploying this is quite simple:

```bash
helm install --namespace esme --name esme --set fullNameOverride=esme \
  deployment/helm/event-sourcing-microservices-example
```

There is not security on the apps, therefore rather than exposing the edge
to the internet we can utilize `kubectl port-forward` to access the app:

```bash
kubectl --namespace esme port-forward svc/edge-service 9000
```

run the following script to add users and friendships to the social network:

```bash
bash deployment/sbin/generate-social-network.sh
```