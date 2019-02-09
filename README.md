# RNS System Web Service and Vehicle and Administrator Client
Java implementation of the RNS system, developing of a vehicle and an administrator client for that web service.
That code is developed for the Distributed Programming II course held at the Politecnico di Torino.
Design explanation of the webservice can be found in the doc folder. 

## RNS System Web Service
The code is a Java implementation (using the JAX-RS framework) of the web service design that you can find in the doc folder. The implementation includes all the main functions specified in the design. The service does not provide data persistency (data are stored in main memory) but manage concurrency, i.e. several clients can operate concurrently on the same service without restrictions.

When deployed, RnsSystem is initialized by reading the data about places and relative connections from the RnsReader interface already used for the **Rns Serializer and library"** rebository (defined in package it.polito.dp2.RNS), while information about vehicles are not be considered (i.e., initially there will be no tracked vehicle in the system). The information about places and their connections is never updated (i.e., the service always uses the information read at startup).
The service exploit the NEO4J REST API in order to store the graph of places and their connections, in the same way done for **Client RESTful web service of Neo4j"** repository, and in order to compute the suggested path for a vehicle.
In order to compute the suggested path, the service finds the shortest paths between source and destination by means of NEO4. Then, if more than one path is found, it selects one. The criterion for selection is chosen arbitrarily by the service.


## RNS Administrator Client 
The administration client for the developed web service, provide read access to information about places, their connections, and vehicles in the system. The client takes the form of a library and implements the interfaces described in the doc folder. The library loads information about places and connections from the web service at startup.

## RNS Vehicle Client 
The vehicle client for the developed web service. This client takes the form of a Java library and implements the interfaces described in the doc folder

## How run tests
```
ant –Dseed=<seed> run-tests
```
