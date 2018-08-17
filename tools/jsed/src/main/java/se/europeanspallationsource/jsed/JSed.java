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


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import org.unix4j.Unix4j;


/**
 * Java implementation of the unix {@code sed} tool.
 *
 * @author claudio.rosati@esss.se
 */
@SuppressWarnings({ "ClassWithoutLogger", "UseOfSystemOutOrSystemErr" })
public class JSed {

	public static void main( String[] args ) {

		if ( args.length == 0 ) {
			printUsage();
			return;
		} 

		List<String> argsList = Arrays.asList(args);

		if ( argsList.contains("-h") || argsList.contains("--help") ) {
			printUsage();
			return;
		}

		if ( argsList.size() != 2 ) {
			printError(MessageFormat.format( "ERROR: Invalid number of parameters: {0} instead of 2.", argsList.size()));
			return;
		}

		File file = new File(argsList.get(1));

		if ( !file.exists() ) {
			printError(MessageFormat.format("ERROR: File doesn't exist [{0}].", argsList.get(1)));
			return;
		} else if ( !file.isFile() ) {
			printError(MessageFormat.format("ERROR: File not regular [{0}].", argsList.get(1)));
			return;
		}

		try {

			File result = File.createTempFile("jsed", ".tmp");
			String pattern = argsList.get(0);

			Unix4j
				.fromFile(file)
				.sed(pattern)
				.toFile(result);

			Files.delete(file.toPath());
			Files.move(result.toPath(), file.toPath());

		} catch ( IOException ex ) {
			ex.printStackTrace(System.err);
		}

	}

	private static void printError ( String message ) {
		System.out.println(message);
		System.out.println();
		printUsage();
	}

	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println("  java -jar jsed-1.0.0.jar [-h] <expression> <file>");
		System.out.println("    -h/--help    Prints this message and exit the program.");
		System.out.println("    <expression> A sed expression, such as \"s/original/replacement/g\".");
		System.out.println("                 Standard Java RegEx expression matching is used.");
		System.out.println("    <file>       The input/output file containing the text to be substituted, ");
		System.out.println("                 and that will be replaced by the substitution.");
	}

}
