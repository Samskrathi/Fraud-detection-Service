package com.retail.fraudservice.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "processed_order")
data class OrderEntity(

    @Id
    @Column(name = "order_id")
    val orderId: String,

    @Column(name = "user_id")
    val userId: String,

    @Column(name = "amount")
    val amount: Double,

    @Column(name = "is_fraud")
    val isFraud: Boolean,

    @Column(name = "fraud_reason")
    val fraudReason: String,

    @Column(name = "processed_at")
    val processedAt: LocalDateTime

)