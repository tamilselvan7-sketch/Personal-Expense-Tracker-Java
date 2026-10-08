import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * One completed sale. Product name and price are copied into the record,
 * so the history stays correct even if the product is later changed or removed.
 */
public class Sale {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private int saleId;
    private int productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double totalAmount;
    private LocalDateTime dateTime;

    public Sale(int saleId, int productId, String productName,
                int quantity, double unitPrice, LocalDateTime dateTime) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity sold must be positive.");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative.");
        }
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = quantity * unitPrice;   // total is always calculated
        this.dateTime = dateTime;
    }

    public int getSaleId()            { return saleId; }
    public int getProductId()         { return productId; }
    public String getProductName()    { return productName; }
    public int getQuantity()          { return quantity; }
    public double getUnitPrice()      { return unitPrice; }
    public double getTotalAmount()    { return totalAmount; }
    public LocalDateTime getDateTime(){ return dateTime; }

    public String getFormattedDateTime() {
        return dateTime.format(FORMAT);
    }

    /** Line format: saleId|dateTime|productId|productName|quantity|unitPrice|total */
    public String toFileString() {
        return saleId + "|" + getFormattedDateTime() + "|" + productId + "|"
                + productName + "|" + quantity + "|" + unitPrice + "|" + totalAmount;
    }

    /** Builds a Sale from a line in sales.txt. Throws IllegalArgumentException if the line is bad. */
    public static Sale fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 7) {
            throw new IllegalArgumentException("expected 7 fields but found " + parts.length);
        }
        try {
            int saleId = Integer.parseInt(parts[0].trim());
            LocalDateTime time = LocalDateTime.parse(parts[1].trim(), FORMAT);
            int productId = Integer.parseInt(parts[2].trim());
            int quantity = Integer.parseInt(parts[4].trim());
            double unitPrice = Double.parseDouble(parts[5].trim());
            // parts[6] (total) is stored for readability only; we recalculate it.
            return new Sale(saleId, productId, parts[3], quantity, unitPrice, time);
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException("invalid number or date in line");
        }
    }
}
