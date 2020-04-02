package com.zx.sms.codec.smpp.msg;

import java.io.UnsupportedEncodingException;

import com.zx.sms.codec.smpp.SmppInvalidArgumentException;

public class DeliverSmReceipt extends DeliverSm {
	
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
			return sb.toString().getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			
		}
		return null;
    }
	
    public void setShortMessage(byte[] value) throws SmppInvalidArgumentException {
    	
    	try {
    		String txt = new String(value,"ISO-8859-1");
        	String[] c = txt.split(" ");
			this.id = c[0].split(":")[1];
			this.sub = c[1].split(":")[1];
			this.dlvrd = c[2].split(":")[1];
			this.submit_date = c[4].split(":")[1];
			this.done_date = c[6].split(":")[1];
			this.stat = c[7].split(":")[1];
			this.err = c[8].split(":")[1];
			this.text = c[9].split(":")[1];
		} catch (Exception e) {
		}
    	super.setShortMessage(value);
    }
    
}
