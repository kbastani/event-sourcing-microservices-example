# Deploy Hyerskale Social Network via Helm

## Prerequisites

> add instructions for downloading and setting up helm here

Add the bitnami helm repository which contains the `kafka` and `zookeeper` charts:

```bash
helm repo add bitnami https://charts.bitnami.com
```

## Update Dependencies

Run a few helm commands to ensure all dependent charts are available:

```bash
helm dep update deployment/helm/social-network
helm dep update deployment/helm/friend-service
helm dep update deployment/helm/user-service
helm dep update deployment/helm/recommendation-service

```

## Deploy

Once Helm is set up, deploying this is quite simple:

```bash
helm install --namespace social-network --name social-network --set fullNameOverride=social-network \
  deployment/helm/social-network
```

There is no security on the apps, therefore rather than exposing the edge
to the internet we can utilize `kubectl port-forward` to access the app:

```bash
kubectl --namespace social-network port-forward svc/edge-service 9000
```

run the following script to add users and friendships to the social network:

```bash
bash deployment/sbin/generate-social-network.sh
```

## Cleanup

To uninstall run the following commands:

```bash
helm delete --purge social-network
kubectl delete pvc datadir-social-network-neo4j-core-0
```