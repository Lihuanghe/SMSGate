package com.zx.sms.connect.manager;

import com.zx.sms.config.PropertiesUtils;

public final class TestConstants {
	public static final Integer Count = Integer.parseInt(PropertiesUtils.getproperties("TestConstants.Count", "100000"));
	public static final Boolean isReSendFailMsg = Boolean.valueOf(System.getProperty("isReSendFailMsg", "false"));
	public static final String testSmsContent = "【中信信用卡】限时9折！您尾号2919信用卡本月账单可将000.78元申请分6期。可于02月20日前回FQ+卡末四位申请，或点 zxcard.cn/GHD 申请（结果实时审批为准），成功后本期仅需还人民币0.00元。如已还款请忽略，回TD退订-【中信信用卡】限时9折！您尾号2919信用卡本月账单可将000.78元申请分6期。可于02月20日前回FQ+卡末四位申请，或点 zxcard.cn/GHD 申请（结果实时审批为准），成功后本期仅需还人民币0.00元。如已还款请忽略，回TD退订";
}
