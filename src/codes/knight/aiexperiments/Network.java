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
	
	public float[] run(float input[]) {
		System.arraycopy(input, 0, inputLayer, 0, inputLayer.length);
		//Feed to first layer from input
		for(int i = 0; i < hiddenLayers[0].length; i++) {
			for(int o = 0; o < inputLayer.length; o++) {
				hiddenLayers[0][i] += firstWeights[i][o] * inputLayer[o];
			}
			hiddenLayers[0][i] = calcSigmoid(hiddenLayers[0][i]);
		}
		//Feed to hidden layers
		for(int i = 1; i < hiddenLayers.length; i++) {
			for(int o = 0; o < hiddenLayers[i].length; o++) {//o was i
				for(int j = 0; j < weights[i][o].length; j++) {
					hiddenLayers[i][o] += weights[i][o][j] * hiddenLayers[i-1][j];
				}
				hiddenLayers[i][o] = calcSigmoid(hiddenLayers[i][o]);
			}
		}
		//Feed to output
		for(int i = 0; i < outputLayer.length; i++) {
			int lastLayer = hiddenLayers.length-1;
			for(int j = 0; j < hiddenLayers[hiddenLayers.length-1].length; j++) {
				outputLayer[i] += hiddenLayers[lastLayer][j] * outputWeights[i][j];
			}
			outputLayer[i] = calcSigmoid(outputLayer[i]);
		}
		return outputLayer;
	}
	
	public void randomize(float min, float max) {
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

	public boolean equals(Network other) {
		return Arrays.deepEquals(firstWeights, other.firstWeights) &&
				Arrays.deepEquals(weights, other.weights) &&
				Arrays.deepEquals(outputWeights, other.outputWeights);
	}
}
