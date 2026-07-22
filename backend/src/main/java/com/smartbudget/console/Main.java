package com.smartbudget.console;

import com.smartbudget.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final List<Transaction> TXNS = new ArrayList<>();

    public static void main(String[] args) {
        seed();

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== SmartBudget Console ===");
            System.out.println("1) List Transactions");
            System.out.println("2) Add Transaction");
            System.out.println("3) Summary");
            System.out.println("4) Exit");
            System.out.print("Choice: ");

            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Please enter a number 1-4.");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1 -> listTransactions();
                case 2 -> addTransaction(scanner);
                case 3 -> showSummary();
                case 4 -> running = false;
                default -> System.out.println("Unknown option: " + choice);
            }
        }

        scanner.close();
        System.out.println("Goodbye!");
    }

    private static void seed() {
        TXNS.add(t(1, 1, 1, "3500.00", "2026-01-01", "January salary", "INCOME"));
        TXNS.add(t(2, 1, 3, "45.20", "2026-01-08", "Groceries", "EXPENSE"));
        TXNS.add(t(3, 1, 4, "25.00", "2026-01-15", "Bus pass", "EXPENSE"));
        TXNS.add(t(4, 2, 1, "4200.00", "2026-01-01", "January salary", "INCOME"));
        TXNS.add(t(5, 2, 5, "120.00", "2026-01-20", "Electricity bill", "EXPENSE"));
        TXNS.add(t(6, 3, 2, "800.00", "2026-02-05", "Freelance gig", "INCOME"));
        TXNS.add(t(7, 3, 3, "60.00", "2026-02-10", "Restaurant", "EXPENSE"));
        TXNS.add(t(8, 1, 1, "3500.00", "2026-02-01", "February salary", "INCOME"));
        TXNS.add(t(9, 4, 1, "2800.00", "2026-02-01", "February salary", "INCOME"));
        TXNS.add(t(10, 5, 3, "52.00", "2026-03-05", "Groceries", "EXPENSE"));
        System.out.println("Seeded " + TXNS.size() + " transactions");
    }

    private static Transaction t(int id, int uid, int cid, String amt,
                                 String date, String desc, String type) {
        return new Transaction(id, uid, cid, new BigDecimal(amt),
                LocalDate.parse(date), desc, type);
    }

    private static void listTransactions() {
        if (TXNS.isEmpty()) {
            System.out.println("(no transactions)");
            return;
        }
        String fmt = "%-5s %-20s %-8s %10s %-12s%n";
        System.out.printf(fmt, "ID", "Description", "Type", "Amount", "Date");
        System.out.println("-".repeat(60));

        String rowFmt = "%-5d %-20s %-8s %10.2f %-12s%n";
        for (Transaction t : TXNS) {
            System.out.printf(rowFmt,
                    t.getTxnId(),
                    t.getDescription(),
                    t.getType(),
                    t.getAmount(),
                    t.getTxnDate());
        }
        System.out.println("-".repeat(60));
        System.out.println("Total rows: " + TXNS.size());
    }

    private static void addTransaction(Scanner scanner) {
        System.out.print("Amount: ");
        BigDecimal amount;
        try {
            amount = new BigDecimal(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Transaction not added.");
            return;
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Amount must be positive. Transaction not added.");
            return;
        }

        System.out.print("Date (yyyy-mm-dd): ");
        LocalDate date;
        try {
            date = LocalDate.parse(scanner.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid date format. Transaction not added.");
            return;
        }
        if (date.isAfter(LocalDate.now())) {
            System.out.println("Date cannot be in the future. Transaction not added.");
            return;
        }

        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();
        if (desc.isEmpty()) {
            System.out.println("Description required. Transaction not added.");
            return;
        }

        System.out.print("Type (INCOME/EXPENSE): ");
        String type = scanner.nextLine().trim().toUpperCase();
        if (!"INCOME".equals(type) && !"EXPENSE".equals(type)) {
            System.out.println("Type must be INCOME or EXPENSE. Transaction not added.");
            return;
        }

        int nextId = TXNS.size() + 1;
        TXNS.add(new Transaction(nextId, 1, 1, amount, date, desc, type));
        System.out.println("Added transaction #" + nextId);
    }

    private static void showSummary() {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (Transaction t : TXNS) {
            if ("INCOME".equals(t.getType())) {
                income = income.add(t.getAmount());
            } else if ("EXPENSE".equals(t.getType())) {
                expense = expense.add(t.getAmount());
            }
        }
        BigDecimal net = income.subtract(expense);

        System.out.println();
        System.out.println("=== Summary ===");
        System.out.printf("Total Income:    %12.2f%n", income);
        System.out.printf("Total Expenses:  %12.2f%n", expense);
        System.out.println("-".repeat(28));
        System.out.printf("Net Balance:     %12.2f%n", net);
    }
}