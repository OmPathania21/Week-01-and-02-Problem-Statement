import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Problem 2: E-commerce Flash Sale Inventory Manager
 * 
 * Implement an inventory management system for flash sales with high concurrency.
 * Requirements:
 * - Track product stock levels in real-time
 * - Process purchase requests in O(1) time
 * - Handle concurrent requests safely
 * - Maintain waiting list when stock runs out
 * - Provide instant stock availability checks
 */
public class Problem2_FlashSaleInventoryManager {
    
    private static class Product {
        String productId;
        AtomicInteger stock;
        long lastUpdated;
        
        public Product(String productId, int initialStock) {
            this.productId = productId;
            this.stock = new AtomicInteger(initialStock);
            this.lastUpdated = System.currentTimeMillis();
        }
    }
    
    private static class InventoryManager {
        private HashMap<String, Product> inventory;
        private HashMap<String, LinkedQueue<String>> waitingLists;
        private HashMap<String, String> purchaseHistory; // userId -> productId
        
        public InventoryManager() {
            this.inventory = new HashMap<>();
            this.waitingLists = new HashMap<>();
            this.purchaseHistory = new HashMap<>();
        }
        
        /**
         * Add product to inventory
         */
        public void addProduct(String productId, int quantity) {
            inventory.put(productId, new Product(productId, quantity));
        }
        
        /**
         * Check stock levels in O(1) time
         */
        public int checkStock(String productId) {
            Product product = inventory.get(productId);
            return product != null ? product.stock.get() : -1; // -1 means not found
        }
        
        /**
         * Process purchase request - thread-safe
         */
        public synchronized PurchaseResult purchaseItem(String productId, String userId) {
            Product product = inventory.get(productId);
            
            if (product == null) {
                return new PurchaseResult(false, "Product not found", -1);
            }
            
            int currentStock = product.stock.get();
            
            if (currentStock > 0) {
                // Purchase successful
                int newStock = product.stock.decrementAndGet();
                purchaseHistory.put(userId, productId);
                return new PurchaseResult(true, "Purchase successful", newStock);
            } else {
                // Add to waiting list
                LinkedQueue<String> waitingList = waitingLists.computeIfAbsent(
                    productId, k -> new LinkedQueue<>()
                );
                int position = waitingList.size() + 1;
                waitingList.enqueue(userId);
                return new PurchaseResult(false, "Added to waiting list at position " + position, position);
            }
        }
        
        /**
         * Process when item becomes available
         */
        public String notifyNextWaiting(String productId) {
            LinkedQueue<String> waitingList = waitingLists.get(productId);
            if (waitingList != null && waitingList.size() > 0) {
                return waitingList.dequeue();
            }
            return null;
        }
        
        /**
         * Get waiting list size for a product
         */
        public int getWaitingListSize(String productId) {
            LinkedQueue<String> waitingList = waitingLists.get(productId);
            return waitingList != null ? waitingList.size() : 0;
        }
        
        /**
         * Get position in waiting list for a user
         */
        public int getWaitingPosition(String productId, String userId) {
            LinkedQueue<String> waitingList = waitingLists.get(productId);
            if (waitingList != null) {
                return waitingList.getPosition(userId);
            }
            return -1;
        }
    }
    
    private static class PurchaseResult {
        boolean success;
        String message;
        int details;
        
        public PurchaseResult(boolean success, String message, int details) {
            this.success = success;
            this.message = message;
            this.details = details;
        }
    }
    
    private static class LinkedQueue<T> {
        private LinkedList<T> queue = new LinkedList<>();
        
        public void enqueue(T item) {
            queue.addLast(item);
        }
        
        public T dequeue() {
            return queue.isEmpty() ? null : queue.removeFirst();
        }
        
        public int size() {
            return queue.size();
        }
        
        public int getPosition(T item) {
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i).equals(item)) {
                    return i + 1;
                }
            }
            return -1;
        }
    }
    
    public static void main(String[] args) {
        InventoryManager manager = new InventoryManager();
        
        // Add products with limited stock
        manager.addProduct("IPHONE15_256GB", 100);
        manager.addProduct("IPHONE15_512GB", 50);
        
        System.out.println("=== Flash Sale Inventory Manager ===\n");
        
        // Check initial stock
        System.out.println("Initial stock:");
        System.out.println("IPHONE15_256GB: " + manager.checkStock("IPHONE15_256GB") + " units");
        System.out.println();
        
        // Simulate purchases
        System.out.println("Processing purchases:");
        for (int i = 1; i <= 102; i++) {
            String userId = "user_" + i;
            PurchaseResult result = manager.purchaseItem("IPHONE15_256GB", userId);
            
            if (i <= 3 || i >= 100) {
                System.out.println("User " + i + ": " + result.message + 
                    (result.success ? " (" + result.details + " remaining)" : 
                     " (Position " + result.details + ")"));
            } else if (i == 4) {
                System.out.println("...");
            }
        }
        
        System.out.println();
        System.out.println("Waiting list size: " + manager.getWaitingListSize("IPHONE15_256GB"));
        System.out.println("Waiting position for user_102: " + 
            manager.getWaitingPosition("IPHONE15_256GB", "user_102"));
    }
}
