#!/bin/bash
# Author : Alexander Tolpeko

echo "Spinning up kubernetes cluster"

kubectl create -f deployment/mysql/storage.yaml 
kubectl create -f deployment/mysql/secret.yaml 
kubectl create -f deployment/mysql/mysql-deployment.yaml 

kubectl create -f deployment/ingress.yaml 

kubectl create -f deployment/user-service/secret.yaml
envsubst < deployment/user-service/user-service-deployment.yaml | kubectl apply -f -

kubectl create -f deployment/community-service/secret.yaml
envsubst < deployment/community-service/community-service-deployment.yaml | kubectl apply -f -

kubectl create -f deployment/auth-service/secret.yaml
envsubst < deployment/auth-service/auth-service-deployment.yaml | kubectl apply -f -

echo "System started"
