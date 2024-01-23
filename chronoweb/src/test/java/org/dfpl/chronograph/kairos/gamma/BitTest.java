package org.dfpl.chronograph.kairos.gamma;

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

	@Test
	public void baseTest() {
		int b = 0b11111111111111111111111111111111;
		System.out.println(b);
	}
}
