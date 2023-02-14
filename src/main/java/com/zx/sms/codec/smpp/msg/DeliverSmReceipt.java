package com.zx.sms.codec.smpp.msg;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zx.sms.codec.smpp.SmppInvalidArgumentException;

public class DeliverSmReceipt extends DeliverSm {
	private static final Logger logger = LoggerFactory.getLogger(DeliverSmReceipt.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 4411244865862324858L;
	private String id;
	private String sub;
	private String dlvrd;
	private String submit_date;
	private String done_date;
	private String stat;
	private String err;
	private String text;
	private Map<String ,String> reportKV ;
	
	public String getReportKV(String key) {
		return  nvl(reportKV.get(key));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public String getDlvrd() {
		return dlvrd;
	}

	public void setDlvrd(String dlvrd) {
		this.dlvrd = dlvrd;
	}

	public String getSubmit_date() {
		return submit_date;
	}

	public void setSubmit_date(String submit_date) {
		this.submit_date = submit_date;
	}

	public String getDone_date() {
		return done_date;
	}

	public void setDone_date(String done_date) {
		this.done_date = done_date;
	}

	public String getStat() {
		return stat;
	}

	public void setStat(String stat) {
		this.stat = stat;
	}

	public String getErr() {
		return err;
	}

	public void setErr(String err) {
		this.err = err;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public boolean isReport() {
		return true;
	}
	
    public byte getEsmClass() {
        return 0x04;
    }
	
    public void setShortMessage(byte[] value) throws SmppInvalidArgumentException {
    	
    	try {
    		 reportKV = parseReport(value);
        	
			this.id = nvl(reportKV.get("id"));
			
			this.sub = nvl(reportKV.get("sub"));
			
			this.dlvrd = nvl(reportKV.get("dlvrd"));
			
			this.submit_date = nvl(reportKV.get("submitdate"));
			
			this.done_date = nvl(reportKV.get("donedate"));
			
			this.stat =nvl(reportKV.get("stat"));
			
			this.err =nvl( reportKV.get("err"));
			
			this.text = nvl(reportKV.get("text"));
			
		} catch (Exception e) {
			logger.error(new String(value),e);
		}
    	
    	super.setShortMessage(value);
    }
    
	//不能修改shortMessage字段
	public byte[] getShortMessage() {
			try {
				byte[] shortMessage =  buildContent().getBytes("ISO-8859-1");
				return shortMessage;
			} catch (Exception e) {
				logger.error("",e);
			}
		return new byte[0];
    }
	
	private String buildContent() {
		StringBuffer sb = new StringBuffer();
		sb.append("id:").append(id);
		
		if(StringUtils.isNotBlank(sub))
			sb.append(" sub:").append(sub);
		
		if(StringUtils.isNotBlank(dlvrd))
			sb.append(" dlvrd:").append(dlvrd);
		
		if(StringUtils.isNotBlank(submit_date))
			sb.append(" submit date:").append(submit_date);
		
		if(StringUtils.isNotBlank(done_date))
			sb.append(" done date:").append(done_date);
		
		if(StringUtils.isNotBlank(stat))
			sb.append(" stat:").append(stat);
		
		if(StringUtils.isNotBlank(err))
			sb.append(" err:").append(err);

		if(StringUtils.isNotBlank(text))
			sb.append(" text:").append(text);
		return sb.toString();
	}
    
    private Map<String ,String> parseReport(byte[] value){
    	Map<String ,String> kv = new HashMap<String ,String>();
    	boolean parseKeyName = true;
    	StringBuffer temp = new StringBuffer();
    	String keyName = "" ;
    	String keyValue = "";
    	for(byte c : value) {
    		
    		if(parseKeyName) {
    			if(' ' == (char)c) {
    				//KeyName里的空格忽略
    				continue;
    			}
    			else if(':' ==  (char)c) {
    				//KeyName 结束
    				keyName = temp.toString();
    				temp.setLength(0);
    				//开始解析数据值
    				parseKeyName = false;
    			}
    			else {
    				temp.append((char)c);
    			}
    		}else {
    			if(' ' == (char)c) {
    				//解析keyValue结束
    				keyValue = temp.toString();
    				temp.setLength(0);
    				//开始解析keyName
    				parseKeyName = true;
    				kv.put(keyName.toLowerCase(), keyValue);
    				keyName = "";
    				keyValue = "";
    			}
    			else {
    				temp.append((char)c);
    			}
    		}
    	}
    	if( temp.length() > 0 ) {
        	if(!parseKeyName  ) {
        		kv.put(keyName.toLowerCase(), temp.toString());
        	}else {
        		kv.put(temp.toString(), "");
        	}
    	}
    		
    	return kv;
    	
    }
    
    private String nvl(String k) {
    	return k == null?"":k;
    }
    
	public String getMsgContent() {
		return buildContent();
	}
}
