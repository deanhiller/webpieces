package webpiecesxxxxxpackage.basesvr;

import org.apache.commons.lang3.RandomStringUtils;

public class RandomInstanceId {

	public static String generate() {
		String instanceId = RandomStringUtils.randomAlphanumeric(6);
		String s1 = instanceId.substring(0, 3);
		String s2 = instanceId.substring(3, 6);
		return s1+"-"+s2; //human readable instance id
	}
}
