package orange.labs.iot.computational.storage.schedule.rest.resources;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.storm.shade.org.json.simple.JSONObject;
import org.apache.storm.shade.org.json.simple.parser.JSONParser;
import org.apache.storm.shade.org.json.simple.parser.ParseException;

import orange.labs.iot.computational.storage.storm.util.PropertyManager;

@Path("/properties")
public class PropertyService {

	@POST
	@Path("/post")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String createProperty(String property_value) throws ParseException {
		try {
			JSONObject inputJsonObj = (JSONObject) new JSONParser().parse(property_value);
			String property = (String) inputJsonObj.get("property");
			String value = (String) inputJsonObj.get("value");

			PropertyManager.setPropertyValue(property, value);

			return inputJsonObj.toString();
		} catch (IOException e) {
			return "Exception occurred" + e.getMessage();
		}
	}

	@GET
	@Path("/get/{property}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPropertyValue(@PathParam("property") String property) {
		try {
			return PropertyManager.getPropertyValue(property);
		} catch (IOException e) {
			return "Exception occurred" + e.getMessage();
		}
	}

	@GET
	@Path("/getAll")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPropertyValues() {
		try {
			return PropertyManager.getProperties();
		} catch (IOException e) {
			return "Exception occurred" + e.getMessage();
		}
	}
}
