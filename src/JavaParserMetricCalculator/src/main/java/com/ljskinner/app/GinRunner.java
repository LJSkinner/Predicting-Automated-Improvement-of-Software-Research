/**
 * 
 */
package com.ljskinner.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;

import com.ljskinner.jputils.JPExtractor;

import org.tinylog.Logger;

/**
 * This class is responsible for utilising the JavaParser metric extractor and
 * running it over open source projects which have been used in Gin.
 * 
 * 
 * @author Luke Skinner
 *
 */
public class GinRunner {

	private static final String METHOD_LIST_FILE_PATH = "/home/valla/Development/Python/MachineLearning/Predicting-Automated-Improvement-of-Software-Research/src/JavaParserMetricCalculator/src/main/resources/MethodList.txt";

	private static final String OUTPUT_FILE_NAME = "StatsFromJavaParser.csv";

	private static final String[] githubProjects = new String[] { "arthas", "disruptor", "druid", "gson", "jcodec",
			"junit", "ibatis", "opennlp", "spark", "spatial4j" };

	private static final String[] projectPaths = new String[] {
			"/home/valla/Development/Python/MachineLearning/Predicting-Automated-Improvement-of-Software-Research/casestudies/arthas/core/src/main/java/",
			"/home/valla/Development/Python/MachineLearning/Predicting-Automated-Improvement-of-Software-Research/casestudies/disruptor/src/main/java/",
			"/home/valla/Development/Python/MachineLearning/Predicting-Automated-Improvement-of-Software-Research/casestudies/druid/src/main/java/",
			"/home/valla/Development/Python/MachineLearning/Predicting-Automated-Improvement-of-Software-Research/casestudies/gson/gson/src/main/java/",
			"/home/valla/Development/Python/MachineLearning/Predicting-Automated-Improvement-of-Software-Research/casestudies/jcodec/src/main/java/",
			"/home/valla/Development/Python/MachineLearning/Predicting-Automated-Improvement-of-Software-Research/casestudies/junit4/src/main/java/",
			"/home/valla/Development/Python/MachineLearning/Predicting-Automated-Improvement-of-Software-Research/casestudies/mybatis-3/src/main/java/",
			"/home/valla/Development/Python/MachineLearning/Predicting-Automated-Improvement-of-Software-Research/casestudies/opennlp/opennlp-tools/src/main/java/",
			"/home/valla/Development/Python/MachineLearning/Predicting-Automated-Improvement-of-Software-Research/casestudies/spark/src/main/java/",
			"/home/valla/Development/Python/MachineLearning/Predicting-Automated-Improvement-of-Software-Research/casestudies/spatial4j/src/main/java/" };

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			processFiles();
		} catch (FileNotFoundException fnfe) {
			Logger.error("There was a problem with creating the file, please review the message: {}",
					fnfe.getMessage());
		} catch (IOException e) {
			Logger.error(
					"An I/O error occured when attempting to read from the file, please review the message: {}",
					e.getLocalizedMessage());
		}
	}

	private static void processFiles() throws IOException {
		try (PrintStream outFile = new PrintStream(new FileOutputStream(OUTPUT_FILE_NAME))) {

			List<String> methods = Files.readAllLines(new File(METHOD_LIST_FILE_PATH).toPath());

			// Header line
			outFile.println("method," + "surfaceIfs,nestedIfs," + "surfaceSwitches,nestedSwitches,"
					+ "surfaceFors,nestedFors," + "surfaceForEachs,nestedForEachs," + "surfaceWhiles,nestedWhiles,"
					+ "surfaceDos,nestedDos," + "iterativeStmts,conditionalStmts");

			for (String method : methods) {
				Logger.info("Processing method {}", method);

				int projectIndex = getProjectIndex(method);

				String methodFqNameMinusArgs = method.substring(0, method.lastIndexOf("("));

				String className = projectPaths[projectIndex]
						+ methodFqNameMinusArgs.substring(0, methodFqNameMinusArgs.lastIndexOf(".")).replace(".", "/")
						+ ".java";

				int[] metrics = computeMetrics(className, method);

				Logger.info("Writing metrics to file...");

				outFile.printf("\"%s\",%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d%n", method, metrics[0], metrics[1],
						metrics[2], metrics[3], metrics[4], metrics[5], metrics[6], metrics[7], metrics[8], metrics[9],
						metrics[10], metrics[11], metrics[12], metrics[13]);
			}
		}
	}

	private static int[] computeMetrics(String sourceFile, String method) throws FileNotFoundException {
		JPExtractor jpExtractor = new JPExtractor(sourceFile);

		if (!jpExtractor.hasMethod(method)) {
			Logger.warn("The method {} could not be found in the source file {}. Metrics will be 0 for this method", method, sourceFile);
		}

		int surfaceIfs = jpExtractor.numberOfSurfaceIfIn(method);

		int nestedIfs = jpExtractor.numberOfNestedIfIn(method);

		int surfaceSwitches = jpExtractor.numberOfSurfaceSwitchIn(method);

		int nestedSwitches = jpExtractor.numberOfNestedSwitchIn(method);

		int surfaceFors = jpExtractor.numberOfSurfaceForIn(method);

		int nestedFors = jpExtractor.numberOfNestedForIn(method);

		int surfaceForEachs = jpExtractor.numberOfSurfaceForEachIn(method);

		int nestedForEachs = jpExtractor.numberOfNestedForEachIn(method);

		int surfaceWhiles = jpExtractor.numberOfSurfaceWhileIn(method);

		int nestedWhiles = jpExtractor.numberOfNestedWhileIn(method);

		int surfaceDos = jpExtractor.numberOfSurfaceDoIn(method);

		int nestedDos = jpExtractor.numberOfNestedDoIn(method);

		int iterativeStmts = surfaceFors + nestedFors + surfaceForEachs + nestedForEachs + surfaceWhiles + nestedWhiles
				+ surfaceDos + nestedDos;

		int conditionalStmts = surfaceIfs + nestedIfs + surfaceSwitches + nestedSwitches;

		return new int[] { surfaceIfs, nestedIfs, surfaceSwitches, nestedSwitches, surfaceFors, nestedFors,
				surfaceForEachs, nestedForEachs, surfaceWhiles, nestedWhiles, surfaceDos, nestedDos, iterativeStmts,
				conditionalStmts };
	}

	private static int getProjectIndex(String method) {
		for (int i = 0; i < projectPaths.length; i++) {
			if (method.contains(githubProjects[i])) {
				return i;
			}
		}

		return -1;
	}

}
