package com.java.luismiguel.ecommerce_api.domain.order;

import com.java.luismiguel.ecommerce_api.domain.order.enums.OrderStatus;

public interface OrderStatusCountProjection {
    OrderStatus getStatus();
    Long getCount();
}
