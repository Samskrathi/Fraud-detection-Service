package com.retail.fraudservice.exceptions

open class AppException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class InvalidOrderEventException(message: String) : AppException(message)

class OrderPersistenceException(message: String, cause: Throwable? = null) : AppException(message, cause)

class FraudResultPublishException(message: String, cause: Throwable? = null) : AppException(message, cause)
