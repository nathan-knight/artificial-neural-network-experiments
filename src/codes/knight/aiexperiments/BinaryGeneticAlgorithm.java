package codes.knight.aiexperiments;

import codes.knight.aiexperiments.gamecore.Agent;

import java.util.*;

public class BinaryGeneticAlgorithm {

	public static class Breeder<T extends Agent> {
		private List<T> m_agents;
		private Random m_random;
		private float m_mutationRate = 0.0001f;
		private int m_keepAliveCount = 5;
		private float m_purgePercent = 0.1f;

		public Breeder(List<T> agents) {
			setAgents(agents);
			m_random = new Random();
		}

		public Breeder<T> setAgents(List<T> agents) {
			m_agents = agents;
			m_agents.sort(Comparator.comparing(Agent::getFitness).reversed());
			return this;
		}

		public Breeder<T> setMutationRate(float rate) {
			m_mutationRate = rate;
			return this;
		}

		public Breeder<T> setKeepAliveCount(int count) {
			m_keepAliveCount = count;
			return this;
		}

		public Breeder<T> setPurgePercent(float percent) {
			m_purgePercent = percent;
			return this;
		}

		private String simplePointCrossOver(String a, String b) {
			int point = m_random.nextInt(a.length());

			StringBuilder binaryChildBuilder = new StringBuilder();

			for (int i = 0; i < point; i++) {
				char nextBit;
				nextBit = a.charAt(i);

				// Flip (mutate) bits randomly at a preset rate
				if (m_random.nextFloat() < m_mutationRate) {
					nextBit = nextBit == '0' ? '1' : '0';
				}

				binaryChildBuilder.append(nextBit);
			}

			for (int i = point; i < b.length(); i++) {
				char nextBit;
				nextBit = b.charAt(i);

				// Flip (mutate) bits randomly at a preset rate
				if (m_random.nextFloat() < m_mutationRate) {
					nextBit = nextBit == '0' ? '1' : '0';
				}

				binaryChildBuilder.append(nextBit);
			}

			return binaryChildBuilder.toString();
		}


		private String uniformCrossOver(String a, String b) {
			StringBuilder binaryChildBuilder = new StringBuilder();

			for (int i = 0; i < a.length(); i++) {
				char nextBit;
				if(m_random.nextBoolean()) {
					nextBit = a.charAt(i);
				} else {
					nextBit = b.charAt(i);
				}

				// Flip (mutate) bits randomly at a preset rate
				if (m_random.nextFloat() < m_mutationRate) {
					nextBit = nextBit == '0' ? '1' : '0';
				}

				binaryChildBuilder.append(nextBit);
			}
			return binaryChildBuilder.toString();
		}

		private Network crossOver(T a, T b) {
			String binaryA = a.getNetwork().toBinary();
			String binaryB = b.getNetwork().toBinary();

			assert binaryA.length() == binaryB.length();

			String childBinary = simplePointCrossOver(binaryA, binaryB);

			return Network.fromString(childBinary, a.getNetwork());
		}

		public List<Network> breed() {
			List<Network> newNetworks = new ArrayList<>();
			List<T> breedableAgents = new ArrayList<>();

			int removeAmount = (int) (m_agents.size() * m_purgePercent);
			for (int i = removeAmount; i < m_agents.size(); i++) {
				breedableAgents.add(m_agents.get(i));
			}

			for (int i = 0; i < removeAmount; i++) {
				newNetworks.add(new Network(m_agents.get(0).getNetwork(), false).randomize());
			}

			for (int i = 0; i < m_keepAliveCount; i++) {
				newNetworks.add(new Network(m_agents.get(i).getNetwork()));
			}

			while (newNetworks.size() < m_agents.size()) {
				newNetworks.add(crossOver(getWeightedRandomAgent(breedableAgents),
						getWeightedRandomAgent(breedableAgents)));
			}

			return newNetworks;
		}


		public float getFitnessSum() {
			return getFitnessSum(m_agents);
		}

		private float getFitnessSum(List<T> agents) {
			float fitnessSum = 0;
			for(T agent : agents) {
				fitnessSum += agent.getFitness();
			}
			return fitnessSum;
		}

		public float getPeakFitness() {
			return m_agents.get(0).getFitness();
		}

		public T getPeakAgent() {
			return m_agents.get(0);
		}

		private T getWeightedRandomAgent(List<T> agents) {
			float weightSum = getFitnessSum(agents);
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

	public static <T extends Agent> List<Network> breed(List<T> agents) {
		return new Breeder<>(agents).breed();
	}
}
