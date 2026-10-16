package com.udea.banco2025.controller;

import com.udea.banco2025.DTO.CustomerDTO;
import com.udea.banco2025.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Test
    void listaLosClientes() throws Exception {
        when(customerService.getAllCustomer()).thenReturn(List.of(new CustomerDTO(1L, "Ana", "Gómez", "111", 100.0)));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("111"));
    }

    @Test
    void obtieneUnClientePorId() throws Exception {
        when(customerService.getCustomerById(1L)).thenReturn(new CustomerDTO(1L, "Ana", "Gómez", "111", 100.0));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana"));
    }

    @Test
    void creaUnCliente() throws Exception {
        when(customerService.createCustomer(any(CustomerDTO.class))).thenReturn(new CustomerDTO(1L, "Ana", "Gómez", "111", 100.0));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ana\",\"lastName\":\"Gómez\",\"accountNumber\":\"111\",\"balance\":100.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void rechazaClienteSinSaldo() {
        assertThatThrownBy(() -> mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Ana\",\"lastName\":\"Gómez\",\"accountNumber\":\"111\"}")))
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void actualizaUnCliente() throws Exception {
        when(customerService.updateCustomer(eq(1L), any(CustomerDTO.class))).thenReturn(new CustomerDTO(1L, "Luis", "Pérez", "111", 75.0));

        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Luis\",\"lastName\":\"Pérez\",\"accountNumber\":\"111\",\"balance\":75.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Luis"));
    }

    @Test
    void borraUnCliente() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());
        verify(customerService).deleteCustomer(1L);
    }
}
