
# 为什么需要图数据库

随着技术的发展，我们对数据的需求已经不再局限于对数据本身的获取了，我们还需要获取数据与数据间的关系（也就是连接数据）。

简单地说，我们可以说图数据库主要用于存储更多的连接数据（因为图结构相比其他数据结构而言，能保存更多的数据间的关系）。

如果我们使用 RDBMS 数据库来存储更多连接的数据，那么它们不能提供用于遍历大量数据的适当性能。 在这些情况下，Graph Database 提高了应用程序性能。

权限访问控制场景中节点相对有限，但节点之间关系的复杂且海量，相对于关系型数据，使用图方式存储能极大提高检索性能。

**举例：**

银行内部的一套管理合同的线上系统，其中包含角色：销售、财务、法务、管理者，资源类型有页面、合同、菜单，合同属性主要包含 ID、金额、地区、提交人，环境信息包含：时间、IP、地区。

小凡是名销售，可以在任意时段查看自己曾经提交过的合同，但只能在工作日的 9:00 - 17:00 期间提交合同

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=NjIxOTNkMTNjYWJjZDEzODRjMzVmZjVlZWY4NTBjOTBfQ1l0ODhBREliVU9aRGpPUVRKTHZIZ0Q3Y1o2Tzl5Uk5fVG9rZW46Ym94Y25BejQ1WlBxNWhRWWRhOEhVMFZiZnRIXzE3NjYwNDgyMzY6MTc2NjA1MTgzNl9WNA)

## Neo4j 概述

### Neo4j 的特点

* SQL 就像简单的查询语言 Neo4j CQL
* 它遵循属性图数据模型
* 它通过使用 Apache Lucence 支持索引
* 它支持 UNIQUE 约束
* 它包含一个用于执行 CQL 命令的 UI：Neo4j数据浏览器
* 它支持完整的 ACID（原子性，一致性，隔离性和持久性）规则
* 它采用原生图形库与本地 GPE（图形处理引擎）
* 它支持查询的数据导出到 JSON 和 XLS 格式
* 它提供了REST API，可以被任何编程语言（如 Java，Spring，Scala 等）访问
* 它提供了可以通过任何 UI MVC 框架（如 Node JS）访问的 Java 脚本
* 它支持两种 Java API：Cypher API 和 Native Java API来开发 Java 应用程序

### Neo4j 的优点

* 它很容易表示连接的数据
* 检索/遍历/导航更多的连接数据是非常容易和快速的
* 它非常容易地表示半结构化数据
* Neo4j CQL 查询语言命令是人性化的可读格式，非常容易学习
* 使用简单而强大的数据模型
* 它不需要复杂的连接来检索连接的/相关的数据，因为它很容易检索它的相邻节点或关系细节没有连接或索引

### Neo4j 的缺点或限制

* AS 的 Neo4j 2.1.3最新版本，它具有支持节点数，关系和属性的限制。
* 它不支持 Sharding。

### Neo4j 属性图数据模型

Neo4j 图数据库遵循属性图模型来存储和管理其数据。

属性图模型规则

* 表示节点，关系和属性中的数据
* 节点和关系都包含属性
* 关系连接节点
* 属性是键值对
* 节点用圆圈表示，关系用方向键表示。
* 关系具有方向：单向和双向。
* 每个关系包含“开始节点”或“从节点”和“到节点”或“结束节点”

在属性图数据模型中，关系应该是定向的。如果我们尝试创建没有方向的关系，那么它将抛出一个错误消息。 在 Neo4j 中，关系也应该是有方向性的。如果我们尝试创建没有方向的关系，那么 Neo4j 会抛出一个错误消息，“关系应该是方向性的”。 Neo4j 图数据库将其所有数据存储在节点和关系中。我们不需要任何额外的关系数据库或无 SQL 数据库来存储 Neo4j 数据库数据。它以图形的形式存储其数据的本机格式。

Neo4j 使用本机 GPE（图形处理引擎）引擎来使用它的本机图存储格式。

图形数据库数据模型的主要构建块是：

* 节点
* 关系
* 属性

简单的属性图的例子

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=ZGZmNzFjZjFiOWZlMWYxZGQ1ZTlhN2Q4ZWYxM2M0ZDJfMm1xNWFPTktXVFVlRkNQV2ZERFRMZ1ZPcm5hSDJVRm9fVG9rZW46Ym94Y255WVcyOFdDdHNTdVpOcmhuRjBlYzljXzE3NjYwNDgyMzY6MTc2NjA1MTgzNl9WNA)

这里我们使用圆圈表示节点。 使用箭头的关系。 关系是有方向性的。 我们可以用 Properties（键值对）来表示 Node 的数据。 在这个例子中，我们在 Node 的 Circle 中表示了每个 Node 的 Id 属性。

## Neo4j 本地安装

docker-compose up -d

暂时无法在飞书文档外展示此内容

本地访问地址：http://localhost:7474/browser/

# 对比基于关系型数据库实现的权限管理

|                  | 整体架构                     | 特点                                                 | 数据更新 | 性能     |
| ------------------ | ------------------------------ | ------------------------------------------------------ | ---------- | ---------- |
| 基于关系型数据库
| postgre+es+redis+OPA
事件中心 | 利用 es 维护权限视图，便于检索                       | 事件驱动 | 依靠缓存 |
| 基于图数据库     | neo4j+OPA(非必需)
事件中心    | 将主客体、关系、属性及条件都存储在一张图中，使用灵活 | 事件驱动 | 优       |

# R/ABAC 初始化数据

## 创建主体和资源节点

```SQL
//创建主体和资源
CREATE (`user1`:user {name:'user1',age:16,level:'P6'}),
        (`user2`:user {name:'user2',age:18,level:'P7'}),
        (`user3`:user {name:'user3'})
;
CREATE (`group1`:group {name:'group1'}),
        (`group2`:group {name:'group2'})
;
CREATE (`org1`:org {name:'org1'})
;
CREATE (`role1`:role {name:'role1'})
;
CREATE (`res1`:resource {name:'res1',type:'B2C'}),
        (`res2`:resource {name:'res2',type:'B2B'}),
        (`res3`:resource {name:'res3'})
;
```

## 创建主体与主体之间的关系

```SQL
//创建主体与主体之间的关系，关系名称统一为 MEMBER_OF
match (user:user {name:'user1'}),(group:group {name:'group1'})
CREATE (user)-[r:MEMBER_OF]->(group)
;
match (user:user {name:'user2'}),(group:group {name:'group2'})
CREATE (user)-[r:MEMBER_OF]->(group)
;
match (user:user {name:'user3'}),(org:org {name:'org1'})
CREATE (user)-[r:MEMBER_OF]->(org)
;
match (group:group {name:'group2'}),(role:role {name:'role1'})
CREATE (group)-[r:MEMBER_OF]->(role)
;
match (org:org {name:'org1'}),(role:role {name:'role1'})
CREATE (org)-[r:MEMBER_OF]->(role)
;
```

## 创建主体与资源之间的关系

关系名称表示 action

关系可具备两个属性

* effect 允许拒绝（0，1）
* condition 条件 ？
  * 如果使用 OPA，考虑将条件详情存入 OPA，关系中只存条件 ID
  * 如果不使用 OPA，考虑将条件写成 Neo4j CQL 语法，方便构造 where 条件进行二次查询

```SQL
// 创建主体与资源之间的关系，关系名称表示 action，effect 1 0 表示 允许拒绝
match (user:user {name:'user3'}),(resource:resource {name:'res1'})
CREATE (user)-[r:READ{effect:'0',condition:'datetime>9 && datetime<17'}]->(resource)
;
match (user:user {name:'user1'}),(resource:resource {name:'res1'})
CREATE (user)-[r:READ{effect:'1',condition:'ip not "127.0.0.1"'}]->(resource)
;
match (user:user {name:'user1'}),(resource:resource {name:'res1'})
CREATE (user)-[r:WRITE{effect:'0'}]->(resource)
;
match (group:group {name:'group1'}),(resource:resource {name:'res1'})
CREATE (group)-[r:WRITE{effect:'1'}]->(resource)
;
match (group:group {name:'group1'}),(resource:resource {name:'res2'})
CREATE (group)-[r:WRITE{effect:'1'}]->(resource)
;
match (group:group {name:'group2'}),(resource:resource {name:'res2'})
CREATE (group)-[r:WRITE{effect:'1'}]->(resource)
;
match (group:group {name:'group1'}),(resource:resource {name:'res2'})
CREATE (group)-[r:READ{effect:'1'}]->(resource)
;
match (role:role {name:'role1'}),(resource:resource {name:'res3'})
CREATE (role)-[r:READ{effect:'1'}]->(resource)
```

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=NzU5YTJjZTJiZmU1NmMxNDI2Y2M3MmMyMjNhMDAyN2JfVXM2eGVJVml2cUVoZHJ2eWQ4TVlSMHBhZlNTaGZrV3hfVG9rZW46Ym94Y241Tk9aWnFXdW1acVIyNDZYVzByQlNoXzE3NjYwNDgyMzY6MTc2NjA1MTgzNl9WNA)

# 如何实现 condition 判断

在实际的权限访问控制场景中，条件约束（condition）可以作用在任意一个关系（action）上。

例如：销售（role）只能在工作日的 9:00 - 17:00 期间才能修改（action）合同（resource）

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=MWQ1OTZiYzY1ZGFmNWRlMmIwZGFkYjFkZjM3NWU2YTNfUENwQnhDWGU1QXNveTF4UEdRRzBJTnc5bFBQMEJRcVlfVG9rZW46Ym94Y25NSzdYblNUbWJDeDJ3SDU0RFl1eGpkXzE3NjYwNDgyMzY6MTc2NjA1MTgzNl9WNA)

用户鉴权流程如何完成条件判断，分为两步

暂时无法在飞书文档外展示此内容

* 引入 OPA 完成条件判定：在对某个关系 action 写入 condition 时，将 condition 存储到 OPA 中，同时生成唯一 ID 存储到该图关系的属性中。需要判断时，将条件 ID 和所需的环境属性一同请求 OPA 服务即可。
* 构造 WHERE 条件在图数据库中进行二次查询：在对某个关系 action 写入 condition 时，将 condition 完整存储到该图关系的属性中。需要判断时，将 condition 中的环境属性替换，使用 Neo4j CQL 语法构造出带condition 的 where 条件进行二次查询。

# 用户鉴权相关

## 判断用户权限

### 无 condition

```SQL
//判断用户权限
// input:(user,aciton,resourcename)
// output:(user,aciton,resourcename)

MATCH paths=(user:user {name:'user1'})-[:MEMBER_OF*0..2]->()-[r:WRITE]->(r1:resource {name:'res1'})
where r.effect='1'
RETURN user.name AS username,type(r) as action, r1.name AS resourcename
//RETURN paths
```

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=NWYzOTdhYmQ3NTk0NTM4ZjczZTRjNTMxY2I1YThiYTlfTkNLQ2ZWcjh2dHdneGdxeDY5bXhSanl2dkYyOFA3aURfVG9rZW46Ym94Y25lOGdHUWFldmgzVnZaOXplaTF6WWtiXzE3NjYwNDgyMzY6MTc2NjA1MTgzNl9WNA)

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=NGJmNjZlYjA4Y2QwZWU5MTU4NDdiMTFlYzdlZDVmNmJfWUQyWTNIRVBuYmdSdExlcEQ2WnA3MFdyUWUxTElINHpfVG9rZW46Ym94Y25JZ2UxMWdUMFZXYktxdXVmTGZ2SWxiXzE3NjYwNDgyMzY6MTc2NjA1MTgzNl9WNA)

### 有 condition

#### 查询图数据库中的关系及条件

```SQL
// input:(user,aciton,resourcename)
// output:(user,aciton,condition,resourcename)

MATCH paths=(user:user {name:'user1'})-[:MEMBER_OF*0..2]->()-[r:READ]->(r1:resource {name:'res1'})
where r.effect='1'
RETURN user.name AS username,type(r) as action,r.condition as condition, r1.name AS resourcename
```

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=ODgwOTRhNWM4YTdhMzMwODk3MjdmM2M5Mzg5MWUzMzJfdUhTZTh1VWl6OEt4Z29UV1ZyZzdkcmtENngwSUg2STFfVG9rZW46Ym94Y25sSEZTeVU4UWx4QThkQXM0cnZzS3RlXzE3NjYwNDgyMzY6MTc2NjA1MTgzNl9WNA)

#### 使用 OPA 判定条件

1. 将条件以及所需的环境属性发送给 OPA

```SQL
// input:(policyId, ip)
// output:(true/false)
```

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=OGMyYzBiZDcxMjNiNTliOGI1MzAzYTVhZWFiNzEyYWNfbWRrZHhBMlNabjBRekRHcEtUV0taYjdveGxhMWlJVGRfVG9rZW46Ym94Y25LbUw3cmpENHRlcDhXcUlFVmVKQUJjXzE3NjYwNDgyMzY6MTc2NjA1MTgzNl9WNA)

#### 构造 WHERE 条件在图数据库中进行二次查询

```SQL
// input:(user,aciton,resourcename,ip)
// output:(user,aciton,resourcename)

MATCH paths=(user:user {name:'user1'})-[:MEMBER_OF*0..2]->()-[r:READ]->(r1:resource {name:'res1'})
where r.effect='1' and {ip} <> '127.0.0.1'
RETURN user.name AS username,type(r) as action, r1.name AS resourcename
```

## 获取用户权限列表

```SQL
//获取用户权限列表
// input:(user)
// output:(user,aciton,resourcename)
// 允许
MATCH paths=(user:user {name:'user1'})-[:MEMBER_OF*0..2]->()-[r]->(r1:resource)
where r.effect='1'
RETURN user.name AS username,type(r) as action, r1.name AS resourcename
// 拒绝
MATCH paths=(user:user {name:'user1'})-[:MEMBER_OF*0..2]->()-[r]->(r1:resource)
where r.effect='0'
RETURN user.name AS username,type(r) as action, r1.name AS resourcename
```

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=MTgxY2ZhMDM5MzAyZGZjYTMwOWQ2MmJmOGMyY2M0NTJfZUZ5b3FCaTF4bElYalVPVG1sS0h2WVY3Q1Z2bUZSR3JfVG9rZW46Ym94Y251WXhpVnZXQ1JIOE9FbDd6YmNnZG5mXzE3NjYwNDgyMzY6MTc2NjA1MTgzNl9WNA)

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=OTIzNjUwMDY2NjE4YTkxZmM4Mjc2NGVlZmUwOGQ3ZmJfUzlCZ21NdHRVZnlId0RadVdhTDB4Y0FpYkk3dzBQaDJfVG9rZW46Ym94Y25UZmxDT3RDSlVEVDdSVnV5ZnVXSWdiXzE3NjYwNDgyMzY6MTc2NjA1MTgzNl9WNA)

## 获取用户权限列表（指定资源属性）

```SQL
//获取用户权限列表（指定资源属性）
MATCH paths=(user:user {name:'user1'})-[:MEMBER_OF*0..2]->()-[r]->(r1:resource)
where r.effect='1' and r1.type='B2B'
RETURN user.name AS username,type(r) as action, r1.name AS resourcename
```

## 获取用户指定 aciton 的资源

```SQL
//获取用户指定 aciton 的资源
// input:(user,aciton)
// output:(user,resourcename)

MATCH paths=(user:user {name:'user1'})-[:MEMBER_OF*0..2]->()-[r:WRITE]->(r1:resource)
where r.effect='1'
RETURN user.name AS username, collect(r1.name) AS resourcename
//RETURN paths
```

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=MzE5ZTE3Njc5MTYxMDAwN2FjZTQyNWQ0NTM3ZWE1NjlfUUZtQ2RESmRTdzNYbTNPbHF2RWdUbTlOSXBGRkhJTTRfVG9rZW46Ym94Y25mcUJycVdFeDhBb2NVdlp1bklPNDRnXzE3NjYwNDgyMzY6MTc2NjA1MTgzNl9WNA)

## 待完善

该方案将「允许/拒绝」绑定在 action 级别。在主体资源关系绑定时设置如：[r:READ{effect:'0'}]，允许/拒绝会作为 action 的一种属性。

那么在鉴权时需要将「拒绝」过滤掉，待研究更好的方案，提高鉴权性能。

# More

如果追求极致的用户鉴权性能，可通过事件驱动维护一张「只有用户和资源的关系表」，不通过跳度来查询，空间换时间

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=Zjc2ZWJiM2E1MWI0Mzg5MjRkYzg1NzUxMTU5NTJlMjJfSFV5RWUwV3p4UHZtQkIzUEFGWk04RXM0WU5LWmJZYTJfVG9rZW46Ym94Y25sbUhucmtBc3ozaUxFTmxTNGtzVDViXzE3NjYwNDgyMzY6MTc2NjA1MTgzNl9WNA)

# 参考资料：

Java spring 开发：[Spring DATA Neo4J - 简介\_w3cschool](https://www.w3cschool.cn/neo4j/neo4j_spring_data_introduction.html)

https://gist.github.com/achmadns/40fe5073cadd2f6dd797f67872d93420

https://www.youtube.com/watch?v=vYTYLNUfAgE

