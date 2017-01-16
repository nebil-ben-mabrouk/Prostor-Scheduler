package orange.labs.iot.computational.storage.storm.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyManager {
	
	private static final String resource = "Scheduler.properties";
	
	public static Properties getKafkaConsumerProperties() throws IOException{
		String propFileName = "KafkaConsumer.properties";
		Properties properties = new Properties();
		InputStream inputStream = null;
		
		try{
			inputStream = PropertyManager.class.getClassLoader().getResourceAsStream(propFileName);
			if (inputStream != null) {
				properties.load(inputStream);
			}
			inputStream.close();
			
		}catch (Exception e) {
			System.err.println("Exception: " + e);
		}	
		return properties;
	}
	
	public static String getPropertyValue(String property) throws IOException {
		String result = "";

		Properties prop = loadProperties();

		result = prop.getProperty(property);

		return result;
	}

	public static String getProperties() throws IOException {
		String result = "";

		Properties prop = loadProperties();

		for (Object key : prop.keySet()) {
			result += key + "=";
			result += prop.getProperty((String) key) + "\n";
		}
		
		return result;
	}

	public static void setPropertyValue(String property, String value) throws IOException {

		Properties prop = loadProperties();

		FileWriter fw = new FileWriter(new File(resource));

		prop.setProperty(property, value);
		prop.store(fw, null);

		fw.close();

	}

	public static Properties loadProperties() throws IOException {

		Properties properties = new Properties();
		InputStream inputStream = null;
		try{
			inputStream = PropertyManager.class.getClassLoader().getResourceAsStream(resource);
			if (inputStream != null) {
				properties.load(inputStream);
			}
			inputStream.close();
			
		}catch (Exception e) {
			System.err.println("Exception: " + e);
		}	
		return properties;
	}
}
