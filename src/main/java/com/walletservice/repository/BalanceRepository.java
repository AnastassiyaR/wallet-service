package com.walletservice.repository;


import com.walletservice.domain.Balance;
import com.walletservice.domain.Currency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface BalanceRepository {

    void insertBalance(Balance balance);

    // Returns updated balance row (with new available_amount) or null if account/currency not found
    Balance deposit(@Param("accountId") Long accountId,
                    @Param("currency") Currency currency,
                    @Param("amount") BigDecimal amount);

    // Returns updated balance row if funds were sufficient or null if insufficient funds or not found
    Balance withdraw(@Param("accountId") Long accountId,
                     @Param("currency") Currency currency,
                     @Param("amount") BigDecimal amount);

    List<Balance> getBalancesByAccountId(@Param("accountId") Long accountId);
}
