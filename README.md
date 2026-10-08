# Inventory & Stock Management System

A console-based inventory and sales tracker written in **Core Java**. It manages products, stock levels, and sales, and saves everything to plain text files, so your data is still there the next time you run it.

Built as a learning project: no database, no GUI, no external libraries, and no frameworks.

## Features

| Area | What you can do |
|------|-----------------|
| **Products** | Add, remove, update, view, search (by ID or name), filter by category |
| **Stock** | Add stock, reduce stock, see current stock, low-stock alerts with a configurable threshold |
| **Sales** | Sell products (stock reduces automatically), receipts, date/time stamps, full sales history |
| **Reports** | Total products, total stock, inventory value, total sales, most-sold products, category-wise summary |
| **Sorting** | Sort by ID, name, price, or stock quantity |
| **Persistence** | Data saved to text files after every change and loaded on start-up |

## Concepts Demonstrated

- **OOP**: encapsulation, constructors, private fields, getters/setters with validation
- **Collections**: `HashMap<Integer, Product>` for products, `ArrayList<Sale>` for sales, `TreeMap` for category grouping
- **Sorting**: `Comparator` and `Collections.sort`
- **File handling**: `BufferedReader` / `BufferedWriter` with try-with-resources
- **Exception handling**: input validation, corrupted-file recovery
- **Enum**: `InventoryManager.SortBy`
- **Java time API**: `LocalDateTime` and `DateTimeFormatter`

## Project Structure

```
InventoryStockManagement/
├── src/
│   ├── InventoryApp.java      # Console menu and user input/output
│   ├── Product.java           # Product model
│   ├── Sale.java              # Sale record model
│   ├── InventoryManager.java  # Product and stock logic
│   ├── SaleManager.java       # Sales logic and statistics
│   └── FileManager.java       # Reads/writes the text files
├── sample-data/               # Example data files you can copy to try the app
├── data/                      # Created automatically on first run (git-ignored)
├── README.md
└── .gitignore
```

## How the Classes Work Together

```
InventoryApp  (menu, Scanner input, printing)
   ├── InventoryManager ── HashMap<Integer, Product>
   ├── SaleManager ─────── ArrayList<Sale>   (uses InventoryManager to check/reduce stock)
   └── FileManager ─────── data/products.txt, data/sales.txt, data/config.txt
```

- `InventoryApp` is the only class that talks to the user.
- `InventoryManager` and `SaleManager` hold the rules and never print or touch files.
- `FileManager` is the only class that touches files.

## Requirements

- JDK 11 or newer (`javac -version` to check)

## Compile and Run

From the project root:

**Windows / macOS / Linux**
```bash
mkdir out
javac -d out src/*.java
java -cp out InventoryApp
```

> On Windows PowerShell, use `javac -d out (Get-ChildItem src\*.java)` if the `*.java` wildcard is not expanded.

**Try it with sample data** (optional):
```bash
mkdir data
cp sample-data/* data/        # Windows: copy sample-data\* data\
java -cp out InventoryApp
```

## Data File Formats

Files use the `|` character as a separator. Lines starting with `#` are comments.

**data/products.txt** : `id|name|category|price|quantity`
```
101|Laptop|Electronics|55000.0|12
103|Notebook A4|Stationery|45.0|4
```

**data/sales.txt** : `saleId|dateTime|productId|productName|quantity|unitPrice|total`
```
1|2026-10-05 10:15:22|102|Wireless Mouse|2|799.5|1599.0
```

**data/config.txt** : the low-stock threshold (a single number)
```
5
```

Missing files are treated as empty. Invalid lines are skipped with a warning instead of crashing the program.

## Validation Rules

- Product ID must be positive and unique
- Price cannot be negative
- Stock cannot be negative
- Quantity sold must be positive and cannot exceed available stock
- Invalid menu or number input is re-requested instead of crashing

## Possible Improvements

- Supplier and purchase-order tracking
- Export reports to CSV
- Sales filtering by date range
- Unit tests with JUnit
- Replace text files with a database (JDBC / SQLite)

## License

Free to use for learning purposes.
