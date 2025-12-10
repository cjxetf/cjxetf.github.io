| 日志类型       | 所属模块           | 作用                                      | 是否物理/逻辑 | 持久化级别                     |
|----------------|--------------------|-------------------------------------------|---------------|-------------------------------|
| **Binlog**     | MySQL Server 层    | 主从复制、数据恢复（Point-in-Time Recovery） | 逻辑日志      | 可配置（`sync_binlog`）        |
| **Redo Log**   | InnoDB 引擎层      | 崩溃恢复（Crash Recovery），保证事务持久性   | 物理日志      | 可配置（`innodb_flush_log_at_trx_commit`） |
| **Undo Log**   | InnoDB 引擎层      | 事务回滚 + MVCC（多版本并发控制）            | 逻辑日志      | 随事务提交异步 purge           |