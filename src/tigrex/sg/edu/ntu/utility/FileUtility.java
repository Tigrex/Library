package tigrex.sg.edu.ntu.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class to read/write files
 * 
 * @author Tigrex
 *
 */
public class FileUtility {

	final private Logger logger = LoggerFactory.getLogger(FileUtility.class);

	/**
	 * Read a text file and return lines of split elements. This method does not
	 * check duplicated lines. Lines starting with "#" or "%" are assumed to be
	 * comments and ignored.
	 * 
	 * @param path
	 *            the path of the file
	 * @param delimiter
	 *            the delimiter used to split a line
	 * @param numOfElements
	 *            the number of elements in a line, the method will throw a
	 *            RuntimeException if a non-comment line does not have the specified
	 *            number of elements
	 * @return
	 */
	public List<List<String>> readFile(String path, String delimiter, int numOfElements) {

		logger.debug("+readFile({})", path);
		List<List<String>> lines = this.readFileHelper(path, delimiter, numOfElements, false);
		logger.debug("-readFile({})", path);

		return lines;
	}

	/**
	 * Read a CSV file and return lines of split elements. This method does not
	 * check duplicated lines. Lines starting with "#" or "%" are assumed to be
	 * comments and ignored.
	 * 
	 * @param path
	 * @param numOfElements
	 * @return
	 */
	public List<List<String>> readCSV(String path, int numOfElements) {
		logger.debug("+readCSV({})", path);
		List<List<String>> lines = this.readFileHelper(path, "", numOfElements, true);
		logger.debug("-readCSV({})", path);

		return lines;
	}

	private List<List<String>> readFileHelper(String path, String delimiter, int numOfElements, boolean isCSV) {

		List<List<String>> lines = new LinkedList<List<String>>();

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String currentLine;

			int numOfLines = 0;

			while ((currentLine = br.readLine()) != null) {

				numOfLines++;

				if (numOfLines % 1000000 == 0) {
					logger.debug("Reading line {}.", numOfLines);
				}

				if (currentLine.startsWith("#") || currentLine.startsWith("%")) {
					continue;
				}

				List<String> line;
				if (isCSV) {

					line = this.parseLine(currentLine);

					if (line.size() != numOfElements) {
						logger.error("Line {} has {} elements, expected {}.", numOfLines, line.size(), numOfElements);
						throw new RuntimeException("Line " + numOfLines + " has " + line.size() + " elements, expected " + numOfElements + ".");
					}

				} else {
					String[] parts = currentLine.split(delimiter);

					if (parts.length != numOfElements) {
						logger.error("Line {} has {} elements, expected {}.", numOfLines, parts.length, numOfElements);
						throw new RuntimeException("Line " + numOfLines + " has " + parts.length + " elements, expected " + numOfElements + ".");
					}

					line = new LinkedList<String>();
					for (String part : parts) {
						line.add(part);
					}

				}

				lines.add(line);

			}

			logger.debug("The total number of lines is {}, non-comment lines {}.", numOfLines, lines.size());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return lines;
	}

	/**
	 * Parse a line in a CSV file. Non-delimiter commas can be enclosed by double quotes. 
	 * 
	 * @param line
	 * @return
	 */
	private List<String> parseLine(String line) {
		// Remove double quotes
		line = line.replaceAll("\"\"", "");

		List<String> parts = new ArrayList<String>();

		StringBuilder builder = new StringBuilder();
		boolean startQuoted = false;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);

			if (c == '"') {
				if (!startQuoted) {
					startQuoted = true;
				} else {
					startQuoted = false;
				}

			} else if (c == ',') {

				if (startQuoted) {
					builder.append(c);
				} else {
					String part = builder.toString();
					parts.add(part);
					builder.setLength(0);
				}
			} else {
				builder.append(c);
			}

		}
		parts.add(builder.toString());

		return parts;
	}

	/**
	 * Write a map data structure to a file, in "key,value" format.
	 * 
	 * @param map
	 * @param path
	 */
	public void writeMapToFile(Map<?, ?> map, String path) {
		
		logger.debug("+writeMapToFile({})", path);
		
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path), "utf-8"));
			
			for (Object key: map.keySet()) {
				Object value = map.get(key);

				String line = key.toString() + "," + value.toString();
				writer.write(line);
				writer.newLine();
			}
			
			writer.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.debug("-writeMapToFile({})", path);
	}
	
	
	/**
	 * Write a map of map data structure to a file, in "key1,key2,value" format.
	 * 
	 * @param map
	 * @param path
	 */
	public void writeMapOfMapToFile(Map<?,Map<Object, Object>> map, String path) {
		
		logger.debug("+writeMapOfMapToFile({})", path);
		
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(path), "utf-8"));
			
			for (Object key1: map.keySet()) {
				
				Map<?,?> innerMap = map.get(key1);
				
				for (Object key2: innerMap.keySet()) {
					Object value = innerMap.get(key2);
					String line = key1.toString() + "," + key2.toString() + "," + value.toString();
					writer.write(line);
					writer.newLine();
					
				}
			}
			
			writer.close();
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.debug("-writeMapOfMapToFile({})", path);
	}
	
	
	public static void main(String[] args) {

		FileUtility utility = new FileUtility();
		
		String path = "test/file1.txt";
		String delimiter = " ";
		int numOfElements = 3;
		List<List<String>> lines = utility.readFile(path, delimiter, numOfElements);
		for (List<String> line : lines) {
			for (String part : line) {
				System.out.print(part + " ");
			}
			System.out.println();
		}
		
		path = "test/file2.csv";
		numOfElements = 3;
		lines = utility.readCSV(path, numOfElements);
		for (List<String> line : lines) {
			for (String part : line) {
				System.out.print(part + " ");
			}
			System.out.println();
		}
		
		
		Map<String, Integer> testMap = new HashMap<String, Integer>();
		testMap.put("a", 0);
		testMap.put("c", 4);
		utility.writeMapToFile(testMap, "test/output1.txt");

		
		Map<String, Map<Object, Object>> testMapOfMap = new HashMap<String, Map<Object, Object>>();
		Map<Object, Object> outgoing1 = new HashMap<Object, Object>();
		outgoing1.put("b", 0);
		outgoing1.put("c", 2);
		testMapOfMap.put("a", outgoing1);
		Map<Object, Object> outgoing2 = new HashMap<Object, Object>();
		outgoing2.put("a", 5);
		outgoing2.put("c", 6);
		testMapOfMap.put("b", outgoing2);
		
		utility.writeMapOfMapToFile(testMapOfMap, "test/output2.txt");

		
	}

}
