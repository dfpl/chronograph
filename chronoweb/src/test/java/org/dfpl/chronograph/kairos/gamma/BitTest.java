package org.dfpl.chronograph.kairos.gamma;

import java.nio.ByteBuffer;

import org.junit.*;

public class BitTest {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() {
	}

	public void baseTest() {
		int b = 0b11111111111111111111111111111111;
		System.out.println(b);
	}
	
	@Test
	public void longTest() {
		byte[] bytes = new byte[16];
		for(int i = 0 ; i < bytes.length ; i++) {
			bytes[i] = Byte.MAX_VALUE;
		}
		long ttt =  ByteBuffer.wrap(bytes).getLong();
		System.out.println(ttt);
	}
}
