package codes.knight.aiexperiments;

import codes.knight.aiexperiments.collectors.CollectorsExperiment;
import codes.knight.aiexperiments.colorgraph.ColorGraph;

public class Experiment {
	
	public static void main(String args[]) {
		if (args.length == 0) {
			System.out.println("No arguments specified! Please specify --collectors or --colorgraph");
		}
		if ("collectors".equalsIgnoreCase(args[0]) || "--collectors".equalsIgnoreCase(args[0])) {
			launchCollectors(args);
		} else if ("--colorgraph".equalsIgnoreCase(args[0])) {
			launchColorGraph(args);
		}
	}

	private static void launchColorGraph(String[] args) {
		ColorGraph colorGraph = new ColorGraph();

		colorGraph.start();
	}

	private static void launchCollectors(String[] args) {
		CollectorsExperiment collectorsExperiment = new CollectorsExperiment();
		for (int i = 0; i < args.length; i++) {
			switch (args[i].toLowerCase()) {
				case "-l":
				case "--layers":
					collectorsExperiment.setNumberOfHiddenLayers(Integer.valueOf(args[++i]));
					break;
				case "-n":
				case "--neurons":
					collectorsExperiment.setNeuronsPerHiddenLayer(Integer.valueOf(args[++i]));
					break;
				case "--draw-nearest-line":
					collectorsExperiment.setShowNearestLine(true);
					break;
				case "--use-center":
					collectorsExperiment.enableCenterDropOff();
					break;
				case "--use-nearest-neighbour":
				case "--use-nearest-neighbor":
					collectorsExperiment.enableNeighbourAwareness();
					break;
			}
		}
		collectorsExperiment.start();
	}

}
