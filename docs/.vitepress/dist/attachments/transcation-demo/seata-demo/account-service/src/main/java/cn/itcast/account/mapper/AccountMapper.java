package cn.itcast.account.mapper;

import cn.itcast.account.entity.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;


public interface AccountMapper extends BaseMapper<Account> {

    @Update("update account_tbl set money = money - ${money} where user_id = #{userId}")
    int deduct(@Param("userId") String userId, @Param("money") int money);

    @Update("update account_tbl set money = money + ${money} where user_id = #{userId}")
    int refund(@Param("userId") String userId, @Param("money") int money);

    @Update("UPDATE account_tbl SET money = money - ${money}, frozen_money = frozen_money + ${freezeMoney} WHERE user_id = #{userId}")
    void deductTCC(String userId, int money, int freezeMoney);
}
