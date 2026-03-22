import java.util.concurrent.ConcurrentHashMap;

/**
 * Problem 6: Distributed Rate Limiter for API Gateway
 * 
 * Build a token bucket rate limiter for API request throttling.
 * Requirements:
 * - Track request counts per client
 * - Allow burst traffic up to limit
 * - Reset counters every hour
 * - Respond within 1ms for rate limit checks
 * - Handle distributed deployment (multiple servers)
 * - Provide clear error messages when limit exceeded
 */
public class Problem6_DistributedRateLimiter {
    
    private static class TokenBucket {
        long tokens;
        long lastRefillTime;
        long maxTokens;
        long refillRate; // tokens per second
        
        public TokenBucket(long maxTokens, long refillRate) {
            this.maxTokens = maxTokens;
            this.tokens = maxTokens;
            this.refillRate = refillRate;
            this.lastRefillTime = System.currentTimeMillis();
        }
        
        /**
         * Refill tokens based on elapsed time
         */
        public void refill() {
            long now = System.currentTimeMillis();
            long elapsedSeconds = (now - lastRefillTime) / 1000;
            
            if (elapsedSeconds > 0) {
                tokens = Math.min(maxTokens, tokens + (elapsedSeconds * refillRate));
                lastRefillTime = now;
            }
        }
        
        /**
         * Try to consume a token
         */
        public boolean tryConsume(long tokensNeeded) {
            refill();
            
            if (tokens >= tokensNeeded) {
                tokens -= tokensNeeded;
                return true;
            }
            return false;
        }
    }
    
    private static class RateLimitStatus {
        long used;
        long limit;
        long resetTime;
        long remainingTokens;
        
        public RateLimitStatus(long used, long limit, long resetTime, long remainingTokens) {
            this.used = used;
            this.limit = limit;
            this.resetTime = resetTime;
            this.remainingTokens = remainingTokens;
        }
        
        @Override
        public String toString() {
            return String.format("Used: %d, Limit: %d, Remaining: %d, Reset: %d seconds",
                used, limit, remainingTokens, resetTime);
        }
    }
    
    private static class RateLimiter {
        private ConcurrentHashMap<String, TokenBucket> clientBuckets;
        private long requestsPerHour;
        private long refillRate; // tokens per second
        
        public RateLimiter(long requestsPerHour) {
            this.clientBuckets = new ConcurrentHashMap<>();
            this.requestsPerHour = requestsPerHour;
            this.refillRate = requestsPerHour / 3600; // Convert to per-second
        }
        
        /**
         * Check rate limit for a client in O(1) time
         */
        public RateLimitResult checkRateLimit(String clientId) {
            TokenBucket bucket = clientBuckets.computeIfAbsent(
                clientId, 
                k -> new TokenBucket(requestsPerHour, refillRate)
            );
            
            if (bucket.tryConsume(1)) {
                long remainingRequests = bucket.tokens;
                return new RateLimitResult(
                    true,
                    "Allowed",
                    remainingRequests,
                    0
                );
            } else {
                // Calculate retry-after time
                long tokensNeeded = 1;
                long secondsToWait = (tokensNeeded - bucket.tokens + refillRate - 1) / refillRate;
                
                return new RateLimitResult(
                    false,
                    "Rate limit exceeded",
                    bucket.tokens,
                    secondsToWait
                );
            }
        }
        
        /**
         * Get rate limit status for a client
         */
        public RateLimitStatus getStatus(String clientId) {
            TokenBucket bucket = clientBuckets.get(clientId);
            
            if (bucket == null) {
                return new RateLimitStatus(0, requestsPerHour, 3600, requestsPerHour);
            }
            
            bucket.refill();
            long used = requestsPerHour - bucket.tokens;
            long remaining = bucket.tokens;
            
            // Estimate reset time (in seconds)
            long secondsUntilReset = 3600; // Simplified - assumes hourly reset
            
            return new RateLimitStatus(used, requestsPerHour, secondsUntilReset, remaining);
        }
        
        /**
         * Reset a client's rate limit
         */
        public void resetClient(String clientId) {
            clientBuckets.put(clientId, new TokenBucket(requestsPerHour, refillRate));
        }
        
        /**
         * Allow bulk requests (for specific use cases)
         */
        public RateLimitResult checkRateLimit(String clientId, long tokensNeeded) {
            TokenBucket bucket = clientBuckets.computeIfAbsent(
                clientId,
                k -> new TokenBucket(requestsPerHour, refillRate)
            );
            
            if (bucket.tryConsume(tokensNeeded)) {
                return new RateLimitResult(
                    true,
                    "Allowed",
                    bucket.tokens,
                    0
                );
            } else {
                long secondsToWait = (tokensNeeded - bucket.tokens + refillRate - 1) / refillRate;
                return new RateLimitResult(
                    false,
                    "Rate limit exceeded",
                    bucket.tokens,
                    secondsToWait
                );
            }
        }
    }
    
    private static class RateLimitResult {
        boolean allowed;
        String message;
        long remainingTokens;
        long retryAfterSeconds;
        
        public RateLimitResult(boolean allowed, String message, long remainingTokens, long retryAfterSeconds) {
            this.allowed = allowed;
            this.message = message;
            this.remainingTokens = remainingTokens;
            this.retryAfterSeconds = retryAfterSeconds;
        }
        
        @Override
        public String toString() {
            if (allowed) {
                return String.format("%s (%d requests remaining)", message, remainingTokens);
            } else {
                return String.format("%s (retry after %d seconds)", message, retryAfterSeconds);
            }
        }
    }
    
    public static void main(String[] args) {
        // 1000 requests per hour
        RateLimiter limiter = new RateLimiter(1000);
        
        System.out.println("=== Distributed Rate Limiter ===\n");
        
        String clientId = "abc123";
        
        // Test successful requests
        System.out.println("Processing requests for client: " + clientId);
        for (int i = 0; i < 5; i++) {
            RateLimitResult result = limiter.checkRateLimit(clientId);
            System.out.println("Request " + (i + 1) + ": " + result);
        }
        
        System.out.println();
        
        // Check status
        RateLimitStatus status = limiter.getStatus(clientId);
        System.out.println("Current status: " + status);
        
        System.out.println();
        
        // Test multiple clients
        System.out.println("Testing multiple clients:");
        String[] clients = {"client1", "client2", "client3"};
        
        for (String client : clients) {
            RateLimitResult result = limiter.checkRateLimit(client);
            System.out.println(client + ": " + result);
        }
        
        System.out.println();
        
        // Simulate burst traffic
        System.out.println("Simulating rapid requests...");
        for (int i = 0; i < 10; i++) {
            RateLimitResult result = limiter.checkRateLimit("api_client");
            if (i < 3 || i >= 8) {
                System.out.println("Request " + (i + 1) + ": " + result);
            } else if (i == 3) {
                System.out.println("...");
            }
        }
    }
}
