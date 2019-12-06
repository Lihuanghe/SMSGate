# 技术问题请加QQ群
![qq 20180420170449](https://user-images.githubusercontent.com/7598107/39042453-6fcfaac0-44bd-11e8-94bf-101c8dad8400.png)

群名称：cmppGate短信
<br/>群   号：770738500

# How To Use

```xml
<dependency>
  <groupId>com.chinamobile.cmos</groupId>
  <artifactId>sms-core</artifactId>
  <version>2.1.10</version>
</dependency>
```

# 常见问题

- `没看懂如何发送短信？`

  短信协议是tcp长连接，类似数据库连接，如jdbc-connection. 所以发送短信前必须要先有一个短信连接。因此你需要在程序启动时建立短信连接。参考demo里的client，调用manager.openEntity()方法，,调用manager.startConnectionCheckTask()开启断线重连。
  然后就像调用其它库一样，在需要发送短信的地方，new 一个对应的Message,调用
  
  Future f = ChannelUtil.syncWriteLongMsgToEntity([clientEntityId],message)方法发送，`要判断f是否为Null，为Null表示发送失败`。

- `如何发送长短信？`

  smsgate默认已经处理好长短信了，就像发送普通短信一样。
  
- `如何发送闪信?`

```java
  CmppSubmitRequestMessage msg = CmppSubmitRequestMessage.create(phone, "10690021", "");
  msg.setMsgContent(new SmsTextMessage("你好，我是闪信！",SmsAlphabet.UCS2,SmsMsgClass.CLASS_0));  //class0是闪信
```

- `如何接收短信？`

  如果你了解netty的handler,那么请看AbstractBusinessHandler的源码即可，这是一个netty的handler.
  
  如果你不了解netty, 你只需知道：
  
  当连接刚刚建立时，smsgate会自动调用handler里的userEventTriggered方法；
  
  当对方发送任一消息给你时，smsgate会自动调用handler里的channelRead方法；
  
  当连接关闭时，smsgate会自动调用handler里的channelInactive方法

- `如何不改源码，实现修改框架默认的handler`

  比如SGIP协议要设置NodeId;你需要这样做：
  
  1、写一个扩展的SgipClientEndpointEntity子类，如：MySgipClientEndpointEntity，重写buildConnector()方法
  
  2、再写一个SgipClientEndpointConnector子类，如：MySgipClientEndpointConnector,重写doinitPipeLine()方法
  
  3、最后再写一个SgipSessionLoginManager子类，如：MySgipSessionLoginManager，重写doLogin方法，实现登陆方法的重写，在方法里创建自己定义的实现。
  
  4、最后在openEntity通道里，new MySgipClientEndpointEntity就可以了

# 新手指引

- 先看doc目录下的`CMPP接口协议V3.0.0.doc`文档 (看不懂的到群里咨询)
- 再看readme里的说明  (看不懂的到群里咨询)
- 导入工程后，运行测试demo: TestCMPPEndPoint，学会配置账号密码等参数
- 由于代码是基于netty网络框架，您有必要先有一些Netty的基础


# 开发短信网关的常见问题

- `长短信拆分合并原理?`

短信支持长短信功能是在手机终端实现的，即：手机陆续收到多个短信片断后，会根据短信PDU里的前6个byte信息进行合并。最终在手机上显示为一条短信，但实际却是接收了多条短信（因此收多条的费用）。

因此，长短信在发送时要进行拆分。在开发短信网关时，由于要对短信内容进行校验，拼签名等处理，因此在接收到短信分片后，要进行合并成一条处理，之后发送时再拆分为多条（当然有可能始终只收到一个片断，造成永远无法合并成一条完整的短信）。

短信内容(PDU)字段的前6字节是长短信的协议头（其余内容才是短信文本），前3个字节固定是 `0x050003`，后3个字节用来做长短信合并的依据（类似IP包的分片）

`1字节 包ID[最大255], `<br/>
`1字节 包总分片数`<br/>
`1字节 分片序号`

如：45，03，01表示ID为45的第1个分片，总共3个分片。45，03，02表示ID为45的第2个分片，总共3个分片。            当手机收到完整的3个分片后，手机才进行合并显示。

- `如何关联状态报告和submit消息?`

运宽商网关响应`submitRequest`消息时，你会收到`submitResponse`消息。在`response`里会有`msgId`。通过这个`msgId`跟之后收到的状态报告(`reportMessage`)里的`msgId`关联。

- `如何记录每个消息的发送日志，并向我的客户发送状态报告?`

当接收到来源客户的`submitRequest`消息后，要回复`response`,注意此时要记录回复`response`时所使用的`msgId`，即你回复给来源客户的`msgId`。

将消息转发给通道后，当接收到`submitResponse后`，通过`response.getRequest()`获取对应的`request` 。注意此时有两个`msgID`，一个是通道给你的`msgID`，一个是你给来源客户的。在数据库里记录相关信息（至少包括消息来源客户，消息出去的通道，两个`msgId`,消息详情）。之后在接收到状态报告后，通过通道给你的`msgId`更新消息回执状态，并根据来源客户将回执回传给客户，注意回传`reportMessage`里的`msgId`要使用你给客户回复`response`时用的`msgId`.


# CMPPGate , SMPPGate , SGIPGate, SMGPGate
中移短信cmpp协议/smpp协议 netty实现编解码

这是一个在netty4框架下实现的cmpp3.0/cmpp2.0短信协议解析及网关端口管理。
代码copy了 `huzorro@gmail.com` 基于netty3.7的cmpp协议解析 [huzorro@gmail.com 的代码 ](https://github.com/huzorro/netty3ext)

目前已支持发送和解析`长文本短信拆分合并`，`WapPush短信`，以及`彩信通知`类型的短信。可以实现对彩信或者wap-push短信的拦截和加工处理。wap短信的解析使用 [smsj] (https://github.com/marre/smsj)的短信库

cmpp协议已经跟华为，东软，亚信的短信网关都做过联调测试，兼容了不同厂家的错误和异常，如果跟网关通信出错，可以打开trace日志查看二进制数据。

因要与短信中心对接，新增了对SMPP协议的支持。

SMPP的协议解析代码是从  [Twitter-SMPP 的代码](https://github.com/fizzed/cloudhopper-smpp) copy过来的。

新增对sgip协议(联通短信协议)的支持

sgip的协议解析代码是从 [huzorro@gmail.com 的代码 ](https://github.com/huzorro/sgipsgw) copy过来后改造的。

新增对smgp协议(电信短信协议)的支持

smgp的协议解析代码是从 [SMS-China 的代码 ](https://github.com/clonalman/SMS-China) copy过来后改造的。

支持发送彩信通知，WAP短信以及闪信(Flash Message):

<DIV>
<img src="./doc/QQ20180518143313.jpg" width="25%" height="25%">
<DIV>

## 性能测试 
在48core，128G内存的物理服务器上测试协议解析效率：35K条/s, cpu使用率25%. 

## Build
执行mvn package . jdk1.6以上. 

## 增加了业务处理API
业务层实现接口：BusinessHandlerInterface，或者继承AbstractBusinessHandler抽象类实现业务即可。 连接保活，消息重发，消息持久化，连接鉴权都已封装，不须要业务层再实现。

## 如何实现自己的Handler,比如按短短信计费
参考 CMPPChargingDemoTest 里的扩展位置

# 实体类说明

## CMPP的连接端口

`com.zx.sms.connect.manager.cmpp.CMPPEndpointEntity`
表示一个Tcp连接的发起端，或者接收端。用来记录连接的IP.port,以及CMPP协议的用户名，密码，业务处理的ChannelHandler集合等其它端口参数。包含三个字类：

1. com.zx.sms.connect.manager.cmpp.CMPPServerEndpointEntity
服务监听端口，包含一个List<CMPPServerChildEndpointEntity>属性。 一个服务端口包含多个CMPPServerChildEndpointEntity端口

2. com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointEntity
服务接收端口，包含CMPP连接用户名，密码，以及协议版本等信息

3. com.zx.sms.connect.manager.cmpp.CMPPClientEndpointEntity
客户端端口，包含CMPP连接用户名，密码，以及协议版本，以及服务端IP.port. 用于连接服务端

## 端口连接器接口
`com.zx.sms.connect.manager.EndpointConnector`
负责一个端口的打开，关闭，查看当前连接数，新增连接，移除连接。每个端口的实体类都对应一个EndpointConnector.当CMPP连接建立完成，将连接加入连接器管理，并给pipeLine上挂载业务处理的ChannelHandler.

1. com.zx.sms.connect.manager.cmpp.CMPPServerEndpointConnector
这个类的open()调用netty的ServerBootstrap.bind()开一个服务监听

2. com.zx.sms.connect.manager.cmpp.CMPPServerChildEndpointConnector
用来收集CMPPServerChildEndpointEntity端口下的所有连接。它的open()方法为空.

3. com.zx.sms.connect.manager.cmpp.CMPPClientEndpointConnector
这个类open()调用netty的Bootstrap.connect()开始一个TCP连接

## 端口管理器
`com.zx.sms.connect.manager.EndpointManager`
该类是单例模式，管理所有端口，并负责所有端口的打开，关闭，以及端口信息保存，以及连接断线重连。

## CMPP协议的连接登陆管理
`com.zx.sms.session.cmpp.SessionLoginManager`
这是一个netty的ChannelHandler实现，主要负责CMPP连接的建立。当CMPP连接建立完成后，会调用EndpointConnector.addChannel(channel)方法，把连接加入连接器管理，连接器负责给channel的pipeline上挂载业务处理的Handler,最后触发
SessionState.Connect事件，通知业务处理Handler连接已建立成功。

## CMPP的连接状态管理器
`com.zx.sms.session.cmpp.SessionStateManager`
这是一个netty的ChannelHandler实现。负责每个连接上CMPP消息的存储，短信重发，流量窗口控制，过期短信的处理

## CMPP协议解析器
CMPP20MessageCodecAggregator [2.0协议]
CMPPMessageCodecAggregator [这是3.0协议]
聚合了CMPP主要消息协议的解析，编码，长短信拆分，合并处理。

## 短信持久化存储实现 StoredMapFactory 
使用BDB的StoreMap实现消息持久化，防止系统意外丢失短信。

## 程序启动处理流程

1. 程序启动类 new 一个CMPPEndpointEntity的实体类并设置IP,port,用户名，密码，业务处理的Handler等参数,
2. 程序启动类 调用EndpointManager.addEndpointEntity(endpoint)方法，将端口加入管理器
3. 程序启动类 调用EndpointManager.openAll()或者EndpointManager.openEndpoint()方法打开端口。
4. EndpointManager会调用EndpointEntity.buildConnector()创建一个端口连接器，并调用EndpointConnector.open()方法打开端口。
5. 如果是CMPPClientEndpointEntity的话，就会向服务器发起TCP连接请求，如果是CMPPServerEndpointEntity则会在本机开启一个服务端口等客户端连接。
6. TCP连接建立完成后。netty会调用EndpointConnector.initPipeLine()方法初始化PipeLine，把CMPP协议解析器，SessionLoginManager加到PipeLine里去，然后netty触发ChannelActive事件。
7. 在SessionLoginManager类里，客户端收到ChannelActive事件后会发送一个CMPPConnnect消息，请求建立CMPP连接.
8. 同样在SessionLoginManager.channelRead()方法里,服务端会收到CMPPConnnect消息，开始对用户名，密码进行鉴权，并给客户端鉴权结果。
9. 鉴权通过后，SessionLoginManager调用EndpointConnector.addChannel(channel)方法，把channel加入ArrayList,并给pipeLine上挂载SessionStateManager和业务处理的ChannelHandler。
10. EndpointConnector.addChannel(channel)完成后，SessionLoginManager调用ctx.fireUserEventTriggered()方法，触发	SessionState.Connect事件。

以上CMPP连接建立完成。

11. 业务处理类收到SessionState.Connect事件，开始业务处理，如下发短信。
12. SessionStateManager会拦截所有read()和write()的消息，进行消息持久化，消息重发，流量控制。

## 增加同步调用api
smsgate自开发以来，一直使用netty的异步发送消息，但实际使用场景中同步发送消息的更方便，或者能方便的取到response。因此增加一个同步调用的api。即：发送消息后等接收到对应的响应后才完成。
使用方法如下：

```java

	//因为长短信要拆分，因此返回一个promiseList.每个拆分后的短信对应一个promise
	List<Promise> futures = ChannelUtil.syncWriteLongMsgToEntity("client",submitmessage);
	for(Promise  future: futures){
		//调用sync()方法，阻塞线程。等待接收response
		future.sync(); 
		//接收成功，如果失败可以获取失败原因，比如遇到连接突然中断错误等等
		if(future.isSuccess()){
			//打印收到的response消息
			logger.info("response:{}",future.get());
		}else{
			打印错误原因
			logger.error("response:{}",future.cause());
		}
	}

	//或者不阻塞进程，不调用sync()方法。
	List<Promise> promises = ChannelUtil.syncWriteLongMsgToEntity("client",submitmessage);
	for(Promise  promise: promises){
		//接收到response后回调Listener方法
		promise.addListener(new GenericFutureListener() {
			@Override
			public void operationComplete(Future future) throws Exception {
				//接收成功，如果失败可以获取失败原因，比如遇到连接突然中断错误等等
				if(future.isSuccess()){
					//打印收到的response消息
					logger.info("response:{}",future.get());
				}else{
					打印错误原因
					logger.error("response:{}",future.cause());
				}
			}
		});
	}

```

## CMPP Api使用举例

```java
public class TestCMPPEndPoint {
	private static final Logger logger = LoggerFactory.getLogger(TestCMPPEndPoint.class);

	@Test
	public void testCMPPEndpoint() throws Exception {
		ResourceLeakDetector.setLevel(Level.ADVANCED);
		final EndpointManager manager = EndpointManager.INS;

		CMPPServerEndpointEntity server = new CMPPServerEndpointEntity();
		server.setId("server");
		server.setHost("127.0.0.1");
		server.setPort(7890);
		server.setValid(true);
		//使用ssl加密数据流
		server.setUseSSL(false);

		CMPPServerChildEndpointEntity child = new CMPPServerChildEndpointEntity();
		child.setId("child");
		child.setChartset(Charset.forName("utf-8"));
		child.setGroupName("test");
		child.setUserName("901783");
		child.setPassword("ICP001");

		child.setValid(true);
		child.setVersion((short)0x30);

		child.setMaxChannels((short)4);
		child.setRetryWaitTimeSec((short)30);
		child.setMaxRetryCnt((short)3);
		child.setReSendFailMsg(true);
//		child.setWriteLimit(200);
//		child.setReadLimit(200);
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new CMPPMessageReceiveHandler()); //在这个handler里接收短信
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		
		manager.addEndpointEntity(server);
	
		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId("client");
		client.setHost("127.0.0.1");
//		client.setLocalhost("127.0.0.1");
//		client.setLocalport(65521);
		client.setPort(7890);
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("901783");
		client.setPassword("ICP001");

		client.setMaxChannels((short)10);
		client.setVersion((short)0x30);
		client.setRetryWaitTimeSec((short)30);
		client.setUseSSL(false);
//		client.setWriteLimit(100);
		client.setReSendFailMsg(true);
		client.setSupportLongmsg(SupportLongMessage.BOTH);
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add( new CMPPSessionConnectedHandler(10000));  //在这个handler里发送短信
		client.setBusinessHandlerSet(clienthandlers);
		
		manager.addEndpointEntity(client);
		
		manager.openEndpoint(server);
		
		Thread.sleep(1000);
		for(int i=0;i<=child.getMaxChannels()+1;i++)
			manager.openEndpoint(client);

        System.out.println("start.....");
        
//		Thread.sleep(300000);
        LockSupport.park();
		EndpointManager.INS.close();
	}
}
```

## SMPP Api使用举例

```java

public class TestSMPPEndPoint {
	private static final Logger logger = LoggerFactory.getLogger(TestSMPPEndPoint.class);

	@Test
	public void testSMPPEndpoint() throws Exception {
	
		final EndpointManager manager = EndpointManager.INS;

		SMPPServerEndpointEntity server = new SMPPServerEndpointEntity();
		server.setId("smppserver");
		server.setHost("127.0.0.1");
		server.setPort(2776);
		server.setValid(true);
		//使用ssl加密数据流
		server.setUseSSL(false);
		
		SMPPServerChildEndpointEntity child = new SMPPServerChildEndpointEntity();
		child.setId("smppchild");
		child.setSystemId("901782");
		child.setPassword("ICP");

		child.setValid(true);
		child.setChannelType(ChannelType.DUPLEX);
		child.setMaxChannels((short)3);
		child.setRetryWaitTimeSec((short)30);
		child.setMaxRetryCnt((short)3);
		child.setReSendFailMsg(true);
		child.setIdleTimeSec((short)15);
//		child.setWriteLimit(200);
//		child.setReadLimit(200);
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new SMPPSessionConnectedHandler(10000));   
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		
		SMPPClientEndpointEntity client = new SMPPClientEndpointEntity();
		client.setId("smppclient");
		client.setHost("127.0.0.1");
		client.setPort(2776);
		client.setSystemId("901782");
		client.setPassword("ICP");
		client.setChannelType(ChannelType.DUPLEX);

		client.setMaxChannels((short)12);
		client.setRetryWaitTimeSec((short)100);
		client.setUseSSL(false);
		client.setReSendFailMsg(true);
//		client.setWriteLimit(200);
//		client.setReadLimit(200);
		client.setSupportLongmsg(SupportLongMessage.SEND);  //接收长短信时不自动合并
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add( new SMPPMessageReceiveHandler()); 
		client.setBusinessHandlerSet(clienthandlers);
		
		
		manager.addEndpointEntity(server);
		manager.addEndpointEntity(client);
		manager.openAll();
		manager.startConnectionCheckTask();
		Thread.sleep(1000);
		for(int i=0;i<child.getMaxChannels();i++)
			manager.openEndpoint(client);
		System.out.println("start.....");
		LockSupport.park();
		EndpointManager.INS.close();
	}
}
	
```

## SGIP Api使用举例

```java
public class TestSgipEndPoint {
	private static final Logger logger = LoggerFactory.getLogger(TestSgipEndPoint.class);

	@Test
	public void testsgipEndpoint() throws Exception {
		ResourceLeakDetector.setLevel(Level.ADVANCED);
		final EndpointManager manager = EndpointManager.INS;

		SgipServerEndpointEntity server = new SgipServerEndpointEntity();
		server.setId("sgipserver");
		server.setHost("127.0.0.1");
		server.setPort(8001);
		server.setValid(true);
		//使用ssl加密数据流
		server.setUseSSL(false);
		
		SgipServerChildEndpointEntity child = new SgipServerChildEndpointEntity();
		child.setId("sgipchild");
		child.setLoginName("333");
		child.setLoginPassowrd("0555");

		child.setValid(true);
		child.setChannelType(ChannelType.DUPLEX);
		child.setMaxChannels((short)3);
		child.setRetryWaitTimeSec((short)30);
		child.setMaxRetryCnt((short)3);
		child.setReSendFailMsg(false);
		child.setIdleTimeSec((short)30);
//		child.setWriteLimit(200);
//		child.setReadLimit(200);
		child.setSupportLongmsg(SupportLongMessage.SEND);  //接收长短信时不自动合并
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		
		serverhandlers.add(new SgipReportRequestMessageHandler());
		serverhandlers.add(new SGIPMessageReceiveHandler());   
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		
		manager.addEndpointEntity(server);
		
		
		SgipClientEndpointEntity client = new SgipClientEndpointEntity();
		client.setId("sgipclient");
		client.setHost("127.0.0.1");
		client.setPort(8001);
		client.setLoginName("333");
		client.setLoginPassowrd("0555");
		client.setChannelType(ChannelType.DUPLEX);

		client.setMaxChannels((short)10);
		client.setRetryWaitTimeSec((short)100);
		client.setUseSSL(false);
		client.setReSendFailMsg(true);
//		client.setWriteLimit(200);
//		client.setReadLimit(200);
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add(new SGIPSessionConnectedHandler(10000));
		client.setBusinessHandlerSet(clienthandlers);
		manager.addEndpointEntity(client);
		manager.openAll();
		Thread.sleep(1000);
		for(int i=0;i<child.getMaxChannels();i++)
			manager.openEndpoint(client);
		System.out.println("start.....");
      
        LockSupport.park();

		EndpointManager.INS.close();
	}
}
```

## Demo 执行日志

```

11:31:52.842 [workGroup2] INFO  c.z.s.c.m.AbstractEndpointConnector - handlers is not shareable . clone it success. com.zx.sms.codec.smpp.SMPP2CMPPBusinessHandler@1d7059df
11:31:52.852 [workGroup1] INFO  c.z.s.c.m.AbstractEndpointConnector - handlers is not shareable . clone it success. com.zx.sms.codec.smpp.SMPP2CMPPBusinessHandler@75e134be
11:31:52.852 [workGroup1] INFO  c.z.s.c.m.AbstractEndpointConnector - handlers is not shareable . clone it success. com.zx.sms.handler.api.gate.SessionConnectedHandler@aa80b58
11:31:52.869 [workGroup1] INFO  c.z.s.s.AbstractSessionLoginManager - login in success on channel [id: 0xfdc7b81e, L:/127.0.0.1:11481 - R:/127.0.0.1:2776]
11:31:52.867 [workGroup2] INFO  c.z.s.s.AbstractSessionLoginManager - login in success on channel [id: 0x1fba3767, L:/127.0.0.1:2776 - R:/127.0.0.1:11481]
11:31:53.863 [busiWork-3] INFO  c.z.s.h.a.s.MessageReceiveHandler - Totle Receive Msg Num:343,   speed : 343/s
11:31:54.872 [busiWork-1] INFO  c.z.s.h.a.s.MessageReceiveHandler - Totle Receive Msg Num:1381,   speed : 1038/s
11:31:55.873 [busiWork-8] INFO  c.z.s.h.a.s.MessageReceiveHandler - Totle Receive Msg Num:2704,   speed : 1323/s
11:31:56.875 [busiWork-2] INFO  c.z.s.h.a.s.MessageReceiveHandler - Totle Receive Msg Num:4010,   speed : 1306/s
11:31:57.880 [busiWork-5] INFO  c.z.s.h.a.s.MessageReceiveHandler - Totle Receive Msg Num:5416,   speed : 1406/s
11:31:58.881 [busiWork-7] INFO  c.z.s.h.a.s.MessageReceiveHandler - Totle Receive Msg Num:7442,   speed : 2026/s
11:31:59.882 [busiWork-8] INFO  c.z.s.h.a.s.MessageReceiveHandler - Totle Receive Msg Num:9581,   speed : 2139/s
11:32:00.883 [busiWork-2] INFO  c.z.s.h.a.s.MessageReceiveHandler - Totle Receive Msg Num:12865,   speed : 3284/s
11:32:01.884 [busiWork-5] INFO  c.z.s.h.a.s.MessageReceiveHandler - Totle Receive Msg Num:15937,   speed : 3072/s
11:32:02.886 [busiWork-5] INFO  c.z.s.h.a.s.MessageReceiveHandler - Totle Receive Msg Num:19489,   speed : 3552/s
11:32:03.887 [busiWork-6] INFO  c.z.s.h.a.s.MessageReceiveHandler - Totle Receive Msg Num:23065,   speed : 3576/s
11:32:04.888 [busiWork-2] INFO  c.z.s.h.a.s.MessageReceiveHandler - Totle Receive Msg Num:26337,   speed : 3272/s

```














