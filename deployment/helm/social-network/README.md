# Deploy Hyerskale Social Network via Helm

## Prerequisites

*   A Kubernetes Cluster
*   [kubectl](https://kubernetes.io/docs/tasks/tools/install-kubectl/#install-kubectl)
*   [helm](https://docs.helm.sh/using_helm/#installing-helm)

Ensure you have access to Kubernetes:

```bash
kubectl get nodes
NAME                                      STATUS    ROLES     AGE       VERSION
vm-0acae492-fe57-46b8-510f-f95b7b007078   Ready     <none>    7d        v1.11.5
vm-0f68c2fd-cff9-4c23-4387-2373bd2b4ae3   Ready     <none>    7d        v1.11.5
vm-ab234512-d336-4f5a-495a-a917657575c1   Ready     <none>    7d        v1.11.5
```

Install helm tiller (RBAC):

```bash
kubectl -n kube-system create serviceaccount tiller
kubectl create clusterrolebinding tiller \
  --clusterrole cluster-admin \
  --serviceaccount=kube-system:tiller
helm init --service-account=tiller
```

Clone this repo locally:

```bash
git clone https://github.com/hyperskale/social-network-example.git
cd social-network-example
```

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