# Deploy Event Sourcing Microservices Example via Helm

> add instructions for downloading and setting up helm here

Deploy the Project Chart:

> This sets up the shared kafka/zookeeper.  Eventually it will deploy the whole rig using subcharts etc.  but for now its seperate to help troubleshoot.

```bash
helm repo add bitnami https://charts.bitnami.com
helm install --namespace esme --name esme --set fullNameOverride=esme deployment/helm/event-sourcing-microservices-example
```

Deploy the Microservices:

```bash
helm install --namespace esme --name edge \
  --set fullnameOverride=edge-service \
  deployment/helm/edge-service

helm install --namespace esme --name friend \
  --set fullnameOverride=friend-service \
  deployment/helm/friend-service

helm install --namespace esme --name user \
--set fullnameOverride=user-service \
deployment/helm/user-service

helm install --namespace esme --name recommendation \
--set fullnameOverride=recommendation-service \
deployment/helm/recommendation-service
```

Port forward to the edge:

```
kubectl --namespace esme port-forward svc/edge-service 9000
```

Create Social Network:

```
bash deployment/sbin/generate-social-network.sh
```