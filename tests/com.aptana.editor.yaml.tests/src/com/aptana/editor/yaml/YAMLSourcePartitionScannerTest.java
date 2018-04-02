/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.yaml;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.junit.After;
import org.junit.Test;

import com.aptana.editor.common.ExtendedFastPartitioner;
import com.aptana.editor.common.IExtendedPartitioner;
import com.aptana.editor.common.NullPartitionerSwitchStrategy;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.common.text.rules.NullSubPartitionScanner;

/**
 * @author Chris
 * @author Sandip
 */
public class YAMLSourcePartitionScannerTest
{

	private IDocumentPartitioner partitioner;

	private void assertContentType(String contentType, String code, int offset)
	{
		assertEquals("Content type doesn't match expectations for: " + code.charAt(offset), contentType,
				getContentType(code, offset));
	}

	@After
	public void tearDown() throws Exception
	{
		partitioner = null;
	}

	private String getContentType(String content, int offset)
	{
		if (partitioner == null)
		{
			IDocument document = new Document(content);
			CompositePartitionScanner partitionScanner = new CompositePartitionScanner(YAMLSourceConfiguration
					.getDefault().createSubPartitionScanner(), new NullSubPartitionScanner(),
					new NullPartitionerSwitchStrategy());
			partitioner = new ExtendedFastPartitioner(partitionScanner, YAMLSourceConfiguration.getDefault()
					.getContentTypes());
			partitionScanner.setPartitioner((IExtendedPartitioner) partitioner);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return partitioner.getContentType(offset);
	}

	@Test
	public void testPartitioningOfDefaultPartition()
	{
		String source = "adapter: sqlite3\n";
		for (int i = 0; i < source.length(); i++)
		{
			assertContentType(YAMLSourceConfiguration.DEFAULT, source, i);
		}
	}

	@Test
	public void testPartitioningOfDirective()
	{
		String source = "%YAML 1.2";
		for (int i = 0; i < source.length(); i++)
		{
			assertContentType(YAMLSourceConfiguration.DIRECTIVE, source, i);
		}
	}

	// FIXME This doesn't work right now, the comment is eaten by the directive rule!
//	public void testDirectiveWithTrailingComment()
//	{
//		String source = "%YAML 1.2 # comment";
//
//		assertContentType(YAMLSourceConfiguration.DIRECTIVE, source, 0);
//		assertContentType(YAMLSourceConfiguration.DIRECTIVE, source, 9);
//		assertContentType(YAMLSourceConfiguration.COMMENT, source, 10);
//		assertContentType(YAMLSourceConfiguration.COMMENT, source, 18);
//	}

	@Test
	public void testPartitioningOfComment()
	{
		String source = "# SQLite version 3.x\n";
		for (int i = 0; i < source.length(); i++)
		{
			assertContentType(YAMLSourceConfiguration.COMMENT, source, i);
		}
	}

	@Test
	public void testPartitioningOfSingleQuotedString()
	{
		String source =
		// 1 2 3 4 5
		// 01234567890123456789012345678901234567 8901234567890
		"' This is a single quoted CSS string'\n";
		for (int i = 0; i <= 36; i++)
		{
			assertContentType(YAMLSourceConfiguration.STRING_SINGLE, source, i);
		}
		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 37);
	}

	@Test
	public void testPartitioningOfEmptySingleQuotedString()
	{
		String source =
		// 1 2 3 4 5
		// 01234567890123456789012345678901234567 8901234567890
		"''\n";
		for (int i = 0; i <= 1; i++)
		{
			assertContentType(YAMLSourceConfiguration.STRING_SINGLE, source, i);
		}
		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 2);
	}

	@Test
	public void testPartitioningOfSingleQuotedStringWithEscape()
	{
		String source =
		// 1 2 3 4 5
		// 01234567890123456789012345678901234567 89012345678 9012
		"' This is a single quoted CSS string with escape \\' '\n";

		for (int i = 0; i <= 52; i++)
		{
			assertContentType(YAMLSourceConfiguration.STRING_SINGLE, source, i);
		}
		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 53);
	}

	@Test
	public void testPartitioningOfSingleQuotedStringWithDoubleQuote()
	{
		String source =
		// 1 2 3 4 5
		// 01234567890123456789012345678901234567 89012345678 9012
		"' This is a single quoted CSS string with double quote \" '\n";

		for (int i = 0; i <= 57; i++)
		{
			assertContentType(YAMLSourceConfiguration.STRING_SINGLE, source, i);
		}
		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 58);
	}

	@Test
	public void testPartitioningOfDoubleQuotedString()
	{
		String source =
		// 1 2 3 4 5
		// 0 123456789012345678901234567890123456 7 8901234567890
		"\" This is a double quoted CSS string\"\n";
		for (int i = 0; i <= 36; i++)
		{
			assertContentType(YAMLSourceConfiguration.STRING_DOUBLE, source, i);
		}
		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 37);
	}

	@Test
	public void testPartitioningOfEmptyDoubleQuotedString()
	{
		String source =
		// 1 2 3 4 5
		// 01234567890123456789012345678901234567 8901234567890
		"\"\"\n";
		for (int i = 0; i <= 1; i++)
		{
			assertContentType(YAMLSourceConfiguration.STRING_DOUBLE, source, i);
		}
		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 2);
	}

	@Test
	public void testPartitioningOfDoubleQuotedStringWithEscape()
	{
		String source =
		// 1 2 3 4 5
		// 0 1234567890123456789012345678901234567 89012345678 9 012
		"\" This is a double quoted CSS string with escape \\\" \"\n";

		for (int i = 0; i <= 52; i++)
		{
			assertContentType(YAMLSourceConfiguration.STRING_DOUBLE, source, i);
		}
		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 53);
	}

	@Test
	public void testPartitioningOfDoubleQuotedStringWithSingleQuote()
	{
		String source =
		// 1 2 3 4 5
		// 0 1234567890123456789012345678901234567 89012345678 9 012
		"\" This is a double quoted CSS string with single quote ' \"\n";

		for (int i = 0; i <= 57; i++)
		{
			assertContentType(YAMLSourceConfiguration.STRING_DOUBLE, source, i);
		}
		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 58);
	}

	@Test
	public void testPartitioningOfAllPartitions()
	{
		String source = " # comment\n ' ' \" \" ` ` \n";

		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 0);
		assertContentType(YAMLSourceConfiguration.COMMENT, source, 1);
		assertContentType(YAMLSourceConfiguration.COMMENT, source, 9);
		assertContentType(YAMLSourceConfiguration.COMMENT, source, 10);
		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 11);
		assertContentType(YAMLSourceConfiguration.STRING_SINGLE, source, 12);
		assertContentType(YAMLSourceConfiguration.STRING_SINGLE, source, 14);
		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 15);
		assertContentType(YAMLSourceConfiguration.STRING_DOUBLE, source, 16);
		assertContentType(YAMLSourceConfiguration.STRING_DOUBLE, source, 18);
		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 19);
		assertContentType(YAMLSourceConfiguration.INTERPOLATED, source, 20);
		assertContentType(YAMLSourceConfiguration.INTERPOLATED, source, 22);
		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 23);
		assertContentType(YAMLSourceConfiguration.DEFAULT, source, 24);
	}

}
