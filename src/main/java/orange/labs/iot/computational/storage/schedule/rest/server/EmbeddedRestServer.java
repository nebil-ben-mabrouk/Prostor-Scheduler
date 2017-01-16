package orange.labs.iot.computational.storage.schedule.rest.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.net.httpserver.HttpServer;

import orange.labs.iot.computational.storage.schedule.rest.resources.LogService;
import orange.labs.iot.computational.storage.storm.util.PropertyManager;

@SuppressWarnings("restriction")
public class EmbeddedRestServer {

	@SuppressWarnings("unchecked")
	private static void createHttpServer(URI uri) throws IOException {
		ResourceConfig restResourceConfig = new PackagesResourceConfig(
				"orange.labs.iot.computational.storage.schedule.rest.resources");
		restResourceConfig.getContainerResponseFilters().add(CORSFilter.class);
		HttpServer restHTTPServer = HttpServerFactory.create(uri, restResourceConfig);
		restHTTPServer.start();
	}

	public void startRestServer() {

		try{
			String hostName = InetAddress.getLocalHost().getHostAddress();
			Integer port = Integer.valueOf(PropertyManager.getPropertyValue("rest.resources.port"));
			URI uri = UriBuilder.fromUri("http://"+hostName+"/").port(port).build();
			EmbeddedRestServer.createHttpServer(uri);
			LogService.localLog("EmbeddedRestServer","Started server successfully at uri: "+ uri);

		} catch(Exception e){
			LogService.localLog("EmbeddedRestServer","Exception occurred: " + e.getMessage());
		}
	}
}
