package com.gizasystems.ssmpoc.service;

import com.gizasystems.ssmpoc.domain.PaymentEvent;
import com.gizasystems.ssmpoc.domain.PaymentState;
import com.gizasystems.ssmpoc.model.Payment;
import org.springframework.statemachine.StateMachine;

public interface PaymentService {
    Payment createNewPayment(Payment payment);
    StateMachine<PaymentState, PaymentEvent> preAuthPayment(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> AuthPayment(Long paymentId);
    StateMachine<PaymentState, PaymentEvent> declineAuthPayment(Long paymentId);
}
