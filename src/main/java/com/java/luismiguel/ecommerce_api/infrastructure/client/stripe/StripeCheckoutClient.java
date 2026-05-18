package com.java.luismiguel.ecommerce_api.infrastructure.client.stripe;

import com.java.luismiguel.ecommerce_api.domain.order.Order;
import com.java.luismiguel.ecommerce_api.domain.order.OrderItem;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class StripeCheckoutClient {
    public  Session createCheckoutSession(Order order) throws StripeException {
        List<SessionCreateParams.LineItem> items = createLineItemList(order);

        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setClientReferenceId(order.getOrderId().toString())
                        .setPaymentIntentData(
                                SessionCreateParams.PaymentIntentData.builder()
                                        .putMetadata("orderId", order.getOrderId().toString())
                                        .build()
                        )
                        .addAllLineItem(items)
                        .setSuccessUrl("http://localhost:8080/success")
                        .setCancelUrl("http://localhost:8080/cancel")
                        .addExpand("payment_intent")
                        .build();

        return Session.create(params);
    }


    private List<SessionCreateParams.LineItem> createLineItemList(Order order) {
        List<SessionCreateParams.LineItem> items = new ArrayList<>();

        for (OrderItem item : order.getItems()) {
            Long productQuantity = item.getQuantity().longValue();
            String productName = item.getProductName();
            Long unitAmount = item.getUnitPrice()
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValue();


            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(productName)
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("brl")
                            .setUnitAmount(unitAmount)
                            .setProductData(productData)
                            .build();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams
                            .LineItem.builder()
                            .setQuantity(productQuantity)
                            .setPriceData(priceData)
                            .build();

            items.add(lineItem);
        }
        return items;
    }
}
