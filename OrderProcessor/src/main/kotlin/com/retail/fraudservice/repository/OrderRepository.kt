package com.retail.fraudservice.repository

import com.retail.fraudservice.model.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<OrderEntity, String>
