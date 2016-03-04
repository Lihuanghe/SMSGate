package org.marre.sms.transport.ucp;

/**
 * @author Lorenz Barth
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class UCPSeries60 extends UcpMsg
{
    public static final byte OP_OPEN_SESSION = 60;

    protected static final int FIELD_OADC = 0;
    protected static final int FIELD_OTON = 1;
    protected static final int FIELD_ONPI = 2;
    protected static final int FIELD_STYP = 3;
    protected static final int FIELD_PWD = 4;
    protected static final int FIELD_NPWD = 5;
    protected static final int FIELD_VERS = 6;
    protected static final int FIELD_LADC = 7;
    protected static final int FIELD_LTON = 8;
    protected static final int FIELD_LNPI = 9;
    protected static final int FIELD_OPID = 10;
    protected static final int FIELD_RES1 = 11;

    /**
     * Constructor for UCPSeries60.
     * 
     * @param operation
     */
    public UCPSeries60(byte operation)
    {
        super(12);
        setOR('O');
        setOT(operation);
    }
}
