package com.booknest.service.notification;

import com.booknest.entity.LendingTransaction;

public class OverdueBorrowerTemplate implements EmailTemplate {

    private final LendingTransaction transaction;
    private final long daysOverdue;

    public OverdueBorrowerTemplate(LendingTransaction transaction) {
        this.transaction = transaction;
        this.daysOverdue = java.time.LocalDate.now().toEpochDay()
                - transaction.getDueDate().toEpochDay();
    }

    @Override
    public String getSubject() {
        return "⚠️ OVERDUE: '" + transaction.getBook().getTitle() + "' is "
                + daysOverdue + " days late!";
    }

    @Override
    public String getBody() {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background: #c0392b; padding: 24px; border-radius: 12px 12px 0 0; text-align: center;">
                    <h1 style="color: #ffffff; margin: 0; font-size: 24px;">⚠️ BookNest</h1>
                </div>
                <div style="background: #ffffff; padding: 32px; border: 2px solid #ddd6ce; border-radius: 0 0 12px 12px;">
                    <h2 style="color: #c0392b;">Book Overdue</h2>
                    <p style="font-size: 16px;">Hi <strong>%s</strong>,</p>
                    <p style="font-size: 16px;">
                        <strong>"%s"</strong> was due on <strong>%s</strong>.
                    </p>
                    <div style="text-align: center; margin: 24px 0;">
                        <span style="background: #fce4e4; color: #c0392b; padding: 12px 24px; border-radius: 8px; font-size: 20px; font-weight: bold;">
                            %d Days Overdue!
                        </span>
                    </div>
                    <p style="font-size: 16px;">
                        Please return it immediately to <strong>%s</strong>.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(
                transaction.getBorrower().getFullName(),
                transaction.getBook().getTitle(),
                transaction.getDueDate().toString(),
                daysOverdue,
                transaction.getLender().getFullName()
        );
    }
}