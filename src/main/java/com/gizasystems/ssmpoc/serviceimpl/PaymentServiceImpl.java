package com.gizasystems.ssmpoc.serviceimpl;

import com.gizasystems.ssmpoc.domain.PaymentEvent;
import com.gizasystems.ssmpoc.domain.PaymentState;
import com.gizasystems.ssmpoc.model.Payment;
import com.gizasystems.ssmpoc.repository.PaymentRepository;
import com.gizasystems.ssmpoc.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    public static final String PAYMENT_ID_HEADER = "payment_id";
    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Override
    public Payment createNewPayment(Payment payment) {
        payment.setState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> preAuthPayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);
        sendEvent(paymentId, stateMachine, PaymentEvent.PRE_AUTHORIZE);
        return stateMachine;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> AuthPayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);
        sendEvent(paymentId, stateMachine, PaymentEvent.AUTHORIZE_APPROVED);
        return stateMachine;
    }

    @Override
    public StateMachine<PaymentState, PaymentEvent> declineAuthPayment(Long paymentId) {
        StateMachine<PaymentState, PaymentEvent> stateMachine = build(paymentId);
        sendEvent(paymentId, stateMachine, PaymentEvent.AUTHORIZE_DECLINED);
        return stateMachine;
    }

    private StateMachine<PaymentState, PaymentEvent> build(Long paymentId){
        Payment payment = paymentRepository.findById(paymentId).get();
        StateMachine<PaymentState, PaymentEvent> stateMachine = stateMachineFactory.getStateMachine(UUID.randomUUID());
        stateMachine.stop();
        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sm -> {
                    sm.addStateMachineInterceptor(paymentStateChangeInterceptor);
                    sm.resetStateMachine(new DefaultStateMachineContext<>(payment.getState(), null, null, null));
                });
        stateMachine.start();
        return stateMachine;
    }

    private void sendEvent(Long paymentId, StateMachine<PaymentState, PaymentEvent> stateMachine, PaymentEvent event){
        Message message = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();
        stateMachine.sendEvent(message);
    }
}
