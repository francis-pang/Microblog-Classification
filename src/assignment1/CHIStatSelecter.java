package assignment1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import assignment1.Indexer;

public class CHIStatSelecter {
	Indexer indexNUS1, indexNUS2, indexDBS1, indexDBS2, indexStarhub,
			indexNUS1_gp, indexNUS2_gp, indexDBS1_gp, indexDBS2_gp, indexStarhub_gp;
	int catSize, vectorSize, negativeTermSize, negSize1, negSize2, negSize3, negSize4;
	Map<String, Integer> NUS1_df, NUS2_df, DBS1_df, DBS2_df, Starhub_df;
	List<String> NUS1_tt, NUS2_tt, DBS1_tt, DBS2_tt, Starhub_tt;
	List<String> NUS1_CHItt, NUS2_CHItt, DBS1_CHItt, DBS2_CHItt, Starhub_CHItt;
	Map<String, Double> NUS1_CHI, NUS2_CHI, DBS1_CHI, DBS2_CHI, Starhub_CHI;
	int NUS1_docNum, NUS2_docNum, DBS1_docNum, DBS2_docNum, Starhub_docNum, N; 
	int[] negSizeArray;
	Map<String, Vector<String>> vectorStore, topNEGStore, similarNEGStore, randomNEGStore;
	
	public CHIStatSelecter(){
		catSize = 5;
		vectorSize = 200;
		negativeTermSize = 200;
		negSize1 = 50;
		negSize2 = 50;
		negSize3 = 50;
		negSize4 = 50;
		negSizeArray = new int[]{negSize1, negSize2, negSize3, negSize4};
		vectorStore = new HashMap<String, Vector<String>>();
		topNEGStore = new HashMap<String, Vector<String>>();
		similarNEGStore = new HashMap<String, Vector<String>>();
		randomNEGStore = new HashMap<String, Vector<String>>();
	}
	
	public void run(){
		// DBS1 = 1000
		// DBS2 = 200
		// NUS1 = 1000
		// NUS2 = 300
		// Starhub = 1000
		String type;
		
		type = "text";
		generateTextVector(type, Arrays.asList(new Indexer("TRAIN/NUS1.txt", "NUS1 ", vectorSize, type),
										new Indexer("TRAIN/NUS2.txt", "NUS2", vectorSize, type),
										new Indexer("TRAIN/DBS1.txt", "DBS1", vectorSize, type),
										new Indexer("TRAIN/DBS2.txt", "DBS2", vectorSize, type),
										new Indexer("TRAIN/starhub.txt", "Starhub", vectorSize, type)
							), //Array of negative index name 
							Arrays.asList("NUS1_NEG("+type+")", "NUS2_NEG("+type+")", "DBS1_NEG("+type+")", "DBS2_NEG("+type+")", "Starhub_NEG("+type+")"));
		
		type = "geoposition";
		generateTextVector(type, Arrays.asList(new Indexer("TRAIN/NUS1.txt", "NUS1 ", vectorSize, type),
										new Indexer("TRAIN/NUS2.txt", "NUS2", vectorSize, type),
										new Indexer("TRAIN/DBS1.txt", "DBS1", vectorSize, type),
										new Indexer("TRAIN/DBS2.txt", "DBS2", vectorSize, type),
										new Indexer("TRAIN/starhub.txt", "Starhub", vectorSize, type)
								), //Array of negative index name 
								Arrays.asList("NUS1_NEG("+type+")", "NUS2_NEG("+type+")", "DBS1_NEG("+type+")", "DBS2_NEG("+type+")", "Starhub_NEG("+type+")"));

//		generateTextVector();
	}
	
	private void generateTextVector(String type, List<Indexer> indexerList, List<String> negativeFileNameList) {
		
		List<Map<String, Integer>> dfMapList = new ArrayList<Map<String, Integer>>();
		List<List<String>> ttList = new ArrayList<List<String>>();
		List<Integer> docNumList = new ArrayList<Integer>();
		List<Map<String, Double>> chiMapList = new ArrayList<Map<String, Double>>();
		List<List<String>> chiTtList = new ArrayList<List<String>>();
		
		//run index
		for(int i=0; i<indexerList.size(); i++){
			Indexer index = indexerList.get(i);
			index.run();
			dfMapList.add(index.getFrequencyMap());
			ttList.add(index.getTopTerms());
			docNumList.add(index.getNumDocs());
		}
		
		for(int i=0; i<indexerList.size(); i++){
			Indexer index = indexerList.get(i);
			vectorStore.put(type, index.generateTermVectors());
		}
		
		//generate chi stat & negative terms
		for(int i=0; i<indexerList.size(); i++){
			List<Map<String, Integer>> residueDfMapList = new ArrayList<Map<String, Integer>>(dfMapList);
			List<Integer> residueDocNumList = new ArrayList<Integer>(docNumList);
			List<List<String>> residueTtList = new ArrayList<List<String>>(ttList);
			residueDfMapList.remove(i);
			residueDocNumList.remove(i);
			residueTtList.remove(i);
			generateNegativeTerms(type, dfMapList.get(i), ttList.get(i), indexerList.get(i), negativeFileNameList.get(i), residueDfMapList, residueTtList);
			chiMapList.add(generatCHI(dfMapList.get(i), ttList.get(i), docNumList.get(i), residueDfMapList, residueDocNumList));
			chiTtList.add(generateList(chiMapList.get(i)));
			indexerList.get(i).generateTermVectorsWithTopTerms(chiTtList.get(i), "CHI");
		}
		
	}

	private void generateTextVector() {
		indexNUS1 = new Indexer("TRAIN/NUS1.txt", "NUS1 ", vectorSize, "text");
		indexNUS2 = new Indexer("TRAIN/NUS2.txt", "NUS2", vectorSize, "text");
		indexDBS1 = new Indexer("TRAIN/DBS1.txt", "DBS1", vectorSize, "text");
		indexDBS2 = new Indexer("TRAIN/DBS2.txt", "DBS2", vectorSize, "text");
		indexStarhub = new Indexer("TRAIN/starhub.txt", "Starhub", vectorSize, "text");
		
		indexNUS1.run();
		indexNUS2.run();
		indexDBS1.run();
		indexDBS2.run();
		indexStarhub.run();
		
		NUS1_df = indexNUS1.getFrequencyMap();
		NUS2_df = indexNUS2.getFrequencyMap();
		DBS1_df = indexDBS1.getFrequencyMap();
		DBS2_df = indexDBS2.getFrequencyMap();
		Starhub_df = indexStarhub.getFrequencyMap(); 
		
		NUS1_tt = indexNUS1.getTopTerms();
		NUS2_tt = indexNUS2.getTopTerms();
		DBS1_tt = indexDBS1.getTopTerms();
		DBS2_tt = indexDBS2.getTopTerms();
		Starhub_tt = indexStarhub.getTopTerms();
		
		NUS1_docNum = indexNUS1.getNumDocs();
		NUS2_docNum = indexNUS2.getNumDocs();
		DBS1_docNum = indexDBS1.getNumDocs();
		DBS2_docNum = indexDBS2.getNumDocs();
		Starhub_docNum = indexStarhub.getNumDocs();
		N = NUS1_docNum + NUS2_docNum + DBS1_docNum + DBS2_docNum + Starhub_docNum;
		
		NUS1_CHI = generatCHI(NUS1_df, NUS1_tt, NUS1_docNum, Arrays.asList(NUS2_df, DBS1_df, DBS2_df, Starhub_df), 
				 Arrays.asList(NUS2_docNum, DBS1_docNum, DBS2_docNum, Starhub_docNum));
		NUS2_CHI = generatCHI(NUS2_df, NUS2_tt, NUS2_docNum, Arrays.asList(NUS1_df, DBS1_df, DBS2_df, Starhub_df), 
				 Arrays.asList(NUS1_docNum, DBS1_docNum, DBS2_docNum, Starhub_docNum));
		DBS1_CHI = generatCHI(DBS1_df, DBS1_tt, DBS1_docNum, Arrays.asList(NUS1_df, NUS2_df, DBS2_df, Starhub_df), 
				 Arrays.asList(NUS1_docNum, NUS2_docNum, DBS2_docNum, Starhub_docNum));
		DBS2_CHI = generatCHI(DBS2_df, DBS2_tt, DBS2_docNum, Arrays.asList(NUS1_df, NUS2_df, DBS1_df, Starhub_df), 
				 Arrays.asList(NUS1_docNum, NUS2_docNum, DBS1_docNum, Starhub_docNum));
		Starhub_CHI = generatCHI(Starhub_df, Starhub_tt, Starhub_docNum, Arrays.asList(NUS1_df, NUS2_df, DBS1_df, DBS2_df), 
				 Arrays.asList(NUS1_docNum, NUS2_docNum, DBS1_docNum, DBS2_docNum));
		
		NUS1_CHItt = generateList(NUS1_CHI);
		NUS2_CHItt = generateList(NUS2_CHI);
		DBS1_CHItt = generateList(DBS1_CHI);
		DBS2_CHItt = generateList(DBS2_CHI);
		Starhub_CHItt = generateList(Starhub_CHI);
		
		printTerms(NUS1_CHItt, NUS1_CHI);
		
		indexNUS1.generateTermVectors();
		indexNUS2.generateTermVectors();
		indexDBS1.generateTermVectors();
		indexDBS2.generateTermVectors();
		indexStarhub.generateTermVectors();
		
		generateNegativeTerms("text", NUS1_df, NUS1_tt, indexNUS1, "NUS1_NEG", Arrays.asList(NUS2_df, DBS1_df, DBS2_df, Starhub_df), 
				 Arrays.asList(NUS2_tt, DBS1_tt, DBS2_tt, Starhub_tt));
		generateNegativeTerms("text", NUS2_df, NUS2_tt, indexNUS2, "NUS2_NEG", Arrays.asList(NUS1_df, DBS1_df, DBS2_df, Starhub_df), 
				 Arrays.asList(NUS1_tt, DBS1_tt, DBS2_tt, Starhub_tt));
		generateNegativeTerms("text", DBS1_df, DBS1_tt, indexDBS1, "DBS1_NEG", Arrays.asList(NUS1_df, NUS2_df, DBS2_df, Starhub_df), 
				 Arrays.asList(NUS1_tt, NUS2_tt, DBS2_tt, Starhub_tt));
		generateNegativeTerms("text", DBS2_df, DBS2_tt, indexDBS2, "DBS2_NEG", Arrays.asList(NUS1_df, NUS2_df, DBS1_df, Starhub_df), 
				 Arrays.asList(NUS1_tt, NUS2_tt, DBS1_tt, Starhub_tt));
		generateNegativeTerms("text", Starhub_df, Starhub_tt, indexStarhub, "Starhub_NEG", Arrays.asList(NUS1_df, NUS2_df, DBS1_df, DBS2_df), 
				 Arrays.asList(NUS1_tt, NUS2_tt, DBS1_tt, DBS2_tt));
	}
	
	private void generateNegativeTerms(String type, Map<String, Integer> fm, List<String> tt, Indexer index, String index_name,
			List<Map<String, Integer>> fm_list, List<List<String>> tt_lists) {
		String textVectorFileName = "Training_Vector_NEG/TRAINING_VECTOR_"+index_name+".txt";
		Map<String, Double> negTermMap = new HashMap<String, Double>();
		ValueComparator bvc =  new ValueComparator(negTermMap);
        TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
        
        int minNegSize = negativeTermSize;
        for(List<String> ttList: tt_lists){
        	if(ttList.size() < minNegSize) minNegSize = ttList.size(); 
        }
        
        // generate negative vector based on df
        negTermMap = mostFrequentNegativeTerms(fm_list, tt_lists, minNegSize);
        topNEGStore.put(type, index.generateVectors("-1", "Training_Vector_NEG/TRAINING_VECTOR_"+index_name+".txt", negTermMap, minNegSize));
	
        // generate based on similar terms to positive
        negTermMap = similarNegativeTerms(fm, tt, fm_list, tt_lists, minNegSize);
        similarNEGStore.put(type, index.generateVectors("-1", "Training_Vector_NEG/SIMILAR_TRAINING_VECTOR_"+index_name+".txt", negTermMap, minNegSize));
	
        // generate negative vector based on random
        negTermMap = randomNegativeTerms(fm_list, tt_lists, minNegSize);
        randomNEGStore.put(type, index.generateVectors("-1", "Training_Vector_NEG/RANDOM_TRAINING_VECTOR_"+index_name+".txt", negTermMap, minNegSize));

	}
	
	private Map<String, Double> randomNegativeTerms(
			List<Map<String, Integer>> fm_list, List<List<String>> tt_lists, int minNegSize) {
		Map<String, Double> negTermMap = new HashMap<String, Double>();
		// generatate random set 
		List<List<Integer>> randomList = new ArrayList<List<Integer>>();
		for(int k=0; k<tt_lists.size(); k++){
			List<Integer> randomIntArray = new ArrayList<Integer>();
			for(int i=0;i<tt_lists.get(k).size();i++){
				randomIntArray.add(i);  // list contains: [0,1,2,3,4,5,6,7,8,9]
				}
			Collections.shuffle(randomIntArray);
			randomList.add(randomIntArray);
		}
		//keep adding, till reaches negative vector size
        for(int i=0; negTermMap.size()<minNegSize; i++ ){
        	List<String> termList = tt_lists.get(i%tt_lists.size());
        	List<Integer> randomIntArray = randomList.get(i%tt_lists.size());
        	//System.out.println(randomIntArray.size()+" "+i+" "+randomIntArray.get(i));
        	//randomIntArray.get(i);
        	String termToAdd = termList.get(randomIntArray.get(i));
        	//String termToAdd = termList.get(randomIntArray.get(i/tt_lists.size()));
        	if(negTermMap.containsKey(termToAdd)){
        		continue;
        	}
        	negTermMap.put(termToAdd, (double)fm_list.get(i%tt_lists.size()).get(termToAdd));
        }
        return negTermMap;
	}

	private Map<String, Double> similarNegativeTerms(Map<String, Integer> fm,
			List<String> tt, List<Map<String, Integer>> fm_list,
			List<List<String>> tt_lists, int minNegSize) {
		Map<String, Double> negTermMap = new HashMap<String, Double>();
		//add similar terms first
		for(int i=0; (i<tt.size() && negTermMap.size()<minNegSize); i++){
			String termToAdd = tt.get(i);
			for(Map<String, Integer> freqMap: fm_list){
				if(freqMap.containsKey(termToAdd)){
					negTermMap.put(termToAdd, (double)freqMap.get(termToAdd));
					break;
				}
			}
		}
		//keep adding, till reaches negative vector size
        for(int i=0; negTermMap.size()<minNegSize; i++ ){
        	List<String> termList = tt_lists.get(i%tt_lists.size());
        	String termToAdd = termList.get(i/tt_lists.size());
        	if(negTermMap.containsKey(termToAdd)){
        		continue;
        	}
        	negTermMap.put(termToAdd, (double)fm_list.get(i%tt_lists.size()).get(termToAdd));
        }
        return negTermMap;
	}

	private Map<String, Double> mostFrequentNegativeTerms(List<Map<String, Integer>> fm_list, List<List<String>> tt_lists, int minNegSize) {
		Map<String, Double> negTermMap = new HashMap<String, Double>();
		//keep adding, till reaches negative vector size
        for(int i=0; negTermMap.size()<minNegSize; i++ ){
        	List<String> termList = tt_lists.get(i%tt_lists.size());
        	String termToAdd = termList.get(i/tt_lists.size());
        	if(negTermMap.containsKey(termToAdd)){
        		continue;
        	}
        	negTermMap.put(termToAdd, (double)fm_list.get(i%tt_lists.size()).get(termToAdd));
        }
        return negTermMap;
	}

	private void generateTrainingVectors() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("NUS_trainingVector.txt"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void printTerms(List<String> topTerms, Map<String, Double> frequencyMap) {
		StringBuilder termBuf = new StringBuilder();
        for (String topTerm : topTerms) {
          termBuf.append(topTerm).
            append("(").
            append(frequencyMap.get(topTerm)).
            append(");");
        }
        System.out.println(">>> top terms: " + termBuf.toString());
	}

	private List<String> generateList(Map<String, Double> map) {
		List<String> termlist = new ArrayList<String>();
		for (Map.Entry<String, Double> entry : map.entrySet())
		{
			termlist.add(entry.getKey());
		    //System.out.println(entry.getKey() + "/" + entry.getValue());
		}
		return termlist;
	}

	private Map<String, Double> generatCHI(Map<String, Integer> CAT_df, List<String> CAT_tt, 
		int CAT_docNum, List<Map<String, Integer>> list, List<Integer> residueDocNum) {
		Map<String, Double> CHI_MAP = new HashMap<String, Double>();
		ValueComparator bvc =  new ValueComparator(CHI_MAP);
        TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
		int A,B,C,D;
		// A = number of documents in C containing W
		// B = number of documents not in C containing W
		// C = number of documents in C not containing W
		// D = number of documents not in C not containing W
		
		// compute number of documents not in C
		int numDocNotInC = getNumDocNotInC(residueDocNum);
		for(int i=0; i<CAT_tt.size(); i++){
			String W = CAT_tt.get(i);
			// A = number of documents in C containing W
			A = CAT_df.get(W);
			// B = number of documents not in C containing W
			B = computB(W, list);
			// C = number of documents in C not containing W
			C = CAT_docNum - A;
			// D = number of documents not in C not containing W
			D = numDocNotInC - B;
			
			//for each term, generate CHI values using ABCD
			double chi = (((double)N*((A*D)-(C*B)))/((A+C)*(B+D)*(A+B)*(C+D))); 
			CHI_MAP.put(W, chi);
			
			//check for correct doc sum
//			int NTemp = A+B+C+D;
//			if(A>38)
//			System.out.println(((NUS1_docNum + NUS2_docNum + DBS1_docNum + DBS2_docNum + Starhub_docNum)== A+B+C+D )+ 
//			" total:" + NTemp + " N:" + N + " W:" + W + " A:" + A + " B:" + B + " C:" + C + " D:" + D + " CHI:" + chi);
		}
		sorted_map.putAll(CHI_MAP);
		return sorted_map;
	}

	private int getNumDocNotInC(List<Integer> residueDocNum) {
		int count = 0;
		for(Integer num: residueDocNum){
			count += num;
		}
		return count;
	}

	private int computD(String W, List<Map<String, Integer>> list, int B) {
		int WCount = 0;
		for(int i=0; i<list.size(); i++){
			System.out.println("list size: " +list.size());
			Map<String, Integer> tempCAT = list.get(i);
			WCount += tempCAT.get(W);
		}
		return WCount - B;
	}

	private int computB(String W, List<Map<String, Integer>> list) {
		int WCount = 0;
		for(int i=0; i<list.size(); i++){
			Map<String, Integer> tempCAT = list.get(i);
			if(tempCAT.containsKey(W))
				WCount += tempCAT.get(W);
		} 
		return WCount;
	}
}

class ValueComparator implements Comparator<String> {

    Map<String, Double> base;
    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}