package com.retail.fraudservice.constants

class AppConstant {
    companion object {
        const val ORDER_CONSUMED_LOG = "Consumed order {} for user {}"
        const val HIGH_VALUE_ORDER_REASON = "HIGH_VALUE_ORDER"
        const val CLEARED_REASON = "CLEARED"
        const val HIGH_VALUE_ORDER_THRESHOLD = 10000.0
    }
}
