package com.booknest.service.notification;

import com.booknest.entity.LendingTransaction;

public class OverdueLenderTemplate implements EmailTemplate {

    private final LendingTransaction transaction;
    private final long daysOverdue;

    public OverdueLenderTemplate(LendingTransaction transaction) {
        this.transaction = transaction;
        this.daysOverdue = java.time.LocalDate.now().toEpochDay()
                - transaction.getDueDate().toEpochDay();
    }

    @Override
    public String getSubject() {
        return "⚠️ Your book is overdue: '" + transaction.getBook().getTitle() + "'";
    }

    @Override
    public String getBody() {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background: #5c3d2e; padding: 24px; border-radius: 12px 12px 0 0; text-align: center;">
                    <h1 style="color: #e8b87a; margin: 0;">📚 BookNest</h1>
                </div>
                <div style="background: #ffffff; padding: 32px; border: 2px solid #ddd6ce; border-radius: 0 0 12px 12px;">
                    <h2 style="color: #c0392b;">Your Book is Overdue</h2>
                    <p>Hi <strong>%s</strong>,</p>
                    <p>
                        Your book <strong>"%s"</strong> is <strong>%d days overdue</strong>.
                    </p>
                    <p>Borrower: <strong>%s</strong> (%s)</p>
                    <p>Due date was: <strong>%s</strong></p>
                    <p style="font-size: 14px; color: #7a6e65;">
                        You can contact the borrower through the BookNest app.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(
                transaction.getLender().getFullName(),
                transaction.getBook().getTitle(),
                daysOverdue,
                transaction.getBorrower().getFullName(),
                transaction.getBorrower().getEmail(),
                transaction.getDueDate().toString()
        );
    }
}