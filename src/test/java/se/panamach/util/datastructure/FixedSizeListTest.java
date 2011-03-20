package se.panamach.util.datastructure;

import org.junit.Assert;
import org.junit.Test;

public class FixedSizeListTest {

	@Test
	public void testListDoesNotExceedLimit() {
		FixedSizeList<Object> list = new FixedSizeList<Object>(5);
		for (int i = 0; i < 10; i++) {
			list.add(new Object());
		}
		
		Assert.assertEquals(5, list.size());
	}
	
	@Test
	public void testListKeepsYoungestElement() {
		FixedSizeList<Integer> list = new FixedSizeList<Integer>(5);
		for (int i = 0; i < 10; i++) {
			list.add(i);
		}
		
		int startVal = 5;
		for (Integer val : list) {
			Assert.assertEquals(startVal, val.intValue());
			startVal++;
		}
	}
	
	@Test
	public void testGetLastElement() {
		FixedSizeList<Integer> list = new FixedSizeList<Integer>(5);
		for (int i = 0; i < 10; i++) {
			list.add(i);
		}
		Assert.assertEquals(9, list.getLastElement().intValue());
	}
	
	@Test
	public void testGetLastElementOnEmptyList() {
		FixedSizeList<Integer> list = new FixedSizeList<Integer>(5);
		
		Assert.assertNull(list.getLastElement());
	}
}
