package com.walletservice.repository;


import com.walletservice.domain.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransactionRepository {

    void insertTransaction(Transaction transaction);

    List<Transaction> getTransactionsByAccountId(@Param("accountId") Long accountId);
}
