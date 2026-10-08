import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ExpenseTracker {
    private static final String FILE_NAME = "expenses.txt";
    private static final ArrayList<Expense> expenses = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadExpenses();
        boolean running = true;

        while (running) {
            showMenu();
            int choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:  addExpense();           break;
                case 2:  viewAllExpenses();      break;
                case 3:  searchByCategory();     break;
                case 4:  searchByDate();         break;
                case 5:  showTotalExpenses();    break;
                case 6:  showCategoryWise();     break;
                case 7:  updateExpense();        break;
                case 8:  deleteExpense();        break;
                case 9:  showHighestExpense();   break;
                case 10: sortByAmount();         break;
                case 11: saveExpenses();         break;
                case 12:
                    saveExpenses();              // save automatically before exiting
                    System.out.println("Goodbye! Your expenses have been saved.");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice! Please enter a number from 1 to 12.");
            }
        }
        scanner.close();
    }

    private static void showMenu() {
        System.out.println("\n===== PERSONAL EXPENSE TRACKER =====");
        System.out.println("1. Add Expense");
        System.out.println("2. View All Expenses");
        System.out.println("3. Search by Category");
        System.out.println("4. Search by Date");
        System.out.println("5. Calculate Total Expenses");
        System.out.println("6. Category-wise Expenses");
        System.out.println("7. Update Expense");
        System.out.println("8. Delete Expense");
        System.out.println("9. Highest Expense");
        System.out.println("10. Sort by Amount");
        System.out.println("11. Save Expenses");
        System.out.println("12. Exit");
    }

    // ---------- Main operations ----------

    private static void addExpense() {
        int id = generateId();
        String date = readDate("Enter date (yyyy-MM-dd): ");
        String category = readText("Enter category: ");
        String description = readText("Enter description: ");
        double amount = readAmount("Enter amount: ");

        expenses.add(new Expense(id, date, category, description, amount));
        System.out.println("Expense added successfully with ID " + id + ".");
    }

    private static void viewAllExpenses() {
        printList(expenses, "No expenses recorded yet.");
    }

    private static void searchByCategory() {
        String category = readText("Enter category to search: ");
        ArrayList<Expense> results = new ArrayList<>();

        for (Expense expense : expenses) {
            if (expense.getCategory().equalsIgnoreCase(category)) {
                results.add(expense);
            }
        }
        printList(results, "No expenses found in category '" + category + "'.");
    }

    private static void searchByDate() {
        String date = readDate("Enter date to search (yyyy-MM-dd): ");
        ArrayList<Expense> results = new ArrayList<>();

        for (Expense expense : expenses) {
            if (expense.getDate().equals(date)) {
                results.add(expense);
            }
        }
        printList(results, "No expenses found on " + date + ".");
    }

    private static void showTotalExpenses() {
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        System.out.printf("Total Expenses: %.2f (%d records)%n", total, expenses.size());
    }

    private static void showCategoryWise() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded yet.");
            return;
        }

        // TreeMap keeps categories sorted; the comparator ignores upper/lower case
        Map<String, Double> totals = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Expense expense : expenses) {
            double current = totals.getOrDefault(expense.getCategory(), 0.0);
            totals.put(expense.getCategory(), current + expense.getAmount());
        }

        System.out.println("--- Category-wise Expenses ---");
        for (Map.Entry<String, Double> entry : totals.entrySet()) {
            System.out.printf("%-15s : %.2f%n", entry.getKey(), entry.getValue());
        }
    }

    private static void updateExpense() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses to update.");
            return;
        }

        int id = readInt("Enter the ID of the expense to update: ");
        Expense expense = findById(id);
        if (expense == null) {
            System.out.println("No expense found with ID " + id + ".");
            return;
        }

        System.out.println("Current: " + expense);
        System.out.println("Press Enter to keep the current value.");

        String date = readOptional("New date (yyyy-MM-dd): ");
        if (!date.isEmpty()) {
            if (isValidDate(date)) {
                expense.setDate(date);
            } else {
                System.out.println("Invalid date. Date was not changed.");
            }
        }

        String category = readOptional("New category: ");
        if (!category.isEmpty()) {
            expense.setCategory(clean(category));
        }

        String description = readOptional("New description: ");
        if (!description.isEmpty()) {
            expense.setDescription(clean(description));
        }

        String amountText = readOptional("New amount: ");
        if (!amountText.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountText);
                if (amount > 0) {
                    expense.setAmount(amount);
                } else {
                    System.out.println("Amount must be greater than zero. Amount was not changed.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Amount was not changed.");
            }
        }
        System.out.println("Expense updated: " + expense);
    }

    private static void deleteExpense() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses to delete.");
            return;
        }

        int id = readInt("Enter the ID of the expense to delete: ");
        Expense expense = findById(id);
        if (expense == null) {
            System.out.println("No expense found with ID " + id + ".");
        } else {
            expenses.remove(expense);
            System.out.println("Expense with ID " + id + " deleted.");
        }
    }

    private static void showHighestExpense() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded yet.");
            return;
        }

        Expense highest = expenses.get(0);
        for (Expense expense : expenses) {
            if (expense.getAmount() > highest.getAmount()) {
                highest = expense;
            }
        }
        System.out.println("Highest Expense:");
        System.out.println(highest);
    }

    private static void sortByAmount() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses to sort.");
            return;
        }

        int order = readInt("Sort order - 1. Low to High, 2. High to Low: ");
        ArrayList<Expense> sorted = new ArrayList<>(expenses);   // copy so the original order stays

        if (order == 1) {
            Collections.sort(sorted, (a, b) -> Double.compare(a.getAmount(), b.getAmount()));
        } else if (order == 2) {
            Collections.sort(sorted, (a, b) -> Double.compare(b.getAmount(), a.getAmount()));
        } else {
            System.out.println("Invalid option. Please choose 1 or 2.");
            return;
        }
        printList(sorted, "No expenses to show.");
    }

    // ---------- File handling ----------

    // File format (one expense per line): id|date|category|description|amount
    private static void saveExpenses() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Expense e : expenses) {
                writer.println(e.getId() + "|" + e.getDate() + "|" + e.getCategory() + "|"
                        + e.getDescription() + "|" + e.getAmount());
            }
            System.out.println("Expenses saved to " + FILE_NAME + ".");
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    private static void loadExpenses() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("No saved data found. Starting with an empty list.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length != 5) {
                    continue;   // skip lines that are not in the expected format
                }
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    double amount = Double.parseDouble(parts[4].trim());
                    expenses.add(new Expense(id, parts[1].trim(), parts[2].trim(), parts[3].trim(), amount));
                } catch (NumberFormatException e) {
                    System.out.println("Skipped a corrupted line: " + line);
                }
            }
            System.out.println(expenses.size() + " expense(s) loaded from " + FILE_NAME + ".");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    // ---------- Helper methods ----------

    private static int generateId() {
        int maxId = 0;
        for (Expense expense : expenses) {
            if (expense.getId() > maxId) {
                maxId = expense.getId();
            }
        }
        return maxId + 1;
    }

    private static Expense findById(int id) {
        for (Expense expense : expenses) {
            if (expense.getId() == id) {
                return expense;
            }
        }
        return null;
    }

    private static void printList(ArrayList<Expense> list, String emptyMessage) {
        if (list.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Expense expense : list) {
            System.out.println(expense);
        }
    }

    // Replaces '|' so the text can never break the file format
    private static String clean(String text) {
        return text.trim().replace("|", "/");
    }

    private static boolean isValidDate(String date) {
        try {
            LocalDate.parse(date);   // accepts yyyy-MM-dd only
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // ---------- Input helpers (keep asking until the input is valid) ----------

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = scanner.nextInt();
                scanner.nextLine();   // clear the leftover newline
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a whole number.");
                scanner.nextLine();   // discard the bad input
            }
        }
    }

    private static double readAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = scanner.nextDouble();
                scanner.nextLine();
                if (value > 0) {
                    return value;
                }
                System.out.println("Amount must be greater than zero.");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a number (e.g. 250.50).");
                scanner.nextLine();
            }
        }
    }

    private static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = clean(scanner.nextLine());
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    private static String readOptional(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static String readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String date = scanner.nextLine().trim();
            if (isValidDate(date)) {
                return date;
            }
            System.out.println("Invalid date! Use the format yyyy-MM-dd (e.g. 2026-10-07).");
        }
    }
}