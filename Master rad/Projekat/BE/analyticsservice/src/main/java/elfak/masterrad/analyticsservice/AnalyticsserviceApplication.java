package elfak.masterrad.analyticsservice;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.clustering.Clusterer;
import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.tools.data.FileHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
public class AnalyticsserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnalyticsserviceApplication.class, args);
	}

	//@EventListener(ApplicationReadyEvent.class)
	private void test() throws IOException {
		/* Load a data set */
		//Dataset data = FileHandler.loadDataset(new File("devtools/data/iris.data"), 4, ",");
		/* Contruct a KNN classifier that uses 5 neighbors to make a
		 *decision. */
		//Classifier knn = new KNearestNeighbors(5);
		//knn.buildClassifier(data);
		/* Load a dataset */
		File file = new File("C:\\Users\\danil\\Desktop\\Master_studije\\Master rad\\Projekat\\BE\\analyticsservice\\src\\main\\resources\\iris.data");
		Dataset data = FileHandler.loadDataset(file, 4, ",");
		/*
		 * Create a new instance of the KMeans algorithm, with no options
		 * specified. By default this will generate 4 clusters.
		 */
		//Clusterer km = new KMeans();
		/*
		 * Cluster the data, it will be returned as an array of data sets, with
		 * each dataset representing a cluster
		 */
		//Dataset[] clusters = km.cluster(data);
		//System.out.println("Cluster count: " + clusters.length);

		Classifier knn = new KNearestNeighbors(5);
		knn.buildClassifier(data);

		/*
		 * Load a data set for evaluation, this can be a different one, but we
		 * will use the same one.
		 */
		Dataset dataForClassification = FileHandler.loadDataset(new File("C:\\Users\\danil\\Desktop\\Master_studije\\Master rad\\Projekat\\BE\\analyticsservice\\src\\main\\resources\\iris.data"), 4, ",");
		/* Counters for correct and wrong predictions. */
		int correct = 0, wrong = 0;
		/* Classify all instances and check with the correct class values */
		for (Instance inst : dataForClassification) {
			Object predictedClassValue = knn.classify(inst);
			Object realClassValue = inst.classValue();
			if (predictedClassValue.equals(realClassValue))
				correct++;
			else
				wrong++;
		}
		System.out.println("Correct predictions  " + correct);
		System.out.println("Wrong predictions " + wrong);
	}

}
