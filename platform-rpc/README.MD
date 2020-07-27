RPC

![Image text](./pic/dubbo-relation.jpg)

###图例说明：
```
图中小方块 Protocol, Cluster, Proxy, Service, Container, Registry, Monitor 代表层或模块，蓝色的表示与业务有交互，绿色的表示只对 Dubbo 内部交互。
图中背景方块 Consumer, Provider, Registry, Monitor 代表部署逻辑拓扑节点。
图中蓝色虚线为初始化时调用，红色虚线为运行时异步调用，红色实线为运行时同步调用。
图中只包含 RPC 的层，不包含 Remoting 的层，Remoting 整体都隐含在 Protocol 中。
```
