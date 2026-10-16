package com.udea.banco2025;

import com.udea.banco2025.entity.Customer;
import com.udea.banco2025.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba de integración con H2: levanta el contexto completo y recorre
 * crear clientes -> transferir -> consultar histórico.
 */
@SpringBootTest
@AutoConfigureMockMvc
class Banco2025ApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void flujoCompletoDeTransferencia() throws Exception {
        mockMvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ana\",\"lastName\":\"Gómez\",\"accountNumber\":\"INT-1\",\"balance\":100.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
        mockMvc.perform(post("/api/customers").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Luis\",\"lastName\":\"Pérez\",\"accountNumber\":\"INT-2\",\"balance\":0.0}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/transactions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senderAccountNumber\":\"INT-1\",\"receiverAccountNumber\":\"INT-2\",\"amount\":40.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(40.0));

        mockMvc.perform(get("/api/transactions/INT-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderAccountNumber").value("INT-1"));

        Customer sender = customerRepository.findByAccountNumber("INT-1").orElseThrow();
        Customer receiver = customerRepository.findByAccountNumber("INT-2").orElseThrow();
        assertThat(sender.getBalance()).isEqualTo(60.0);
        assertThat(receiver.getBalance()).isEqualTo(40.0);
        assertThat(sender).isEqualTo(customerRepository.findById(sender.getId()).orElseThrow());
        assertThat(sender.hashCode()).isEqualTo(sender.getId().hashCode());
        assertThat(sender.toString()).contains("INT-1");
    }
}
