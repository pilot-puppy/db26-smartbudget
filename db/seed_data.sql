-- ============================================================
-- TICKET-F005 (Day 1, Sprint 0) — Insert Seed Data
-- TICKET-F004 (Day 1, Sprint 0) — Test Constraints
-- ============================================================
-- Seed data populates your tables with realistic test data.
-- Insert order matters! Parents first, then children:
--   categories → users → transactions → savings_goals
-- ============================================================


-- ============================================================
-- TODO TICKET-F005: Insert 5 categories
-- ============================================================
-- WHAT: Seed data is pre-populated test data that makes development easier.
--       Without it, every time you reset the database you'd start with empty tables.
--
-- HOW:  Write 5 INSERT INTO categories (name, type) VALUES (...) statements.
--       Include at least 2 INCOME categories (e.g., Salary, Freelance)
--       and at least 3 EXPENSE categories (e.g., Food, Transport, Utilities).
--       You do NOT need to specify category_id — SERIAL auto-assigns it.
--
-- WHY:  The React frontend needs categories for its dropdown.
--       The API endpoint GET /api/categories returns this data.
--
-- OBSERVE: After inserting, run SELECT * FROM categories;
--          You should see 5 rows with auto-assigned category_id values 1-5.
INSERT INTO categories (name, type) VALUES
    ('Salary',     'INCOME'),
    ('Freelance',  'INCOME'),
    ('Food',       'EXPENSE'),
    ('Transport',  'EXPENSE'),
    ('Utilities',  'EXPENSE');

-- ============================================================
-- TODO TICKET-F005: Insert 10 users
-- ============================================================
-- WHAT: Each user has a unique name and email. The email UNIQUE constraint
--       means no two users can share the same email address.
--
-- HOW:  Write 10 INSERT INTO users (name, email) VALUES (...) statements.
--       Use realistic names like 'Alice Smith', 'Bob Jones', etc.
--       Use email format like 'alice@bank.com'.
--       Do NOT specify user_id or created_at — they auto-populate.
--
-- WHY:  10 users gives enough variety to test multi-user features.
--
-- OBSERVE: Run SELECT * FROM users; — you should see 10 rows.
--          Try inserting a duplicate email — it should FAIL with a unique violation.


INSERT INTO users (name, email) VALUES
    ('Alice Smith',   'alice@bank.com'),
    ('Bob Jones',     'bob@bank.com'),
    ('Carol Reed',    'carol@bank.com'),
    ('Dave Patel',    'dave@bank.com'),
    ('Eve Lin',       'eve@bank.com'),
    ('Frank Moore',   'frank@bank.com'),
    ('Grace Novak',   'grace@bank.com'),
    ('Henry Osei',    'henry@bank.com'),
    ('Iris Petrova',  'iris@bank.com'),
    ('Jack Quinn',    'jack@bank.com');

-- ============================================================
-- TODO TICKET-F005: Insert 50+ transactions
-- ============================================================
-- WHAT: Transactions are the core data. Each references a user_id and category_id.
--       These IDs must match existing rows in the users and categories tables
--       (this is enforced by the FOREIGN KEY constraints you added).
--
-- HOW:  Write 50+ INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (...) statements.
--       Spread them across multiple users and categories.
--       Use different dates spanning 2-3 months (for monthly summary testing).
--       Mix INCOME and EXPENSE types.
--       Make amounts realistic: salaries around 3000-5000, food around 30-100, etc.
--
-- WHY:  50+ records provide enough data to test filtering, sorting, aggregation,
--       and make charts look meaningful.
--
-- OBSERVE: Run SELECT COUNT(*) FROM transactions; — should return 50+.
--          Run the JOIN query from queries.sql to see full details.


INSERT INTO transactions
    (user_id, category_id, amount, txn_date, description, type) VALUES
    -- January: salaries for all 10 users
    ( 1, 1, 3500.00, '2026-01-01', 'January salary',    'INCOME'),
    ( 2, 1, 4200.00, '2026-01-01', 'January salary',    'INCOME'),
    ( 3, 1, 3300.00, '2026-01-01', 'January salary',    'INCOME'),
    ( 4, 1, 2800.00, '2026-01-01', 'January salary',    'INCOME'),
    ( 5, 1, 3100.00, '2026-01-01', 'January salary',    'INCOME'),
    ( 6, 1, 3950.00, '2026-01-01', 'January salary',    'INCOME'),
    ( 7, 1, 4600.00, '2026-01-01', 'January salary',    'INCOME'),
    ( 8, 1, 3050.00, '2026-01-01', 'January salary',    'INCOME'),
    ( 9, 1, 3750.00, '2026-01-01', 'January salary',    'INCOME'),
    (10, 1, 2950.00, '2026-01-01', 'January salary',    'INCOME'),
    -- January: expenses
    ( 1, 3,   45.20, '2026-01-08', 'Groceries',         'EXPENSE'),
    ( 1, 4,   25.00, '2026-01-15', 'Bus pass',          'EXPENSE'),
    ( 1, 5,  110.00, '2026-01-22', 'Electricity bill',  'EXPENSE'),
    ( 2, 5,  120.00, '2026-01-20', 'Electricity bill',  'EXPENSE'),
    ( 2, 3,   72.30, '2026-01-11', 'Weekly shop',       'EXPENSE'),
    ( 3, 3,   58.90, '2026-01-14', 'Groceries',         'EXPENSE'),
    ( 4, 4,   35.00, '2026-01-18', 'Taxi to airport',   'EXPENSE'),
    ( 5, 3,   64.10, '2026-01-09', 'Groceries',         'EXPENSE'),
    ( 6, 5,   88.00, '2026-01-25', 'Internet bill',     'EXPENSE'),
    ( 7, 4,  150.00, '2026-01-19', 'Train season pass', 'EXPENSE'),
    ( 8, 3,   41.75, '2026-01-16', 'Groceries',         'EXPENSE'),
    ( 9, 5,   99.99, '2026-01-27', 'Gas bill',          'EXPENSE'),
    (10, 4,   18.50, '2026-01-13', 'Bus tickets',       'EXPENSE'),
    -- February: salaries for all 10 users
    ( 1, 1, 3500.00, '2026-02-01', 'February salary',   'INCOME'),
    ( 2, 1, 4200.00, '2026-02-01', 'February salary',   'INCOME'),
    ( 3, 1, 3300.00, '2026-02-01', 'February salary',   'INCOME'),
    ( 4, 1, 2800.00, '2026-02-01', 'February salary',   'INCOME'),
    ( 5, 1, 3100.00, '2026-02-01', 'February salary',   'INCOME'),
    ( 6, 1, 3950.00, '2026-02-01', 'February salary',   'INCOME'),
    ( 7, 1, 4600.00, '2026-02-01', 'February salary',   'INCOME'),
    ( 8, 1, 3050.00, '2026-02-01', 'February salary',   'INCOME'),
    ( 9, 1, 3750.00, '2026-02-01', 'February salary',   'INCOME'),
    (10, 1, 2950.00, '2026-02-01', 'February salary',   'INCOME'),
    -- February: freelance income + expenses
    ( 3, 2,  800.00, '2026-02-05', 'Freelance gig',     'INCOME'),
    ( 7, 2, 1250.00, '2026-02-14', 'Consulting day',    'INCOME'),
    ( 1, 3,   38.40, '2026-02-12', 'Groceries',         'EXPENSE'),
    ( 2, 4,   42.00, '2026-02-08', 'Fuel',              'EXPENSE'),
    ( 3, 3,   60.00, '2026-02-10', 'Restaurant',        'EXPENSE'),
    ( 4, 5,  130.00, '2026-02-21', 'Electricity bill',  'EXPENSE'),
    ( 5, 4,   28.00, '2026-02-17', 'Bus pass',          'EXPENSE'),
    ( 6, 3,   77.60, '2026-02-19', 'Groceries',         'EXPENSE'),
    ( 7, 3,  210.00, '2026-02-23', 'Team dinner',       'EXPENSE'),
    ( 8, 5,  105.40, '2026-02-26', 'Water bill',        'EXPENSE'),
    ( 9, 3,   53.20, '2026-02-15', 'Groceries',         'EXPENSE'),
    (10, 5,   79.00, '2026-02-24', 'Internet bill',     'EXPENSE'),
    -- March: salaries, freelance and expenses
    ( 1, 1, 3500.00, '2026-03-01', 'March salary',      'INCOME'),
    ( 2, 1, 4200.00, '2026-03-01', 'March salary',      'INCOME'),
    ( 3, 1, 3300.00, '2026-03-01', 'March salary',      'INCOME'),
    ( 4, 1, 2800.00, '2026-03-01', 'March salary',      'INCOME'),
    ( 5, 1, 3100.00, '2026-03-01', 'March salary',      'INCOME'),
    ( 2, 2,  500.00, '2026-03-10', 'Side project',      'INCOME'),
    ( 9, 2,  640.00, '2026-03-12', 'Weekend freelance', 'INCOME'),
    ( 1, 3,   49.80, '2026-03-07', 'Groceries',         'EXPENSE'),
    ( 2, 5,   95.00, '2026-03-20', 'Internet bill',     'EXPENSE'),
    ( 3, 4,   31.50, '2026-03-16', 'Train ticket',      'EXPENSE'),
    ( 4, 3,   68.20, '2026-03-11', 'Groceries',         'EXPENSE'),
    ( 5, 3,   52.00, '2026-03-05', 'Groceries',         'EXPENSE'),
    ( 6, 4,  175.00, '2026-03-18', 'Flight to Berlin',  'EXPENSE'),
    ( 7, 5,  118.75, '2026-03-22', 'Electricity bill',  'EXPENSE'),
    ( 8, 4,   22.40, '2026-03-09', 'Bus tickets',       'EXPENSE'),
    ( 9, 3,   61.30, '2026-03-14', 'Groceries',         'EXPENSE'),
    (10, 3,  135.00, '2026-03-28', 'Birthday dinner',   'EXPENSE');


-- ============================================================
-- TODO TICKET-F005: Insert 3+ savings goals
-- ============================================================
-- WHAT: Savings goals track progress toward a financial target.
--       current_amount starts at 0 or a partial amount.
--
-- HOW:  Write 3+ INSERT INTO savings_goals (user_id, name, target_amount, current_amount, deadline) VALUES (...).
--       Examples: 'Holiday Fund' target 2000, current 500, deadline 2026-12-01
--       Assign goals to different users.
--
-- WHY:  The Savings Goals page needs this data to display progress bars.
--
-- OBSERVE: Run SELECT * FROM savings_goals; — check the progress percentages
--          (current_amount / target_amount * 100).

INSERT INTO savings_goals
    (user_id, goal_name, target_amount, current_amount, deadline) VALUES
    (1, 'Holiday Fund',     2000.00,  450.00, '2026-12-01'),
    (2, 'New Laptop',       1500.00, 1500.00, '2026-06-30'),
    (3, 'Emergency Buffer', 5000.00,  800.00, '2026-12-31'),
    (4, 'Wedding',         10000.00, 2500.00, '2027-09-15'),
    (6, 'Car Deposit',      4000.00, 1250.00, '2027-03-01'),
    (7, 'House Deposit',   25000.00, 6400.00, '2028-01-31'),
    (9, 'Rainy Day Fund',   3000.00,    0.00, NULL);



-- ============================================================
-- TODO TICKET-F004: Test constraint violations
-- ============================================================
-- WHAT: Constraints are rules the database enforces. If violated, the INSERT fails.
--       This is a safety net — even if application code has bugs, the database
--       prevents invalid data from being stored.
--
-- HOW:  Try these INSERTs (each should FAIL):
--         1. A transaction with amount = -10 (violates CHECK amount > 0)
--         2. A transaction with user_id = 999 (violates FOREIGN KEY — user doesn't exist)
--         3. A user with a duplicate email (violates UNIQUE constraint)
--         4. A category with type = 'INVALID' (violates CHECK type IN ('INCOME','EXPENSE'))
--
-- WHY:  Proving constraints work builds confidence in your schema design.
--       If constraints don't catch bad data, fix them before moving on.
--
-- OBSERVE: Each of the 4 INSERTs above should produce an ERROR message.
--          Read the error message — it tells you which constraint was violated.
--
-- The 4 statements are kept commented out so this script stays runnable end-to-end.
-- Paste them one at a time into psql to see each constraint fire.

-- 1. CHECK (amount > 0)
-- INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type)
-- VALUES (1, 3, -10.00, '2026-01-05', 'Negative amount', 'EXPENSE');
--   ERROR:  new row for relation "transactions" violates check constraint

-- 2. FOREIGN KEY (user_id) — user 999 does not exist
-- INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type)
-- VALUES (999, 3, 10.00, '2026-01-05', 'Ghost user', 'EXPENSE');
--   ERROR:  insert or update on table "transactions" violates foreign key constraint

-- 3. UNIQUE (email)
-- INSERT INTO users (name, email) VALUES ('Alice Clone', 'alice@bank.com');
--   ERROR:  duplicate key value violates unique constraint

-- 4. CHECK (type IN ('INCOME','EXPENSE'))
-- INSERT INTO categories (name, type) VALUES ('Mystery', 'INVALID');
--   ERROR:  new row for relation "categories" violates check constraint

-- Row counts after seeding: 5 categories, 10 users, 62 transactions, 7 savings goals.
SELECT COUNT(*) FROM categories;
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM transactions;
SELECT COUNT(*) FROM savings_goals;