package com.zx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.zx.sms.config.ConfigFileUtil;
import com.zx.sms.connect.manager.CMPPEndpointManager;
import com.zx.sms.connect.manager.EndpointEntity;

/**
 * @author Lihuanghe(18852780@qq.com)
 *
 */
public class App {
	private static Logger logger ;
	public static void main(String[] args) throws Exception {
		String logconf = System.getProperty("logback.configurationFile");
		if(StringUtils.isBlank(logconf)){
			System.setProperty("logback.configurationFile","logback.xml");
		}
		logger = LoggerFactory.getLogger(App.class);
		new App().start("", args);
	}

	public void start(String name, String[] args) throws Exception {
		Options options = new Options();
		options.addOption("h", false, "print help for the command");
		options.addOption("conf", true, "config file path");
		options.addOption(OptionBuilder.withLongOpt("ch").withArgName("channelIds, .../all").hasArgs().withValueSeparator(',')
				.withDescription("start channel with id").create());

		CommandLine cli = null;
		try {
			cli = new PosixParser().parse(options, args);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			return;
		}

		if (cli.hasOption("h")) {

			System.out.println(name + "-conf configuration.xml -ch all");
			return;
		}

		String configFile = "configuration.xml";
		if (cli.hasOption("conf")) {
			configFile = cli.getOptionValue("conf");
		}
		ConfigFileUtil.loadconfiguration(configFile);

		HashSet<String> willstart = new HashSet<String>();
		boolean isAll = true;
		if (cli.hasOption("ch")) {
			List<String> optionsValues = Arrays.asList(cli.getOptionValues("ch"));
			if (optionsValues.contains("all")) {
				isAll = true;
			} else {
				isAll = false;
				willstart.addAll(optionsValues);
			}
		}
		loadSpringConfig();

		final CMPPEndpointManager manager = CMPPEndpointManager.INS;

		List list = ConfigFileUtil.loadServerEndpointEntity();//服务终端实体类集合
		manager.addAllEndpointEntity(getaddEndpointEntity(list, willstart, isAll));
		logger.info("load Server complete.");
		list.clear();  
     	list = ConfigFileUtil.loadClientEndpointEntity();
    	manager.addAllEndpointEntity(getaddEndpointEntity(list, willstart, isAll));
		logger.info("load Client complete.");
		manager.openAll();
		logger.debug("Sever Start complete.");
		LockSupport.park();
	}

	private List<EndpointEntity> getaddEndpointEntity(List list, HashSet<String> willstart, boolean isAll) {
		if (list.size() == 0) {
			System.err.println("endpoint is zero,check file client.xml or server.xml in config file.");
			return null;
		}
		List<EndpointEntity> manager = new ArrayList<EndpointEntity>();
		for (Object obj : list) {
			if (obj instanceof EndpointEntity) {
				EndpointEntity entity = (EndpointEntity) obj;
				if (isAll || (entity.isValid() && willstart.contains(entity.getId()))) {
					logger.info("add Endpoint {}",entity);
					manager.add(entity);
				}
			}
		}
		return manager;

	}
	
	private void loadSpringConfig()
	{
		//
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
	}
}
