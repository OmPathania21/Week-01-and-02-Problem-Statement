import java.util.*;

/**
 * Problem 1: Social Media Username Availability Checker
 * 
 * Design a system to check username availability in real-time.
 * Requirements:
 * - Check if a username exists in O(1) time
 * - Handle 1000 concurrent username checks per second
 * - Suggest similar available usernames if taken
 * - Track popularity of attempted usernames
 */
public class Problem1_UsernameAvailabilityChecker {
    
    private static class UsernameSystem {
        private HashSet<String> registeredUsernames;
        private HashMap<String, Integer> usernameAttempts;
        
        public UsernameSystem() {
            this.registeredUsernames = new HashSet<>();
            this.usernameAttempts = new HashMap<>();
        }
        
        /**
         * Check if username is available in O(1) time
         */
        public boolean checkAvailability(String username) {
            // Track this attempt
            usernameAttempts.put(username, usernameAttempts.getOrDefault(username, 0) + 1);
            
            // O(1) lookup
            return !registeredUsernames.contains(username);
        }
        
        /**
         * Register a username
         */
        public boolean registerUsername(String username) {
            if (checkAvailability(username)) {
                registeredUsernames.add(username);
                return true;
            }
            return false;
        }
        
        /**
         * Suggest similar available usernames if the requested one is taken
         */
        public List<String> suggestAlternatives(String username) {
            List<String> suggestions = new ArrayList<>();
            
            if (checkAvailability(username)) {
                return suggestions; // Already available
            }
            
            // Try appending numbers
            for (int i = 1; i <= 5; i++) {
                String suggestion = username + i;
                if (!registeredUsernames.contains(suggestion)) {
                    suggestions.add(suggestion);
                }
            }
            
            // Try adding underscore and number
            for (int i = 1; i <= 5; i++) {
                String suggestion = username + "_" + i;
                if (!registeredUsernames.contains(suggestion)) {
                    suggestions.add(suggestion);
                }
            }
            
            // Try replacing vowels
            String modified = username.replace('a', '4').replace('e', '3').replace('i', '1');
            if (!registeredUsernames.contains(modified) && !suggestions.contains(modified)) {
                suggestions.add(modified);
            }
            
            return suggestions;
        }
        
        /**
         * Get most attempted username
         */
        public String getMostAttempted() {
            return usernameAttempts.entrySet().stream()
                    .max(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }
        
        /**
         * Get top N most attempted usernames
         */
        public List<Map.Entry<String, Integer>> getTopAttempted(int n) {
            return usernameAttempts.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(n)
                    .toList();
        }
    }
    
    public static void main(String[] args) {
        UsernameSystem system = new UsernameSystem();
        
        // Register some usernames
        system.registerUsername("john_doe");
        system.registerUsername("jane_smith");
        system.registerUsername("admin");
        
        System.out.println("=== Username Availability Checker ===\n");
        
        // Test availability check
        System.out.println("Checking availability:");
        System.out.println("'john_doe' available: " + system.checkAvailability("john_doe")); // false
        System.out.println("'jane_smith' available: " + system.checkAvailability("jane_smith")); // false
        System.out.println("'john_smith' available: " + system.checkAvailability("john_smith")); // true
        System.out.println();
        
        // Test attempt tracking
        system.checkAvailability("admin");
        system.checkAvailability("admin");
        system.checkAvailability("admin");
        
        System.out.println("Most attempted username: " + system.getMostAttempted());
        System.out.println();
        
        // Test suggestions
        System.out.println("Suggestions for 'john_doe':");
        List<String> suggestions = system.suggestAlternatives("john_doe");
        suggestions.forEach(s -> System.out.println("  - " + s));
        System.out.println();
        
        // Show top attempted
        System.out.println("Top 5 most attempted usernames:");
        system.getTopAttempted(5).forEach(e -> 
            System.out.println("  " + e.getKey() + ": " + e.getValue() + " attempts")
        );
    }
}
