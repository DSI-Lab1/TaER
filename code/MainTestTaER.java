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
 * Example of how to run the TaER algorithm from the source code.
 * @see AlgoTaER
 * @author Zefeng Chen
 */

public class MainTestTaER {

    public static void main(String[] args) throws IOException {
        // Input file path
    	String input = "contextPaper";
    	String inputPath = fileToPath(input + "_event.txt");
    	
    	// Output file path
    	String outputPath = input + "output_TaER.txt";
    	//String outputPath1 = input + "output_TaER_patterns.txt";
    	
    	// Minimum support (an integer representing a number of occurrences)
    	int minsup = 3;
        
        // Minimum confidence (represents a percentage)
        float minconf = 0.5f;
        
        String s1 = "3";
        //String s2 = "18";
        String s3 = "2";
        
        List<String> XQuery = new ArrayList<String>();
        List<String> YQuery = new ArrayList<String>();
        
        XQuery.add(s1);
        //XQuery.add(s2);
        YQuery.add(s3);
        
        // (1) First extracts frequent episodes with NONEPI
        AlgoTaER algo = new AlgoTaER();
        List<Episode> frequentEpisodes = algo.findFrequentEpisodes(inputPath, minsup, XQuery, YQuery);
        
        // (2) Second, find episodes rules from frequent episodes        
        List<String> rules = algo.findNONEpiRulesWithPruning(frequentEpisodes, minconf, XQuery, YQuery);
        algo.printStats(XQuery, YQuery, minsup, minconf);
        algo.writeToFile(outputPath, XQuery, YQuery, minsup, minconf);
        algo.saveRulesToFile(outputPath);
    }
    
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestTaER.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
