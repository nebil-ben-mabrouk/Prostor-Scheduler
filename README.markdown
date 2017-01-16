# Prostor-Scheduler

## Overview
Prostor-Scheduler is a custom locagtion-aware scheduler for Apache Storm. It allows to deploy each component instance (of a Storm topology) on supervisor hosts specified by the developer.

Before using Prostor-Scheduler, it is recommended to be familiar with the concepts of [Apache Storm](http://storm.apache.org/).

Prostor-Scheduler considers a set of Storm components (spouts and bolts), the number of instances of each component, and the supervisors' hostnames on which each component intance must be deployed.

Prostor-Scheduler comprehends an embedded REST server that communicates scheduling information and logs to REST clients.

Communicating scheduling information is particularly important. Indeed, in Apache Storm, the identifiers of component instances are generated at run-time when deploying the topology.  Some [stream groupings](http://storm.apache.org/releases/current/Concepts.html) in Apache Storm (notably custom groupings) need to know these identifiers in order to stream data to the appropriate component.  For such a case, the embedded REST server  communicates the mapping "component-instance-id => supervisor-hostname" to stream groupings (implemented within Storm topologies). 

The Embedded REST Server communicates via the port '8181' on the Nimbus host.  This port can be modified by simply editing the file '/src/main/resources/Scheduler.properties'. Developers should change the line "rest.resources.port=8181".


## Integrate Prostor-Scheduler in Storm Nimbus
1- Copy the jar file 'prostor-scheduler.jar' into the directory 'STORM-HOME/lib/' (where 'STORM-HOME' is the install directory of Storm Nimbus).

2- Declare the scheduler in the configuration file 'storm.yaml', by adding the following line:
 storm.scheduler: orange.labs.iot.computational.storage.storm.schedulers.LocationAwareScheduler. 

3- Check whether the scheduler is correctly running by calling the embedded REST server from a web browser: 
   http://'nimbus-host':8181/log/global  (this address returns the scheduler log).


## Configure the components (to be scheduled) in Storm topologies  
In Storm topologies' configurations, developers have to add the deployment information for each component. 

These information are composed of the "componentId" and the targeted supervisors' hostnames, underscore-separated (e.g., "hostname1_hostname2").

Below, is an example of a yaml configuration file for a Storm Flux topology:

```
  name: "myTopology"

  spouts:
    - id: "Source"
      className: "myPackage.mySpout"
      parallelism: 2

  bolts:
    - id: "ProcessingComponent"
      className: "myPackage.myComponent"
      parallelism: 2
    
  streams:
    - name: "Source --> ProcessingComponent"
      from: "Source"
      to: "ProcessingComponent"
      grouping: 
        streamId: "myCustomDataStream"
        type: CUSTOM
        customClass:
          className: "myPackage.myCustomStreamGrouping"
        
  config:
    topology.workers: 2
    Source: "hostname1_hostname2"
    ProcessingComponent: "hostname1_hostname2"
```

In the above example, Prostor-Scheduler deploys two instances of the spout 'Source'. An instance on 'hostname1', and a second instance on 'hostname2'.  Similarly for the bolt 'ProcessingComponent'.

### Get the scheduling information of a given component
Call the embedded REST server from a web browser: 
http://'nimbus-host':8181/schedule/'topology-id'/'component-id'

