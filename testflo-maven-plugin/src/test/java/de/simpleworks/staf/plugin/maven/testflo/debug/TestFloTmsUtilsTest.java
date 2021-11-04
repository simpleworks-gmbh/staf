package de.simpleworks.staf.plugin.maven.testflo.debug;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.simpleworks.staf.plugin.maven.testflo.commons.TestFloTmsUtils;

public class TestFloTmsUtilsTest {
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	@Test
	public void testGetComment_issueIdIsNull() {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("issueId can't be null.");
		TestFloTmsUtils.getComment(null, null, null);
	}

	@Test
	public void testGetComment_rowIndexIsNull() {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("rowIndex can't be null.");
		TestFloTmsUtils.getComment(Long.valueOf(1), null, null);
	}

	@Test
	public void testGetComment_commentIsNull() {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("comment can't be null.");
		TestFloTmsUtils.getComment(Long.valueOf(1), Integer.valueOf(1), null);
	}

	@SuppressWarnings("static-method")
	@Test
	public void testGetComment_emptyComment() {
		final String text = "";
		final String expected = "{\"issueId\":1,\"rowIndex\":1,\"comment\":\"\"}";
		Assert.assertEquals("unexpected comment.", expected,
				TestFloTmsUtils.getComment(Long.valueOf(1), Integer.valueOf(1), text));
	}

	@SuppressWarnings("static-method")
	@Test
	public void testGetComment() {
		final String text = "text text";
		final String expected = "{\"issueId\":1,\"rowIndex\":1,\"comment\":\"text text\"}";
		Assert.assertEquals("unexpected comment.", expected,
				TestFloTmsUtils.getComment(Long.valueOf(1), Integer.valueOf(1), text));
	}

	@SuppressWarnings("static-method")
	@Test
	public void testGetComment_JSON() {
		final String text = "{\"issueId\":\"l\",\"rowIndex\":1,\"comment\":\"\"}";
		final String expected = "{\"issueId\":1,\"rowIndex\":1,\"comment\":\"{\\\"issueId\\\":\\\"l\\\",\\\"rowIndex\\\":1,\\\"comment\\\":\\\"\\\"}\"}";
		Assert.assertEquals("unexpected comment.", expected,
				TestFloTmsUtils.getComment(Long.valueOf(1), Integer.valueOf(1), text));
	}

	// XXXX
	@Test
	public void testGetTestStepStatus_issueIdIsNull() {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("issueId can't be null.");
		TestFloTmsUtils.getTestStepStatus(null, null, null);
	}

	@Test
	public void testGetTestStepStatus_rowIndexIsNull() {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("rowIndex can't be null.");
		TestFloTmsUtils.getTestStepStatus(Long.valueOf(1), null, null);
	}

	@Test
	public void testGetTestStepStatus_statusIsNull() {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("status can't be null.");
		TestFloTmsUtils.getTestStepStatus(Long.valueOf(1), Integer.valueOf(1), null);
	}

	@SuppressWarnings("static-method")
	@Test
	public void testGetTestStepStatus_emptyStatus() {
		final String text = "";
		final String expected = "{\"issueId\":1,\"rowIndex\":1,\"status\":\"\"}";
		Assert.assertEquals("unexpected comment.", expected,
				TestFloTmsUtils.getTestStepStatus(Long.valueOf(1), Integer.valueOf(1), text));
	}

	@SuppressWarnings("static-method")
	@Test
	public void testGetTestStepStatus() {
		final String text = "text text";
		final String expected = "{\"issueId\":1,\"rowIndex\":1,\"status\":\"text text\"}";
		Assert.assertEquals("unexpected status.", expected,
				TestFloTmsUtils.getTestStepStatus(Long.valueOf(1), Integer.valueOf(1), text));
	}

	// XXXX
	@Test
	public void testGetTestStepCell_issueIdIsNull() {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("issueId can't be null.");
		TestFloTmsUtils.getTestStepCell(null, null, null, null);
	}

	@Test
	public void testGetTestStepCell_rowIndexIsNull() {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("rowIndex can't be null.");
		TestFloTmsUtils.getTestStepCell(Long.valueOf(1), null, null, null);
	}

	@Test
	public void testGetTestStepCell_columnIndexIsNull() {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("columnIndex can't be null.");
		TestFloTmsUtils.getTestStepCell(Long.valueOf(1), Integer.valueOf(1), null, null);
	}

	@Test
	public void testGetTestStepCell_statusIsNull() {
		exceptionRule.expect(IllegalArgumentException.class);
		exceptionRule.expectMessage("cellValue can't be null.");
		TestFloTmsUtils.getTestStepCell(Long.valueOf(1), Integer.valueOf(1), Integer.valueOf(1), null);
	}

	@SuppressWarnings("static-method")
	@Test
	public void testGetTestStepCell_emptyStatus() {
		final String text = "";
		final String expected = "{\"issueId\":1,\"rowIndex\":1,\"columnIndex\":1,\"cellValue\":\"\"}";
		Assert.assertEquals("unexpected comment.", expected,
				TestFloTmsUtils.getTestStepCell(Long.valueOf(1), Integer.valueOf(1), Integer.valueOf(1), text));
	}

	@SuppressWarnings("static-method")
	@Test
	public void testGetTestStepCell() {
		final String text = "text text";
		final String expected = "{\"issueId\":1,\"rowIndex\":1,\"columnIndex\":1,\"cellValue\":\"text text\"}";
		Assert.assertEquals("unexpected status.", expected,
				TestFloTmsUtils.getTestStepCell(Long.valueOf(1), Integer.valueOf(1), Integer.valueOf(1), text));
	}

	@SuppressWarnings("static-method")
	@Test
	public void testGetTestStepCell_JSON() {
		final String text = "{\"issueId\":1,\"rowIndex\":1,\"status\":\"text text\"}";
		final String expected = "{\"issueId\":1,\"rowIndex\":1,\"columnIndex\":1,\"cellValue\":\"{\\\"issueId\\\":1,\\\"rowIndex\\\":1,\\\"status\\\":\\\"text text\\\"}\"}";
		Assert.assertEquals("unexpected status.", expected,
				TestFloTmsUtils.getTestStepCell(Long.valueOf(1), Integer.valueOf(1), Integer.valueOf(1), text));
	}
}
