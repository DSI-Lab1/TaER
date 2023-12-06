/*
 * Copyright Zefeng Chen et al.
 */
package SRC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Example of how to run the NONEPI algorithm from the source code.
 * @see AlgoNONEPI_star
 * @author Zefeng Chen
 */
public class MainTestNONEPI_star {

    public static void main(String[] args) throws IOException {
        // Input file path
    	String input = "FIFA";
    	String inputPath = fileToPath(input + "_event.txt");
    	
    	// Output file path
    	String outputPath = input + "output_Ta.txt";
    	String outputPath1 = input + "output_Ta_patterns.txt";
    	
    	// Minimum support (an integer representing a number of occurrences)
    	int minsup = 4000;
        
        // Minimum confidence (represents a percentage)
        float minconf = 0.5f;
        
        String s1 = "44";
        //String s2 = "18";
        String s3 = "155";
        
        List<String> XQuery = new ArrayList<String>();
        List<String> YQuery = new ArrayList<String>();
        
        XQuery.add(s1);
        //XQuery.add(s2);
        YQuery.add(s3);
        
        // (1) First extracts frequent episodes with NONEPI
        AlgoNONEPI_star algo = new AlgoNONEPI_star();
        List<Episode> frequentEpisodes = algo.findFrequentEpisodes(inputPath, minsup, XQuery, YQuery);
        
        // (2) Second, find episodes rules from frequent episodes        
        List<String> rules = algo.findNONEpiRulesWithPruning(frequentEpisodes, minconf, XQuery, YQuery);
        algo.printStats(XQuery, YQuery, minsup, minconf);
        algo.writeToFile(outputPath, XQuery, YQuery, minsup, minconf);
        algo.saveRulesToFile(outputPath1);
    }
    
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestNONEPI_star.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
