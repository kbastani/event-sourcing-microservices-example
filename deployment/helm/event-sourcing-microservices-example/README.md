
```
$ helm repo add bitnami https://charts.bitnami.com
$ helm install --namespace esme --name esme --set fullNameOverride=esme .

$ helm install --namespace esme --name discovery --set fullnameOverride=discovery-service discovery-service

$ helm install --namespace esme --name edge --set fullnameOverride=edge-service deployment/helm/edge-service

$ helm install --namespace esme --name friend --set fullnameOverride=friend-service friend-service

$ helm install --namespace esme --name user --set fullnameOverride=user-service user-service

$ helm install --namespace esme --name recommendation --set fullnameOverride=recommendation-service deployment/helm/recommendation-service



```