package com.workintech.s19d2.service;

import com.workintech.s19d2.entity.Account;
import com.workintech.s19d2.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public Account find(Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    @Override
    public Account save(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public Account delete(Long id) {
        Account account = find(id);
        if (account != null) {
            accountRepository.delete(account);
        }
        return account;
    }
}