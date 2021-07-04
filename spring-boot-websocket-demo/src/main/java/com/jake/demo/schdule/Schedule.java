package com.jake.demo.schdule;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jake.demo.model.Quotation;

@Component
public class Schedule {

    @Autowired
    private SimpMessageSendingOperations simpMessageSendingOperations;

    private Random random = new Random();

    @Scheduled(cron = "0/5 * * * * *")
    public void mockQuotation() {
        LocalDateTime now = LocalDateTime.now();

        Quotation q1 = Quotation.builder().symbol("AAA").price(50 + random.nextInt(10)).time(now).build();
        simpMessageSendingOperations.convertAndSend("/topic/quotation", q1);

        Quotation q2 = Quotation.builder().symbol("BBB").price(20 + random.nextInt(10)).time(now).build();
        simpMessageSendingOperations.convertAndSend("/topic/quotation", q2);

    }
}
