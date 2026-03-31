package com.retail.fraudservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class FraudResultEvent(
    @JsonProperty("order_id")
    val orderId: String,

    @JsonProperty("user_id")
    val userId: String,

    @JsonProperty("amount")
    val amount: Double,

    @JsonProperty("is_fraud")
    val isFraud: Boolean,

    @JsonProperty("fraud_reason")
    val fraudReason: String,

    @JsonProperty("processed_at")
    val processedAt: LocalDateTime
)
