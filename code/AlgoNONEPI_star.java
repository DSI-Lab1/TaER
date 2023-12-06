/*
 * Copyright Zefeng Chen et al.
 */
package SRC;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author Zefeng Chen et al.
 */
public class AlgoNONEPI_star {

	/** Start time of the algorithm */
	private long startExecutionTime;

	/** End time of the algorithm */
	private long endExecutionTime;

	/** List of frequent episodes */
	private List<Episode> FrequentEpisodes;

	/** List of episode rules */
	private List<String> allRules = new ArrayList<>();

	/** Candidate episode count */
	private int CandidatEpisodesCount;

	/** Number of frequent episodes */
	private int episodeCount;

	/** Maximum size */
	private int maxsize;

	/** Episode rule count */
	private int ruleCount;

	/** Rules Calculation Times */
	int calculationTimes;
	
	/** Constructor */
	public AlgoNONEPI_star() {

	}

	/**
	 * Scans the input sequence and return the list of single events episodes
	 *
	 * @param path the path to the file that holds the sequencexs
	 * @throws java.io.IOException if the path is incorrect
	 */
	private Map<String, List<Occurrence>> scanSequence(String path) throws IOException {
		BufferedReader reader;
		reader = new BufferedReader(new FileReader(path));
		String line;
		Map<String, List<Occurrence>> SingleEventEpisode = new HashMap<>();
		while ((line = reader.readLine()) != null) {
			String[] lineSplited = line.split("\\|");
			String event = lineSplited[0];
			long timeStamp = Integer.parseInt(lineSplited[1]);
			List<String> events;
			events = new ArrayList<>();
			events.add(event);
			Episode epi = new Episode(events);
			Occurrence occ = new Occurrence(timeStamp, timeStamp);
			if (SingleEventEpisode.containsKey(epi.toString())) {
				SingleEventEpisode.get(epi.toString()).add(occ);
			} else {
				SingleEventEpisode.put(epi.toString(), new ArrayList<>());
				SingleEventEpisode.get(epi.toString()).add(occ);
			}

		}
		reader.close();
		return SingleEventEpisode;
	}

	/**
	 * Recognize the new episode's occurrences starting from two episodes
	 *
	 * @param alpha       a N-node episode to grow
	 * @param singleEvent a single event to grow alpha by.
	 * @return the list of new episode's occurrences
	 */

	private List<Occurrence> OccurrenceRecognition(Episode alpha, Episode singleEvent) { //alpha is the event in the outer loop and singleEvent is the inner loop
		List<Occurrence> oc_1 = alpha.getOccurrences(); //Alpha, occurrence of first item
		//System.out.println("oc_1: " + oc_1);
		List<Occurrence> oc_2 = singleEvent.getOccurrences(); // occurrence of last item
		//System.out.println("oc_2: " + oc_2);
		List<Occurrence> new_occurrences;
		new_occurrences = new ArrayList<>();
		int i = 0, j = 0, k; 
		boolean trouve;
		int taille_1 = oc_1.size(), taille_2; //The number of occurrences corresponding to the first item.
		while (i < taille_1) {
			Occurrence I1 = oc_1.get(i); //Traverse. Starting from the first item of item 0
			trouve = false;
			k = i + 1; 
			taille_2 = oc_2.size(); 
			while (j < taille_2) {
				Occurrence I2 = oc_2.get(j); 
				if (I2.getStart() > I1.getEnd()) {
					Occurrence occ = new Occurrence(I1.getStart(), I2.getEnd()); 
					new_occurrences.add(occ); 
					trouve = true; 
					while (k < taille_1) { 
						if (oc_1.get(k).getStart() > occ.getEnd()) {
							break;
						}
						k = k + 1;
					}
				}
				if (trouve) {
					break;
				} else {
					j++;
				}

			}
			i = k;
		}
		return new_occurrences;
	}

	/**
	 * Generate the set of episode rules from the frequent episodes set (with
	 * pruning)
	 *
	 * @param FrequentEpisodes set of frequent episodes already recognized
	 * @param minconf          confidence threshold
	 * @return set of all episodes rules
	 */
	public List<String> findNONEpiRulesWithPruning(List<Episode> FrequentEpisodes, float minconf, List<String> XQuery, List<String> YQuery) {
		allRules = new ArrayList<>();
		for (int i = 0; i < FrequentEpisodes.size(); i++) {
			Episode alpha = FrequentEpisodes.get(i);
			Episode beta = Predecessor(alpha.toString());
			boolean stop = false;
			while (!stop && beta != null) {
				int beta_support = 0;
				for (Episode t_beta : FrequentEpisodes) {
					if (beta.toString().equals(t_beta.toString())) {
						beta_support = t_beta.getSupport();
						break;
					}
				}
				int alpha_support = alpha.getSupport();
				if (((float) alpha_support / (float) beta_support) >= minconf) {
					calculationTimes++;
					allRules.add(beta.toSPMFString() + " ==> " + alpha.toSPMFString().substring(beta.toSPMFString().length(), alpha.toSPMFString().length())
					+ " #SUP: " + alpha_support 
					+ " #CONF: "
					+ (float) alpha_support / (float) beta_support);
					List<String> gammaList = alpha.toList().subList(beta.toList().size(), alpha.toList().size());
					//if(!isSubList(gammaList, YQuery) || !isSubList(beta.toList(), XQuery)) {
					if(!(isSubList(alpha.toList(), YQuery) && isSubList(beta.toList(), XQuery) && !isSubList(beta.toList(), YQuery))) {
						allRules.remove(allRules.size() - 1);
						//allRules.add(isSubList(beta.toList(), XQuery) + " / " + isSubList(gammaList, YQuery));
					}
					beta = Predecessor(beta.toString());
				} else {
					stop = true;
				}
			}
		}
		
		this.endExecutionTime = System.currentTimeMillis();
		this.ruleCount = allRules.size();
		return allRules;
	}

	/**
	 * Generate new Episodes and filter only the frequent ones
	 *
	 * @param input      A sequence of events
	 * @param minsupport Support threshold
	 * @return f_episode list of all frequent episodes
	 * @throws IOException If the path is incorrect or the file doesn't exists
	 */
	public List<Episode> findFrequentEpisodes(String input, int minsupport, List<String> XQuery, List<String> YQuery) throws IOException {
		this.startExecutionTime = System.currentTimeMillis();
		MemoryLogger.getInstance().reset(); //Used to record memory usage
		List<Episode> f_episode = new ArrayList<>(); //Create a new list of Episodes
		this.startExecutionTime = System.currentTimeMillis(); //Used to record time
		Map<String, List<Occurrence>> singleEpisodeEvent; //Event + timestamp
		singleEpisodeEvent = scanSequence(input); // Searching for frequent events with a length of 1
		Object[] episodes = singleEpisodeEvent.keySet().toArray(); //Frequent occurrence set
		this.CandidatEpisodesCount = episodes.length; //Number of frequent events with a length of 1
		// this.FrequentEpisodes = new HashMap<>();
		for (Object episode : episodes) { //Cutting event sets, traversing
			int t_sup = singleEpisodeEvent.get(episode.toString()).size(); //The occurrence time of the set was obtained through the HashMap and the Key, and the number of occurrences represents the support level
			if (t_sup >= minsupport) { //If the 1-event is frequent, perform the following to make it enter the frequent list
				List<String> t_events = new ArrayList<>(); //List of String types used for events
				Collections.addAll(t_events, StrToList(episode.toString())); //Put the frequent event set in
				Episode t_epi = new Episode(t_events); //Build a function to construct the event
				List<Occurrence> occurrences = singleEpisodeEvent.get(episode.toString()); //Include the occurrence time of the event
				t_epi.setOccurrences(occurrences);
				t_epi.setSupport(t_sup); 
				f_episode.add(t_epi); 
			}
		}
		
		List<Episode> t_freq = new ArrayList<>();
		for(Episode e : f_episode) {
			t_freq.add(e);
		}
		
		//int i = 0, j;
		//System.out.println("t_freq: " + t_freq);
		int thesize = t_freq.size();//Size of frequent 1-event list
		for(int i = 0; i < thesize; i++) {
			Episode alpha = t_freq.get(i);
			dfs(minsupport, alpha, t_freq, f_episode);
		}
		this.endExecutionTime = System.currentTimeMillis();
		MemoryLogger.getInstance().checkMemory();
		episodeCount = f_episode.size();
		return f_episode;
	}

	/**
	 * Check if it is injective
	 * 
	 * @param events a list of events
	 * @return true if injective, otherwise false
	 */
	private boolean isInjective(List<String> events) { //Recursive search for whether it includes
		if (events.isEmpty()) {
			return true;
		}
		String event = events.get(events.size() - 1);
		events = events.subList(0, events.size() - 1);
		if (events.contains(event)) {
			return false;
		}
		return isInjective(events);
	}

	/**
	 * Get the predecessor
	 * 
	 * @param alpha
	 * @return the predecessor episode or null
	 */
	private Episode Predecessor(String alpha) {
		String[] temp_alpha = StrToList(alpha);
		if (temp_alpha.length != 1) {
			String[] t_predecessor = new String[temp_alpha.length - 1];
			for (int i = 0; i < t_predecessor.length; i++) {
				t_predecessor[i] = temp_alpha[i];
			}
			List<String> events = new ArrayList<>();
			Collections.addAll(events, t_predecessor);
			return new Episode(events);
		}
		return null;
	}

	/**
	 * Convert a String to an array of String
	 * 
	 * @param string the string
	 * @return an array of String
	 */
	private static String[] StrToList(String string) {
		int index_1 = string.indexOf("<");
		String tempString = string.substring(index_1 + 1, string.length() - 1);
		if (tempString.contains("->")) {
			return tempString.split("->");
		}
		return new String[] { tempString };
	}

	/**
	 * Get the frequent episodes
	 * 
	 * @return the list of frequent episodes
	 */
	public List<Episode> getFrequentEpisodes() {
		return this.FrequentEpisodes;
	}

	/**
	 * Print statistics about the last execution of the algorithm.
	 */
	public void printStats(List<String> XQuery, List<String> YQuery, int minsup, double minconf) {
		System.out.println("=============  NONEPI* - STATS =============");
		System.out.println(" minsup: " + minsup + "   minconf: " + minconf);
		System.out.println(" XQuery: " + XQuery + "   YQuery: " + YQuery);
		System.out.println(" Candidates count : " + this.CandidatEpisodesCount);
		System.out.println(" The algorithm stopped at size : " + maxsize);
		System.out.println(" Frequent episodes count : " + episodeCount);
		System.out.println(" Rules Calculation Times : " + calculationTimes);
		System.out.println(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb");
		System.out.println(" Total time ~ : " + (double)(this.endExecutionTime - this.startExecutionTime)/1000 + " s");
		System.out.println(" Episode rule count: " + this.ruleCount);
		System.out.println("===================================================");
	}
	
	public void writeToFile(String outputPath, List<String> XQuery, List<String> YQuery, int minsup, double minconf) throws IOException {
		try {
			//FileOutputStream outputStream = new FileOutputStream(outputPath, false);
			FileWriter writer = new FileWriter(outputPath, true);
			writer.write("=============  NONEPI* - STATS =============\n");
			writer.write(" minsup: " + minsup + "   minconf: " + minconf + "\n");
			writer.write(" XQuery: " + XQuery + "   YQuery: " + YQuery + "\n");
			writer.write(" Candidates count : " + this.CandidatEpisodesCount + "\n");
			writer.write(" The algorithm stopped at size : " + maxsize + "\n");
			writer.write(" Frequent episodes count : " + episodeCount + "\n");
			writer.write(" Rules Calculation Times : " + calculationTimes + "\n");
			writer.write(" Maximum memory usage : " + MemoryLogger.getInstance().getMaxMemory() + " mb" + "\n");
			writer.write(" Total time ~ : " + (double)(this.endExecutionTime - this.startExecutionTime)/1000 + " s");
			writer.write(" Episode rule count: " + this.ruleCount + "\n");
			writer.write("==========================================\n");
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void dfs(int minsupport, Episode alpha, List<Episode> t_freq, List<Episode> f_episode) {
		int thesize = t_freq.size();
		for(int j = 0; j < thesize; j++) {
			List<String> newEvents = new ArrayList<>();
			Collections.addAll(newEvents, StrToList(alpha.toString()));
			newEvents.add(newEvents.size(), StrToList(t_freq.get(j).toString())[0]);
			Episode new_epi = new Episode(newEvents);
			CandidatEpisodesCount++;
			if (isInjective(newEvents)) {
				List<Occurrence> newOccurrences = OccurrenceRecognition(alpha, t_freq.get(j));
				int t_support = newOccurrences.size();
				if (t_support >= minsupport) {
					new_epi.setOccurrences(newOccurrences);
					new_epi.setSupport(t_support);
					// this.FrequentEpisodes.put(new_epi.toString(), newOccurrences);
					f_episode.add(new_epi);
					//alpha = new_epi;
					if (new_epi.getEvents().size() >= maxsize) {
						maxsize = new_epi.getEvents().size();
					}
					dfs(minsupport, new_epi, t_freq, f_episode);
				}
			}
		}
	}

	/**
	 * Save the rules to a file
	 * 
	 * @param outputPath the output file path
	 * @throws IOException 
	 */
	public void saveRulesToFile(String outputPath) throws IOException {
		try {
			FileWriter writer = new FileWriter(outputPath, true);
			writer.write(rulesAsString());
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isSubList(List<String> t, List<String> s) {
		int n = s.size(), m = t.size();
        int i = 0, j = 0;
        while (i < n && j < m) {
            if (s.get(i).equals(t.get(j))) {
                i++;
            }
            j++;
        }
        return i == n;
    }

	/**
	 * Get a string representation of this set of rules (for printing or writing to
	 * file)
	 * 
	 * @return a string
	 */
	private String rulesAsString() {
		StringBuilder buffer = new StringBuilder();

		// For each rule
		for (int z = 0; z < allRules.size(); z++) {
			String rule = allRules.get(z);

			buffer.append(rule);
			buffer.append(System.lineSeparator());
		}
		return buffer.toString();
	}
}
