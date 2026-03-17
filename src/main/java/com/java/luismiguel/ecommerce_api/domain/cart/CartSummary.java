package com.java.luismiguel.ecommerce_api.domain.cart;

import java.math.BigDecimal;

public interface CartSummary {
    Integer getTotalItems();
    BigDecimal getTotalAmount();
}
