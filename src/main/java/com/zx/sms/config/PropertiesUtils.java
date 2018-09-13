package com.zx.sms.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

public class PropertiesUtils {
	private static final Properties global = loadProperties("global.properties");
	private static final Properties je = loadProperties("je.properties");
    public static String globalBDBBaseHome = PropertiesUtils.getproperties("BDBBaseHome",System.getProperty("java.io.tmpdir"));
	public static String getdefaultTransportCharset()
	{
		return global.getProperty("defaultTransportCharset");
	}
	
	public static String getproperties(String key,String defaultValue)
	{
		String ret = global.getProperty(key);
		return  StringUtils.isBlank(ret) ? defaultValue :ret;
	}
	
	public static Properties getJeProperties(){
		Properties properties = new Properties();
		properties.putAll(je);
		return properties;
	}

	private static Properties loadProperties(String resources) {

		// 使用InputStream得到一个资源文件

		InputStream inputstream = PropertiesUtils.class.getClassLoader().getResourceAsStream(resources);

		// new 一个Properties

		Properties properties = new Properties();

		try {

			// 加载配置文件

			properties.load(inputstream);

			return properties;

		} catch (IOException e) {

			throw new RuntimeException(e);

		} finally {

			try {

				inputstream.close();

			} catch (IOException e) {

				throw new RuntimeException(e);

			}

		}
	}

}
