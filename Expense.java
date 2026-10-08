/**
 * Represents a single expense entry.
 * All fields are private (encapsulation) and accessed through getters/setters.
 */
public class Expense {
    private int id;
    private String date;        // format: yyyy-MM-dd
    private String category;
    private String description;
    private double amount;

    // Parameterized constructor
    public Expense(int id, String date, String category, String description, double amount) {
        this.id = id;
        this.date = date;
        this.category = category;
        this.description = description;
        this.amount = amount;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    // Setters (ID is not changed after creation, so it has no setter)
    public void setDate(String date) {
        this.date = date;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return String.format("ID: %-3d | Date: %s | Category: %-14s | Amount: %9.2f | %s",
                id, date, category, amount, description);
    }
}