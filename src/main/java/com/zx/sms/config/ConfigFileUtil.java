package com.zx.sms.config;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.common.GlobalConstance;
import com.zx.sms.common.util.SpringContextUtil;
import com.zx.sms.connect.manager.EndpointEntity.ChannelType;
import com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity;
import com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity;
import com.zx.sms.handler.api.BusinessHandlerInterface;
import com.zx.sms.service.LoadConfigFromDBService;

/**
 *@author Lihuanghe(18852780@qq.com)
 *加载configuration文件
 */
public class ConfigFileUtil {
	private static final Logger logger = LoggerFactory.getLogger(ConfigFileUtil.class);
	private static DefaultConfigurationBuilder configbuilder = new DefaultConfigurationBuilder();
	public static CombinedConfiguration config;
	private static boolean isLoad = false;

	public synchronized static void loadconfiguration(String filepath) {
		// 多线程并发时，需要判断一次是否被其它线程load过了
		if (isLoad)
			return;

		configbuilder.setFileName(filepath);
		try {
			config = configbuilder.getConfiguration(true);
			isLoad = true;
		} catch (ConfigurationException e) {
			logger.error("load config {} failed.", filepath, e);
		}
	}

	private static void initLoad() {
		if (!isLoad) {
			loadconfiguration("configuration.xml");
//			loadconfiguration("DBSQL.sql");
		}

	}

	public static File getJeproperties() {
		initLoad();
		Configuration props = config.getConfiguration("BDBproperties");
		if (props == null)
			return null;
		return ((PropertiesConfiguration) props).getFile();
	}

	private static PropertiesConfiguration loadGlobalConfigEntity() {
		initLoad();
		PropertiesConfiguration props = (PropertiesConfiguration) config.getConfiguration("global");
		return props;
	}

	public static String getGlobalPropertiesByKey(String key, String defaultValue) {
		return defaultValue == null ? loadGlobalConfigEntity().getString(key) : loadGlobalConfigEntity().getString(key, defaultValue);
	}

	public static String getGlobalBDBBaseHome() {
		String baseHome = loadGlobalConfigEntity().getString("BDBBaseHome");
		if (baseHome.endsWith("/") || baseHome.endsWith("\\")) {
			return baseHome;
		} else {
			return baseHome + "/";
		}
	}

	public static Charset getGlobaldefaultTransportCharset() {
		String chartset = loadGlobalConfigEntity().getString("defaultTransportCharset");
		return Charset.forName(chartset);

	}

	//从服务加载DB
	public static List<CMPPServerEndpointEntity> loadServerEndpointBean()  {
		LoadConfigFromDBService loadConfigFromDBService=  (LoadConfigFromDBService) SpringContextUtil.getBean(LoadConfigFromDBService.class);
		
		return loadConfigFromDBService.loadServerEndpointEntity(); 

	}


	//从客户加载DB
	public static List<CMPPClientEndpointEntity> loadClientEndpointEntityBean()  {
		LoadConfigFromDBService loadConfigFromDBService=  (LoadConfigFromDBService) SpringContextUtil.getBean(LoadConfigFromDBService.class);
		
		return loadConfigFromDBService.loadClientEndpointEntity(); 
	}
	//从服务加载xml配置文件
	public static List<CMPPServerEndpointEntity> loadServerEndpointEntity() {
		initLoad();
		XMLConfiguration clientconfig = (XMLConfiguration) config.getConfiguration("serverEndPoint");
		List<HierarchicalConfiguration> servers = clientconfig.configurationsAt("server");
		if (servers.size() == 0)
			return null;
		List<CMPPServerEndpointEntity> result = new ArrayList<CMPPServerEndpointEntity>();
		for (HierarchicalConfiguration server : servers) {
			CMPPServerEndpointEntity tmpSever = new CMPPServerEndpointEntity();

			tmpSever.setId(server.getString("id"));
			tmpSever.setDesc(server.getString("desc"));

			tmpSever.setValid(server.getBoolean("isvalid", true));

			tmpSever.setHost(server.getString("host"));
			tmpSever.setPort(server.getInteger("port", 7890));
			tmpSever.setMaxChannels(server.getShort("maxChannels", (short) 0x7fff));
			HierarchicalConfiguration endpoints = server.configurationAt("endpoints");
			List<HierarchicalConfiguration> sessions = endpoints.configurationsAt("endpoint");
			if (sessions.size() == 0)
				break;

			for (HierarchicalConfiguration session : sessions) {

				CMPPServerChildEndpointEntity tmp = new CMPPServerChildEndpointEntity();
				buildCMPPEndpointEntity(session, tmp);
				tmpSever.addchild(tmp);
			}

			result.add(tmpSever);
		}
		return result;
	}
	
	//从客户加载xml配置文件
	public static List<CMPPClientEndpointEntity> loadClientEndpointEntity() {
		initLoad();
		XMLConfiguration clientconfig = (XMLConfiguration) config.getConfiguration("clientEndPoint");

		HierarchicalConfiguration root = clientconfig.configurationAt("endpoints");
		List<HierarchicalConfiguration> sessions = root.configurationsAt("endpoint");
		if (sessions.size() == 0)
			return null;
		List<CMPPClientEndpointEntity> result = new ArrayList<CMPPClientEndpointEntity>();
		for (HierarchicalConfiguration session : sessions) {
			CMPPClientEndpointEntity tmp = new CMPPClientEndpointEntity();
			buildCMPPEndpointEntity(session, tmp);
			result.add(tmp);
		}
		return result;
	}
	private static void buildCMPPEndpointEntity(HierarchicalConfiguration session, CMPPEndpointEntity tmp) {
		initLoad();
		tmp.setId(session.getString("id"));
		tmp.setDesc(session.getString("desc"));
		tmp.setChannelType(ChannelType.valueOf(ChannelType.class, session.getString("type", "DUPLEX")));
		tmp.setValid(session.getBoolean("isvalid", true));
		tmp.setGroupName(session.getString("group"));
		tmp.setHost(session.getString("host"));
		tmp.setPort(session.getInteger("port", 7890));
		tmp.setUserName(session.getString("user"));
		tmp.setPassword(session.getString("passwd"));
		tmp.setVersion(session.getShort("version", (short) 0x30));
		tmp.setIdleTimeSec(session.getShort("idleTime", (short) 30));
		tmp.setLiftTime(session.getLong("lifeTime", 259200L));
		tmp.setMaxRetryCnt(session.getShort("maxRetry", (short) 3));
		tmp.setRetryWaitTimeSec(session.getShort("retryWaitTime", (short) 60));
		tmp.setMaxChannels(session.getShort("maxChannels"));
		tmp.setWindows(session.getShort("windows", (short) 3));
		tmp.setChartset(Charset.forName(session.getString("charset", GlobalConstance.defaultTransportCharset.name())));

		HierarchicalConfiguration handlerSet = session.configurationAt("businessHandlerSet");

		List<Object> handlers = handlerSet.getList("handler");

		List<Class<BusinessHandlerInterface>> bizHandlers = new ArrayList<Class<BusinessHandlerInterface>>();
		tmp.setBusinessHandlerSet(bizHandlers);

		if (handlers != null && handlers.size() > 0) {
			for (Object handler : handlers) {
				if (handler == null)
					continue;
				if (handler instanceof String && StringUtils.isBlank((String) handler)) {
					continue;
				}
				BusinessHandlerInterface tmpHandler = null;
				try {
					Class<BusinessHandlerInterface> clz = (Class<BusinessHandlerInterface>) Class.forName((String) handler);
					if (clz.isAssignableFrom(BusinessHandlerInterface.class)) {
						logger.error("{} is not subClass of {} .",clz,BusinessHandlerInterface.class);
						continue;
					}
					bizHandlers.add(clz);
				} catch (Exception e) {
					logger.error("业务处理类加载失败。", e);
				}

			}
		}

	}

}
