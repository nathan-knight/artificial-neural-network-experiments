package codes.knight.aiexperiments;

import codes.knight.aiexperiments.collectors.Collectors;

public class Experiment {
	
	public static void main(String args[]) {
		if ("collectors".equalsIgnoreCase(args[0])) {
			launchCollectors(args);
		}
	}

	private static void launchCollectors(String[] args) {
		Collectors collectors = new Collectors();
		for (int i = 0; i < args.length; i++) {
			switch (args[i].toLowerCase()) {
				case "-l":
				case "--layers":
					collectors.setNumberOfHiddenLayers(Integer.valueOf(args[++i]));
					break;
				case "-n":
				case "--neurons":
					collectors.setNeuronsPerHiddenLayer(Integer.valueOf(args[++i]));
					break;
				case "--draw-nearest-line":
					collectors.setShowNearestLine(true);
					break;
				case "--use-center":
					collectors.enableCenterDropOff();
					break;
				case "--use-nearest-neighbour":
				case "--use-nearest-neighbor":
					collectors.enableNeighbourAwareness();
					break;
			}
		}
		collectors.start();
	}

}
