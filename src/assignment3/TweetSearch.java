package assignment3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TweetSearch {
	public final static int NUMBER_OF_TWEET_TO_RETRIEVE = 500;

	public static Map<String, List<JSONObject>> search(String[] queries,
			List<Configuration> configurations) {

		if (queries.length < 1) {
			System.out
			.println("java twitter4j.examples.search.SearchTweets [query]");
			System.exit(-1);
		}

		Map<String, List<JSONObject>> queryResultsMap = new HashMap<String, List<JSONObject>>();

		int configurationCounter = 0;	//This is the counter to use when the query time out	

		for (int i = 0; i < queries.length; i++) {

			List<JSONObject> singleQueryResult;

			try {
				singleQueryResult = singleQuerySearch(queries[i],

						configurations.get(configurationCounter));
				queryResultsMap.put(queries[i], singleQueryResult);
			} catch (TwitterException e) {
				e.printStackTrace();

				if((configurationCounter+1) < configurations.size()){
					System.out.println("This is the num " + 
				((++configurationCounter)+1) + " configuration counter");
				}
			}
		}

		return queryResultsMap;
	}

	public static List<JSONObject> singleQuerySearch(String queryString,
			Configuration configuration) throws TwitterException {

		Twitter twitter = new TwitterFactory(configuration).getInstance();
		Query query = new Query(queryString);
		query.setLang("en");

		long lastID = Long.MAX_VALUE;

		List<JSONObject> jsonTweets = new ArrayList<JSONObject>();

		while (jsonTweets.size() < NUMBER_OF_TWEET_TO_RETRIEVE) {
			if (NUMBER_OF_TWEET_TO_RETRIEVE - jsonTweets.size() > 100)
				query.setCount(100);
			else
				query.setCount(NUMBER_OF_TWEET_TO_RETRIEVE - jsonTweets.size());

			try {
				// A trial run reveal that the result retrieved is unqiue
				QueryResult result = twitter.search(query);

				// Convert the tweet to JSON object
				for (int i = 0; i < result.getTweets().size(); i++) {

					Status status = result.getTweets().get(i);

					String statusJsonInStringForm = TwitterObjectFactory
							.getRawJSON(status);

					JSONObject jsonStatus = new JSONObject(
							statusJsonInStringForm);

					jsonTweets.add(jsonStatus);
				}

				System.out.println(queryString + ": Gathered "
						+ jsonTweets.size() + " tweets");

				for (JSONObject t : jsonTweets) {
					if (t.getLong("id") < lastID) {
						lastID = t.getLong("id");
					}
				}
			} catch (JSONException e) {
				System.out.println("Cannot convert to JSON object: " + e);
				e.printStackTrace();
			}

			query.setMaxId(lastID - 1);
		}

		return jsonTweets;
	}
}