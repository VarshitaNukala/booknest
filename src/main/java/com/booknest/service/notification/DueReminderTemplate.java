package com.booknest.service.notification;

import com.booknest.entity.LendingTransaction;

public class DueReminderTemplate implements EmailTemplate {

    private final LendingTransaction transaction;

    public DueReminderTemplate(LendingTransaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public String getSubject() {
        return "📚 Reminder: '" + transaction.getBook().getTitle() + "' is due on "
                + transaction.getDueDate();
    }

    @Override
    public String getBody() {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background: #5c3d2e; padding: 24px; border-radius: 12px 12px 0 0; text-align: center;">
                    <h1 style="color: #e8b87a; margin: 0; font-size: 24px;">📚 BookNest</h1>
                </div>
                <div style="background: #ffffff; padding: 32px; border: 2px solid #ddd6ce; border-radius: 0 0 12px 12px;">
                    <h2 style="color: #5c3d2e; margin-bottom: 16px;">Book Due Soon</h2>
                    <p style="font-size: 16px; color: #2c1810;">Hi <strong>%s</strong>,</p>
                    <p style="font-size: 16px; color: #2c1810;">
                        Just a friendly reminder that <strong>"%s"</strong> by <em>%s</em> is due on:
                    </p>
                    <div style="text-align: center; margin: 24px 0;">
                        <span style="background: #fff3cd; color: #856404; padding: 12px 24px; border-radius: 8px; font-size: 20px; font-weight: bold;">
                            %s
                        </span>
                    </div>
                    <p style="font-size: 16px; color: #2c1810;">
                        Please return it to <strong>%s</strong>.
                    </p>
                    <p style="font-size: 14px; color: #7a6e65;">
                        You can request an extension from the BookNest app if you need more time.
                    </p>
                    <hr style="border: 1px solid #ddd6ce; margin: 24px 0;">
                    <p style="font-size: 12px; color: #999; text-align: center;">
                        This is an automated message from BookNest Platform.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(
                transaction.getBorrower().getFullName(),
                transaction.getBook().getTitle(),
                transaction.getBook().getAuthor(),
                transaction.getDueDate().toString(),
                transaction.getLender().getFullName()
        );
    }
}