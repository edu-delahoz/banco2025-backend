package com.udea.banco2025.controller;

import com.udea.banco2025.DTO.TransactionDTO;
import com.udea.banco2025.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    private static final String BODY = "{\"senderAccountNumber\":\"111\",\"receiverAccountNumber\":\"222\",\"amount\":30.0}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    void realizaUnaTransferencia() throws Exception {
        when(transactionService.transferMoney(any(TransactionDTO.class)))
                .thenReturn(new TransactionDTO(10L, "111", "222", 30.0, null));

        mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.amount").value(30.0));
    }

    @Test
    void devuelve400SiLaTransferenciaEsInvalida() throws Exception {
        when(transactionService.transferMoney(any(TransactionDTO.class)))
                .thenThrow(new IllegalArgumentException("Saldo insuficiente en la cuenta del remitente."));

        mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Saldo insuficiente en la cuenta del remitente."));
    }

    @Test
    void consultaElHistoricoDeUnaCuenta() throws Exception {
        when(transactionService.getTransactionsForAccount("111"))
                .thenReturn(List.of(new TransactionDTO(1L, "111", "222", 10.0, null)));

        mockMvc.perform(get("/api/transactions/111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiverAccountNumber").value("222"));
    }
}
