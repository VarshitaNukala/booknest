# 📚 BookNest — Book Club & Lending Platform

A Spring Boot backend for book club management, book lending with multiple policies, Stripe payments, waitlist queuing, and community features.

---

## 🛠 Tech Stack

Java 21 | Spring Boot 4.0.6 | PostgreSQL | Redis Cloud | Stripe | JWT | Swagger

---

## ✨ What It Does

- **JWT Auth** with refresh token rotation & 4-tier RBAC
- **Book Clubs** — create, search, join (public/private with approval)
- **Lending Engine** — 3 policies via Strategy Pattern (Free, Deposit, Subscription)
- **Stripe Payments** — deposit, auto-refund, late fees, webhook handling
- **Waitlist** — FIFO queue with email notifications
- **Reading Progress** — track pages, percentage, public/private
- **Feed & Discussions** — posts, nested comments, book discussions
- **Analytics** — top books, active members, reading stats (native SQL)
- **Scheduled Reminders** — due/overdue email alerts daily at 9 AM
- **Redis Caching** — club lookups cached via Redis Cloud

---

## 📁 Project Stats

| Metric | Count |
|--------|-------|
| Entities | 12 |
| REST Endpoints | 48 |
| Services | 13 |
| Controllers | 12 |
| Design Patterns | 4 (Strategy, Factory, Template, Observer) |
| Lending Policies | 3 |
| User Roles | 4 |

---

## 🚀 Quick Start

```bash
git clone https://github.com/VarshitaNukala/booknest.git
cd booknest-platform

# Create PostgreSQL database
psql -U postgres
CREATE DATABASE booknest;

# Copy config template & add your credentials
cp application-dev-template.yml application-dev.yml

# Run
mvn spring-boot:run