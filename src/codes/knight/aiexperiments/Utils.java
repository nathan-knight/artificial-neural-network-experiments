package codes.knight.aiexperiments;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

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

	public static void saveNetworkToDisk(Network network, String filename) {
		Path file = Paths.get("samples/" + filename);
		try {
			Files.write(file, Collections.singletonList(network.toBinary()), Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
