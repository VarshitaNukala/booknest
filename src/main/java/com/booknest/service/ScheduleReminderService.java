package com.booknest.service;


import com.booknest.entity.LendingTransaction;
import com.booknest.repository.LendingTransactionRepository;
import com.booknest.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleReminderService {

    private final LendingTransactionRepository transactionRepository;
    private final NotificationService notificationService;
    /**
     * Runs daily at 9 AM to check for due/overdue books
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void checkDueBooks() {
        LocalDate today = LocalDate.now();
        LocalDate twoDaysFromNow = today.plusDays(2);

        // Find books due in 2 days
        List<LendingTransaction> upcomingDue = transactionRepository
                .findTransactionsDueOn(twoDaysFromNow);

        for (LendingTransaction transaction : upcomingDue) {
            notificationService.sendDueReminder(transaction);
            log.info("Sent due reminder for book: {} to user: {}",
                    transaction.getBook().getTitle(),
                    transaction.getBorrower().getEmail());
        }

        // Find overdue books
        List<LendingTransaction> overdue = transactionRepository
                .findOverdueTransactions(today);

        for (LendingTransaction transaction : overdue) {
            notificationService.sendOverdueNotice(transaction);
            log.warn("Sent overdue notice for book: {} to user: {}",
                    transaction.getBook().getTitle(),
                    transaction.getBorrower().getEmail());
        }
    }
}