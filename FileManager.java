import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes the text files in the "data" folder.
 * Missing files are treated as empty; bad lines are skipped with a warning.
 */
public class FileManager {

    private static final int DEFAULT_THRESHOLD = 5;

    private String productsPath;
    private String salesPath;
    private String configPath;

    public FileManager(String folder) {
        File dir = new File(folder);
        if (!dir.exists() && !dir.mkdirs()) {
            System.out.println("Warning: could not create folder '" + folder + "'.");
        }
        productsPath = folder + File.separator + "products.txt";
        salesPath = folder + File.separator + "sales.txt";
        configPath = folder + File.separator + "config.txt";
    }

    // ---------- Products ----------

    public Map<Integer, Product> loadProducts() {
        Map<Integer, Product> products = new HashMap<>();
        for (String[] item : readLines(productsPath)) {
            try {
                Product p = Product.fromFileString(item[1]);
                if (products.containsKey(p.getId())) {
                    throw new IllegalArgumentException("duplicate product ID " + p.getId());
                }
                products.put(p.getId(), p);
            } catch (IllegalArgumentException e) {
                System.out.println("Warning: skipped products.txt line " + item[0] + " (" + e.getMessage() + ")");
            }
        }
        return products;
    }

    public boolean saveProducts(Map<Integer, Product> products) {
        List<String> lines = new ArrayList<>();
        lines.add("# id|name|category|price|quantity");
        for (Product p : products.values()) {
            lines.add(p.toFileString());
        }
        return writeLines(productsPath, lines);
    }

    // ---------- Sales ----------

    public List<Sale> loadSales() {
        List<Sale> sales = new ArrayList<>();
        for (String[] item : readLines(salesPath)) {
            try {
                sales.add(Sale.fromFileString(item[1]));
            } catch (IllegalArgumentException e) {
                System.out.println("Warning: skipped sales.txt line " + item[0] + " (" + e.getMessage() + ")");
            }
        }
        return sales;
    }

    public boolean saveSales(List<Sale> sales) {
        List<String> lines = new ArrayList<>();
        lines.add("# saleId|dateTime|productId|productName|quantity|unitPrice|total");
        for (Sale s : sales) {
            lines.add(s.toFileString());
        }
        return writeLines(salesPath, lines);
    }

    // ---------- Low-stock threshold ----------

    public int loadThreshold() {
        for (String[] item : readLines(configPath)) {
            try {
                int value = Integer.parseInt(item[1].trim());
                if (value >= 0) {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("Warning: invalid threshold in config.txt, using default.");
            }
            break;   // only the first line matters
        }
        return DEFAULT_THRESHOLD;
    }

    public boolean saveThreshold(int threshold) {
        List<String> lines = new ArrayList<>();
        lines.add(String.valueOf(threshold));
        return writeLines(configPath, lines);
    }

    // ---------- Shared helpers ----------

    /** Returns {lineNumber, lineText} pairs, skipping blank lines and '#' comment lines. */
    private List<String[]> readLines(String path) {
        List<String[]> result = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            return result;   // first run: nothing saved yet
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }
                result.add(new String[] { String.valueOf(lineNumber), line });
            }
        } catch (IOException e) {
            System.out.println("Warning: could not read " + path + " (" + e.getMessage() + ")");
        }
        return result;
    }

    private boolean writeLines(String path, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Warning: could not save " + path + " (" + e.getMessage() + ")");
            return false;
        }
    }
}
