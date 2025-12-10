package cn.itcast.account.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName("account_tbl")
public class Account {
    @TableId
    private Long id;
    private String userId;
    private Integer money;
    /**
     * 冻结金额，在 Seata TCC 模式下使用
     */
    private Integer frozenMoney;
}
