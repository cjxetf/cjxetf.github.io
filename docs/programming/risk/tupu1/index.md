
# 什么是 RBAC？

RBAC，即基于角色的访问控制，采用的方法是根据用户在组织中的角色授予（或拒绝）对资源的访问。每个角色都被分配了一系列的权限和限制，这很好，因为你不需要跟踪每个系统用户和他们的属性。你只需要更新相应的角色，将角色分配给用户，或者删除分配。但这可能很难管理和扩展。使用 RBAC 静态角色模型的企业经历了角色爆炸：大公司可能有数万个相似但不同的角色或用户，他们的角色会随着时间的推移而改变，因此很难跟踪角色或审计不需要的权限。RBAC 具有固定的访问权限，没有规定短暂的权限，也没有考虑位置、时间或设备等属性。使用 RBAC 的企业很难满足复杂的访问控制要求，以满足其他组织需求的监管要求。

# 什么是 ABAC？

ABAC 是 “基于属性的访问控制 “的缩写。从高层次上讲，ABAC 被定义为一种访问控制方法，“在这种方法中，根据分配的主体属性、环境条件以及用这些属性和条件指定的一组策略，批准或拒绝主体对对象进行操作的请求。” ABAC 是一个细粒度的模型，因为你可以给用户分配任何属性，但同时它也成为一种负担，很难管理：

* 在定义权限的时候，用户和对象之间的关系无法可视化。
* 如果规则设计的有点复杂或者混乱，对于管理员来说，维护和跟踪会很麻烦。

当有大量的权限需要处理时，会造成性能问题。

# 什么是 NGAC？

NGAC，即下一代访问控制，采用将访问决定数据建模为图形的方法。NGAC 可以实现系统化、策略一致的访问控制方法，以高精细度授予或拒绝用户管理能力。

有几种类型的实体；它们代表了您要保护的资源、它们之间的关系以及与系统互动的行为者。这些实体是：

* 用户
* 对象
* 用户属性，如组织单位
* 对象属性，如文件夹
* 策略类，如文件系统访问、位置和时间

NGAC 是基于这样一个假设：你可以用一个图来表示你要保护的系统，这个图代表了你要保护的资源和你的组织结构，这个图对你有意义，并且符合你的组织语义。在这个对你的组织非常特殊的模型之上，你可以叠加策略。在资源模型和用户模型之间，定义了权限。这样 NGAC 提供了一种优雅的方式来表示你要保护的资源，系统中的不同角色，以及如何用权限把这两个世界联系在一起。

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=MDJiZThjMmY5N2EzMjA0Y2ViM2QxM2E2ZWIxZDEwMmFfaVdzaHNMcDhJaUZzbVlVRmR2M0l1SzRjZDd3Q0lxUUVfVG9rZW46VzUyZ2JCa0RFb0wzaUt4T1dUbmNlbWwxbjNjXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

# 为什么选择 NGAC？

在 ABAC 的情况下，需要跟踪所有对象的属性，这造成了可管理性的负担。RBAC 减少了负担，因为我们提取了所有角色的访问信息，但是这种模式存在角色爆炸的问题，也会变得不可管理。有了 NGAC，我们就有了我们所需要的一切 —— 以一种紧凑、集中的方式。

当访问决策很复杂时，ABAC 的处理时间会成倍上升。RBAC 在规模上变得特别难以管理，而 NGAC 则可以线性扩展。

NGAC 真正出彩的地方在于灵活性。它可以被配置为允许或不允许访问，不仅基于对象属性，而且基于其他条件 —— 时间、位置、月相等。

NGAC 的其他关键优势包括能够一致地设置策略（以满足合规性要求）和设置历时性策略的能力。例如，NGAC 可以在中断期间授予开发人员一次性的资源访问权，而不会留下不必要的权限，以免日后导致安全漏洞。NGAC 可以在一个访问决策中评估和组合多个策略，同时保持其线性时间的复杂度。

# 业务架构

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=ZWI0ZjJhNDA3MGMzOGMzZGEyNDMwYjlhZWUwNTU3ZGZfcDdHVEFXcFZnNDQ4Z1FnY2FsOVZzTXZUbUpGZnAwYUVfVG9rZW46SEhJbWJIUXhDb0FvR3l4dDlXY2NjTkE4bm9jXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

暂时无法在飞书文档外展示此内容

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=NDFhOTkyNDhkMjZkNzU0NWQ3ZTQ2YTIzNzIyZTE1ZGNfZlFvS1pDMUVUU01pQXRoQWRpeUxmdThJTzM1bmNaWHpfVG9rZW46V21sM2JPT3Q4b1NRMmR4WFhBZWNWSnVHbkFnXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

# 技术实现

## 图数据库

随着技术的发展，我们对数据的需求已经不再局限于对数据本身的获取了，我们还需要获取数据与数据间的关系（也就是连接数据）。

简单地说，我们可以说图数据库主要用于存储更多的连接数据（因为图结构相比其他数据结构而言，能保存更多的数据间的关系）。

如果我们使用 RDBMS 数据库来存储更多连接的数据，那么它们不能提供用于遍历大量数据的适当性能。 在这些情况下，Graph Database 提高了应用程序性能。

权限访问控制场景中节点相对有限，但节点之间关系的复杂且海量，相对于关系型数据，使用图方式存储能极大提高检索性能。

**举例：**

银行内部的一套管理合同的线上系统，其中包含角色：销售、财务、法务、管理者，资源类型有页面、合同、菜单，合同属性主要包含 ID、金额、地区、提交人，环境信息包含：时间、IP、地区。

小凡是名销售，可以在任意时段查看自己曾经提交过的合同，但只能在工作日的 9:00 - 17:00 期间提交合同

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=ODlhMDdjNjIzZjE4NWNhODIxMDMxZGRmYzJhZmM2YzVfMVFMUHR5ME5iM0pQR2ZQNGc2ZkhuU29qZkE4ckxNUzNfVG9rZW46QTJnQmJ6dXBwbzliVE14Yk5wYmNHTm5QbnplXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

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

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=OGQwNzA4N2U4YTM4MzBhMTMwZjI5NTQ2MjI3MmYxYWJfNEw4SWxsNHR3eUQ3OTAzVWlFUFJoVjBMZW5SWlowcGNfVG9rZW46S2VPZ2JqNElybzdJWVd4aThvUGNCN2ltbmRlXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

这里我们使用圆圈表示节点。 使用箭头的关系。 关系是有方向性的。 我们可以用 Properties（键值对）来表示 Node 的数据。 在这个例子中，我们在 Node 的 Circle 中表示了每个 Node 的 Id 属性。

## Neo4j 本地安装

docker-compose up -d

暂时无法在飞书文档外展示此内容

本地访问地址：http://localhost:7474/browser/

## 初始化数据

### 创建主体和资源节点

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

### 创建主体与主体之间的关系

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

### 创建主体与资源之间的关系

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

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=N2IzNWUwMjQzNTJmMzBkYWUxNmZlNjUxZGRiMDQ5ODdfTmFibEtNZTJiZElaSXppeHNNMWtJalB0NjRhTkJlRXRfVG9rZW46Vlh5Y2JQTFhDb2lieG54WGMzTGNicFVmblBoXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

## 如何实现 condition 判断

在实际的权限访问控制场景中，条件约束（condition）可以作用在任意一个关系（action）上。

例如：销售（role）只能在工作日的 9:00 - 17:00 期间才能修改（action）合同（resource）

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=MmFmZjhkNjRlMDQ0ZTU0MzU1YTNjNWNiNWU1ZDEyMGFfdnhuOHhseHRSMFpsYUI3MWZkUnNjWWw2eGVxamo2VnhfVG9rZW46WDZKd2JGaWpxb3h1elJ4TmtaUGNGclpQbmtnXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

用户鉴权流程如何完成条件判断，分为两步

暂时无法在飞书文档外展示此内容

* 引入 OPA 完成条件判定：在对某个关系 action 写入 condition 时，将 condition 存储到 OPA 中，同时生成唯一 ID 存储到该图关系的属性中。需要判断时，将条件 ID 和所需的环境属性一同请求 OPA 服务即可。
* 构造 WHERE 条件在图数据库中进行二次查询：在对某个关系 action 写入 condition 时，将 condition 完整存储到该图关系的属性中。需要判断时，将 condition 中的环境属性替换，使用 Neo4j CQL 语法构造出带condition 的 where 条件进行二次查询。

## 用户鉴权相关

### 判断用户权限

#### 无 condition

```SQL
//判断用户权限
// input:(user,aciton,resourcename)
// output:(user,aciton,resourcename)

MATCH paths=(user:user {name:'user1'})-[:MEMBER_OF*0..2]->()-[r:WRITE]->(r1:resource {name:'res1'})
where r.effect='1'
RETURN user.name AS username,type(r) as action, r1.name AS resourcename
//RETURN paths
```

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=NjJlMmU5ZGM0ZDQ1NTA1YTlmZWJlMGQ4NDVjYmViNmVfVGJBa29TdVAydHNhZ1lBVnJ6OTBnamlVazJYcFhSUzVfVG9rZW46VG16SmJjb1Fjb1kycmZ4QnZoNmNvbUNWbkROXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=MDFkYzZhZDZjZTlkMmQ4MGQyZDgxMzlmODhjYWJlODVfMGt5ejZsZFo2akI2R0tkTFpEOURUTTkwdU1jcDBxY0lfVG9rZW46U0xrdGJEREdFb3VtcU94MUNyYWNQUDRubnBoXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

#### 有 condition

#### 查询图数据库中的关系及条件

```SQL
// input:(user,aciton,resourcename)
// output:(user,aciton,condition,resourcename)

MATCH paths=(user:user {name:'user1'})-[:MEMBER_OF*0..2]->()-[r:READ]->(r1:resource {name:'res1'})
where r.effect='1'
RETURN user.name AS username,type(r) as action,r.condition as condition, r1.name AS resourcename
```

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=MGIyZDQzMGVhODkxOTk3NTA2Yjg1OWUyMTlmNjM5M2VfWEJOTFN3cXRTRWJEZGFpdnB4aTdzU1JrbGMxV2ZBMEJfVG9rZW46SWNwU2J2Znk1bzk2VmV4R293N2Nkdld1bmxkXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

#### 构造 WHERE 条件在图数据库中进行二次查询

```SQL
// input:(user,aciton,resourcename,ip)
// output:(user,aciton,resourcename)

MATCH paths=(user:user {name:'user1'})-[:MEMBER_OF*0..2]->()-[r:READ]->(r1:resource {name:'res1'})
where r.effect='1' and {ip} <> '127.0.0.1'
RETURN user.name AS username,type(r) as action, r1.name AS resourcename
```

### 获取用户权限列表

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

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=MDNmYjM0ZGE0ZWNkN2UwMTQ0ZGRhMTg2MmQ0ZmNkOGFfSzllbzFEUWtBVHI3N3lwZlE3RmRLQlZyT0lWM3NGeElfVG9rZW46VjlmcmI3Y2FRb0cyb2t4dGExVGNsWVozbmxjXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=ZGQ3MjE3NzQ3YWNkZTM3NjNmM2UzNzkzNTRiNjVmYjRfa0lhaFVvcnFjVEpZNlA2dEg1V1ZyelBGOXplSWNqaWVfVG9rZW46VTJhRmIzd0prb3I5MlV4MHgwT2NpbXdabklnXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

### 获取用户权限列表（指定资源属性）

```SQL
//获取用户权限列表（指定资源属性）
MATCH paths=(user:user {name:'user1'})-[:MEMBER_OF*0..2]->()-[r]->(r1:resource)
where r.effect='1' and r1.type='B2B'
RETURN user.name AS username,type(r) as action, r1.name AS resourcename
```

### 获取用户指定 aciton 的资源

```SQL
//获取用户指定 aciton 的资源
// input:(user,aciton)
// output:(user,resourcename)

MATCH paths=(user:user {name:'user1'})-[:MEMBER_OF*0..2]->()-[r:WRITE]->(r1:resource)
where r.effect='1'
RETURN user.name AS username, collect(r1.name) AS resourcename
//RETURN paths
```

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=ZjJjZjg4Nzk1ZmViZTgxMDI3NDZiOWQxNTcyODQ4NjVfVGI2Qkx2Vnc1NWVvNWFxcWhMQkhldFlielcxWG1JQ21fVG9rZW46Tll3TWJZdmdvb1E5UFd4TUZ1dmNKcVBEbjRnXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

# 配置流程

## 创建主体 / 资源

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=NGQ0ODNjZjkyYzE5ZmI5YTg1M2QwYjdlYThkOTcwZTlfVHFYQjNZU0liQ3JSQ1d4VmZnQVp4ZHh0eXBnQ2xRVW9fVG9rZW46SVZrcWJqVWlsb0E1Tkl4SlVkWWNUYkFRbjhjXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

## **策略化授权**

授权更加清晰，将数据权限和资源权限灵活组合，提高资源管理效率。

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=MDcyY2U4MjQ0YmFmMTgzNDg2NThkOWMxYjFiOGYwNjNfM0xhRGNKclFXYWV3SmY5WklKT2x3aXR2OGtrRzNCNXZfVG9rZW46TWlJYmJFMUdXb3E4S1R4MlFBZWNhcVFnbmh1XzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

## **权限视图**

高效审计提高复杂场景授权效率，便于权限治理。

![](https://steamory.feishu.cn/space/api/box/stream/download/asynccode/?code=NTk1MTQzZDgxZGE2ZWM4NjI3ZDViOGY5OGVmZmE1M2JfVG9VdXlzT09lQ1ZnR0w5ZFZsMWcxeVFQQndMN3cxdDRfVG9rZW46QTlabmJYQ25Pb3V5cW14T3FkQWNhS3RTbkVkXzE3NjYwNDg1Mjg6MTc2NjA1MjEyOF9WNA)

# **典型场景**

## **API​ 统一鉴权**

当企业使用多个应用程序时，需要共享某些资源，例如用户信息、订单数据等。企业可以使用 API 统一鉴权来确保每个服务和应用程序都能够访问它们所需的资源。

例如，当用户访问订单管理功能时，订单微服务将向 API 统一鉴权服务发送一个包含访问令牌的请求。API 统一鉴权服务将验证访问令牌的有效性，并检查该用户是否具有访问订单管理的权限。如果验证通过，则订单微服务将返回用户的订单信息。如果验证不通过，则订单微服务将返回一个错误消息，表示该用户无权访问该资源。

## **功能菜单权限管理**

企业的一些业务软件系统中常常会有多个功能菜单，如用户管理、订单管理、库存管理等。这些菜单需要根据用户的角色或权限进行控制，以确保每个用户只能访问其具有权限的功能菜单。

例如某企业的 ERP 系统需要对菜单进行权限管理。系统中有三种角色：管理员、销售员和采购员，管理员可以访问所有菜单，销售员只能访问销售相关的菜单，采购员只能访问采购相关的菜单。企业可以在功能菜单权限管理系统中定义这些角色，当用户登录系统时，系统会验证该用户的角色，并根据其角色来确定其可以访问的菜单。

## **数据权限**​**管理**

数据权限管理是指企业在数据访问控制方面对用户进行细粒度的控制，以确保每个用户只能访问其具有权限的数据。例如某企业员工的个人信息和薪资记录，需要对这些数据进行权限管理，以确保敏感数据只能被授权的用户访问，HR 部门可以访问所有员工的数据，而普通员工只能访问自己的数据。数据权限管理功能可以根据员工的部门、岗位、职级等信息，将员工分为不同的权限组，并控制每个权限组可以访问的数据范围。

当用户访问员工数据时，系统会验证该用户的身份和权限，并根据其权限来确定其可以访问的数据范围。如果用户尝试访问其没有权限的数据，系统将拒绝该请求，并返回一个错误消息，告诉用户他们没有权限访问该数据。
