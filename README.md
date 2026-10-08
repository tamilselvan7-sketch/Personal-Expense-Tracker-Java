# Personal Expense Tracker (Java)

A simple console-based application to record and manage daily expenses, built with core Java only. No database, no GUI, no external libraries.

## Features

- Add, view, update and delete expenses
- Search expenses by category or by date
- Total expenses and category-wise totals
- Highest expense finder
- Sort expenses by amount (low to high or high to low)
- Automatic loading from and saving to `expenses.txt`
- Input validation (dates, positive amounts, valid IDs) with exception handling

## Project Structure

```
Personal-Expense-Tracker-Java/
├── Expense.java          # Expense model (fields, constructor, getters/setters, toString)
├── ExpenseTracker.java   # Main class: menu and all operations
├── expenses.txt          # Saved data (one expense per line)
└── README.md
```

## Java Concepts Used

Classes and objects, constructors, encapsulation (private fields, getters/setters), `ArrayList`, `TreeMap`, methods, if-else, switch-case, loops, exception handling (`InputMismatchException`, `NumberFormatException`, `IOException`, `DateTimeParseException`), file handling (`FileReader`, `FileWriter`, `BufferedReader`, `PrintWriter`), searching, sorting, and string handling.

## How to Run

Requires JDK 8 or later.

```bash
javac Expense.java ExpenseTracker.java
java ExpenseTracker
```

Run the commands from the project folder so `expenses.txt` is found.

## Data File Format

Each line: `id|date|category|description|amount`

```
1|2026-10-01|Food|Lunch at cafe|250.0
```

## Sample Output

```
5 expense(s) loaded from expenses.txt.

===== PERSONAL EXPENSE TRACKER =====
1. Add Expense
...
12. Exit
Enter your choice: 5
Total Expenses: 3399.50 (5 records)

Enter your choice: 6
--- Category-wise Expenses ---
Education       : 899.50
Entertainment   : 450.00
Food            : 1450.00
Transport       : 600.00

Enter your choice: 9
Highest Expense:
ID: 4   | Date: 2026-10-05 | Category: Food           | Amount:   1200.00 | Groceries
```

## Future Enhancements

1. Monthly and yearly expense reports
2. Budget limits with overspending alerts
3. Export data to CSV
4. Date-range search
5. Recurring expenses (rent, subscriptions)

## License

Free to use for learning purposes.
