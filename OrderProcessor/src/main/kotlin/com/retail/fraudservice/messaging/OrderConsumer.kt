package com.retail.fraudservice.messaging

import com.retail.fraudservice.constants.AppConstant
import com.retail.fraudservice.service.OrderService
import com.retail.fraudservice.model.OrderEvent
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderConsumer(
    private val orderService: OrderService
) {

    private val log = LoggerFactory.getLogger(OrderConsumer::class.java)

    @KafkaListener(
        topics = ["\${app.kafka.input-topic}"],
        groupId = "\${spring.kafka.consumer.group-id}"
    )
    fun consume(orderEvent: OrderEvent) {
        log.info(AppConstant.ORDER_CONSUMED_LOG, orderEvent.orderId, orderEvent.userId)
        orderService.detectFraud(orderEvent)
    }
}
