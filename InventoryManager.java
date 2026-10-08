import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Holds all products (in a HashMap keyed by product ID) and contains
 * the business logic for products and stock. It does no printing and no file work.
 */
public class InventoryManager {

    /** The ways products can be sorted. */
    public enum SortBy { ID, NAME, PRICE, STOCK }

    private Map<Integer, Product> products;
    private int lowStockThreshold;

    public InventoryManager(Map<Integer, Product> products, int lowStockThreshold) {
        this.products = products;
        this.lowStockThreshold = lowStockThreshold;
    }

    // ---------- Product management ----------

    public void addProduct(Product product) {
        if (products.containsKey(product.getId())) {
            throw new IllegalArgumentException("Product ID " + product.getId() + " already exists.");
        }
        products.put(product.getId(), product);
    }

    public Product removeProduct(int id) {
        Product removed = products.remove(id);
        if (removed == null) {
            throw new IllegalArgumentException("No product found with ID " + id + ".");
        }
        return removed;
    }

    /** Returns the product or null if it does not exist. */
    public Product findById(int id) {
        return products.get(id);
    }

    /** Case-insensitive "contains" search on the product name. */
    public List<Product> searchByName(String keyword) {
        List<Product> result = new ArrayList<>();
        String lowerKeyword = keyword.trim().toLowerCase();
        for (Product p : products.values()) {
            if (p.getName().toLowerCase().contains(lowerKeyword)) {
                result.add(p);
            }
        }
        return sort(result, SortBy.ID);
    }

    public List<Product> filterByCategory(String category) {
        List<Product> result = new ArrayList<>();
        for (Product p : products.values()) {
            if (p.getCategory().equalsIgnoreCase(category.trim())) {
                result.add(p);
            }
        }
        return sort(result, SortBy.ID);
    }

    public List<Product> getAllProducts(SortBy sortBy) {
        return sort(new ArrayList<>(products.values()), sortBy);
    }

    private List<Product> sort(List<Product> list, SortBy sortBy) {
        Comparator<Product> comparator;
        switch (sortBy) {
            case NAME:
                comparator = Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case PRICE:
                comparator = Comparator.comparingDouble(Product::getPrice);
                break;
            case STOCK:
                comparator = Comparator.comparingInt(Product::getQuantity);
                break;
            default:
                comparator = Comparator.comparingInt(Product::getId);
        }
        Collections.sort(list, comparator);
        return list;
    }

    // ---------- Stock management ----------

    public void addStock(int id, int amount) {
        Product p = getExistingProduct(id);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to add must be positive.");
        }
        p.setQuantity(p.getQuantity() + amount);
    }

    public void reduceStock(int id, int amount) {
        Product p = getExistingProduct(id);
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to reduce must be positive.");
        }
        if (amount > p.getQuantity()) {
            throw new IllegalArgumentException(
                    "Only " + p.getQuantity() + " unit(s) of '" + p.getName() + "' available.");
        }
        p.setQuantity(p.getQuantity() - amount);
    }

    public List<Product> getLowStockProducts() {
        List<Product> result = new ArrayList<>();
        for (Product p : products.values()) {
            if (p.isLowStock(lowStockThreshold)) {
                result.add(p);
            }
        }
        return sort(result, SortBy.STOCK);   // lowest stock first
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold cannot be negative.");
        }
        this.lowStockThreshold = threshold;
    }

    // ---------- Report helpers ----------

    public int getTotalProducts() {
        return products.size();
    }

    public int getTotalStock() {
        int total = 0;
        for (Product p : products.values()) {
            total += p.getQuantity();
        }
        return total;
    }

    public double getTotalInventoryValue() {
        double total = 0;
        for (Product p : products.values()) {
            total += p.getPrice() * p.getQuantity();
        }
        return total;
    }

    /** Groups products by category (categories are sorted alphabetically). */
    public Map<String, List<Product>> groupByCategory() {
        Map<String, List<Product>> groups = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (Product p : products.values()) {
            if (!groups.containsKey(p.getCategory())) {
                groups.put(p.getCategory(), new ArrayList<>());
            }
            groups.get(p.getCategory()).add(p);
        }
        return groups;
    }

    /** Used by FileManager when saving. */
    public Map<Integer, Product> getProductMap() {
        return new HashMap<>(products);
    }

    private Product getExistingProduct(int id) {
        Product p = products.get(id);
        if (p == null) {
            throw new IllegalArgumentException("No product found with ID " + id + ".");
        }
        return p;
    }
}
