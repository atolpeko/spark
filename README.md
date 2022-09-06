# Spark 

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Cloud-native social network.

**Technology stack:** Java SE, Spring Core, Spring Boot, Spring MVC, Spring Data, Spring Security, Spring Cloud, Apache Tomcat, Jackson, Maven, JUnit, Mockito, MySQL, H2, Hibernate, Docker, Kubernetes.

## Build

Maven will build a Docker image for each microservice and push it to your remote Docker repository.
```
export SPARK_REP_PREFIX=...                 
mvn clean deploy 
```
> Note: `...` is used as a Docker image prefix. Put you Docker Hub login instead of this placeholder.


## Run locally

You can spin up the Kubernetes deployment locally using Minikube.
The first step is to start minikube cluster.

```
minikube start
```

Then, enable ingress addon.

```
minikube addons enable ingress
```

Give the `minikube_run` shell script the right to execute and run it.
```
chmod +x minikube_run.sh 
export SPARK_VERSION=...  
./minikube_run.sh 
```
> Note: `...` is used as a Docker image version.


## Using Spark

Spark serves clients on port 80, providing information dynamically through HATEOAS.

All spark endpoints follow the REST API Resource naming conventions. It exposes the following endpoints:
 * http://localhost/api/users
 * http://localhost/api/communities
 * http://localhost/api/communities{id}/posts
 * http://localhost/api/communities{id}/posts{id}/comments
 * http://localhost/api/communities{id}/posts{id}/likes
 * http://localhost/api/communities{id}/posts{id}/comments{id}/likes


Spark declares 2 user roles: Users and Admins. To authenticate and obtain an access token, hit the `http://localhost/api/oauth/token` endpoint with the following parameters:

```
grant_type: password
username: ...
password: ...
client_id: web-client
client_secret: jfehlf34378sdr_12j1hdkl
scope: read write
```

Spark provides 1 pre-assigned administrator. His credentials are:

```
username: admin
password: jkhfdjk2323766
```

## Spark Internals

Spark is made up of 3 microservices: <br>
* User Service
* Community Service 
* Auth Service 

You can see the architecture on the chart:

![spark](https://user-images.githubusercontent.com/83589564/188613454-b33b8968-473a-40bf-8248-4f17e33157fe.svg)


