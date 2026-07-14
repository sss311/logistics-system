package com.logistics.service;

import com.logistics.model.Customer;
import java.util.List;

public interface CustomerService {
    Customer createCustomer(String id, String creditLevel);
    Customer getById(String id);
    List<Customer> getAll();
    Customer adjustCredit(String id, String creditLevel);
}