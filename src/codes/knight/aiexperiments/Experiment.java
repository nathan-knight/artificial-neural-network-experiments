package codes.knight.aiexperiments;

import codes.knight.aiexperiments.collectors.Collectors;

public class Experiment {
	
	public static void main(String args[]) {
		Collectors collectors = new Collectors()
				.setShowNearestLine(true)
				.setNeuronsPerHiddenLayer(5)
				.enableCenterDropOff()
				.start();
	}

}
