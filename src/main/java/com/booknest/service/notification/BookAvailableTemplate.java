package com.booknest.service.notification;

public class BookAvailableTemplate implements EmailTemplate {

    private final String bookTitle;
    private final String lenderName;

    public BookAvailableTemplate(String bookTitle, String lenderName) {
        this.bookTitle = bookTitle;
        this.lenderName = lenderName;
    }

    @Override
    public String getSubject() {
        return "🎉 Book Available: " + bookTitle;
    }

    @Override
    public String getBody() {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <div style="background: #27ae60; padding: 24px; border-radius: 12px 12px 0 0; text-align: center;">
                    <h1 style="color: #fff; margin: 0;">📚 BookNest</h1>
                </div>
                <div style="background: #fff; padding: 32px; border: 2px solid #ddd; border-radius: 0 0 12px 12px;">
                    <h2 style="color: #27ae60;">Book Available!</h2>
                    <p>Great news! <strong>"%s"</strong> is now available.</p>
                    <p>Lender: <strong>%s</strong></p>
                    <p style="margin-top: 20px;">You're next in the waitlist — log in to BookNest to borrow it now before someone else does!</p>
                    <hr style="margin: 24px 0;">
                    <p style="color: #999; font-size: 12px;">BookNest — Share books, build community</p>
                </div>
            </div>
            """.formatted(bookTitle, lenderName);
    }
}