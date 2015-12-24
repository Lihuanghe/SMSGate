# CMPPGate
中移短信cmpp协议netty实现编解码

这是一个在netty5框架下实现的cmpp3.0短信协议解析及网关端口管理。
代码copy了 `huzorro@gmail.com` 基于netty3.7的cmpp协议解析 [huzorro@gmail.com 的代码 ](https://github.com/huzorro/netty3ext)

##性能测试
在48core，128G内存的物理服务器上测试协议解析效率：35K条/s, cpu使用率25%. 

## Build
执行mvn package . 建设使用jdk1.7.

## 增加了业务处理API
业务层实现接口：BusinessHandlerInterface，或者继承AbstractBusinessHandler抽象类实现业务即可。 连接保活，消息重发，消息持久化，连接鉴权都已封装，不须要业务层再实现。

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
这是一个netty的ChannelHandler实现。集中负责每个连接上CMPP消息的存储，短信重发，流量窗口控制，过期短信的处理

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

## Api使用举例

```

public class TestCMPPEndPoint {
	private static final Logger logger = LoggerFactory.getLogger(TestCMPPEndPoint.class);

	@Test
	public void testCMPPEndpoint() throws Exception {
	
		final CMPPEndpointManager manager = CMPPEndpointManager.INS;

		// 注意下面所有Entity的Id字段是不允许重复的，Id标识整个JVM唯一的一个网关端口
		// 创建一个CMPP的服务端，模拟一个短信网关
		CMPPServerEndpointEntity server = new CMPPServerEndpointEntity();
		server.setId("server");
		server.setHost("127.0.0.1");
		server.setPort(7891);
		server.setValid(true);
		
		//给这个网关增加一个允许接入的账号
		CMPPServerChildEndpointEntity child = new CMPPServerChildEndpointEntity();
		child.setId("child");
		child.setChartset(Charset.forName("utf-8"));
		child.setGroupName("test");
		child.setUserName("901782");
		child.setPassword("ICP");
		child.setValid(true);
		child.setWindows((short)16);
		child.setVersion((short)48);
		child.setMaxChannels((short)20);
		child.setRetryWaitTimeSec((short)100);
		child.setMaxRetryCnt((short)3);
		
		//给这个账号添加一个业务处理: SessionConnectedHandler 。当client连接完成,用户名密码正确后，立即给Client发送200000条短信
		List<BusinessHandlerInterface> serverhandlers = new ArrayList<BusinessHandlerInterface>();
		serverhandlers.add(new SessionConnectedHandler());
		child.setBusinessHandlerSet(serverhandlers);
		server.addchild(child);
		
		//把Server加入到管理器
		manager.addEndpointEntity(server);
	
	
		//模拟创建一个Client,要去连接上面的网关，使用上面账号密码
		CMPPClientEndpointEntity client = new CMPPClientEndpointEntity();
		client.setId("client");
		client.setHost("127.0.0.1");
		client.setPort(7891);
		client.setChartset(Charset.forName("utf-8"));
		client.setGroupName("test");
		client.setUserName("901782");
		client.setPassword("ICP");
		client.setReadLimit(0);
		child.setWriteLimit(100);
		client.setWindows((short)16);
		client.setVersion((short)48);
		client.setRetryWaitTimeSec((short)100);
		
		//这里是给Client增加一个处理业务:MessageReceiveHandler ，统计接收到的短信条数和速度，并打印到控制台。
		List<BusinessHandlerInterface> clienthandlers = new ArrayList<BusinessHandlerInterface>();
		clienthandlers.add(new MessageReceiveHandler());
		client.setBusinessHandlerSet(clienthandlers);
		
		//把Client加入到管理器
		manager.addEndpointEntity(client);
		
		//管理器打开端口
		manager.openAll();
		
		//主线程挂起，在控制台看实时打印的Log
		Thread.sleep(300000);
		
		//关闭所有连接和端口
		CMPPEndpointManager.INS.close();
	}
}

```


















