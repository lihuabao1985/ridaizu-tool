package config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import com.google.common.base.Strings;

public class Config {

	private Config() {

	}

	public static String getString(String key) {
		if (key == null || key == "" || "".equals(key)) {
			return null;
		}
		Properties properties = new Properties();
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream("config.properties"), "UTF-8");
			properties.load(inputStreamReader);
			inputStreamReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties.getProperty(key);
	}

	public static String getString(String key, String def) {
		if (Strings.isNullOrEmpty(key)) {
			return null;
		}
		Properties properties = new Properties();
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream("config.properties"), "UTF-8");
			properties.load(inputStreamReader);
			inputStreamReader.close();
		} catch (FileNotFoundException e) {
			return def;
		} catch (IOException e) {
			return def;
		}

		String value = properties.getProperty(key);

		return Strings.isNullOrEmpty(value) ? def : value;
	}

}
