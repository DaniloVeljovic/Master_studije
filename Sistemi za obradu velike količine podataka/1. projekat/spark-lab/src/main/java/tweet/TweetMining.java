package tweet;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;

public class TweetMining {

    private String pathToFile;

    public TweetMining(String pathToFile){
        this.pathToFile = pathToFile;
    }

    public JavaRDD<Tweet> loadData(){
        SparkConf conf = new SparkConf().setAppName("TweetMining");

        JavaSparkContext sc = new JavaSparkContext(conf);
        /*JavaRDD<Tweet> tweets = sc.textFile(pathToFile).map(new Function<String, Tweet>() {
            @Override
            public Tweet call(String s) throws Exception {
                return Parse.parseJsonToTweet(s);
            }
        });*/

        JavaRDD<Tweet> tweets = sc.textFile(pathToFile).map(line -> Parse.parseJsonToTweet(line));

        return tweets;
    }

    public JavaRDD<String> mentionOnTweet(){
        JavaRDD<Tweet> tweets = loadData();

        JavaRDD<String> mentions = tweets.flatMap(new FlatMapFunction<Tweet, String>() {
            @Override
            public Iterable<String> call(Tweet tweet) throws Exception {
                return Arrays.asList(tweet.getText().split(" "));
            }
        })
                .filter(new Function<String, Boolean>() {
                    @Override
                    public Boolean call(String s) throws Exception {
                        return s.startsWith("@") && s.length() > 1;
                    }
                });

        System.out.println("mentions count() " + mentions.count());
        return mentions;
    }


    public JavaPairRDD<String, Integer> countMentions(){
        JavaRDD<String> mentions = mentionOnTweet();

        JavaPairRDD<String, Integer> mapPair = mentions.mapToPair(word -> new Tuple2<>(word, 1));
        JavaPairRDD<String, Integer> result = mapPair.reduceByKey((a, b) -> a + b);


        return result;
    }

    public List<Tuple2<Integer, String>> top10mentions(){
        JavaPairRDD<String, Integer> mentions = countMentions();
        List<Tuple2<Integer, String>> retVal = mentions.mapToPair(pair -> new Tuple2<>(pair._2(), pair._1())).sortByKey(false).take(10);

        return retVal;
    }


}
