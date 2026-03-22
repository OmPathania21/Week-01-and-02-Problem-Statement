import java.util.*;

/**
 * Problem 4: Plagiarism Detection System
 * 
 * Build a plagiarism detector using n-grams and hash tables.
 * Requirements:
 * - Break documents into n-grams
 * - Store n-grams in hash table with document references
 * - Find matching n-grams between documents
 * - Calculate similarity percentage
 * - Identify most similar documents in O(n) time
 */
public class Problem4_PlagiarismDetectionSystem {
    
    private static class Document {
        String documentId;
        String content;
        Set<String> nGrams;
        
        public Document(String documentId, String content) {
            this.documentId = documentId;
            this.content = content;
            this.nGrams = new HashSet<>();
        }
    }
    
    private static class PlagiarismDetector {
        private Map<String, Document> documents;
        private Map<String, Set<String>> nGramIndex; // n-gram -> set of document IDs
        private int nGramSize;
        
        public PlagiarismDetector(int nGramSize) {
            this.documents = new HashMap<>();
            this.nGramIndex = new HashMap<>();
            this.nGramSize = nGramSize;
        }
        
        /**
         * Extract n-grams from text
         */
        private Set<String> extractNGrams(String text) {
            Set<String> nGrams = new HashSet<>();
            String[] words = text.toLowerCase().split("\\s+");
            
            for (int i = 0; i <= words.length - nGramSize; i++) {
                StringBuilder ngram = new StringBuilder();
                for (int j = 0; j < nGramSize; j++) {
                    if (j > 0) ngram.append(" ");
                    ngram.append(words[i + j]);
                }
                nGrams.add(ngram.toString());
            }
            
            return nGrams;
        }
        
        /**
         * Analyze and index a document
         */
        public void analyzeDocument(String documentId, String content) {
            Document doc = new Document(documentId, content);
            doc.nGrams = extractNGrams(content);
            documents.put(documentId, doc);
            
            // Index n-grams
            for (String nGram : doc.nGrams) {
                nGramIndex.computeIfAbsent(nGram, k -> new HashSet<>()).add(documentId);
            }
            
            System.out.println("Document '" + documentId + "' analyzed. Extracted " + 
                doc.nGrams.size() + " n-grams.");
        }
        
        /**
         * Calculate similarity between two documents (Jaccard similarity)
         */
        public double calculateSimilarity(String docId1, String docId2) {
            Document doc1 = documents.get(docId1);
            Document doc2 = documents.get(docId2);
            
            if (doc1 == null || doc2 == null) {
                return 0.0;
            }
            
            Set<String> set1 = doc1.nGrams;
            Set<String> set2 = doc2.nGrams;
            
            // Jaccard similarity = |intersection| / |union|
            Set<String> intersection = new HashSet<>(set1);
            intersection.retainAll(set2);
            
            Set<String> union = new HashSet<>(set1);
            union.addAll(set2);
            
            return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
        }
        
        /**
         * Find all documents similar to a given document
         */
        public List<SimilarityResult> findSimilarDocuments(String documentId, double threshold) {
            List<SimilarityResult> results = new ArrayList<>();
            
            for (String otherDocId : documents.keySet()) {
                if (!otherDocId.equals(documentId)) {
                    double similarity = calculateSimilarity(documentId, otherDocId);
                    if (similarity >= threshold) {
                        results.add(new SimilarityResult(otherDocId, similarity));
                    }
                }
            }
            
            // Sort by similarity descending
            results.sort((a, b) -> Double.compare(b.similarity, a.similarity));
            return results;
        }
        
        /**
         * Find common n-grams between documents
         */
        public Set<String> findCommonNGrams(String docId1, String docId2) {
            Document doc1 = documents.get(docId1);
            Document doc2 = documents.get(docId2);
            
            if (doc1 == null || doc2 == null) {
                return new HashSet<>();
            }
            
            Set<String> common = new HashSet<>(doc1.nGrams);
            common.retainAll(doc2.nGrams);
            return common;
        }
    }
    
    private static class SimilarityResult {
        String documentId;
        double similarity;
        
        public SimilarityResult(String documentId, double similarity) {
            this.documentId = documentId;
            this.similarity = similarity;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %.2f%%", documentId, similarity * 100);
        }
    }
    
    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector(5); // 5-gram
        
        System.out.println("=== Plagiarism Detection System ===\n");
        
        // Analyze documents
        String essay123 = "The quick brown fox jumps over the lazy dog. " +
                         "The fox is very clever and agile. " +
                         "Dogs are loyal animals.";
        
        String essay089 = "A quick brown fox jumps over a lazy dog. " +
                         "The fox is clever and moves fast. " +
                         "Dogs are loyal.";
        
        String essay092 = "The quick brown fox jumps over the lazy dog. " +
                         "The fox is very clever and agile. " +
                         "Dogs are loyal animals. " +
                         "The fox hunts at night. " +
                         "Dogs have strong senses.";
        
        String essay999 = "Python is a programming language. " +
                         "Java is also popular. " +
                         "C++ is used for system programming.";
        
        detector.analyzeDocument("essay_123", essay123);
        detector.analyzeDocument("essay_089", essay089);
        detector.analyzeDocument("essay_092", essay092);
        detector.analyzeDocument("essay_999", essay999);
        
        System.out.println();
        
        // Find similar documents
        System.out.println("Finding documents similar to 'essay_123':");
        List<SimilarityResult> similar = detector.findSimilarDocuments("essay_123", 0.2);
        for (SimilarityResult result : similar) {
            System.out.println("  " + result);
            
            Set<String> commonNGrams = detector.findCommonNGrams("essay_123", result.documentId);
            System.out.println("    Common n-grams: " + commonNGrams.size());
        }
        
        System.out.println();
        System.out.println("Similarity Matrix:");
        System.out.println("essay_123 vs essay_089: " + 
            String.format("%.2f%%", detector.calculateSimilarity("essay_123", "essay_089") * 100));
        System.out.println("essay_123 vs essay_092: " + 
            String.format("%.2f%%", detector.calculateSimilarity("essay_123", "essay_092") * 100));
        System.out.println("essay_123 vs essay_999: " + 
            String.format("%.2f%%", detector.calculateSimilarity("essay_123", "essay_999") * 100));
    }
}
