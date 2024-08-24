package com.milko.wallet_service.service;

import com.milko.wallet_service.dto.input.PaymentRequestInputDto;
import com.milko.wallet_service.model.PaymentRequest;

import java.math.BigDecimal;

public interface FeeService {
    BigDecimal getFee(PaymentRequest paymentRequest);
}
