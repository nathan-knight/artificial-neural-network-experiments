package codes.knight.aiexperiments;

import codes.knight.aiexperiments.gamecore.Agent;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {

	public static String floatToString(float f) {
		StringBuilder currentString = new StringBuilder(Integer.toBinaryString(Float.floatToIntBits(f)));
		// Pad the binary representation to ensure uniform 32 character length
		while (currentString.length() < 32) currentString.insert(0, "0");
		return currentString.toString();
	}

	public static float stringToFloat(String s) {
		assert s.length() == 32;
		return Float.intBitsToFloat(new BigInteger(s, 2).intValue());
	}

	private static void writeLinesToFile(List<String> lines, String filename) {
		Path file = Paths.get(filename);
		try {
			Files.write(file, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveNetworkToDisk(Network network, String filename) {
		writeLinesToFile(Collections.singletonList(network.toBinary()), "samples/" + filename);
	}

	public static <T extends Agent> void saveAgentsToDisk(int generation, List<T> agents, String filename) {
		List<String> lines = new ArrayList<>();
		lines.add(String.valueOf(generation));
		for (Agent a : agents) {
			lines.add(a.getNetwork().toBinary());
		}
		writeLinesToFile(lines, filename);
	}
}
