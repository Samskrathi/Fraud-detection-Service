package com.retail.fraudservice.service

import com.retail.fraudservice.constants.AppConstant
import com.retail.fraudservice.exceptions.FraudResultPublishException
import com.retail.fraudservice.exceptions.InvalidOrderEventException
import com.retail.fraudservice.exceptions.OrderPersistenceException
import com.retail.fraudservice.model.FraudResultEvent
import com.retail.fraudservice.model.OrderEntity
import com.retail.fraudservice.model.OrderEvent
import com.retail.fraudservice.repository.OrderRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.concurrent.ExecutionException

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val kafkaTemplate: KafkaTemplate<String, FraudResultEvent>,
    @Value("\${app.kafka.output-topic}") private val outputTopic: String
) {

    fun detectFraud(orderEvent: OrderEvent) {
        validate(orderEvent)

        val isFraud = orderEvent.amount > AppConstant.HIGH_VALUE_ORDER_THRESHOLD
        val processedAt = LocalDateTime.now()
        val fraudReason = if (isFraud) AppConstant.HIGH_VALUE_ORDER_REASON else AppConstant.CLEARED_REASON
        val orderEntity = OrderEntity(
            orderId = orderEvent.orderId,
            userId = orderEvent.userId,
            amount = orderEvent.amount,
            isFraud = isFraud,
            fraudReason = fraudReason,
            processedAt = processedAt
        )

        try {
            orderRepository.save(orderEntity)
        } catch (exception: Exception) {
            throw OrderPersistenceException("Failed to save order ${orderEvent.orderId}", exception)
        }

        val fraudResultEvent = FraudResultEvent(
            orderId = orderEvent.orderId,
            userId = orderEvent.userId,
            amount = orderEvent.amount,
            isFraud = isFraud,
            fraudReason = fraudReason,
            processedAt = processedAt
        )

        try {
            kafkaTemplate.send(outputTopic, orderEvent.orderId, fraudResultEvent).get()
        } catch (exception: ExecutionException) {
            val rootCause = exception.cause ?: exception
            throw FraudResultPublishException(
                "Failed to publish fraud result for order ${orderEvent.orderId}: ${rootCause.message}",
                rootCause
            )
        } catch (exception: Exception) {
            throw FraudResultPublishException(
                "Failed to publish fraud result for order ${orderEvent.orderId}: ${exception.message}",
                exception
            )
        }
    }

    private fun validate(orderEvent: OrderEvent) {
        if (orderEvent.orderId.isBlank()) {
            throw InvalidOrderEventException("Order id must not be blank")
        }
        if (orderEvent.userId.isBlank()) {
            throw InvalidOrderEventException("User id must not be blank")
        }
        if (orderEvent.currency.isBlank()) {
            throw InvalidOrderEventException("Currency must not be blank")
        }
        if (orderEvent.location.isBlank()) {
            throw InvalidOrderEventException("Location must not be blank")
        }
        if (orderEvent.amount < 0) {
            throw InvalidOrderEventException("Amount must not be negative")
        }
    }
}
