import codes.knight.aiexperiments.Network;
import codes.knight.aiexperiments.utils.Utils;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class UtilTests {

	@Test
	public void testPositiveFloatConversion() {
		float toConvert = 10.1f;
		assertEquals(toConvert, Utils.stringToFloat(Utils.floatToString(toConvert)));
	}

	@Test
	public void testNegativeFloatConversion() {
		float toConvert = -10.1999f;
		assertEquals(toConvert, Utils.stringToFloat(Utils.floatToString(toConvert)));
	}

	@Test
	public void testZeroFloatConversion() {
		assertEquals(0f, Utils.stringToFloat(Utils.floatToString(0)));
	}

	@Test
	public void testRandomNetworkEncoding() {
		Network network = new Network(3, 3, 3, 3);
		network.randomize(-1, 1);

		assertTrue(network.equals(Network.fromString(network.toBinary(), 3, 3, 3, 3)));
	}

	@Test
	public void testNetworkCopy() {
		Network network = new Network(3, 3, 3, 3);
		network.randomize(-1, 1);

		assertTrue(network.equals(new Network(network)));
	}
}
