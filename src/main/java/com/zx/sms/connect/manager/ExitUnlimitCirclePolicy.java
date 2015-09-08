package com.zx.sms.connect.manager;

import io.netty.util.concurrent.Future;

public interface ExitUnlimitCirclePolicy {
	boolean notOver(Future future);
}
