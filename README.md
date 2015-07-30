# CMPPGate
中移短信cmpp协议netty实现编解码

这是一个在netty5框架下实现的cmpp3.0短信协议解析及网关端口管理。
代码copy了 `huzorro@gmail.com` 基于netty3.7的cmpp协议解析 [huzorro@gmail.com 的代码 ](https://github.com/huzorro/netty3ext)

##性能测试
在48core，128G内存的物理服务器上测试协议解析效率：35K条/s, cpu使用率25%. 

程序启动：`java -jar sms-0.0.1-SNAPSHOT.jar -conf configuration.xml`

## Build
执行mvn package . 建设使用jdk1.7.

## 启动
打包后，执行 java -jar ${JAR-NAME} -conf ${CONFIG-FILE-NAME}

## 增加了业务处理API
业务层实现接口：BusinessHandlerInterface，或者继承AbstractBusinessHandler抽象类实现业务即可。 连接保活，消息重发，消息持久化，连接鉴权都已封装，不须要业务层再实现。

