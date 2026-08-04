# SmartBudget ER Diagram (F002)

Entity-relationship model for the Day 1 schema in `db/create_tables.sql`.

```mermaid
erDiagram
    users ||--o{ transactions : "has"
    categories ||--o{ transactions : "classifies"
    users ||--o{ savings_goals : "owns"

    users {
        SERIAL user_id PK
        VARCHAR name
        VARCHAR email UK
        TIMESTAMP created_at
    }

    categories {
        SERIAL category_id PK
        VARCHAR name
        VARCHAR type "INCOME or EXPENSE"
    }

    transactions {
        SERIAL txn_id PK
        INT user_id FK
        INT category_id FK
        NUMERIC amount
        DATE txn_date
        VARCHAR description
        VARCHAR type "INCOME or EXPENSE"
    }

    savings_goals {
        SERIAL goal_id PK
        INT user_id FK
        VARCHAR goal_name
        NUMERIC target_amount
        NUMERIC current_amount
        DATE deadline
    }
```

## Relationships

| Parent | Child | Cardinality | Foreign key |
|--------|-------|-------------|-------------|
| `users` | `transactions` | 1:N | `transactions.user_id` → `users.user_id` |
| `categories` | `transactions` | 1:N | `transactions.category_id` → `categories.category_id` |
| `users` | `savings_goals` | 1:N | `savings_goals.user_id` → `users.user_id` |

## Notes

- `categories.type` and `transactions.type` are restricted to `INCOME` / `EXPENSE`.
- `transactions.amount` and `savings_goals.target_amount` must be greater than zero.
- `users.email` is unique across all users.
