package codes.knight.aiexperiments;

import java.util.Arrays;
import java.util.Random;

public class Network {

	private double curveMod = 1;

	private int inputNeuronCount;
	private int hiddenLayerCount;
	private int m_neuronsPerLayer;
	private int outputNeuronCount;
	
	float hiddenLayers[][];
	float inputLayer[];
	float outputLayer[];

	float firstWeights[][];
	float weights[][][];
	float outputWeights[][];
	
	private Random random = new Random();
	
	public Network(int inputNeurons, int hiddenLayers, int neuronsPerLayer, int outputNeurons) {
		inputNeuronCount = inputNeurons;
		hiddenLayerCount = hiddenLayers;
		m_neuronsPerLayer = neuronsPerLayer;
		outputNeuronCount = outputNeurons;

		inputLayer = new float[inputNeurons];
		firstWeights = new float[neuronsPerLayer][inputNeurons];
		outputLayer = new float[outputNeurons];
		outputWeights = new float[outputNeurons][neuronsPerLayer];

		weights = new float[hiddenLayers][neuronsPerLayer][neuronsPerLayer];
		this.hiddenLayers = new float[hiddenLayers][neuronsPerLayer];
	}

	public Network(Network other) {
		this(other, true);
	}

	public Network(Network other, boolean copyWeights) {
		inputNeuronCount = other.inputNeuronCount;
		hiddenLayerCount = other.hiddenLayerCount;
		m_neuronsPerLayer = other.m_neuronsPerLayer;
		outputNeuronCount = other.outputNeuronCount;

		inputLayer = new float[inputNeuronCount];
		firstWeights = new float[m_neuronsPerLayer][inputNeuronCount];
		outputLayer = new float[outputNeuronCount];
		outputWeights = new float[outputNeuronCount][m_neuronsPerLayer];

		weights = new float[hiddenLayerCount][m_neuronsPerLayer][m_neuronsPerLayer];
		this.hiddenLayers = new float[hiddenLayerCount][m_neuronsPerLayer];

		if (!copyWeights) {
			return;
		}

		for (int node = 0; node < m_neuronsPerLayer; node++) {
			firstWeights[node] = Arrays.copyOf(other.firstWeights[node], inputNeuronCount);
		}

		for (int layer = 0; layer < hiddenLayerCount; layer++) {
			for (int node = 0; node < m_neuronsPerLayer; node++) {
				weights[layer][node] = Arrays.copyOf(other.weights[layer][node], m_neuronsPerLayer);
			}
		}

		for (int node = 0; node < outputNeuronCount; node++) {
			outputWeights[node] = Arrays.copyOf(other.outputWeights[node], m_neuronsPerLayer);
		}
	}
	
	public float[] run(float input[]) {
		// Insert input into the input layer
		System.arraycopy(input, 0, inputLayer, 0, inputLayer.length);

		//Feed to first layer from input
		for(int neuron = 0; neuron < hiddenLayers[0].length; neuron++) {
			for(int inputNeuron = 0; inputNeuron < inputLayer.length; inputNeuron++) {
				hiddenLayers[0][neuron] += firstWeights[neuron][inputNeuron] * inputLayer[inputNeuron];
			}
			hiddenLayers[0][neuron] = calcSigmoid(hiddenLayers[0][neuron]);
		}

		//Feed to hidden layers
		for(int layer = 1; layer < hiddenLayers.length; layer++) {
			for(int neuron = 0; neuron < hiddenLayers[layer].length; neuron++) {
				for(int lastNeuron = 0; lastNeuron < weights[layer][neuron].length; lastNeuron++) {
					hiddenLayers[layer][neuron] += weights[layer][neuron][lastNeuron] * hiddenLayers[layer-1][lastNeuron];
				}
				hiddenLayers[layer][neuron] = calcSigmoid(hiddenLayers[layer][neuron]);
			}
		}
		//Feed to output
		for(int neuron = 0; neuron < outputLayer.length; neuron++) {
			int lastLayer = hiddenLayers.length-1;
			for(int lastNeuron = 0; lastNeuron < hiddenLayers[lastLayer].length; lastNeuron++) {
				outputLayer[neuron] += outputWeights[neuron][lastNeuron] * hiddenLayers[lastLayer][lastNeuron];
			}
			outputLayer[neuron] = calcSigmoid(outputLayer[neuron]);
		}
		return outputLayer;
	}

	public Network randomize() {
		return randomize(-1, 1);
	}
	
	public Network randomize(float min, float max) {
		for(float f[][] : weights) {
			for(float ff[] : f) {
				for(int i = 0; i < ff.length; i++) {
					ff[i] = (random.nextFloat() - 1f) * 2f;
				}
			}
		}
		for(float ff[] : firstWeights) {
			for(int i = 0; i < ff.length; i++) {
				ff[i] = (random.nextFloat() - 1f) * 2f;
			}
		}
		for(float ff[] : outputWeights) {
			for(int i = 0; i < ff.length; i++) {
				ff[i] = (random.nextFloat() - 1f) * 2f;
			}
		}

		return this;
	}
	
	private float calcSigmoid(double activation) {
		return (float) (1.0f / (1.0f + Math.pow(Math.E, (activation * -1) / curveMod)));
	}

	public String toBinary() {
		StringBuilder stringBuilder = new StringBuilder();

		for (float node[] : firstWeights) {
			for(float weight : node) {
				stringBuilder.append(Utils.floatToString(weight));
			}
		}

		for (float layer[][] : weights) {
			for (float node[] : layer) {
				for(float weight : node) {
					stringBuilder.append(Utils.floatToString(weight));
				}
			}
		}

		for (float node[] : outputWeights) {
			for(float weight : node) {
				stringBuilder.append(Utils.floatToString(weight));
			}
		}

		return stringBuilder.toString();
	}

	public Network setWeightsFromString(String string) {
		for(float ff[] : firstWeights) {
			for(int i = 0; i < ff.length; i++) {
				ff[i] = Utils.stringToFloat(string.substring(0, 32));
				string = string.substring(32);
			}
		}

		for(float f[][] : weights) {
			for(float ff[] : f) {
				for(int i = 0; i < ff.length; i++) {
					ff[i] = Utils.stringToFloat(string.substring(0, 32));
					string = string.substring(32);
				}
			}
		}

		for(float ff[] : outputWeights) {
			for(int i = 0; i < ff.length; i++) {
				ff[i] = Utils.stringToFloat(string.substring(0, 32));
				string = string.substring(32);
			}
		}

		return this;
	}

	public static Network fromString(String string, int inputNeurons, int hiddenLayers, int neuronsPerLayer, int outputNeurons) {
		return new Network(inputNeurons, hiddenLayers, neuronsPerLayer, outputNeurons).setWeightsFromString(string);
	}

	public static Network fromString(String string, Network otherNetwork) {
		return new Network(otherNetwork.inputNeuronCount, otherNetwork.hiddenLayerCount,
				otherNetwork.m_neuronsPerLayer, otherNetwork.outputNeuronCount).setWeightsFromString(string);
	}

	public boolean equals(Network other) {
		return Arrays.deepEquals(firstWeights, other.firstWeights) &&
				Arrays.deepEquals(weights, other.weights) &&
				Arrays.deepEquals(outputWeights, other.outputWeights);
	}
}
