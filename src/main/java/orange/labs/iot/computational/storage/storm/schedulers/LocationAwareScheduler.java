package orange.labs.iot.computational.storage.storm.schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.storm.generated.StormTopology;
import org.apache.storm.scheduler.Cluster;
import org.apache.storm.scheduler.ExecutorDetails;
import org.apache.storm.scheduler.IScheduler;
import org.apache.storm.scheduler.SupervisorDetails;
import org.apache.storm.scheduler.Topologies;
import org.apache.storm.scheduler.TopologyDetails;
import org.apache.storm.scheduler.WorkerSlot;

import orange.labs.iot.computational.storage.schedule.rest.resources.LogService;
import orange.labs.iot.computational.storage.schedule.rest.resources.ScheduleService;
import orange.labs.iot.computational.storage.schedule.rest.server.EmbeddedRestServer;

public class LocationAwareScheduler implements IScheduler {

	private static final String SCHEDULER = "LocationAwareScheduler";

	private Map<String, SupervisorDetails> supervisorLocations = new HashMap<String, SupervisorDetails>();

	public void prepareSupervisors(Cluster cluster) {

		LogService.localLog(SCHEDULER, "Supervisors: ");
		supervisorLocations = new HashMap<String, SupervisorDetails>();

		for (SupervisorDetails supervisor : cluster.getSupervisors().values()) {

			supervisorLocations.put(supervisor.getHost(), supervisor);

			LogService.localLog(SCHEDULER,
					supervisor.getHost() + ":" + supervisor.getId().substring(supervisor.getId().length() - 12) + ": "
							+ supervisor.getAllPorts().toString());

		}
	}

	public void scheduleTopology(TopologyDetails topology, Cluster cluster) throws InterruptedException, IOException {
		Map<WorkerSlot, List<ExecutorDetails>> workerToExecutors = new HashMap<WorkerSlot, List<ExecutorDetails>>();

		LogService.localLog(SCHEDULER, topology.getId() + "," + topology.getId() + ", workers: "
				+ topology.getNumWorkers() + ", tasks: " + topology.getExecutorToComponent().toString());

		if (cluster.needsScheduling(topology)) {

			StormTopology stormTopology = topology.getTopology();
			HashSet<String> components = new HashSet<String>();

			components.addAll(stormTopology.get_spouts().keySet());
			components.addAll(stormTopology.get_bolts().keySet());

			Map topologyConf = topology.getConf();

			LogService.localLog(SCHEDULER, "Scheduling components: ");

			for (String name : components) {

				if (null != cluster.getNeedsSchedulingComponentToExecutors(topology).get(name)
						&& !cluster.getNeedsSchedulingComponentToExecutors(topology).get(name).isEmpty()) {
					List<ExecutorDetails> executors = new ArrayList<ExecutorDetails>(
							cluster.getNeedsSchedulingComponentToExecutors(topology).get(name));
					Collections.sort(executors, new Comparator<ExecutorDetails>() {
						@Override
						public int compare(ExecutorDetails o1, ExecutorDetails o2) {
							return o1.getStartTask() - o2.getStartTask();
						}
					});

					List<String> locations = new ArrayList<String>();

					if (topologyConf.containsKey(name)) {

						locations = Arrays.asList(((String) topologyConf.get(name)).split("_"));
						LogService.localLog(SCHEDULER, name + ": " + locations.toString());

						Iterator<ExecutorDetails> execIterator = executors.iterator();

						for (String location : locations) {

							if (execIterator.hasNext()) {

								if (location != null && location != "" && supervisorLocations.get(location) != null) {
									SupervisorDetails supervisor = supervisorLocations.get(location);

									if (!cluster.getAvailableSlots(supervisor).isEmpty()) {

										WorkerSlot tobeAssigned = cluster.getAvailableSlots(supervisor).get(0);
										if (!workerToExecutors.containsKey(tobeAssigned)) {
											workerToExecutors.put(tobeAssigned, new ArrayList<ExecutorDetails>());
										}

										ExecutorDetails executor = execIterator.next();
										workerToExecutors.get(tobeAssigned).add(executor);

										// send assignment to Kafka
										ScheduleService.addSchedule(topology.getId(), name, executor.getStartTask(), location);
									
										LogService.localLog(SCHEDULER, executor + "==>"
												+ tobeAssigned.getNodeId()
														.substring(tobeAssigned.getNodeId().length() - 12)
												+ ":" + tobeAssigned.getPort());
									}
								}
							}
						}
					}
				}
			}
		}

		// scheduling
		LogService.localLog(SCHEDULER,  "Assigning excutors to workerslots");
		for (WorkerSlot ws : workerToExecutors.keySet()) {
			LogService.localLog(SCHEDULER, "Assigning " + workerToExecutors.get(ws) + " to " + ws.getPort());
			if (!cluster.isSlotOccupied(ws)) {
				cluster.assign(ws, topology.getId(), workerToExecutors.get(ws));
				LogService.localLog(SCHEDULER, workerToExecutors.get(ws) + " assigned to " + ws.getPort());
			}
		}
	}

	public void scheduleTopologiesWithLocationAwareness(Topologies topologies, Cluster cluster)
			throws InterruptedException, IOException {
		Collection<TopologyDetails> topologiesList = topologies.getTopologies();
		LogService.localLog(SCHEDULER, "Topologies: " + topologiesList);

		for (TopologyDetails topology : topologiesList) {
			scheduleTopology(topology, cluster);
		}
	}

	@Override
	public void prepare(Map conf) {
		LogService.localLog(SCHEDULER, "Preparing the location-aware scheduler");

		try {
		
			EmbeddedRestServer restServer = new EmbeddedRestServer();
			restServer.startRestServer();
			
		} catch (Exception e) {
			LogService.localLog(SCHEDULER, "Error occurred: " + e.getMessage());
		}
	}

	@Override
	public void schedule(Topologies topologies, Cluster cluster) {
		try {
			LogService.localLog(SCHEDULER, "Executing the location-aware scheduler");

			// processing supervisors
			prepareSupervisors(cluster);

			// processing topologies
			scheduleTopologiesWithLocationAwareness(topologies, cluster);

		} catch (Exception e) {
			LogService.localLog(SCHEDULER, "Error occurred: " + e.getMessage());
		}
	}
}
