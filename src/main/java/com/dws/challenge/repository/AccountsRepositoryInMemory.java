package com.dws.challenge.repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.AccountNotExistException;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.InsufficientBalanceException;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.EmailNotificationService;
import com.dws.challenge.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Slf4j
public class AccountsRepositoryInMemory implements AccountsRepository {

    @Autowired
    private NotificationService notificationService;

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        return accounts.get(accountId);
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

    @Override
    public void transferMoney(String accountFromId, String accountToId, BigDecimal amount) {
        // Validate accounts exist
        if(!accounts.containsKey(accountFromId)){
            throw new AccountNotExistException("From Account id " + accountFromId + " not exists!");
        }
        if(!accounts.containsKey(accountToId)){
            throw new AccountNotExistException("From Account id " + accountToId + " not exists!");
        }

        Account fromAccount = accounts.get(accountFromId);
        Account toAccount = accounts.get(accountToId);
        synchronized(fromAccount) {
            synchronized (toAccount) {
                // Check for sufficient balance
                if (amount.compareTo(fromAccount.getBalance()) > 0) {
                    throw new InsufficientBalanceException("Insufficient balance for this transfer.");
                }

                // Perform the transfer
                fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
                toAccount.setBalance(toAccount.getBalance().add(amount));

                //Update accounts in the map
                accounts.put(accountFromId, fromAccount);
                accounts.put(accountToId, toAccount);
                notificationService.notifyAboutTransfer(fromAccount,"Amount:"+amount+" transferred to account id :"+ accountToId);
                notificationService.notifyAboutTransfer(toAccount,"Amount:"+amount+" received from account id :"+ accountFromId);
                log.info("Transfer successful: {} from {} to {}",amount,accountFromId,accountToId);
            }
        }
    }

}
