package com.flatironschool.javacs;

import io.indico.Indico;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {
	
	// map from URLs that contain the term(s) to relevance score
	private Map<String, Double> map;
  private JedisIndex index;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public WikiSearch(Map<String, Double> map) {
		this.map = map;
	}
	
	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public Double getRelevance(String url) {
		Double tf = map.get(url);
    tf = tf == null ? 0: tf;
		return tf;
	}

	/**
	 * Prints the contents in order of term frequency.
	 * 
	 * @param map
	 */
	public void print() {
		List<Entry<String, Double>> entries = sort();
		for (Entry<String, Double> entry: entries) {
			System.out.println(entry);
		}
	}
	
	/**
	 * Computes the union of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
      Map<String, Double> result = new HashMap();
      for(String url: this.map.keySet()) {
          System.out.println("Iterating through url: " + url);
          result.put(url, this.map.get(url));
      }
      for(String url: that.map.keySet()) {
          result.put(url, that.map.get(url));
          if(this.map.containsKey(url)) {
              result.put(url, this.map.get(url) + that.map.get(url));
          }
      }
      return new WikiSearch(result);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
      Map<String, Double> result = new HashMap();
      for(String url: this.map.keySet()) {
          if(that.map.containsKey(url)) {
              result.put(url, this.map.get(url) + that.map.get(url));
          }
      }
      return new WikiSearch(result);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
        // FILL THIS IN!
      Map<String, Double> result = new HashMap();
      for(String url: this.map.keySet()) {
          if(!that.map.containsKey(url)) {
              result.put(url, this.map.get(url));
          }
      }
      return new WikiSearch(result);
	}
	
	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */
    public List<Entry<String, Double>> sort() {
        List<Entry<String, Double>> results = new LinkedList<Entry<String, Double>>(this.map.entrySet());
        // System.out.println("results is: " + results);
        Comparator<Entry<String, Double>> EntryComparator = new Comparator<Entry<String, Double>>() {
                @Override
                public int compare(Entry<String, Double> entry1, Entry<String, Double> entry2) {
                    return entry2.getValue().compareTo(entry1.getValue());
            };
          };
        Collections.sort(results, EntryComparator);
        return results;

	}

	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */
	public static WikiSearch search(String term, JedisIndex index, String leaning) {
    System.out.println("Search called for term: " + term);
		Map<String, Double> map = index.getCounts(term, leaning);
		return new WikiSearch(map);
	}

	public static void main(String[] args) throws IOException {
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		
		// search for the first term
		String term1 = "legend";
		System.out.println("Query: " + term1);
		WikiSearch search1 = search(term1, index, "Green");
		search1.print();
		
		// search for the second term
		String term2 = "zelda";
		System.out.println("Query: " + term2);
		WikiSearch search2 = search(term2, index, "Green");
		search2.print();
		
		// compute the intersection of the searches
		System.out.println("Query: " + term1 + " AND " + term2);
		WikiSearch intersection = search1.and(search2);
		intersection.print();

    // search for philosophy.
		// String term3 = "family";
		// System.out.println("Query: " + term3);
		// WikiSearch search3 = search(term3, index);
		// search3.print();
	}
}
