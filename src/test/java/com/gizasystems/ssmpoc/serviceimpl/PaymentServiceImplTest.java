package com.gizasystems.ssmpoc.serviceimpl;

import com.gizasystems.ssmpoc.domain.PaymentEvent;
import com.gizasystems.ssmpoc.domain.PaymentState;
import com.gizasystems.ssmpoc.model.Payment;
import com.gizasystems.ssmpoc.repository.PaymentRepository;
import com.gizasystems.ssmpoc.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;
    @Autowired
    PaymentRepository paymentRepository;
    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder().amount(new BigDecimal("12.99")).build();
    }

    @Test
    void preAuthPayment() {
        Payment savedPayment = paymentService.createNewPayment(payment);
        System.out.println("expected to be: NEW");
        System.out.println(savedPayment.getState());

        StateMachine<PaymentState, PaymentEvent> stateMachine =  paymentService.preAuthPayment(savedPayment.getId());
        System.out.println("expected to be: PRE_AUTH || PRE_AUTH_ERROR in case of action declined || PRE_AUTH in case action approved");
        System.out.println(stateMachine.getState().getId());

        Payment preAuthedPayment = paymentRepository.findById(savedPayment.getId()).get();
        System.out.println(preAuthedPayment);
    }
}