package com.booknest.service.notification;

import com.booknest.entity.LendingTransaction;
import com.booknest.service.notification.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailSender emailSender;

    public void sendDueReminder(LendingTransaction transaction) {
        EmailTemplate template = new DueReminderTemplate(transaction);
        emailSender.send(transaction.getBorrower().getEmail(), template);
    }

    public void sendOverdueNotice(LendingTransaction transaction) {
        EmailTemplate borrowerTemplate = new OverdueBorrowerTemplate(transaction);
        emailSender.send(transaction.getBorrower().getEmail(), borrowerTemplate);

        EmailTemplate lenderTemplate = new OverdueLenderTemplate(transaction);
        emailSender.send(transaction.getLender().getEmail(), lenderTemplate);
    }

    public void sendBookAvailable(String toEmail, String bookTitle, String lenderName) {
        EmailTemplate template = new BookAvailableTemplate(bookTitle, lenderName);
        emailSender.send(toEmail, template);
    }
}