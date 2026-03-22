import java.util.*;

/**
 * Problem 8: Parking Lot Management with Open Addressing
 * 
 * Implement a parking system using open addressing (linear probing).
 * Requirements:
 * - Assign parking spots based on license plate hash
 * - Use linear probing when preferred spot is occupied
 * - Track entry/exit times for billing
 * - Find nearest available spot to entrance
 * - Generate parking statistics
 */
public class Problem8_ParkingLotManagement {
    
    private static class ParkingSpot {
        enum Status { EMPTY, OCCUPIED, DELETED }
        
        String licensePlate;
        Status status;
        long entryTime;
        long exitTime;
        double fee;
        
        public ParkingSpot() {
            this.status = Status.EMPTY;
            this.licensePlate = null;
            this.entryTime = 0;
            this.exitTime = 0;
            this.fee = 0;
        }
    }
    
    private static class ParkingLot {
        private ParkingSpot[] spots;
        private HashMap<String, Integer> licensePlateToSpot; // For quick lookup
        private int totalSpots;
        private int occupiedSpots;
        private double hourlyRate;
        private List<ParkingTransaction> transactions;
        private int totalProbes;
        private int probeCount;
        
        public ParkingLot(int totalSpots, double hourlyRate) {
            this.spots = new ParkingSpot[totalSpots];
            this.licensePlateToSpot = new HashMap<>();
            this.totalSpots = totalSpots;
            this.occupiedSpots = 0;
            this.hourlyRate = hourlyRate;
            this.transactions = new ArrayList<>();
            this.totalProbes = 0;
            
            // Initialize all spots
            for (int i = 0; i < totalSpots; i++) {
                spots[i] = new ParkingSpot();
            }
        }
        
        /**
         * Simple hash function for license plate
         */
        private int hash(String licensePlate) {
            return Math.abs(licensePlate.hashCode()) % totalSpots;
        }
        
        /**
         * Park a vehicle using linear probing
         */
        public ParkingResult parkVehicle(String licensePlate) {
            if (occupiedSpots == totalSpots) {
                return new ParkingResult(false, -1, 0, "Parking lot is full");
            }
            
            int preferredSpot = hash(licensePlate);
            int currentSpot = preferredSpot;
            int probes = 0;
            
            // Linear probing
            while (spots[currentSpot].status == ParkingSpot.Status.OCCUPIED) {
                probes++;
                currentSpot = (currentSpot + 1) % totalSpots;
                
                // Safety check to prevent infinite loop
                if (currentSpot == preferredSpot) {
                    return new ParkingResult(false, -1, 0, "No available spots found");
                }
            }
            
            // Park the vehicle
            spots[currentSpot].status = ParkingSpot.Status.OCCUPIED;
            spots[currentSpot].licensePlate = licensePlate;
            spots[currentSpot].entryTime = System.currentTimeMillis();
            licensePlateToSpot.put(licensePlate, currentSpot);
            occupiedSpots++;
            totalProbes += probes;
            
            return new ParkingResult(true, currentSpot, probes, "Vehicle parked successfully");
        }
        
        /**
         * Remove a vehicle and calculate fee
         */
        public ParkingResult exitVehicle(String licensePlate) {
            Integer spotNumber = licensePlateToSpot.get(licensePlate);
            
            if (spotNumber == null) {
                return new ParkingResult(false, -1, 0, "Vehicle not found");
            }
            
            ParkingSpot spot = spots[spotNumber];
            long exitTime = System.currentTimeMillis();
            long durationMs = exitTime - spot.entryTime;
            long durationHours = (durationMs + 3599000) / 3600000; // Round up to nearest hour
            if (durationHours == 0) durationHours = 1;
            
            double fee = durationHours * hourlyRate;
            
            // Mark as empty
            spot.status = ParkingSpot.Status.EMPTY;
            spot.exitTime = exitTime;
            spot.fee = fee;
            
            licensePlateToSpot.remove(licensePlate);
            occupiedSpots--;
            
            // Record transaction
            transactions.add(new ParkingTransaction(licensePlate, spotNumber, 
                spot.entryTime, exitTime, fee));
            
            return new ParkingResult(true, spotNumber, durationHours, 
                String.format("Fee: $%.2f (Duration: %d hours)", fee, durationHours));
        }
        
        /**
         * Get nearest available spot
         */
        public int getNearestAvailableSpot() {
            for (int i = 0; i < totalSpots; i++) {
                if (spots[i].status == ParkingSpot.Status.EMPTY) {
                    return i;
                }
            }
            return -1;
        }
        
        /**
         * Get occupancy percentage
         */
        public double getOccupancyPercentage() {
            return (occupiedSpots * 100.0) / totalSpots;
        }
        
        /**
         * Get average probes per parking
         */
        public double getAverageProbes() {
            int parked = totalSpots - getAvailableSpots();
            return parked > 0 ? totalProbes / (double) parked : 0;
        }
        
        /**
         * Get number of available spots
         */
        public int getAvailableSpots() {
            return totalSpots - occupiedSpots;
        }
        
        /**
         * Get statistics
         */
        public ParkingStatistics getStatistics() {
            return new ParkingStatistics(
                occupiedSpots,
                totalSpots,
                getOccupancyPercentage(),
                getAverageProbes(),
                transactions
            );
        }
    }
    
    private static class ParkingResult {
        boolean success;
        int spotNumber;
        long durationOrProbes;
        String message;
        
        public ParkingResult(boolean success, int spotNumber, long durationOrProbes, String message) {
            this.success = success;
            this.spotNumber = spotNumber;
            this.durationOrProbes = durationOrProbes;
            this.message = message;
        }
    }
    
    private static class ParkingTransaction {
        String licensePlate;
        int spotNumber;
        long entryTime;
        long exitTime;
        double fee;
        
        public ParkingTransaction(String licensePlate, int spotNumber, 
                                 long entryTime, long exitTime, double fee) {
            this.licensePlate = licensePlate;
            this.spotNumber = spotNumber;
            this.entryTime = entryTime;
            this.exitTime = exitTime;
            this.fee = fee;
        }
    }
    
    private static class ParkingStatistics {
        int occupied;
        int total;
        double occupancyPercentage;
        double averageProbes;
        List<ParkingTransaction> transactions;
        
        public ParkingStatistics(int occupied, int total, double occupancy, 
                                double avgProbes, List<ParkingTransaction> transactions) {
            this.occupied = occupied;
            this.total = total;
            this.occupancyPercentage = occupancy;
            this.averageProbes = avgProbes;
            this.transactions = transactions;
        }
        
        @Override
        public String toString() {
            return String.format("Parking Statistics:\n" +
                    "  Occupied: %d/%d (%.1f%%)\n" +
                    "  Average Probes: %.2f\n" +
                    "  Total Transactions: %d",
                occupied, total, occupancyPercentage, averageProbes, transactions.size());
        }
    }
    
    public static void main(String[] args) {
        ParkingLot parkingLot = new ParkingLot(500, 5.0); // 500 spots, $5/hour
        
        System.out.println("=== Parking Lot Management ===\n");
        
        // Test parking vehicles
        String[] vehicles = {"ABC-1234", "ABC-1235", "XYZ-9999", "PQR-5678", "LMN-0987"};
        
        System.out.println("Parking vehicles:");
        for (String vehicle : vehicles) {
            ParkingResult result = parkingLot.parkVehicle(vehicle);
            System.out.println(vehicle + " -> Spot #" + result.spotNumber + 
                " (" + result.durationOrProbes + " probes) - " + result.message);
        }
        
        System.out.println();
        System.out.println("Parking lot status:");
        System.out.println("  Occupied: " + (500 - parkingLot.getAvailableSpots()) + " / 500");
        System.out.println("  Occupancy: " + String.format("%.2f%%", parkingLot.getOccupancyPercentage()));
        System.out.println("  Available spots: " + parkingLot.getAvailableSpots());
        
        System.out.println();
        System.out.println("Exiting vehicles:");
        
        try {
            Thread.sleep(1000); // Simulate some parking time
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        for (int i = 0; i < 3; i++) {
            ParkingResult result = parkingLot.exitVehicle(vehicles[i]);
            System.out.println(vehicles[i] + " from spot #" + result.spotNumber + 
                " - " + result.message);
        }
        
        System.out.println();
        System.out.println(parkingLot.getStatistics());
    }
}
