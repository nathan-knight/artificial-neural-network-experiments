package codes.knight.aiexperiments;

import java.util.ArrayList;

import codes.knight.aiexperiments.gamecore.Agent;

public class GeneticAlgorithm<T extends Agent> {
	
	private ArrayList<T> agents;
	private float mutationChance = 0.001f;
	
	public GeneticAlgorithm(ArrayList<T> agents) {
		this.agents = agents;
	}
	
	public ArrayList<Network> nextGeneration() {
		ArrayList<Network> newGeneration = new ArrayList<Network>();
		
		for(int i = 0; i < agents.size(); i++) {
			T parentA = getWeightedRandomAgent();
			T parentB = getWeightedRandomAgent();
			
			Network networkA = parentA.getNetwork();
			Network networkB = parentB.getNetwork();
			
			//If parents are too weak, create new genes
			if(parentA.getFitness() < 0.05f) networkA.randomize(-1, 1);
			if(parentB.getFitness() < 0.05f) networkB.randomize(-1, 1);
			
			Network child = breed(networkA, networkB);
			newGeneration.add(child);
			
		}
		
		return newGeneration;
		
	}
	
	public Network breed(Network n1, Network n2) {
		Network cn = new Network(n1.inputLayer.length, n1.hiddenLayers.length, n1.hiddenLayers[0].length, n1.outputLayer.length);
		
		//FIRST WEIGHT LAYER (INPUT)
		for(int neuron = 0; neuron < n1.firstWeights.length; neuron++) {
			for(int weight = 0; weight < n1.firstWeights[neuron].length; weight++) {
				if(Math.random() < 0.5) {
					if(Math.random() < mutationChance) {
						cn.firstWeights[neuron][weight] = (float) (n1.firstWeights[neuron][weight] + (Math.random() - .5) * 2);
					} else {
						cn.firstWeights[neuron][weight] = n1.firstWeights[neuron][weight];
					}
				} else {
					if(Math.random() < mutationChance) {
						cn.firstWeights[neuron][weight] = (float) (n2.firstWeights[neuron][weight] + (Math.random() - .5) * 2);
					} else {
						cn.firstWeights[neuron][weight] = n2.firstWeights[neuron][weight];
					}
				}
			}
		}
		
		//WEIGHTS (HIDDEN LAYERS)
		for(int layer = 0; layer < n1.weights.length; layer++) {
			for(int neuron = 0; neuron < n1.weights[0].length; neuron++) {
				for(int weight = 0; weight < n1.weights[0][0].length; weight++) {
					if(Math.random() < 0.5) {
						if(Math.random() < mutationChance) {
							cn.weights[layer][neuron][weight] = (float) (n1.weights[layer][neuron][weight] + (Math.random() - .5) * 2);
						} else {
							cn.weights[layer][neuron][weight] = n1.weights[layer][neuron][weight];
						}
					} else {
						if(Math.random() < mutationChance) {
							cn.weights[layer][neuron][weight] = (float) (n2.weights[layer][neuron][weight] + (Math.random() - .5) * 2);
						} else {
							cn.weights[layer][neuron][weight] = n2.weights[layer][neuron][weight];
						}
					}
				}
			}
		}
		
		//LAST WEIGHT LAYER (OUTPUT)
		for(int neuron = 0; neuron < n1.outputWeights.length; neuron++) {
			for(int weight = 0; weight < n1.outputWeights[0].length; weight++) {
				if(Math.random() < 0.5) {
					if(Math.random() < mutationChance) {
						cn.outputWeights[neuron][weight] = (float) (n1.outputWeights[neuron][weight] + (Math.random() - .5) * 2);
					} else {
						cn.outputWeights[neuron][weight] = n1.outputWeights[neuron][weight];
					}
				} else {
					if(Math.random() < mutationChance) {
						cn.outputWeights[neuron][weight] = (float) (n2.outputWeights[neuron][weight] + (Math.random() - .5) * 2);
					} else {
						cn.outputWeights[neuron][weight] = n2.outputWeights[neuron][weight];
					}
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
