package com.workintech.s19d2.controller;

import com.workintech.s19d2.entity.Account;
import com.workintech.s19d2.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<Account> findAll() {
        return accountService.findAll();
    }

    @GetMapping("/{id}")
    public Account findById(@PathVariable Long id) {
        return accountService.find(id);
    }

    @PostMapping
    public Account save(@RequestBody Account account) {
        return accountService.save(account);
    }

    @PutMapping("/{id}")
    public Account update(@PathVariable Long id, @RequestBody Account account) {
        account.setId(id);
        return accountService.save(account);
    }

    @DeleteMapping("/{id}")
    public Account delete(@PathVariable Long id) {
        return accountService.delete(id);
    }
}