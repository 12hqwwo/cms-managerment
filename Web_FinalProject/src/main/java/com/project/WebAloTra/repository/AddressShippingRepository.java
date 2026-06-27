package com.project.WebAloTra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.WebAloTra.entity.AddressShipping;
import com.project.WebAloTra.entity.Customer;

import java.util.List;

public interface AddressShippingRepository extends JpaRepository<AddressShipping, Long> {
    List<AddressShipping> findAllByCustomer_Account_Id(Long accountId);
}
