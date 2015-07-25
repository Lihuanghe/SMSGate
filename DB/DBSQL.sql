--创建服务器记录表
create table Servers
(
	s_id		int primary key ,
--服务器id
	s_no		varchar(30) not null,
--用户描述
	s_desc		varchar(30) not null,
--是否有效连接
	s_isvalid	tinyint(1)  not null,
--主机ip
	s_host		varchar(30) not null,
--端口
	s_port		int (10) not null
);

--创建终端用户记录表
create table	ClientEndPoints
(
	e_id				int primary key,
--终端用户id
	c_id			varchar(30) not null,
--终端用户描述
	c_desc 			varchar(30) not null,
--上行、下行（0、1、2）
	c_type 				int(10) not null,
--端口是否可用（true、false）
	c_isvalid		tinyint(1)  not null,
--省份归属
	c_group 		varchar(30) not null,
--主机ip
	c_host			varchar(20) not null,
--端口
	c_port				int(10) not null, 
--用户名
	c_userId 		varchar(30) not null
--用户密码
	c_passwd 		varchar(30) not null,
--协议版本
	c_version 			int(10) not null,
--空闲时间
	c_idleTime 			int(10) not null,
--生命周期
	c_lifeTime 			int(10) not null,
--最大从发次数
	c_maxRetry 			int(10) not null,
--重发等待时间
	c_retryWaitTime 	int(10) not null, 
--最大通道连接数
	c_maxChannel 		int(10) not null,
	c_window 			int(10) not null,
--字符编码
	c_charset		 varchar(30)not null,
	s_id				int (10)not null
	
) ;
--创建handle处理集合表
create table Handlers
(
	id 			int primary key ,
--终端用户
	e_id		int (10)not null,
	handle 	varchar(30) not null
);





