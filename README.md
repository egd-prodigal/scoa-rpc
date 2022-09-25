# scoa-rpc
scoa rpc，是我心血来潮编写的一套微服务RPC框架，只适用于springboot，基于eureka注册中心，实现以"服务"为核心的RPC架构

## 使用方法
> 初稿，还有无数需要完善的地方，暂未提交中央仓库，以下请参照sample项目

服务提供方添加如下依赖：
```xml
<dependency>
    <groupId>io.github.egd-prodigal</groupId>
    <artifactId>scoa-rpc-provider</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
服务调用方添加如下依赖
```xml
<dependency>
    <groupId>io.github.egd-prodigal</groupId>
    <artifactId>scoa-rpc-consumer</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
服务基于接口，存放在一个独立的模块里，由服务提供方跟调用方分别依赖，示例：
```xml
<dependency>
    <groupId>io.github.egd-prodigal</groupId>
    <artifactId>scoa-rpc-sample-client</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
添加完如上依赖，开始写代码：
在服务调用方的启动类增加 **@EnableScoaRpcConsumer**，并配置扫描的包路径为服务接口的包路径，代码片段如下：
```java
@EnableScoaRpcConsumer(basePackages = "io.github.egd.prodigal.scoa.rpc.client")
public class SampleConsumerStarter {
    ...
}

package io.github.egd.prodigal.scoa.rpc.client;
// 这是服务接口Client
public interface DemoClient {
    ...
}
```

在服务提供方的启动类增加 **@EnableScoaRpcProvider**，并配置扫描的包路径为实现类的路径，代码片段如下：
```java
@EnableScoaRpcProvider(basePackages = "io.github.egd.prodigal.scoa.rpc.sample.provider.provider")
public class SampleProviderStarter {
    ...
}

pacakge io.github.egd.prodigal.scoa.rpc.sample.provider.provider;

@ScoaRpcProvider(version = "1.0.1", group = "sample")
public class DemoClientProvider implements DemoClient {
    ...
}
```

服务调用方于被调用方都增加注解并配置好之后，就可以直接使用了。  
服务调用方在service类里面直接注入client，使用 **@ScoaRpcConsumer** 注解，注解参数配置内容于服务提供方一致，如下：
```java
@ScoaRpcConsumer(version = "1.0.1", group = "sample")
private DemoClient demoClient;
```
服务提供方编写的Provider类也被spring管理，因此可以注入其他service类。  
这样，就可以愉快的进行微服务调用了。  

> 本RPC框架以服务为核心，忽略了应用载体。
