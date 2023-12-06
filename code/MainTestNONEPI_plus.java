/*
 * Copyright Zefeng Chen et al.
 */
package SRC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;


/**
 * Example of how to run the NONEPI algorithm from the source code.
 * @see AlgoNONEPI_plus
 * @author Zefeng Chen
 */
public class MainTestNONEPI_plus {

    public static void main(String[] args) throws IOException {
        // Input file path
    	String input = "contextPaper";
    	String inputPath = fileToPath(input + "_event.txt");
    	
    	// Output file path
    	String outputPath = input + "output.txt";
    	
    	// Minimum support (an integer representing a number of occurrences)
    	int minsup = 3;
        
        // Minimum confidence (represnts a percentage)
        float minconf = 0.5f;
        
        // (1) First extracts frequent episodes with NONEPI
        AlgoNONEPI_plus algo = new AlgoNONEPI_plus();
        List<Episode> frequentEpisodes = algo.findFrequentEpisodes(inputPath, minsup);
        
        // (2) Second, find episodes rules from frequent episodes        
        List<String> rules = algo.findNONEpiRulesWithPruning(outputPath, frequentEpisodes, minconf);
        algo.printStats();
        algo.writeToFile(outputPath);
        algo.saveRulesToFile(outputPath);
    }
    
	public static String fileToPath(String filename) throws UnsupportedEncodingException{
		URL url = MainTestNONEPI_plus.class.getResource(filename);
		 return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
	}
}
