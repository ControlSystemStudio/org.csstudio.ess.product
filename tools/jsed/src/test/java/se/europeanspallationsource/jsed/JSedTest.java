/*
 * Copyright 2018 claudiorosati.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.europeanspallationsource.jsed;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author claudio.rosati@esss.se
 */
@SuppressWarnings({ "ClassWithoutLogger", "UseOfSystemOutOrSystemErr" })
public class JSedTest {

	private static final List<String> TEST_LINES = Arrays.asList(
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
		"<?pde version=\"3.5\"?>",
		"",
		"<product name=\"ESS CS-Studio\" uid=\"cs-studio-ess\" id=\"se.ess.ics.csstudio.product.product\" application=\"se.ess.ics.csstudio.product.application\" version=\"4.6.1.7\" useFeatures=\"true\" includeLaunchers=\"true\">",
		"",
		"   <aboutInfo>",
		"    <product.version>4.6.1.7</product.version>",
		"	<efx-site>http://download.eclipse.org/efxclipse/runtime-released/2.4.0/site</efx-site>"
	);
	private static Path testFile;

	@BeforeClass
	public static void setUpClass() throws IOException {

		testFile = Files.createTempFile("jsed", ".test");

		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("Writing the following test lines into: " + testFile.toString());
		System.out.println("--------------------------------------------------------------------------------");
		TEST_LINES.forEach(l -> System.out.println("  " + l));
		System.out.println("--------------------------------------------------------------------------------");

		Files.write(testFile, TEST_LINES);

	}

	@AfterClass
	public static void tearDownClass() throws IOException {
		Files.delete(testFile);
	}

	@Test
	public void testMain1() throws IOException {

		String sedCommand = "s/(<product[^>]* version=\")[^\"]*(\"[^>]*>)/$1AA.BB.CC.DD$2/g";

		JSed.main(new String[] { sedCommand, testFile.toString() });

		List<String> readLines = Files.readAllLines(testFile);

		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("Result after JSed execution");
		System.out.println("--------------------------------------------------------------------------------");
		readLines.forEach(l -> System.out.println("  " + l));
		System.out.println("--------------------------------------------------------------------------------");

		for ( int i = 0; i < readLines.size(); i++ ) {
			if ( i == 3 ) {
				assertThat(readLines.get(i)).contains("AA.BB.CC.DD");
			} else {
				assertThat(readLines.get(i)).doesNotContain("AA.BB.CC.DD");
			}
		}

	}

	@Test
	public void testMain2() throws IOException {

		String sedCommand = "s/(<product\\.version>)[^<]*(<[^>]*>)/$1FF.GG.55.ZZ$2/g";

		JSed.main(new String[] { sedCommand, testFile.toString() });

		List<String> readLines = Files.readAllLines(testFile);

		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("Result after JSed execution");
		System.out.println("--------------------------------------------------------------------------------");
		readLines.forEach(l -> System.out.println("  " + l));
		System.out.println("--------------------------------------------------------------------------------");

		for ( int i = 0; i < readLines.size(); i++ ) {
			if ( i == 6 ) {
				assertThat(readLines.get(i)).contains("FF.GG.55.ZZ");
			} else {
				assertThat(readLines.get(i)).doesNotContain("FF.GG.55.ZZ");
			}
		}

	}

}
