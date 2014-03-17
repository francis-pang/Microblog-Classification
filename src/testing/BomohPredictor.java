package testing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Tagger.TaggedToken;

public class BomohPredictor {
	
	ArrayList<Integer> prediction = new ArrayList<Integer>();
	ArrayList<String> tweetstore = new ArrayList<String>();

	private void run(String testDataFile) throws IOException, JSONException{
		
		//////////////////////////////////////////////
		// 1. Read in adjectives from general inquirer
		
		HashMap<String, Integer> posLex = readLexiconFromGI("data\\GI_positive.txt", 0); //0
		HashMap<String, Integer> negLex = readLexiconFromGI("data\\GI_negative.txt", 1); //1
		HashMap<String, Integer> neuLex = readLexiconFromGI("data\\GI_neutral.txt", 2); //2
		
		
		/////////////////////////////
		// 2. read in training tweets
		
		String modelFilename = "/cmu/arktweetnlp/model.20120919";
		List<TaggedToken> taggedTokens;
		Tagger tagger = new Tagger();
		tagger.loadModel(modelFilename);
		 
		BufferedReader br;
		String line, word;
		int pos, neg, neu, pred;
		br = new BufferedReader(new FileReader(testDataFile));
		while((line = br.readLine()) != null){
			
			pos = neg = neu = 0;
			JSONObject tweet = new JSONObject(line);
			String text = tweet.getString("text");
			tweetstore.add(text);
			
			
			taggedTokens = tagger.tokenizeAndTag(text);
			for (TaggedToken t : taggedTokens){	
				if (t.tag.equalsIgnoreCase("#")){ t.token = t.token.substring(1); } // remove hashtag at front
				word = t.token.toUpperCase();
				
				 if (posLex.containsKey(word)) pos++;
				 if (negLex.containsKey(word)) neg++;
				 if (neuLex.containsKey(word)) neu++;
			}
			
			/*
			 * Scoring:
			 * 	pos = pos > neg
			 *	neg = neg > pos
			 *	neu = no pos no neg or pos==neg
			 * */
			
			if (pos > neg) pred=0;
			else if (neg > pos) pred=1;
			else pred=2;
			
			prediction.add(pred);
			System.out.println(pos + " " + neg + " " + neu + " " + pred + " " + text);
		}
		 
		br.close();
	}
	
	private void coconutDropFromSky(String fn) throws IOException{
		BufferedReader br;
		String line;
		String curr[];
		ArrayList<ArrayList<String>> gold = new ArrayList<ArrayList<String>>(); 

		br = new BufferedReader(new FileReader(fn));
		while((line = br.readLine()) != null){
			curr = line.split(",");
			
			gold.add(new ArrayList<String>());
			
		}
		br.close();

	}
	
	
	private HashMap<String, Integer> readLexiconFromGI(String fn, Integer x) throws IOException{
		BufferedReader br;
		String line;
		HashMap<String, Integer> Lexicon = new HashMap<String, Integer>();

		br = new BufferedReader(new FileReader(fn));
		while((line = br.readLine()) != null){
			Lexicon.put(line, x);
		}
		br.close();
		
		return Lexicon;
	}
	
	public static void main(String[] args) throws IOException, JSONException{
		BomohPredictor bomoh = new BomohPredictor();
		bomoh.run("TEST\\tweets_test.txt");
//		for (int i=0; i<bomoh.prediction.size(); i++){
//			System.out.print(bomoh.prediction.get(i) + "    ");
//			System.out.println(bomoh.tweetstore.get(i));
//		}
	}
}
