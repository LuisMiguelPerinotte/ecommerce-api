package com.java.luismiguel.ecommerce_api.api.dto.admin.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesReportDTO(
        BigDecimal totalRevenue,
        Long totalOrders,
        LocalDate startDate,
        LocalDate endDate
) {
}
