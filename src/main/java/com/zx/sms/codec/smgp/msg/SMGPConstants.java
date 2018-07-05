package com.zx.sms.codec.smgp.msg;


public class SMGPConstants {

	public static final int SMGP_LOGIN = 0x00000001;

	public static final int SMGP_LOGIN_RESP = 0x80000001;

	public static final int SMGP_SUBMIT = 0x00000002;

	public static final int SMGP_SUBMIT_RESP = 0x80000002;

	public static final int SMGP_DELIVER = 0x00000003;

	public static final int SMGP_DELIVER_RESP = 0x80000003;

	public static final int SMGP_ACTIVE_TEST = 0x00000004;

	public static final int SMGP_ACTIVE_TEST_RESP = 0x80000004;

	public static final int SMGP_EXIT_TEST = 0x00000006;

	public static final int SMGP_EXIT_RESP = 0x80000006;
	
	public static final short OPT_TP_PID             =0x0001;
	public static final short OPT_TP_UDHI            =0x0002;
	public static final short OPT_LINK_ID            =0x0003;
	public static final short OPT_CHARGE_USER_TYPE   =0x0004;
	public static final short OPT_CHARGE_TERM_TYPE   =0x0005;
	public static final short OPT_CHARGE_TERM_PSEUDO =0x0006;
	public static final short OPT_DEST_TERM_TYPE     =0x0007;
	public static final short OPT_DEST_TERM_PSEUDO   =0x0008;
	public static final short OPT_PK_TOTAL           =0x0009;
	public static final short OPT_PK_NUMBER          =0x000a;
	public static final short OPT_SUBMIT_MSG_TYPE    =0x000b;
	public static final short OPT_SP_DEAL_RESULT     =0x000c;
	public static final short OPT_SRC_TERM_TYPE      =0x000d;
	public static final short OPT_SRC_TERM_PSEUDO    =0x000e;
	public static final short OPT_NODES_COUNT        =0x000f;
	public static final short OPT_MSG_SRC            =0x0010;
	public static final short OPT_SRC_TYPE           =0x0011;
	public static final short OPT_M_SERVICE_ID       =0x0012;

}