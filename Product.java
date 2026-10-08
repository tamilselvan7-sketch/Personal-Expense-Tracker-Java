/**
 * Represents one product in the inventory.
 * All fields are private; validation happens in the constructor and setters.
 */
public class Product {

    private int id;
    private String name;
    private String category;
    private double price;
    private int quantity;

    public Product(int id, String name, String category, double price, int quantity) {
        if (id <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number.");
        }
        this.id = id;
        setName(name);
        setCategory(category);
        setPrice(price);
        setQuantity(quantity);
    }

    // ---------- Getters ----------
    public int getId()          { return id; }
    public String getName()     { return name; }
    public String getCategory() { return category; }
    public double getPrice()    { return price; }
    public int getQuantity()    { return quantity; }

    // ---------- Setters (with validation) ----------
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        this.name = clean(name);
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty.");
        }
        this.category = clean(category);
    }

    public void setPrice(double price) {
        if (Double.isNaN(price) || Double.isInfinite(price) || price < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
        this.price = price;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Stock cannot be negative.");
        }
        this.quantity = quantity;
    }

    /** True when stock is at or below the given threshold. */
    public boolean isLowStock(int threshold) {
        return quantity <= threshold;
    }

    // ---------- File conversion ----------
    /** Converts this product to one line of text: id|name|category|price|quantity */
    public String toFileString() {
        return id + "|" + name + "|" + category + "|" + price + "|" + quantity;
    }

    /** Builds a Product from a line in products.txt. Throws IllegalArgumentException if the line is bad. */
    public static Product fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 5) {
            throw new IllegalArgumentException("expected 5 fields but found " + parts.length);
        }
        try {
            int id = Integer.parseInt(parts[0].trim());
            double price = Double.parseDouble(parts[3].trim());
            int quantity = Integer.parseInt(parts[4].trim());
            return new Product(id, parts[1], parts[2], price, quantity);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid number in line");
        }
    }

    // The '|' character is our file separator, so it is replaced inside text fields.
    private static String clean(String text) {
        return text.trim().replace("|", "/");
    }
}
