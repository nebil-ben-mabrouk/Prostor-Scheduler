package orange.labs.iot.computational.storage.schedule.rest.resources;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.storm.shade.org.json.simple.JSONObject;

@Path("/schedule")
public class ScheduleService {

	static Map<String,Map<Integer,String>> schedules = new HashMap<String,Map<Integer,String>>();

	@Path("/{topology}/{component}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response  getLog(@PathParam("topology") String topology,@PathParam("component") String component) {
	
		String key= topology+"_"+component;
		Map<Integer,String> assignments = new HashMap<Integer,String>();
		
		if (schedules.containsKey(key)) assignments = schedules.get(key);
		
		JSONObject assignmentList = new JSONObject();
		
		if (!assignments.isEmpty()) {
			for (int executor : assignments.keySet()) assignmentList.put(executor, assignments.get(executor));	
		}
		
		return Response.status(200).entity(assignmentList.toJSONString()).build();
	}
	
	public static void addSchedule(String topology, String component, int executor, String location){
		String key= topology+"_"+component;
		Map<Integer,String> assignments = new HashMap<Integer,String>();
		
		if (schedules.containsKey(key)) assignments = schedules.get(key);
		
		assignments.put(executor,location);
		schedules.put(key, assignments);
	}
}
