/*
 * Copyright 2016 Gordon D. Thompson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gordthompson.util.cleanupeclipselaunchconfigurations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Remove obsolete "Launch configuration" entries in Eclipse that clog up the
 * list when doing an Export to a Runnable JAR file.
 * 
 * ref: http://stackoverflow.com/a/21687507/2144390
 * 
 * @version 1.0.2
 * @author Gord Thompson
 *
 */

public class CleanupEclipseLaunchConfigurationsMain {

	public static void main(String[] args) {
		// edit the following to suit (or hack the code to use "args"
		// so you can run it from the command line for multiple locations)
		String workspaceRoot = "C:/Users/Gord/workspace/"; // note trailing slash

		String launchesPath = workspaceRoot + ".metadata/.plugins/org.eclipse.debug.core/.launches/";

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}

		int fileCount = 0, deleteCount = 0;
		for (File launchFile : new File(launchesPath).listFiles()) {
			if (launchFile.getName().endsWith(".launch")) {
				fileCount++;
				Document doc = null;
				try {
					doc = dBuilder.parse(launchFile);
				} catch (SAXException | IOException e) {
					e.printStackTrace(System.err);
					System.exit(2);
				}
				doc.getDocumentElement().normalize();
				
				String projectName = "";
				NodeList saList  = doc.getElementsByTagName("stringAttribute");
				for (int i = 0; i < saList.getLength(); i++) {
					Element saElement = (Element) saList.item(i);
					if (saElement.getAttribute("key").equals("org.eclipse.jdt.launching.PROJECT_ATTR")) {
						projectName = saElement.getAttribute("value");
						break;
					}
				}
				String locationFilePath = workspaceRoot + ".metadata/.plugins/org.eclipse.core.resources/.projects/" + projectName + "/.location";
				String projectRoot = "";
				if (new File(locationFilePath).exists()) {
					try {
						projectRoot = extractProjectRoot(locationFilePath);
					} catch (IOException e) {
						e.printStackTrace(System.err);
						System.exit(3);
					}
				} else {
					projectRoot = workspaceRoot + projectName + "/";
				}
				
				NodeList laList = doc.getElementsByTagName("listAttribute");
				for (int i = 0; i < laList.getLength(); i++) {
					Element laElement = (Element) laList.item(i);
					if (laElement.getAttribute("key").equals("org.eclipse.debug.core.MAPPED_RESOURCE_PATHS")) {
						NodeList leList = laElement.getElementsByTagName("listEntry");
						for (int j = 0; j < leList.getLength(); j++) {
							Element leElement = (Element) leList.item(j);
							String javaSourcePath = leElement.getAttribute("value");
							if (javaSourcePath.endsWith(".java")) {
								// trim "/projectname/" from beginning of javaSourcePath
								File javaFile = new File(projectRoot + javaSourcePath.substring(projectName.length() + 2));
								if (!javaFile.exists()) {
									System.out.printf("Deleting \"%s\"%n", launchFile.getName());
									launchFile.delete();
									deleteCount++;
									break;
								}
							}
						}
					}
				}
			}
		}
		System.out.printf("%d .launch file(s) processed, %d deleted.%n", fileCount, deleteCount);
		System.out.println("Remember to restart Eclipse if it is currently open.");
	}
	
	private static String extractProjectRoot(String locationFileSpec) throws IOException {
		byte[] locationBytes = Files.readAllBytes(new File(locationFileSpec).toPath());
		String locationString = new String(locationBytes, "ISO-8859-1");
		String marker = "URI//file:";
		int startOffset = locationString.indexOf(marker) + marker.length();
		int endOffset = locationString.indexOf(0, startOffset);
		return new String(Arrays.copyOfRange(locationBytes, startOffset, endOffset), "UTF-8") + "/";
	}

}
