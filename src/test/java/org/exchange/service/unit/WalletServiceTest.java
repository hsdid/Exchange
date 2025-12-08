package org.exchange.service.unit;

import org.exchange.modules.user.domain.Role;
import org.exchange.modules.user.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {
//    @Mock
//    private TransactionRepository transactionRepository;
//    @Mock
//    private ExchangeService exchangeService;
//    @Mock
//    private AuditLogService auditLogService;
//    @InjectMocks
//    private WalletService walletService;
//
//    @Test
//    void buy_crypto_currency_lowers_balance() {
//        Cryptocurrency cryptoBTC = new Cryptocurrency("BTC", "Bitcoin", "10000");
//        when(exchangeService.getCryptocurrency("BTC")).thenReturn(cryptoBTC);
//
//        User user = new User("John Doe", "jon@email.com", "password", Role.USER);
//
//        String result = walletService.buyCryptoCurrency("BTC", new BigDecimal("1.0"), user, LocalDateTime.now());
//
//        Assertions.assertTrue(result.startsWith("Purchased"));
//
//        BigDecimal balance = walletService.getBalance();
//        Assertions.assertEquals(new BigDecimal("40000.0"), balance);
//
//        verify(auditLogService).logTransaction(any(Transaction.class));
//
//        verify(transactionRepository).save(any(Transaction.class));
//    }
//
//    @Test void cannot_buy_more_than_balance() {
//        Cryptocurrency cryptoBTC = new Cryptocurrency("BTC", "Bitcoin", "100000");
//        when(exchangeService.getCryptocurrency("BTC")).thenReturn(cryptoBTC);
//
//        User user = new User("John Doe", "jon@email.com", "password", Role.USER);
//
//        String result = walletService.buyCryptoCurrency("BTC", new BigDecimal("1.0"), user, LocalDateTime.now());
//
//        Assertions.assertEquals("Insufficient funds", result);
//    }
//
//    @Test void throw_exception_for_unknown_currency() {
//        User user = new User("John Doe", "jon@email.com", "password", Role.USER);
//        Assertions.assertThrows(IllegalArgumentException.class, () -> walletService.buyCryptoCurrency("XRP", new BigDecimal("1.0"), user, LocalDateTime.now()));
//    }
}
