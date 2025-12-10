package cn.itcast.account.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@TableName("tcc_transaction_log")
public class TccTransactionLog {


    @TableId(type = IdType.INPUT)
    private Long id;

    /**
     * 全局事务ID
     */
    private String xid;

    /**
     * 分支事务ID
     */
    private Long branchId;

    /**
     * 事务状态
     */
    private Status status;

    /**
     * 创建时间
     */
    private LocalDateTime gmtCreated;

    /**
     * 修改时间
     */
    private LocalDateTime gmtModified;

    @Getter
    @AllArgsConstructor
    public enum Status {
        TRYING(0),
        CONFIRMED(1),
        CANCELLED(2);

        @EnumValue
        private final int code;
    }

    public static TccTransactionLog create(String xid, Long branchId) {
        TccTransactionLog log = new TccTransactionLog();
        log.setXid(xid);
        log.setBranchId(branchId);
        log.setStatus(Status.TRYING);
        log.setGmtCreated(LocalDateTime.now());
        log.setGmtModified(LocalDateTime.now());
        return log;
    }

    public static TccTransactionLog create(String xid, Long branchId, Status status) {
        TccTransactionLog log = new TccTransactionLog();
        log.setXid(xid);
        log.setBranchId(branchId);
        log.setStatus(Status.TRYING);
        log.setGmtCreated(LocalDateTime.now());
        log.setGmtModified(LocalDateTime.now());
        return log;
    }
}
