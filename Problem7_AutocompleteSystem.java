import java.util.*;

/**
 * Problem 7: Autocomplete System for Search Engine
 * 
 * Build a Google-like autocomplete system.
 * Requirements:
 * - Stores search queries with frequency counts
 * - Returns top 10 suggestions for any prefix in <50ms
 * - Updates frequencies based on new searches
 * - Handles typos and suggests corrections
 * - Optimizes for memory (10M queries × avg 30 characters)
 */
public class Problem7_AutocompleteSystem {
    
    private static class TrieNode {
        HashMap<Character, TrieNode> children;
        int frequency;
        String query;
        
        public TrieNode() {
            this.children = new HashMap<>();
            this.frequency = 0;
            this.query = "";
        }
    }
    
    private static class AutocompleteSystem {
        private TrieNode root;
        private HashMap<String, Integer> queryFrequency; // Global frequency map
        private static final int TOP_K = 10;
        
        public AutocompleteSystem() {
            this.root = new TrieNode();
            this.queryFrequency = new HashMap<>();
        }
        
        /**
         * Insert a query and update its frequency
         */
        public void insertQuery(String query) {
            query = query.toLowerCase().trim();
            
            // Update global frequency
            int newFreq = queryFrequency.getOrDefault(query, 0) + 1;
            queryFrequency.put(query, newFreq);
            
            // Insert into Trie
            TrieNode node = root;
            for (char c : query.toCharArray()) {
                node = node.children.computeIfAbsent(c, k -> new TrieNode());
            }
            node.frequency = newFreq;
            node.query = query;
        }
        
        /**
         * Search for top K suggestions matching a prefix
         */
        public List<String> search(String prefix) {
            prefix = prefix.toLowerCase().trim();
            
            TrieNode node = root;
            
            // Navigate to the prefix node
            for (char c : prefix.toCharArray()) {
                node = node.children.get(c);
                if (node == null) {
                    return new ArrayList<>();
                }
            }
            
            // DFS to collect all queries with this prefix
            List<String> suggestions = new ArrayList<>();
            dfs(node, suggestions);
            
            // Sort by frequency (descending)
            suggestions.sort((a, b) -> {
                int freqA = queryFrequency.getOrDefault(a, 0);
                int freqB = queryFrequency.getOrDefault(b, 0);
                return Integer.compare(freqB, freqA);
            });
            
            // Return top K
            return suggestions.subList(0, Math.min(TOP_K, suggestions.size()));
        }
        
        /**
         * DFS to collect all queries from a node
         */
        private void dfs(TrieNode node, List<String> results) {
            if (node == null) return;
            
            if (node.frequency > 0) {
                results.add(node.query);
            }
            
            for (TrieNode child : node.children.values()) {
                dfs(child, results);
            }
        }
        
        /**
         * Get frequency of a specific query
         */
        public int getFrequency(String query) {
            return queryFrequency.getOrDefault(query.toLowerCase(), 0);
        }
        
        /**
         * Get trending queries
         */
        public List<String> getTrendingQueries(int limit) {
            return queryFrequency.entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .toList();
        }
        
        /**
         * Update query frequency
         */
        public void updateFrequency(String query) {
            insertQuery(query);
        }
    }
    
    public static void main(String[] args) {
        AutocompleteSystem autocomplete = new AutocompleteSystem();
        
        System.out.println("=== Autocomplete System ===\n");
        
        // Insert sample queries
        String[] queries = {
            "Java tutorial",
            "JavaScript tutorial",
            "Java download",
            "Python tutorial",
            "Java interview",
            "JavaScript interview",
            "Java collections",
            "Java streams",
            "JavaScript async",
            "Python data science",
            "JavaScript frameworks",
            "Java testing",
            "Python machine learning"
        };
        
        // Simulate search frequencies
        int[] frequencies = {1000, 800, 750, 900, 600, 550, 650, 700, 500, 850, 700, 400, 480};
        
        System.out.println("Loading queries with frequencies...");
        for (int i = 0; i < queries.length; i++) {
            for (int j = 0; j < frequencies[i]; j++) {
                autocomplete.insertQuery(queries[i]);
            }
        }
        
        System.out.println("Loaded " + queries.length + " unique queries\n");
        
        // Test autocomplete
        String[] prefixes = {"java", "javascript", "python", "java t", "jav", "pyth"};
        
        System.out.println("=== Search Suggestions ===");
        for (String prefix : prefixes) {
            System.out.println("\nPrefix: \"" + prefix + "\"");
            List<String> suggestions = autocomplete.search(prefix);
            
            for (int i = 0; i < suggestions.size(); i++) {
                String query = suggestions.get(i);
                int freq = autocomplete.getFrequency(query);
                System.out.printf("  %d. \"%s\" (%,d searches)\n", i + 1, query, freq);
            }
        }
        
        System.out.println("\n=== Trending Queries ===");
        List<String> trending = autocomplete.getTrendingQueries(5);
        for (int i = 0; i < trending.size(); i++) {
            System.out.printf("%d. %s (%,d searches)\n", i + 1, trending.get(i),
                autocomplete.getFrequency(trending.get(i)));
        }
    }
}
