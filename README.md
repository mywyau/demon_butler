# demon_butler

Service to help manage and run my other app micro-services using docker

At the moment we are containerising our app frontend and backend microservices using docker and docker compose.

We wanted a system to coordinate and run all desired microserice containers easily.

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
sbt "demon stop"
```

### To list all services defined in config - WIP
```
sbt "demon list"
```



