import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console user interface. Reads input, calls the managers, prints results,
 * and saves data to files after every change.
 */
public class InventoryApp {

    private final Scanner scanner = new Scanner(System.in);
    private final FileManager fileManager;
    private final InventoryManager inventory;
    private final SaleManager saleManager;

    public InventoryApp() {
        fileManager = new FileManager("data");
        inventory = new InventoryManager(fileManager.loadProducts(), fileManager.loadThreshold());
        saleManager = new SaleManager(inventory, fileManager.loadSales());
    }

    public static void main(String[] args) {
        new InventoryApp().run();
    }

    // =====================================================
    //  MAIN MENU
    // =====================================================

    private void run() {
        System.out.println("Loaded " + inventory.getTotalProducts() + " product(s) and "
                + saleManager.getSales().size() + " sale(s) from the data folder.");
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter your choice: ", 1);
            System.out.println();
            switch (choice) {
                case 1:  addProduct();       break;
                case 2:  removeProduct();    break;
                case 3:  updateProduct();    break;
                case 4:  viewAllProducts();  break;
                case 5:  searchProduct();    break;
                case 6:  filterByCategory(); break;
                case 7:  manageStock();      break;
                case 8:  sellProduct();      break;
                case 9:  lowStockReport();   break;
                case 10: salesHistory();     break;
                case 11: inventoryReport();  break;
                case 12:
                    running = false;
                    System.out.println("Data saved. Goodbye!");
                    break;
                default:
                    System.out.println("  Invalid choice. Please enter a number from 1 to 12.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("===== INVENTORY & STOCK MANAGEMENT =====");
        System.out.println();
        System.out.println(" 1. Add Product");
        System.out.println(" 2. Remove Product");
        System.out.println(" 3. Update Product");
        System.out.println(" 4. View All Products");
        System.out.println(" 5. Search Product");
        System.out.println(" 6. Filter by Category");
        System.out.println(" 7. Add Stock");
        System.out.println(" 8. Sell Product");
        System.out.println(" 9. Low Stock Report");
        System.out.println("10. Sales History");
        System.out.println("11. Inventory Report");
        System.out.println("12. Exit");
        System.out.println();
    }

    // =====================================================
    //  PRODUCT MANAGEMENT
    // =====================================================

    private void addProduct() {
        printTitle("ADD PRODUCT");
        int id = readInt("Product ID: ", 1);
        if (inventory.findById(id) != null) {
            System.out.println("  Error: Product ID " + id + " already exists.");
            return;
        }
        String name = readString("Name: ");
        String category = readString("Category: ");
        double price = readDouble("Price: ", 0);
        int quantity = readInt("Initial stock: ", 0);

        try {
            inventory.addProduct(new Product(id, name, category, price, quantity));
            saveProducts();
            System.out.println("  Product added successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void removeProduct() {
        printTitle("REMOVE PRODUCT");
        int id = readInt("Product ID to remove: ", 1);
        Product p = inventory.findById(id);
        if (p == null) {
            System.out.println("  Error: No product found with ID " + id + ".");
            return;
        }
        String answer = readString("Remove '" + p.getName() + "'? (y/n): ");
        if (answer.equalsIgnoreCase("y")) {
            inventory.removeProduct(id);
            saveProducts();
            System.out.println("  Product removed. (Past sales records are kept.)");
        } else {
            System.out.println("  Cancelled.");
        }
    }

    private void updateProduct() {
        printTitle("UPDATE PRODUCT");
        int id = readInt("Product ID to update: ", 1);
        Product p = inventory.findById(id);
        if (p == null) {
            System.out.println("  Error: No product found with ID " + id + ".");
            return;
        }
        System.out.println("  (Press Enter to keep the current value)");

        String name = readOptional("Name [" + p.getName() + "]: ");
        if (!name.isEmpty()) {
            p.setName(name);
        }
        String category = readOptional("Category [" + p.getCategory() + "]: ");
        if (!category.isEmpty()) {
            p.setCategory(category);
        }
        while (true) {
            String priceText = readOptional("Price [" + formatMoney(p.getPrice()) + "]: ");
            if (priceText.isEmpty()) {
                break;
            }
            try {
                p.setPrice(Double.parseDouble(priceText));
                break;
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid number.");
            } catch (IllegalArgumentException e) {
                System.out.println("  " + e.getMessage());
            }
        }
        saveProducts();
        System.out.println("  Product updated successfully.");
    }

    private void viewAllProducts() {
        printTitle("ALL PRODUCTS");
        System.out.println("Sort by:  1. ID   2. Name   3. Price   4. Stock");
        int choice = readInt("Choose (1-4): ", 1);
        InventoryManager.SortBy sortBy;
        switch (choice) {
            case 2:  sortBy = InventoryManager.SortBy.NAME;  break;
            case 3:  sortBy = InventoryManager.SortBy.PRICE; break;
            case 4:  sortBy = InventoryManager.SortBy.STOCK; break;
            default: sortBy = InventoryManager.SortBy.ID;
        }
        System.out.println();
        printProducts(inventory.getAllProducts(sortBy));
    }

    private void searchProduct() {
        printTitle("SEARCH PRODUCT");
        System.out.println("1. Search by ID");
        System.out.println("2. Search by name");
        int choice = readInt("Choose (1-2): ", 1);
        System.out.println();
        if (choice == 1) {
            int id = readInt("Enter product ID: ", 1);
            Product p = inventory.findById(id);
            System.out.println();
            if (p == null) {
                System.out.println("  No product found with ID " + id + ".");
            } else {
                printProducts(List.of(p));
            }
        } else if (choice == 2) {
            String keyword = readString("Enter name (or part of it): ");
            System.out.println();
            printProducts(inventory.searchByName(keyword));
        } else {
            System.out.println("  Invalid choice.");
        }
    }

    private void filterByCategory() {
        printTitle("FILTER BY CATEGORY");
        Map<String, List<Product>> groups = inventory.groupByCategory();
        if (groups.isEmpty()) {
            System.out.println("  No products yet.");
            return;
        }
        System.out.println("Available categories: " + String.join(", ", groups.keySet()));
        String category = readString("Enter category: ");
        System.out.println();
        printProducts(inventory.filterByCategory(category));
    }

    // =====================================================
    //  STOCK MANAGEMENT
    // =====================================================

    private void manageStock() {
        printTitle("STOCK MANAGEMENT");
        System.out.println("1. Add stock");
        System.out.println("2. Reduce stock (damaged / lost items)");
        int choice = readInt("Choose (1-2): ", 1);
        if (choice != 1 && choice != 2) {
            System.out.println("  Invalid choice.");
            return;
        }
        int id = readInt("Product ID: ", 1);
        Product p = inventory.findById(id);
        if (p == null) {
            System.out.println("  Error: No product found with ID " + id + ".");
            return;
        }
        System.out.println("  " + p.getName() + " - current stock: " + p.getQuantity());
        int amount = readInt("Quantity: ", 1);

        try {
            if (choice == 1) {
                inventory.addStock(id, amount);
            } else {
                inventory.reduceStock(id, amount);
            }
            saveProducts();
            System.out.println("  Stock updated. Current stock of " + p.getName() + ": " + p.getQuantity());
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void lowStockReport() {
        printTitle("LOW STOCK REPORT");
        System.out.println("Current threshold: " + inventory.getLowStockThreshold()
                + " (products with this much stock or less are listed)");
        System.out.println();
        printProducts(inventory.getLowStockProducts());

        System.out.println();
        String answer = readString("Change the threshold? (y/n): ");
        if (answer.equalsIgnoreCase("y")) {
            int newValue = readInt("New threshold: ", 0);
            inventory.setLowStockThreshold(newValue);
            fileManager.saveThreshold(newValue);
            System.out.println("  Threshold set to " + newValue + ".");
            System.out.println();
            printProducts(inventory.getLowStockProducts());
        }
    }

    // =====================================================
    //  SALES
    // =====================================================

    private void sellProduct() {
        printTitle("SELL PRODUCT");
        int id = readInt("Product ID: ", 1);
        Product p = inventory.findById(id);
        if (p == null) {
            System.out.println("  Error: No product found with ID " + id + ".");
            return;
        }
        System.out.println("  " + p.getName() + " | Price: " + formatMoney(p.getPrice())
                + " | In stock: " + p.getQuantity());
        int quantity = readInt("Quantity to sell: ", 1);

        try {
            Sale sale = saleManager.sellProduct(id, quantity);
            saveProducts();
            fileManager.saveSales(saleManager.getSales());
            printReceipt(sale);
            if (p.isLowStock(inventory.getLowStockThreshold())) {
                System.out.println("  Note: '" + p.getName() + "' is now low on stock ("
                        + p.getQuantity() + " left).");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private void printReceipt(Sale sale) {
        System.out.println();
        System.out.println("  ------------- SALE RECEIPT -------------");
        System.out.println("  Sale ID    : " + sale.getSaleId());
        System.out.println("  Date/Time  : " + sale.getFormattedDateTime());
        System.out.println("  Product    : " + sale.getProductName());
        System.out.println("  Quantity   : " + sale.getQuantity());
        System.out.println("  Unit Price : " + formatMoney(sale.getUnitPrice()));
        System.out.println("  TOTAL      : " + formatMoney(sale.getTotalAmount()));
        System.out.println("  ----------------------------------------");
    }

    private void salesHistory() {
        printTitle("SALES HISTORY");
        List<Sale> sales = saleManager.getSales();
        if (sales.isEmpty()) {
            System.out.println("  No sales recorded yet.");
            return;
        }
        String format = "%-6s %-20s %-22s %5s %12s %12s%n";
        System.out.printf(format, "Sale", "Date/Time", "Product", "Qty", "Unit Price", "Total");
        printLine(82);
        for (Sale s : sales) {
            System.out.printf(format, s.getSaleId(), s.getFormattedDateTime(),
                    truncate(s.getProductName(), 22), s.getQuantity(),
                    formatMoney(s.getUnitPrice()), formatMoney(s.getTotalAmount()));
        }
        printLine(82);
        System.out.println("Total sales: " + sales.size() + "   |   Total revenue: "
                + formatMoney(saleManager.getTotalSalesAmount()));
    }

    // =====================================================
    //  REPORTS
    // =====================================================

    private void inventoryReport() {
        printTitle("INVENTORY REPORT");
        System.out.println("Total products        : " + inventory.getTotalProducts());
        System.out.println("Total stock quantity  : " + inventory.getTotalStock());
        System.out.println("Inventory value       : " + formatMoney(inventory.getTotalInventoryValue()));
        System.out.println("Total sales amount    : " + formatMoney(saleManager.getTotalSalesAmount()));
        System.out.println("Total units sold      : " + saleManager.getTotalUnitsSold());

        System.out.println();
        System.out.println("--- Low-stock products (threshold: " + inventory.getLowStockThreshold() + ") ---");
        printProducts(inventory.getLowStockProducts());

        System.out.println();
        System.out.println("--- Most sold products (top 5) ---");
        List<Map.Entry<String, Integer>> top = saleManager.getMostSoldProducts(5);
        if (top.isEmpty()) {
            System.out.println("  No sales yet.");
        }
        int rank = 1;
        for (Map.Entry<String, Integer> entry : top) {
            System.out.println("  " + rank + ". " + entry.getKey() + " - " + entry.getValue() + " unit(s)");
            rank++;
        }

        System.out.println();
        System.out.println("--- Category-wise summary ---");
        Map<String, List<Product>> groups = inventory.groupByCategory();
        if (groups.isEmpty()) {
            System.out.println("  No products yet.");
            return;
        }
        String format = "%-18s %9s %12s %14s%n";
        System.out.printf(format, "Category", "Products", "Total Stock", "Stock Value");
        printLine(56);
        for (Map.Entry<String, List<Product>> group : groups.entrySet()) {
            int stock = 0;
            double value = 0;
            for (Product p : group.getValue()) {
                stock += p.getQuantity();
                value += p.getPrice() * p.getQuantity();
            }
            System.out.printf(format, truncate(group.getKey(), 18),
                    group.getValue().size(), stock, formatMoney(value));
        }
    }

    // =====================================================
    //  DISPLAY HELPERS
    // =====================================================

    private void printProducts(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("  No products found.");
            return;
        }
        String format = "%-6s %-24s %-14s %12s %7s  %-5s%n";
        System.out.printf(format, "ID", "Name", "Category", "Price", "Stock", "Flag");
        printLine(74);
        for (Product p : products) {
            String flag = p.isLowStock(inventory.getLowStockThreshold()) ? "LOW" : "";
            System.out.printf(format, p.getId(), truncate(p.getName(), 24),
                    truncate(p.getCategory(), 14), formatMoney(p.getPrice()),
                    p.getQuantity(), flag);
        }
        printLine(74);
        System.out.println(products.size() + " product(s) listed.");
    }

    private void printTitle(String title) {
        System.out.println("--- " + title + " ---");
    }

    private void printLine(int length) {
        System.out.println("-".repeat(length));
    }

    private String formatMoney(double amount) {
        return String.format("Rs. %.2f", amount);
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    // =====================================================
    //  INPUT HELPERS (keep asking until the input is valid)
    // =====================================================

    private String readLine() {
        if (!scanner.hasNextLine()) {          // input stream closed (e.g. Ctrl+D)
            System.out.println("\nInput closed. Exiting. (Data was already saved.)");
            System.exit(0);
        }
        return scanner.nextLine();
    }

    /** Reads text; keeps asking until something non-empty is typed. */
    private String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = readLine().trim();
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("  Input cannot be empty.");
        }
    }

    /** Reads text; an empty answer is allowed. */
    private String readOptional(String prompt) {
        System.out.print(prompt);
        return readLine().trim();
    }

    /** Reads a whole number that is at least 'min'. */
    private int readInt(String prompt, int min) {
        while (true) {
            System.out.print(prompt);
            String text = readLine().trim();
            try {
                int value = Integer.parseInt(text);
                if (value >= min) {
                    return value;
                }
                System.out.println("  Value must be at least " + min + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid whole number.");
            }
        }
    }

    /** Reads a decimal number that is at least 'min'. */
    private double readDouble(String prompt, double min) {
        while (true) {
            System.out.print(prompt);
            String text = readLine().trim();
            try {
                double value = Double.parseDouble(text);
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    System.out.println("  Please enter a valid number.");
                } else if (value < min) {
                    System.out.println("  Value cannot be less than " + (int) min + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("  Please enter a valid number.");
            }
        }
    }

    private void saveProducts() {
        fileManager.saveProducts(inventory.getProductMap());
    }
}
