package codes.knight.aiexperiments;

import codes.knight.aiexperiments.gamecore.Agent;

import java.util.ArrayList;

public class GeneticAlgorithm<T extends Agent> {
	
	private ArrayList<T> agents;
	private float mutationChance = 0.005f;
	private float rate = 0.5f;
	
	public GeneticAlgorithm(ArrayList<T> agents) {
		this.agents = agents;
	}
	
	public ArrayList<Network> nextGeneration() {
		ArrayList<Network> newGeneration = new ArrayList<Network>();

		if(getFitnessSum() / agents.size() < 0.05f) {
			System.out.println("Terminating population");
			for(int i = 0; i < agents.size(); i++) {
				Network newNetwork = new Network(agents.get(0).getNetwork().inputLayer.length, agents.get(0).getNetwork().hiddenLayers.length,
						agents.get(0).getNetwork().hiddenLayers[0].length, agents.get(0).getNetwork().outputLayer.length);
				newNetwork.randomize(-5f, 5f);
				newGeneration.add(newNetwork);
			}
			return newGeneration;
		}
		
		for(int i = 0; i < agents.size(); i++) {
			T parentA = getWeightedRandomAgent();
			T parentB = getWeightedRandomAgent();
			
			Network networkA = parentA.getNetwork();
			Network networkB = parentB.getNetwork();
			
			//If parents are too weak, create new genes
			if(parentA.getFitness() < 0.1f) networkA.randomize(-1, 1);
			if(parentB.getFitness() < 0.1f) networkB.randomize(-1, 1);
			
			Network child = breed(networkA, networkB, parentA.getFitness(), parentB.getFitness());
			newGeneration.add(child);
			
		}
		
		return newGeneration;
		
	}
	
	private Network breed(Network n1, Network n2, float n1Fitness, float n2Fitness) {
		Network cn = new Network(n1.inputLayer.length, n1.hiddenLayers.length, n1.hiddenLayers[0].length, n1.outputLayer.length);

		//Take a weighted random parent to be master and the other to be secondary
		Network masterParent = Math.random() * (n1Fitness + n2Fitness) < n1Fitness ? n1 : n2;
		Network secondaryParent = (masterParent == n1) ? n2 : n1;

		//Take the ratio of the master parent's fitness to the secondary parent's fitness
		float ratio = (masterParent == n1) ? n2Fitness / n1Fitness : n1Fitness / n2Fitness;
		if(Float.isNaN(ratio)) {
			ratio = 1;
		}
		
		//FIRST WEIGHT LAYER (INPUT)
		for(int neuron = 0; neuron < n1.firstWeights.length; neuron++) {
			for(int weight = 0; weight < n1.firstWeights[neuron].length; weight++) {
				if(Math.random() < mutationChance) {
					cn.firstWeights[neuron][weight] = (float) (masterParent.firstWeights[neuron][weight] + (Math.random() - .5) * 2);
				} else {
					float newWeight = masterParent.firstWeights[neuron][weight] + ratio * rate * secondaryParent.firstWeights[neuron][weight];

					cn.firstWeights[neuron][weight] = newWeight;
				}
			}
		}
		
		//WEIGHTS (HIDDEN LAYERS)
		for(int layer = 0; layer < n1.weights.length; layer++) {
			for(int neuron = 0; neuron < n1.weights[0].length; neuron++) {
				for(int weight = 0; weight < n1.weights[0][0].length; weight++) {
					if(Math.random() < mutationChance) {
						cn.weights[layer][neuron][weight] = (float) (masterParent.weights[layer][neuron][weight] + (Math.random() - .5) * 2);
					} else {
						cn.weights[layer][neuron][weight] = masterParent.weights[layer][neuron][weight] + ratio * rate * secondaryParent.weights[layer][neuron][weight];
					}
				}
			}
		}
		
		//LAST WEIGHT LAYER (OUTPUT)
		for(int neuron = 0; neuron < n1.outputWeights.length; neuron++) {
			for(int weight = 0; weight < n1.outputWeights[0].length; weight++) {
				if(Math.random() < mutationChance) {
					cn.outputWeights[neuron][weight] = (float) (masterParent.outputWeights[neuron][weight] + (Math.random() - .5) * 2);
				} else {
					cn.outputWeights[neuron][weight] = masterParent.outputWeights[neuron][weight] + ratio * rate * secondaryParent.outputWeights[neuron][weight];
				}
			}
		}
		return cn;
	}
	
	public float getFitnessSum() {
		float fitnessSum = 0;
		for(T agent : agents) {
			fitnessSum += agent.getFitness();
		}
		return fitnessSum;
	}
	
	public T getWeightedRandomAgent() {
		float weightSum = getFitnessSum();
		float weightChoice = (float) (Math.random() * weightSum);
		int selection = 0;
		T choice = null;
		do {
			if(selection > agents.size()) break;
			choice = agents.get(selection);
			weightChoice -= choice.getFitness();
			selection++;
		} while(weightChoice > 0);
		return choice;
	}

}
