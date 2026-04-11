package com.retail.fraudservice.model

import com.fasterxml.jackson.annotation.JsonProperty

data class OrderEvent(
    @JsonProperty("order_id")
    val orderId: String,

    @JsonProperty("user_id")
    val userId: String,

    @JsonProperty("amount")
    val amount: Double,

    @JsonProperty("currency")
    val currency: String,

    @JsonProperty("location")
    val location: String
)