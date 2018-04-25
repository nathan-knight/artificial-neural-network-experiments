package codes.knight.aiexperiments;

import java.math.BigInteger;

public class Utils {

	public static String floatToString(float f) {
		StringBuilder currentString = new StringBuilder(Integer.toBinaryString(Float.floatToIntBits(f)));
		// Pad the binary representation to ensure uniform 32 character length
		while (currentString.length() < 32) currentString.insert(0, "0");
		return currentString.toString();
	}

	public static float stringToFloat(String s) {
		return Float.intBitsToFloat(new BigInteger(s, 2).intValue());
	}
}
