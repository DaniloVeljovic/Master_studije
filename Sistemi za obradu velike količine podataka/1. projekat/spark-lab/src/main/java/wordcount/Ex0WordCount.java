package wordcount;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.sources.In;

import scala.Tuple2;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Ex0WordCount implements Serializable {
    private static String pathToFile;

    public Ex0WordCount(String file){
        this.pathToFile = file;
    }

    public JavaRDD<String> loadData(){
        SparkConf conf = new SparkConf().setAppName("WordCount");

        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<String> words = sc.textFile(pathToFile)
                .flatMap(line -> Arrays.asList(line.split(" ")));

        return words;
    }

    public JavaPairRDD<String, Integer> wordCount(){
        JavaRDD<String> words = loadData();
        JavaPairRDD<String, Integer> couples = words.mapToPair(word -> new Tuple2<>(word, 1));

        JavaPairRDD<String, Integer> result = couples.reduceByKey((a, b) -> a + b);

        return result;
    }

    public JavaPairRDD<String, Integer> filterOnWordCount(int x){
        JavaPairRDD<String, Integer> wordCounts = wordCount();

        JavaPairRDD<String, Integer> filtered = wordCounts.filter(couple -> couple._2() > x);

        return filtered;
    }

    public static void main(String[] args) {
        if(args.length < 1){
            System.err.println("Usage Ex0WordCount <filetxt>");
            System.exit(1);
        }

        Ex0WordCount wc = new Ex0WordCount(args[0]);
        JavaPairRDD<String, Integer> javaPair = wc.wordCount();
        javaPair.collect().forEach(System.out::println);
        //javaPair..collectAsMap().forEach((i, j) -> System.out.println("theta"));
        //wc.wordCount().saveAsTextFile("C:\\Users\\danil\\Desktop\\danilo.txt");

        //List<Tuple2<String, Integer>> collect = wc.wordCount().collect();


    }
}



