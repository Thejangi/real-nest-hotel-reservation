package com.realnest.service.impl;

import com.realnest.entity.Customer;
import com.realnest.repository.CustomerRepository;
import com.realnest.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Long id, Customer customer) {
        Customer existing = customerRepository.findById(id).orElseThrow();
        existing.setFullName(customer.getFullName());
        existing.setEmail(customer.getEmail());
        existing.setPhone(customer.getPhone());
        existing.setAddress(customer.getAddress());
        existing.setNationality(customer.getNationality());
        existing.setGender(customer.getGender());
        existing.setIdNumber(customer.getIdNumber());
        return customerRepository.save(existing);
    }

    @Override
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    @Override
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).orElseThrow();
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }
}
