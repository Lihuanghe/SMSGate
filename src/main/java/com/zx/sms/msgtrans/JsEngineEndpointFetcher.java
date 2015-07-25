package com.zx.sms.msgtrans;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.connect.manager.CMPPEndpointManager;
import com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity;

public class JsEngineEndpointFetcher implements EndpointFetcher<CMPPEndpointEntity> {
	private static final Logger logger = LoggerFactory.getLogger(JsEngineEndpointFetcher.class);
	private static final ScriptEngineManager manager = new ScriptEngineManager();
	private static final ScriptEngine engine = manager.getEngineByName("js");
	private static final Compilable compileable = (Compilable) engine;
	private static final String defaultName = "default";

	private static final ConcurrentHashMap<String, CompiledScript> jsMap = new ConcurrentHashMap<String, CompiledScript>();

	@Override
	public void fetch(TransParamater parameter, List<CMPPEndpointEntity> out) {
		String scriptName = parameter.getGateID();
		CompiledScript script = null;
		if (StringUtils.isNotBlank(scriptName)) {
			script = jsMap.get(scriptName);
		}
		if (script == null) {
			scriptName = defaultName;
			script = jsMap.get(scriptName);
		}
		Bindings bindings = engine.createBindings();
		bindings.put("param", parameter);
		bindings.put("log", logger);
		try {
			script.eval(bindings);
		} catch (ScriptException e) {
			logger.error("eval script {} error", scriptName);
		}
		String dstId = (String) bindings.get("gate");
		if (StringUtils.isNotBlank(dstId)) {
			out.add((CMPPEndpointEntity) CMPPEndpointManager.INS.getEndpointEntity(dstId));
		}
	}

	/**
	 * JS code : if(param.getIp() == "127.0.0.1") {gate = "987833";}
	 * if(param.getIp() == "127.0.0.1" && param.username =="testUser") {gate =
	 * "987133";}
	 * 
	 **/
	public void addScript(String keyName, String code) {
		try {
			jsMap.put(keyName, compileable.compile(code));
		} catch (ScriptException e) {
			logger.error("Js code compile Error: {}", keyName);
		}
	}

}
