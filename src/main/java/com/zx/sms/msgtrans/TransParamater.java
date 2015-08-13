package com.zx.sms.msgtrans;

import com.zx.sms.connect.manager.EndpointEntity;

/**
 * 转发规则判断使用的参数
 **/
public class TransParamater {
	private String Ip;
	private String Port;
	private String User;
	private String Group;
	private String GateID;
	private EndpointEntity.ChannelType MsgType;
	private String Command;
	private String CommandLength;
	private String MSID;//手机号码
	private String SpCode;//10086,10085
	public String getIp() {
		return Ip;
	}
	public void setIp(String ip) {
		Ip = ip;
	}
	public String getPort() {
		return Port;
	}
	public void setPort(String port) {
		Port = port;
	}
	public String getUser() {
		return User;
	}
	public void setUser(String user) {
		User = user;
	}
	public String getGroup() {
		return Group;
	}
	public void setGroup(String group) {
		Group = group;
	}
	public String getGateID() {
		return GateID;
	}
	public void setGateID(String gateID) {
		GateID = gateID;
	}
	public EndpointEntity.ChannelType getMsgType() {
		return MsgType;
	}
	public void setMsgType(EndpointEntity.ChannelType msgType) {
		MsgType = msgType;
	}
	public String getCommand() {
		return Command;
	}
	public void setCommand(String command) {
		Command = command;
	}
	public String getCommandLength() {
		return CommandLength;
	}
	public void setCommandLength(String commandLength) {
		CommandLength = commandLength;
	}
	public String getMSID() {
		return MSID;
	}
	public void setMSID(String mSID) {
		MSID = mSID;
	}
	public String getSpCode() {
		return SpCode;
	}
	public void setSpCode(String spCode) {
		SpCode = spCode;
	}
	@Override
	public String toString() {
		return "TransParamater [Ip=" + Ip + ", Port=" + Port + ", User=" + User + ", Group=" + Group + ", GateID=" + GateID + ", MsgType=" + MsgType
				+ ", Command=" + Command + ", CommandLength=" + CommandLength + ", MSID=" + MSID + ", SpCode=" + SpCode + "]";
	}
	

}
