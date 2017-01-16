package orange.labs.iot.computational.storage.schedule.rest.resources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.storm.shade.org.json.simple.JSONObject;
import org.apache.storm.shade.org.json.simple.parser.JSONParser;
import org.apache.storm.shade.org.json.simple.parser.ParseException;

@Path("/log")
public class LogService {

	static Map<String,String> logs = new HashMap<String,String>();

	static DateFormat dateFormatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, new Locale("fr","FR"));

	@POST
	@Path("/post")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response log(InputStream incomingData) throws ParseException {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
			String line = null;
			while ((line = in.readLine()) != null) {
				stringBuilder.append(line);
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		String received_data = stringBuilder.toString(); 
		JSONObject jsonObject =  (JSONObject) new JSONParser().parse(received_data);
		
		for(Object key:jsonObject.keySet()){
			String component = (String) key;
			String logMsg = (String)jsonObject.get(component);
			
			String componentLog ="";
			
			if (logs.containsKey(component)){
				dateFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
				componentLog = logs.get(component)+
						dateFormatter.format(new Date(System.currentTimeMillis())).toString() + ": " + logMsg + "\n";
			} else{
				componentLog = "Log \n" + dateFormatter.format(new Date(System.currentTimeMillis())).toString() + ": " + logMsg + "\n";
			}
			logs.put(component, componentLog);
		}
		
		// return HTTP response 200 in case of success
		return Response.status(200).entity(stringBuilder.toString()).build();
	}

	@Path("/{component}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response  getLog(@PathParam("component") String component) {		
		String response = "No log from component: "+component+" \n";
		if (null!=logs.get(component)) response = logs.get(component).toString();
		return Response.status(200).entity(response).build();
	}
	
	@Path("/global")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response  getLog() throws UnknownHostException {
		String response = "";		
		if (null!=logs.get("global")) response = logs.get("global").toString();
		else response= "No log from host: "+InetAddress.getLocalHost().getHostAddress()+"\n";
		return Response.status(200).entity(response).build();
	}
	
	@Path("/clear/{component}")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response  clearLog(@PathParam("component") String component){
		if (logs.containsKey(component)) {
			logs.put(component, "Log: \n");
			return Response.status(200).entity(logs.get(component).toString()).build();
		}		
		else return Response.status(200).entity("No log from component: "+component+" \n").build();
		
	}
	
	@Path("/clearGlobal")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response  clearGlobalLog() throws UnknownHostException{	
		if (logs.containsKey("global")) {
			logs.put("global", "Log: \n");
			return Response.status(200).entity(logs.get("global").toString()).build();
		}		
		else return Response.status(200).entity("No log from host: "+InetAddress.getLocalHost().getHostAddress()+" \n").build();
	}
	
	@Path("/clearAll")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response  clearLog(){	
		for (String key : logs.keySet()){
			logs.put(key, "Log \n");
		}
		return Response.status(200).entity("All logs are empty \n".toString()).build();
	}
	
	@Path("/getComponents")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response  getComponents(){	
		String response = "Components' list is empty";
		if (!logs.keySet().isEmpty()) {
			response = "List of components sending logs: \n";
			for (String component: logs.keySet()){
				response += component+"\n";
			}			
		}
		return Response.status(200).entity(response).build();
	}
	
	public static void localLog(String component, String logMsg){
		dateFormatter.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		String currentLogMessage = dateFormatter.format(new Date(System.currentTimeMillis())).toString() + ": " + logMsg + "\n";
		String log ="";
		
		if (logs.containsKey(component)) log = logs.get(component)+currentLogMessage;
		else log = "Log \n" + currentLogMessage;	
		logs.put(component, log);
		
		currentLogMessage = "[" +component+ "] "+currentLogMessage;
		if (logs.containsKey("global")) log = logs.get("global")+currentLogMessage;
		else log = "Log \n" + currentLogMessage;
		logs.put("global", log);
	}
}
