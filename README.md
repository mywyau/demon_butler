# demon_butler

Please read about and upto the traefik section and setting up services for local development

Service to help manage and run my other app microservices using docker.

Please update 

```
application.conf
```

with your systems absolute **Base Path** to where you store your
projects.

### E.g.

### Absolute Path
```
/Users/michaelyau/desk_booking/frontend/wander
```

### Base Path
```
/Users/michaelyau/desk_booking/frontend
```

The config should pick up the project file name e.g. wander and start the docker-compose script there.


At the moment we are containerising our app frontend and backend microservices using docker and docker compose.

We wanted a system to coordinate and run all desired microservice containers easily.

##  Requirements:
    
- A configurable system for Microservices, we should be able to add more microservices as a configuration file to run as a set.
- Must be possible to run in parallel to help speed up processing of building containers
- Must run container app code from a central directory/location on the system. 
- Must be able to stop and remove docker containers via a simple command
- Must have some notification or progress system console output for UX

### To start all services defined in config

```
sbt "demon startDev"
```

### To run a single service defined in config - TODO

```
sbt "demon startDev"
```

### To start all services defined in config

```
sbt "demon stopAll"
```

### To list all services defined in config - WIP
```
sbt "demon list"
```

## Traefik

Once we have built our service can run our frontend services in their containers. Reverse proxy is facilitated via Traefik. The goal is to help unify all the frontend microservices under a single base domain. This will allow us to pass cookies/headers for the JWT token for auth through the different micro frontends

### Run traefik

```
./run_traefik.sh  
```

Dashboard is available at:

```
localhost:8080/dashboard
```

### Modifying hosts on our local system 

We may need to update our laptop systems hosts file at: 

```
/etc/hosts
```

May need sudo to override permissions

```
sudo vim /etc/hosts
```

We can then add our services new routes from traefik to the hosts file:

```
127.0.0.1 wander.localhost
127.0.0.1 reggie.localhost
```