package com.logistics.service.impl;

import com.logistics.common.BusinessException;
import com.logistics.common.constant.CustomerStatus;
import com.logistics.common.constant.CreditLevelEnum;
import com.logistics.mapper.CustomerMapper;
import com.logistics.model.Customer;
import com.logistics.service.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerMapper customerMapper) {
        this.customerMapper = customerMapper;
    }

    @Override
    public Customer createCustomer(String id, String creditLevel) {
        // 默认值处理
        if (creditLevel == null || creditLevel.isBlank()) {
            creditLevel = CreditLevelEnum.BRONZE;
        }
        if (id == null || id.isBlank()) {
            id = "CUS_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        }

        Customer customer = new Customer();
        customer.setId(id);
        customer.setCreditLevel(creditLevel);
        customer.setCodBalance(0);
        customer.setStatus(CustomerStatus.NORMAL);

        customerMapper.insert(customer);
        return customer;
    }

    @Override
    public Customer getById(String id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException(404, "Customer not found");
        }
        return customer;
    }

    @Override
    public List<Customer> getAll() {
        return customerMapper.selectAll();
    }

    @Override
    public Customer adjustCredit(String id, String creditLevel) {
        Customer customer = getById(id);

        // 清洗不可见字符：去除所有空白字符和Unicode控制字符
        if (creditLevel != null) {
            creditLevel = creditLevel.replaceAll("[\\s\\p{Cntrl}]", "");
        }

        // 校验信用等级取值
        if (creditLevel == null ||
                (!"金".equals(creditLevel) && !"银".equals(creditLevel) && !"铜".equals(creditLevel))) {
            throw new BusinessException("credit_level 非法");
        }

        customerMapper.updateCreditLevel(id, creditLevel);
        customer.setCreditLevel(creditLevel);
        return customer;
    }
}