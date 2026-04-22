package com.java.luismiguel.ecommerce_api.domain.product;

import java.util.UUID;

public interface LowStockProductProjection {
    UUID getProductId();
    String getName();
    Integer getStockQuantity();
}
