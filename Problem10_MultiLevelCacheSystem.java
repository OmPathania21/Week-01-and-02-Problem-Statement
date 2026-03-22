import java.util.*;

/**
 * Problem 10: Multi-Level Cache System with Hash Tables
 * 
 * Design a cache hierarchy for a video streaming service.
 * Requirements:
 * - L1 Cache: 10,000 most popular videos (in-memory HashMap)
 * - L2 Cache: 100,000 frequently accessed videos (SSD-backed)
 * - L3: Database (slow, all videos)
 * - Implements LRU eviction at each level
 * - Promotes videos between levels based on access patterns
 * - Tracks cache hit ratios for each level
 * - Handles cache invalidation when content updates
 */
public class Problem10_MultiLevelCacheSystem {
    
    private static class VideoData {
        String videoId;
        String title;
        byte[] data;
        long accessCount;
        long lastAccessTime;
        
        public VideoData(String videoId, String title, byte[] data) {
            this.videoId = videoId;
            this.title = title;
            this.data = data;
            this.accessCount = 0;
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
    
    private static class CacheLevel {
        String levelName;
        int maxCapacity;
        LinkedHashMap<String, VideoData> cache;
        long hits = 0;
        long misses = 0;
        long totalAccessTime = 0;
        int accessCount = 0;
        
        public CacheLevel(String levelName, int maxCapacity) {
            this.levelName = levelName;
            this.maxCapacity = maxCapacity;
            
            // LinkedHashMap with access-order for LRU
            this.cache = new LinkedHashMap<String, VideoData>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                    return size() > maxCapacity;
                }
            };
        }
        
        /**
         * Get video from cache
         */
        public VideoData get(String videoId) {
            VideoData video = cache.get(videoId);
            
            if (video != null) {
                hits++;
                long accessTime = System.currentTimeMillis() - video.lastAccessTime;
                totalAccessTime += accessTime;
                accessCount++;
                video.accessCount++;
                video.lastAccessTime = System.currentTimeMillis();
                return video;
            }
            
            misses++;
            return null;
        }
        
        /**
         * Put video in cache
         */
        public void put(String videoId, VideoData video) {
            cache.put(videoId, video);
        }
        
        /**
         * Check if video exists in cache
         */
        public boolean contains(String videoId) {
            return cache.containsKey(videoId);
        }
        
        /**
         * Get cache hit rate
         */
        public double getHitRate() {
            long total = hits + misses;
            return total > 0 ? (hits * 100.0) / total : 0;
        }
        
        /**
         * Get average access time
         */
        public double getAverageAccessTime() {
            return accessCount > 0 ? totalAccessTime / (double) accessCount : 0;
        }
        
        /**
         * Get cache size
         */
        public int getSize() {
            return cache.size();
        }
        
        /**
         * Invalidate cache entry
         */
        public void invalidate(String videoId) {
            cache.remove(videoId);
        }
    }
    
    private static class MultiLevelCache {
        private CacheLevel l1Cache; // 10,000 most popular
        private CacheLevel l2Cache; // 100,000 frequently accessed
        private HashMap<String, VideoData> l3Database; // All videos
        private int promotionThreshold; // Access count to promote from L2 to L1
        
        public MultiLevelCache(int l1Size, int l2Size, int promotionThreshold) {
            this.l1Cache = new CacheLevel("L1", l1Size);
            this.l2Cache = new CacheLevel("L2", l2Size);
            this.l3Database = new HashMap<>();
            this.promotionThreshold = promotionThreshold;
        }
        
        /**
         * Get video from cache hierarchy
         */
        public VideoData getVideo(String videoId) {
            long startTime = System.currentTimeMillis();
            
            // Try L1
            VideoData video = l1Cache.get(videoId);
            if (video != null) {
                return video;
            }
            
            // Try L2
            video = l2Cache.get(videoId);
            if (video != null) {
                // Check if should promote to L1
                if (video.accessCount >= promotionThreshold) {
                    promoteToL1(videoId, video);
                }
                return video;
            }
            
            // Try L3 (database)
            video = l3Database.get(videoId);
            if (video != null) {
                video.accessCount++;
                video.lastAccessTime = System.currentTimeMillis();
                
                // Add to L2
                l2Cache.put(videoId, video);
                return video;
            }
            
            return null; // Video not found
        }
        
        /**
         * Promote video from L2 to L1
         */
        private void promoteToL1(String videoId, VideoData video) {
            l1Cache.put(videoId, video);
            // Keep in L2 as well for now
        }
        
        /**
         * Add video to system
         */
        public void addVideo(String videoId, VideoData video) {
            l3Database.put(videoId, video);
        }
        
        /**
         * Invalidate cache entry (when content updates)
         */
        public void invalidateCache(String videoId) {
            l1Cache.invalidate(videoId);
            l2Cache.invalidate(videoId);
        }
        
        /**
         * Get system statistics
         */
        public CacheStatistics getStatistics() {
            long totalHits = l1Cache.hits + l2Cache.hits;
            long totalMisses = l1Cache.misses + l2Cache.misses;
            double overallHitRate = (totalHits + totalMisses) > 0 ? 
                (totalHits * 100.0) / (totalHits + totalMisses) : 0;
            
            return new CacheStatistics(
                l1Cache.getHitRate(),
                l2Cache.getHitRate(),
                overallHitRate,
                l1Cache.getAverageAccessTime(),
                l2Cache.getAverageAccessTime(),
                l1Cache.getSize(),
                l2Cache.getSize(),
                l3Database.size()
            );
        }
    }
    
    private static class CacheStatistics {
        double l1HitRate;
        double l2HitRate;
        double overallHitRate;
        double l1AvgTime;
        double l2AvgTime;
        int l1Size;
        int l2Size;
        int l3Size;
        
        public CacheStatistics(double l1Hit, double l2Hit, double overallHit,
                              double l1Time, double l2Time,
                              int l1Size, int l2Size, int l3Size) {
            this.l1HitRate = l1Hit;
            this.l2HitRate = l2Hit;
            this.overallHitRate = overallHit;
            this.l1AvgTime = l1Time;
            this.l2AvgTime = l2Time;
            this.l1Size = l1Size;
            this.l2Size = l2Size;
            this.l3Size = l3Size;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Cache Statistics:\n" +
                "  L1: Hit Rate %.1f%%, Avg Time %.2fms, Size %d/10000\n" +
                "  L2: Hit Rate %.1f%%, Avg Time %.2fms, Size %d/100000\n" +
                "  L3: Database, Size %d\n" +
                "  Overall Hit Rate: %.1f%%",
                l1HitRate, l1AvgTime, l1Size,
                l2HitRate, l2AvgTime, l2Size,
                l3Size,
                overallHitRate
            );
        }
    }
    
    public static void main(String[] args) {
        MultiLevelCache cache = new MultiLevelCache(100, 500, 5);
        
        System.out.println("=== Multi-Level Cache System ===\n");
        
        // Populate L3 database with videos
        System.out.println("Loading videos into database...");
        for (int i = 1; i <= 1000; i++) {
            String videoId = "video_" + i;
            byte[] data = new byte[1024 * (10 + i % 50)]; // Variable sizes
            VideoData video = new VideoData(videoId, "Video " + i, data);
            cache.addVideo(videoId, video);
        }
        System.out.println("Loaded 1000 videos\n");
        
        // Simulate access patterns
        System.out.println("Simulating access patterns...");
        Random rand = new Random(42);
        
        // Access popular videos multiple times (L3 -> L2)
        for (int i = 0; i < 100; i++) {
            String videoId = "video_" + (rand.nextInt(100) + 1);
            cache.getVideo(videoId);
        }
        
        // Access moderately popular videos (L2 -> L1)
        for (int i = 0; i < 50; i++) {
            String videoId = "video_" + (rand.nextInt(200) + 1);
            cache.getVideo(videoId);
        }
        
        // Access L1 cached videos multiple times
        for (int i = 0; i < 100; i++) {
            String videoId = "video_" + (rand.nextInt(50) + 1);
            cache.getVideo(videoId);
        }
        
        System.out.println();
        System.out.println(cache.getStatistics());
        
        System.out.println();
        System.out.println("=== Cache Invalidation Test ===");
        cache.invalidateCache("video_10");
        System.out.println("Invalidated video_10");
        
        System.out.println();
        System.out.println(cache.getStatistics());
    }
}
