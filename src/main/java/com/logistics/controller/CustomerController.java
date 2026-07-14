package com.logistics.controller;

import com.logistics.dto.CustomerCreateRequest;
import com.logistics.dto.CustomerCreditRequest;
import com.logistics.model.Customer;
import com.logistics.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public Customer createCustomer(@RequestBody CustomerCreateRequest request) {
        return customerService.createCustomer(request.getId(), request.getCreditLevel());
    }

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerService.getAll();
    }

    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable String id) {
        return customerService.getById(id);
    }

    @PostMapping("/{id}/adjust-credit")
    public Customer adjustCredit(@PathVariable String id, @RequestBody CustomerCreditRequest request) {
        System.out.println("DEBUG: creditLevel = [" + request.getCreditLevel() + "]");
        return customerService.adjustCredit(id, request.getCreditLevel());
    }
}