package com.walletservice.repository;


import com.walletservice.domain.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountRepository {

    void saveAccount(Account account);

    Account getAccountById(@Param("id") Long id);
}
