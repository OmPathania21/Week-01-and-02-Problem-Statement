import java.util.*;

/**
 * Problem 3: DNS Cache with TTL (Time To Live)
 * 
 * Create a DNS caching system with TTL-based expiration.
 * Requirements:
 * - Stores domain name → IP address mappings
 * - Implements TTL-based expiration
 * - Automatically removes expired entries
 * - Handles cache misses by querying upstream DNS
 * - Reports cache hit/miss ratios
 * - Implements LRU eviction when cache is full
 */
public class Problem3_DNSCacheWithTTL {
    
    private static class DNSEntry {
        String domain;
        String ipAddress;
        long timestamp;
        long ttl; // Time to live in milliseconds
        
        public DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.timestamp = System.currentTimeMillis();
            this.ttl = ttlSeconds * 1000;
        }
        
        /**
         * Check if entry has expired
         */
        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > ttl;
        }
    }
    
    private static class DNSCache {
        private LinkedHashMap<String, DNSEntry> cache;
        private long cacheHits = 0;
        private long cacheMisses = 0;
        private static final int MAX_CACHE_SIZE = 1000;
        
        public DNSCache() {
            // LinkedHashMap with access-order for LRU
            this.cache = new LinkedHashMap<String, DNSEntry>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            };
        }
        
        /**
         * Resolve domain to IP - returns from cache if available and not expired
         */
        public String resolve(String domain) {
            // Clean expired entries
            cleanExpiredEntries();
            
            if (cache.containsKey(domain)) {
                DNSEntry entry = cache.get(domain);
                
                if (!entry.isExpired()) {
                    cacheHits++;
                    System.out.println("Cache HIT for " + domain + " -> " + entry.ipAddress);
                    return entry.ipAddress;
                } else {
                    // Entry expired
                    cache.remove(domain);
                    cacheMisses++;
                }
            }
            
            // Cache miss - query upstream
            cacheMisses++;
            String ipAddress = queryUpstreamDNS(domain);
            
            // Store in cache with TTL
            cache.put(domain, new DNSEntry(domain, ipAddress, 300)); // 5 minutes TTL
            System.out.println("Cache MISS for " + domain + " -> Queried upstream -> " + ipAddress);
            
            return ipAddress;
        }
        
        /**
         * Simulate querying upstream DNS server
         */
        private String queryUpstreamDNS(String domain) {
            Map<String, String> dnsMap = new HashMap<>();
            dnsMap.put("google.com", "172.217.14.206");
            dnsMap.put("github.com", "140.82.118.3");
            dnsMap.put("stackoverflow.com", "151.101.1.69");
            dnsMap.put("twitter.com", "104.244.42.1");
            dnsMap.put("facebook.com", "157.240.128.35");
            
            return dnsMap.getOrDefault(domain, "8.8.8.8");
        }
        
        /**
         * Remove all expired entries
         */
        public void cleanExpiredEntries() {
            List<String> expiredDomains = new ArrayList<>();
            
            for (Map.Entry<String, DNSEntry> entry : cache.entrySet()) {
                if (entry.getValue().isExpired()) {
                    expiredDomains.add(entry.getKey());
                }
            }
            
            expiredDomains.forEach(cache::remove);
        }
        
        /**
         * Get cache statistics
         */
        public CacheStats getStats() {
            cleanExpiredEntries();
            long total = cacheHits + cacheMisses;
            double hitRate = total > 0 ? (cacheHits * 100.0) / total : 0;
            return new CacheStats(cacheHits, cacheMisses, hitRate, cache.size());
        }
        
        /**
         * Clear entire cache
         */
        public void clear() {
            cache.clear();
            cacheHits = 0;
            cacheMisses = 0;
        }
    }
    
    private static class CacheStats {
        long hits;
        long misses;
        double hitRate;
        int cacheSize;
        
        public CacheStats(long hits, long misses, double hitRate, int cacheSize) {
            this.hits = hits;
            this.misses = misses;
            this.hitRate = hitRate;
            this.cacheSize = cacheSize;
        }
        
        @Override
        public String toString() {
            return String.format("Cache Stats: Hits=%d, Misses=%d, Hit Rate=%.2f%%, Size=%d",
                hits, misses, hitRate, cacheSize);
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        DNSCache cache = new DNSCache();
        
        System.out.println("=== DNS Cache with TTL ===\n");
        
        // First resolve - will miss and query upstream
        cache.resolve("google.com");
        cache.resolve("google.com"); // Should hit
        cache.resolve("github.com");
        cache.resolve("google.com"); // Should hit
        cache.resolve("twitter.com");
        cache.resolve("github.com"); // Should hit
        
        System.out.println();
        System.out.println(cache.getStats());
        System.out.println();
        
        // Simulate TTL expiration (in real scenario this would be seconds)
        System.out.println("Simulating cache cleanup...");
        cache.cleanExpiredEntries();
        
        // Resolve again
        cache.resolve("google.com");
        
        System.out.println();
        System.out.println(cache.getStats());
    }
}
