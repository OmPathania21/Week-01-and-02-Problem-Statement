import java.util.*;

/**
 * Problem 9: Two-Sum Problem Variants for Financial Transactions
 * 
 * Detect fraudulent transaction pairs and perform transaction analysis.
 * Requirements:
 * - Classic Two-Sum: Find pairs that sum to target amount
 * - Two-Sum with time window: Pairs within 1 hour
 * - K-Sum: Find K transactions that sum to target
 * - Duplicate detection: Same amount, same merchant, different accounts
 * - All under 100ms response time
 */
public class Problem9_TwoSumTransactions {
    
    private static class Transaction {
        long id;
        double amount;
        String merchant;
        String accountId;
        long timestamp;
        
        public Transaction(long id, double amount, String merchant, String accountId, long timestamp) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.accountId = accountId;
            this.timestamp = timestamp;
        }
        
        @Override
        public String toString() {
            return String.format("Transaction(id=%d, amount=%.2f, merchant=%s, account=%s)", 
                id, amount, merchant, accountId);
        }
    }
    
    private static class TransactionAnalyzer {
        private List<Transaction> transactions;
        
        public TransactionAnalyzer() {
            this.transactions = new ArrayList<>();
        }
        
        /**
         * Add a transaction to the system
         */
        public void addTransaction(Transaction transaction) {
            transactions.add(transaction);
        }
        
        /**
         * Classic Two-Sum: Find pairs that sum to target amount - O(n)
         */
        public List<TransactionPair> findTwoSum(double target) {
            List<TransactionPair> results = new ArrayList<>();
            HashMap<Double, List<Transaction>> amountMap = new HashMap<>();
            
            // Build hash map of amounts
            for (Transaction t : transactions) {
                amountMap.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
            }
            
            // Find pairs
            Set<String> foundPairs = new HashSet<>();
            
            for (Transaction t : transactions) {
                double complement = target - t.amount;
                
                if (amountMap.containsKey(complement)) {
                    for (Transaction complementTx : amountMap.get(complement)) {
                        // Avoid same transaction and duplicate pairs
                        if (!t.id.equals(complementTx.id)) {
                            String pairKey = t.id < complementTx.id ? 
                                (t.id + "-" + complementTx.id) : 
                                (complementTx.id + "-" + t.id);
                            
                            if (!foundPairs.contains(pairKey)) {
                                foundPairs.add(pairKey);
                                results.add(new TransactionPair(t, complementTx, target));
                            }
                        }
                    }
                }
            }
            
            return results;
        }
        
        /**
         * Two-Sum with time window: Find pairs within specified time window
         */
        public List<TransactionPair> findTwoSumWithinTimeWindow(double target, long timeWindowMs) {
            List<TransactionPair> results = new ArrayList<>();
            
            for (int i = 0; i < transactions.size(); i++) {
                Transaction t1 = transactions.get(i);
                
                for (int j = i + 1; j < transactions.size(); j++) {
                    Transaction t2 = transactions.get(j);
                    
                    // Check time window
                    long timeDiff = Math.abs(t2.timestamp - t1.timestamp);
                    
                    if (timeDiff <= timeWindowMs && 
                        Math.abs(t1.amount + t2.amount - target) < 0.01) {
                        results.add(new TransactionPair(t1, t2, target));
                    }
                }
            }
            
            return results;
        }
        
        /**
         * K-Sum: Find K transactions that sum to target amount
         */
        public List<List<Transaction>> findKSum(int k, double target) {
            List<List<Transaction>> results = new ArrayList<>();
            List<Transaction> currentCombination = new ArrayList<>();
            
            findKSumHelper(0, k, target, currentCombination, results);
            
            return results;
        }
        
        private void findKSumHelper(int startIndex, int k, double target, 
                                    List<Transaction> current, 
                                    List<List<Transaction>> results) {
            if (k == 0) {
                if (Math.abs(target) < 0.01) {
                    results.add(new ArrayList<>(current));
                }
                return;
            }
            
            for (int i = startIndex; i < transactions.size(); i++) {
                Transaction t = transactions.get(i);
                current.add(t);
                findKSumHelper(i + 1, k - 1, target - t.amount, current, results);
                current.remove(current.size() - 1);
            }
        }
        
        /**
         * Detect duplicate transactions: Same amount, same merchant, different accounts
         */
        public List<DuplicateGroup> detectDuplicates() {
            HashMap<String, List<Transaction>> duplicateMap = new HashMap<>();
            
            for (Transaction t : transactions) {
                String key = t.amount + "-" + t.merchant;
                duplicateMap.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
            }
            
            List<DuplicateGroup> results = new ArrayList<>();
            
            for (Map.Entry<String, List<Transaction>> entry : duplicateMap.entrySet()) {
                List<Transaction> txList = entry.getValue();
                
                if (txList.size() > 1) {
                    Set<String> uniqueAccounts = new HashSet<>();
                    for (Transaction t : txList) {
                        uniqueAccounts.add(t.accountId);
                    }
                    
                    // Only report if different accounts
                    if (uniqueAccounts.size() > 1) {
                        results.add(new DuplicateGroup(txList, uniqueAccounts));
                    }
                }
            }
            
            return results;
        }
    }
    
    private static class TransactionPair {
        Transaction t1;
        Transaction t2;
        double targetSum;
        
        public TransactionPair(Transaction t1, Transaction t2, double targetSum) {
            this.t1 = t1;
            this.t2 = t2;
            this.targetSum = targetSum;
        }
        
        @Override
        public String toString() {
            return String.format("Pair: (id:%d, %.2f) + (id:%d, %.2f) = %.2f",
                t1.id, t1.amount, t2.id, t2.amount, t1.amount + t2.amount);
        }
    }
    
    private static class DuplicateGroup {
        List<Transaction> transactions;
        Set<String> accountIds;
        double amount;
        String merchant;
        
        public DuplicateGroup(List<Transaction> transactions, Set<String> accountIds) {
            this.transactions = transactions;
            this.accountIds = accountIds;
            this.amount = transactions.get(0).amount;
            this.merchant = transactions.get(0).merchant;
        }
        
        @Override
        public String toString() {
            return String.format("Duplicate: %.2f at %s across accounts: %s", 
                amount, merchant, accountIds);
        }
    }
    
    public static void main(String[] args) {
        TransactionAnalyzer analyzer = new TransactionAnalyzer();
        
        System.out.println("=== Two-Sum Transaction Analysis ===\n");
        
        // Add sample transactions
        long now = System.currentTimeMillis();
        analyzer.addTransaction(new Transaction(1, 500, "Store A", "acc1", now));
        analyzer.addTransaction(new Transaction(2, 300, "Store B", "acc2", now + 600000));
        analyzer.addTransaction(new Transaction(3, 200, "Store C", "acc1", now + 1200000));
        analyzer.addTransaction(new Transaction(4, 400, "Store A", "acc3", now + 100000));
        analyzer.addTransaction(new Transaction(5, 500, "Store A", "acc4", now + 200000));
        analyzer.addTransaction(new Transaction(6, 100, "Store B", "acc2", now + 500000));
        
        System.out.println("Transactions loaded.\n");
        
        // Test Two-Sum
        System.out.println("=== Two-Sum (target = 500) ===");
        List<TransactionPair> pairs = analyzer.findTwoSum(500);
        for (TransactionPair pair : pairs) {
            System.out.println("  " + pair);
        }
        
        System.out.println();
        
        // Test Two-Sum with time window (10 minutes)
        System.out.println("=== Two-Sum within 10-minute window (target = 600) ===");
        List<TransactionPair> timeWindowPairs = analyzer.findTwoSumWithinTimeWindow(600, 600000);
        for (TransactionPair pair : timeWindowPairs) {
            System.out.println("  " + pair);
        }
        
        System.out.println();
        
        // Test K-Sum
        System.out.println("=== Three-Sum (target = 1000) ===");
        List<List<Transaction>> kSumResults = analyzer.findKSum(3, 1000);
        for (List<Transaction> result : kSumResults) {
            System.out.print("  Combination: ");
            double sum = 0;
            for (Transaction t : result) {
                System.out.print(String.format("%.0f ", t.amount));
                sum += t.amount;
            }
            System.out.println(String.format("= %.0f", sum));
        }
        
        System.out.println();
        
        // Test duplicate detection
        System.out.println("=== Duplicate Detection ===");
        List<DuplicateGroup> duplicates = analyzer.detectDuplicates();
        for (DuplicateGroup dup : duplicates) {
            System.out.println("  " + dup);
        }
    }
}
