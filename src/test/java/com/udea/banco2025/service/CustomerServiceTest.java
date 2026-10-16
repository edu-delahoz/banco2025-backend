package com.udea.banco2025.service;

import com.udea.banco2025.DTO.CustomerDTO;
import com.udea.banco2025.entity.Customer;
import com.udea.banco2025.mapper.CustomerMapper;
import com.udea.banco2025.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        CustomerMapper mapper = Mappers.getMapper(CustomerMapper.class);
        customerService = new CustomerService(customerRepository, mapper);
    }

    private Customer customer(Long id, String account, double balance) {
        return new Customer(id, account, "Ana", "Gómez", balance);
    }

    @Test
    void getAllCustomerDevuelveTodosComoDTO() {
        when(customerRepository.findAll()).thenReturn(List.of(customer(1L, "111", 100.0), customer(2L, "222", 50.0)));

        List<CustomerDTO> result = customerService.getAllCustomer();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAccountNumber()).isEqualTo("111");
        assertThat(result.get(1).getBalance()).isEqualTo(50.0);
    }

    @Test
    void getCustomerByIdDevuelveElCliente() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer(1L, "111", 100.0)));

        CustomerDTO dto = customerService.getCustomerById(1L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFirstName()).isEqualTo("Ana");
        assertThat(dto.getLastName()).isEqualTo("Gómez");
    }

    @Test
    void getCustomerByIdFallaSiNoExiste() {
        when(customerRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(9L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Customer not found");
    }

    @Test
    void createCustomerGuardaYDevuelveDTO() {
        CustomerDTO input = new CustomerDTO(null, "Ana", "Gómez", "111", 100.0);
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        CustomerDTO result = customerService.createCustomer(input);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAccountNumber()).isEqualTo("111");
    }

    @Test
    void updateCustomerModificaLosCampos() {
        Customer existing = customer(1L, "111", 100.0);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(existing);

        CustomerDTO result = customerService.updateCustomer(1L, new CustomerDTO(null, "Luis", "Pérez", "333", 75.0));

        assertThat(result.getFirstName()).isEqualTo("Luis");
        assertThat(result.getLastName()).isEqualTo("Pérez");
        assertThat(result.getAccountNumber()).isEqualTo("333");
        assertThat(result.getBalance()).isEqualTo(75.0);
    }

    @Test
    void updateCustomerFallaSiNoExiste() {
        when(customerRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomer(9L, new CustomerDTO()))
                .hasMessage("Customer not found");
        verify(customerRepository, never()).save(any());
    }

    @Test
    void deleteCustomerBorraSiExiste() {
        when(customerRepository.existsById(1L)).thenReturn(true);

        customerService.deleteCustomer(1L);

        verify(customerRepository).deleteById(1L);
    }

    @Test
    void deleteCustomerFallaSiNoExiste() {
        when(customerRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> customerService.deleteCustomer(9L))
                .hasMessage("Customer not found");
        verify(customerRepository, never()).deleteById(any());
    }
}
