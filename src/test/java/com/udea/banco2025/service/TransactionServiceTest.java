package com.udea.banco2025.service;

import com.udea.banco2025.DTO.TransactionDTO;
import com.udea.banco2025.entity.Customer;
import com.udea.banco2025.entity.Transaction;
import com.udea.banco2025.repository.CustomerRepository;
import com.udea.banco2025.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionDTO request(String sender, String receiver, Double amount) {
        return new TransactionDTO(null, sender, receiver, amount, null);
    }

    @Test
    void transferMoneyMueveElSaldoYGuardaLaTransaccion() {
        Customer sender = new Customer(1L, "111", "Ana", "Gómez", 100.0);
        Customer receiver = new Customer(2L, "222", "Luis", "Pérez", 20.0);
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.of(sender));
        when(customerRepository.findByAccountNumber("222")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });

        TransactionDTO result = transactionService.transferMoney(request("111", "222", 30.0));

        assertThat(sender.getBalance()).isEqualTo(70.0);
        assertThat(receiver.getBalance()).isEqualTo(50.0);
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getSenderAccountNumber()).isEqualTo("111");
        assertThat(result.getReceiverAccountNumber()).isEqualTo("222");
        assertThat(result.getAmount()).isEqualTo(30.0);
        assertThat(result.getTimestamp()).isNotNull();
        verify(customerRepository).save(sender);
        verify(customerRepository).save(receiver);
    }

    @Test
    void transferMoneyConservaLaFechaRecibida() {
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 16, 8, 0);
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.of(new Customer(1L, "111", "A", "B", 100.0)));
        when(customerRepository.findByAccountNumber("222")).thenReturn(Optional.of(new Customer(2L, "222", "C", "D", 0.0)));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionDTO dto = request("111", "222", 10.0);
        dto.setTimestamp(fecha);

        assertThat(transactionService.transferMoney(dto).getTimestamp()).isEqualTo(fecha);
    }

    @Test
    void transferMoneyExigeCuentas() {
        assertThatThrownBy(() -> transactionService.transferMoney(request(null, "222", 10.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorios");
        assertThatThrownBy(() -> transactionService.transferMoney(request("111", null, 10.0)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void transferMoneyExigeMontoPositivo() {
        assertThatThrownBy(() -> transactionService.transferMoney(request("111", "222", null)))
                .hasMessageContaining("mayor que cero");
        assertThatThrownBy(() -> transactionService.transferMoney(request("111", "222", 0.0)))
                .hasMessageContaining("mayor que cero");
    }

    @Test
    void transferMoneyFallaSiNoExisteElRemitente() {
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transferMoney(request("111", "222", 10.0)))
                .hasMessage("La cuenta del remitente no existe.");
    }

    @Test
    void transferMoneyFallaSiNoExisteElReceptor() {
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.of(new Customer(1L, "111", "A", "B", 100.0)));
        when(customerRepository.findByAccountNumber("222")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transferMoney(request("111", "222", 10.0)))
                .hasMessage("La cuenta del receptor no existe.");
    }

    @Test
    void transferMoneyFallaConSaldoInsuficiente() {
        when(customerRepository.findByAccountNumber("111")).thenReturn(Optional.of(new Customer(1L, "111", "A", "B", 5.0)));
        when(customerRepository.findByAccountNumber("222")).thenReturn(Optional.of(new Customer(2L, "222", "C", "D", 0.0)));

        assertThatThrownBy(() -> transactionService.transferMoney(request("111", "222", 10.0)))
                .hasMessage("Saldo insuficiente en la cuenta del remitente.");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void getTransactionsForAccountDevuelveElHistorico() {
        LocalDateTime fecha = LocalDateTime.now();
        when(transactionRepository.findBySenderAccountNumberOrReceiverAccountNumber("111", "111"))
                .thenReturn(List.of(new Transaction(1L, "111", "222", 10.0, fecha), new Transaction(2L, "333", "111", 5.0, fecha)));

        List<TransactionDTO> result = transactionService.getTransactionsForAccount("111");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReceiverAccountNumber()).isEqualTo("222");
        assertThat(result.get(1).getSenderAccountNumber()).isEqualTo("333");
        assertThat(result.get(1).getAmount()).isEqualTo(5.0);
        assertThat(result.get(0).getTimestamp()).isEqualTo(fecha);
    }
}
