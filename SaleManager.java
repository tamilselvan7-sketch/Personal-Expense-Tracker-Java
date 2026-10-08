import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles sales: validates and records them, and calculates sales statistics.
 * It uses InventoryManager to check and reduce stock.
 */
public class SaleManager {

    private InventoryManager inventory;
    private List<Sale> sales;
    private int nextSaleId;

    public SaleManager(InventoryManager inventory, List<Sale> loadedSales) {
        this.inventory = inventory;
        this.sales = loadedSales;

        // Continue numbering after the highest saved sale ID
        int maxId = 0;
        for (Sale s : sales) {
            if (s.getSaleId() > maxId) {
                maxId = s.getSaleId();
            }
        }
        this.nextSaleId = maxId + 1;
    }

    /**
     * Sells a product: checks everything, reduces stock, records the sale.
     * Throws IllegalArgumentException with a friendly message if the sale is not allowed.
     */
    public Sale sellProduct(int productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity sold must be positive.");
        }
        Product product = inventory.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("No product found with ID " + productId + ".");
        }
        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException("Cannot sell " + quantity + ". Only "
                    + product.getQuantity() + " unit(s) of '" + product.getName() + "' in stock.");
        }

        inventory.reduceStock(productId, quantity);

        Sale sale = new Sale(nextSaleId, productId, product.getName(),
                quantity, product.getPrice(), LocalDateTime.now());
        nextSaleId++;
        sales.add(sale);
        return sale;
    }

    public List<Sale> getSales() {
        return new ArrayList<>(sales);
    }

    public double getTotalSalesAmount() {
        double total = 0;
        for (Sale s : sales) {
            total += s.getTotalAmount();
        }
        return total;
    }

    public int getTotalUnitsSold() {
        int total = 0;
        for (Sale s : sales) {
            total += s.getQuantity();
        }
        return total;
    }

    /** Returns up to 'limit' entries of (product name, units sold), best sellers first. */
    public List<Map.Entry<String, Integer>> getMostSoldProducts(int limit) {
        Map<String, Integer> unitsByProduct = new HashMap<>();
        for (Sale s : sales) {
            int soFar = unitsByProduct.getOrDefault(s.getProductName(), 0);
            unitsByProduct.put(s.getProductName(), soFar + s.getQuantity());
        }

        List<Map.Entry<String, Integer>> ranking = new ArrayList<>(unitsByProduct.entrySet());
        Collections.sort(ranking, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return b.getValue() - a.getValue();   // highest first
            }
        });

        if (ranking.size() > limit) {
            return new ArrayList<>(ranking.subList(0, limit));
        }
        return ranking;
    }
}
