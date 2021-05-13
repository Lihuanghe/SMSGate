package com.zx.sms.codec.smpp.msg;

import java.io.UnsupportedEncodingException;

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

	//不能修改shortMessage字段
	public byte[] getShortMessage() {
		StringBuffer sb = new StringBuffer();
		sb.append("id:").append(id);
		sb.append(" sub:").append(sub);
		sb.append(" dlvrd:").append(dlvrd);
		sb.append(" submit date:").append(submit_date);
		sb.append(" done date:").append(done_date);
		sb.append(" stat:").append(stat);
		sb.append(" err:").append(err);
		sb.append(" text:").append(text);
		try {
			byte[] shortMessage =  sb.toString().getBytes("ISO-8859-1");
			super.setShortMessage(shortMessage);
			return shortMessage;
		} catch (Exception e) {
			logger.error("",e);
		}
		return null;
    }
	
    public void setShortMessage(byte[] value) throws SmppInvalidArgumentException {
    	String txt= "";
    	try {
    		txt = new String(value,"ISO-8859-1");
        	String[] c = txt.split(" ");
        	
        	String[] arr_id = c[0].split(":");
			this.id = arr_id.length>1?arr_id[1]:"";
			
			String[] arr_sub = c[1].split(":");
			this.sub = arr_sub.length>1?arr_sub[1]:"";
			
			String[] arr_dlvrd = c[2].split(":");
			this.dlvrd = arr_dlvrd.length>1?arr_dlvrd[1]:"";
			
			String[] arr_submit_date = c[4].split(":");
			this.submit_date = arr_submit_date.length>1?arr_submit_date[1]:"";
			
			String[] arr_done_date = c[6].split(":");
			this.done_date = arr_done_date.length>1?arr_done_date[1]:"";
			
			String[] arr_stat = c[7].split(":");
			this.stat =arr_stat.length>1?arr_stat[1]:"";
			
			String[] arr_err = c[8].split(":");
			this.err =arr_err.length>1?arr_err[1]:"";
			
			String[] arr_text = c[9].split(":");
			this.text = arr_text.length>1?arr_text[1]:"";
			
		} catch (Exception e) {
			logger.error(txt,e);
		}
    	
    	super.setShortMessage(value);
    }
    
}
