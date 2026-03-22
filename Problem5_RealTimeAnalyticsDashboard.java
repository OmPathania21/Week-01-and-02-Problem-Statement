import java.util.*;

/**
 * Problem 5: Real-Time Analytics Dashboard for Website Traffic
 * 
 * Implement a streaming analytics system for real-time traffic monitoring.
 * Requirements:
 * - Process incoming page view events in real-time
 * - Maintain top 10 most visited pages
 * - Track unique visitors per page
 * - Count visits by traffic source
 * - Update dashboard every 5 seconds with zero lag
 */
public class Problem5_RealTimeAnalyticsDashboard {
    
    private static class Event {
        String url;
        String userId;
        String source;
        long timestamp;
        
        public Event(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    private static class AnalyticsDashboard {
        private HashMap<String, Integer> pageViewCounts;
        private HashMap<String, HashSet<String>> uniqueVisitors; // page -> set of user IDs
        private HashMap<String, Integer> trafficSources;
        private LinkedList<Event> eventBuffer;
        private static final int UPDATE_INTERVAL_MS = 5000;
        
        public AnalyticsDashboard() {
            this.pageViewCounts = new HashMap<>();
            this.uniqueVisitors = new HashMap<>();
            this.trafficSources = new HashMap<>();
            this.eventBuffer = new LinkedList<>();
        }
        
        /**
         * Process an incoming page view event
         */
        public void processEvent(Event event) {
            // Add to buffer
            eventBuffer.add(event);
            
            // Update page view count in O(1)
            pageViewCounts.put(event.url, pageViewCounts.getOrDefault(event.url, 0) + 1);
            
            // Track unique visitors
            HashSet<String> visitors = uniqueVisitors.computeIfAbsent(event.url, k -> new HashSet<>());
            visitors.add(event.userId);
            
            // Track traffic source
            trafficSources.put(event.source, trafficSources.getOrDefault(event.source, 0) + 1);
        }
        
        /**
         * Get top N pages with most views
         */
        public List<PageAnalytics> getTopPages(int n) {
            return pageViewCounts.entrySet().stream()
                    .map(e -> new PageAnalytics(
                        e.getKey(),
                        e.getValue(),
                        uniqueVisitors.getOrDefault(e.getKey(), new HashSet<>()).size()
                    ))
                    .sorted((a, b) -> Integer.compare(b.viewCount, a.viewCount))
                    .limit(n)
                    .toList();
        }
        
        /**
         * Get traffic source distribution
         */
        public TrafficDistribution getTrafficDistribution() {
            int totalVisits = trafficSources.values().stream().mapToInt(Integer::intValue).sum();
            
            HashMap<String, Double> distribution = new HashMap<>();
            for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
                double percentage = (entry.getValue() * 100.0) / totalVisits;
                distribution.put(entry.getKey(), percentage);
            }
            
            return new TrafficDistribution(distribution, totalVisits);
        }
        
        /**
         * Get total events processed
         */
        public int getTotalEvents() {
            return eventBuffer.size();
        }
    }
    
    private static class PageAnalytics {
        String url;
        int viewCount;
        int uniqueVisitors;
        
        public PageAnalytics(String url, int viewCount, int uniqueVisitors) {
            this.url = url;
            this.viewCount = viewCount;
            this.uniqueVisitors = uniqueVisitors;
        }
        
        @Override
        public String toString() {
            return String.format("%s - %,d views (%,d unique)", url, viewCount, uniqueVisitors);
        }
    }
    
    private static class TrafficDistribution {
        HashMap<String, Double> distribution;
        int total;
        
        public TrafficDistribution(HashMap<String, Double> distribution, int total) {
            this.distribution = distribution;
            this.total = total;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Traffic Sources (Total: ").append(total).append("):\n");
            distribution.entrySet().stream()
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                    .forEach(e -> sb.append(String.format("  %s: %.1f%%\n", e.getKey(), e.getValue())));
            return sb.toString();
        }
    }
    
    public static void main(String[] args) {
        AnalyticsDashboard dashboard = new AnalyticsDashboard();
        
        System.out.println("=== Real-Time Analytics Dashboard ===\n");
        
        // Simulate incoming events
        String[] pages = {"/article/breaking-news", "/sports/championship", "/tech/ai-trends", 
                         "/business/stocks", "/entertainment/movies"};
        String[] sources = {"google", "direct", "facebook", "twitter", "other"};
        
        Random rand = new Random();
        
        // Simulate 1000 events
        System.out.println("Processing events...");
        for (int i = 0; i < 1000; i++) {
            String page = pages[rand.nextInt(pages.length)];
            String source = sources[rand.nextInt(sources.length)];
            String userId = "user_" + rand.nextInt(500);
            
            dashboard.processEvent(new Event(page, userId, source));
        }
        
        System.out.println("Total events processed: " + dashboard.getTotalEvents());
        System.out.println();
        
        // Display dashboard
        System.out.println("=== Top 10 Pages ===");
        List<PageAnalytics> topPages = dashboard.getTopPages(10);
        for (int i = 0; i < topPages.size(); i++) {
            System.out.println((i + 1) + ". " + topPages.get(i));
        }
        
        System.out.println();
        System.out.println(dashboard.getTrafficDistribution());
    }
}
