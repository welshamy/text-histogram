import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * First, buble sort contiguous blocks of 2500 ints using quick sort (done in
 * place O(n*log(n)) on average). Write sorted blocks to separate files.
 * 
 * Second, sort ints in files using merge sort (O(n*log(n)) which is suitable
 * for reading from and writing to files sequentially.
 * 
 * Third, go through the merge sorted file counting the number of occurrences of
 * each int (O(n)).
 * 
 * Each line in the output file contains an integer value followed by its count.
 * 
 * @author Wesam Elshamy <wesamelshamy@gmail.com>
 * 
 */
public class TextHistogram {

	// This is the size of the array that holds the integers 
	// we want to count their occurrences.  This will be the 
	// max number of integers we want to count that is stored
	// in memory at any time. 
	private static final int ARRAY_SIZE = 2500;
	
	/**
	 * This program takes two arguments to run: 
	 * The name of the file containing the integers to 
	 * be counted, one per line.  And the name of the 
	 * output file.
	 *  
	 * @param args Parameter array containing:
	 * args[0]: the name of the input file.
	 * args[1]: the name of the output file.
	 */
	
	public static void main(String args[]) {
		List<File> blockFiles = new ArrayList<File>();
		String arrayFilename = args[0];
		
		createBlocks(blockFiles, arrayFilename);
		
		File merged = mergeBlocks(blockFiles).get(0);
		
		File output = writeNumberCounts(merged, args[1]);
		merged.delete();
		
		System.out.println("Output written to: " + output.getAbsolutePath());
	}
	
	/**
	 * Writes each unique number listed in the original file 
	 * and its number of occurrences to a new output file. 
	 * @param file The file containing the sorted numbers.
	 * @return The file containing the result.
	 */
	private static File writeNumberCounts(File file, String outputFilename) {
		File output = new File(outputFilename);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			BufferedWriter writer = new BufferedWriter(new FileWriter(output));
			
			String line;
			int count = 0;
			reader.mark(100);
			String oldLine = reader.readLine();
			reader.reset();
			
			while ((line = reader.readLine()) != null) {
				if (line.equals(oldLine)) {
					count++;
				} else {
					writer.append(oldLine + " " + count + "\n");
					count = 1;
				}
				oldLine = line;
			}
			writer.append(oldLine + " " + count + "\n");
			
			reader.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	/**
	 * Merges sorts the files listed in the passed list of files.  
	 * 
	 * @param blockFiles A list of files to be merged.
	 * @return A list of the merged files.
	 */
	private static List<File> mergeBlocks(List<File> blockFiles) {
		int i = 0;
		List<File> mergedFiles = new ArrayList<File>();
		while (i < blockFiles.size() - 1) {
			File file1 = blockFiles.get(i);
			File file2 = blockFiles.get(i + 1);
			mergeSort(file1, file2, mergedFiles);
			i+=2;
		}
		
		// If we have odd number of block files, we add the one 
		// remaining file to the list of merged files. 
		if (i == blockFiles.size() - 1)
			mergedFiles.add(blockFiles.get(i));
		
		int bound = (blockFiles.size() % 2 == 1) ? blockFiles.size() - 1 : blockFiles.size();
		
		for (int j = 0; j < bound; j++) {
			blockFiles.get(j).delete();
		}
		
		if (mergedFiles.size() != 1) {
			mergedFiles = mergeBlocks(mergedFiles);
		}
		return mergedFiles;
	}
	
	/**
	 * Merge sorts the numbers listed in the two passed text files 
	 * and writes the output to a temp file and adds it to a 
	 * <code>List<File></code>.
	 * 
	 * @param file1 First file in the merge sort.
	 * @param file2 Second file in the merge sort.
	 * @param mergedFiles A <code>List</code> of merged files.
	 */
	private static void mergeSort(File file1, File file2, List<File> mergedFiles) {
		try {
			BufferedReader reader1 = new BufferedReader(new FileReader(file1));
			BufferedReader reader2 = new BufferedReader(new FileReader(file2));
			
			File mergeFile = File.createTempFile("merged_", null);
			mergedFiles.add(mergeFile);
			BufferedWriter writer = new BufferedWriter(new FileWriter(mergeFile));
			
			int num1 = Integer.parseInt(reader1.readLine());
			int num2 = Integer.parseInt(reader2.readLine());
			
			int finished = -1;
			while (true) {
				if (num1 < num2) {
					writer.append(String.valueOf(num1) + "\n");
					String line ;
					if ((line = reader1.readLine()) != null) {
						num1 = Integer.parseInt(line);
						continue;
					} else {
						finished = 1;
						break;
					}
				} else {
					writer.append(String.valueOf(num2) + "\n");
					String line ;
					if ((line = reader2.readLine()) != null) {
						num2 = Integer.parseInt(line);
						continue;
					} else {
						finished = 2;
						break;
					}
				}
			}
			if (finished == 1) {
				String line;
				writer.append(String.valueOf(num2) + "\n");
				while ((line = reader2.readLine()) != null) {
					writer.append(line + "\n");
				}
			} else if (finished == 2) {
				String line;
				writer.append(String.valueOf(num1) + "\n");
				while ((line = reader1.readLine()) != null) {
					writer.append(line + "\n");
				}				
			}
			reader1.close();
			reader2.close();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Breaks the long list of integers in the given text file 
	 * into smaller lists with the max allowable size.  Each small 
	 * list is then sorted and written to a temp file. 
	 * 
	 * @param blockFiles <code>List</code> of temp files to be created.
	 * @param filename Name of the file containing the long list of 
	 * integers to be counted.
	 */
	private static void createBlocks(List<File> blockFiles, String fileName) {
		try {
			int[] numbers = new int[ARRAY_SIZE];
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			int j = 0;
			
			while ((line = reader.readLine()) != null) {
				numbers[j] = Integer.parseInt(line);
				j++;
				if (j == ARRAY_SIZE) {
					quickSort(numbers, 0, j - 1);
					blockFiles.add(writeBlock(numbers, j - 1));
					j = 0;
				}
			}
			
			if (j > 0) {
				quickSort(numbers, 0, j - 1);
				blockFiles.add(writeBlock(numbers, j - 1));
			}

			reader.close();

		} catch (Exception IOException) {
			System.err.println("Error reading file");
			IOException.printStackTrace();
		}
	}

	/**
	 * Creates blocks of sorted ints.
	 * 
	 * @param array Array of ints to be written to file.
	 * @param bound Index of the last element in the array.
	 */
	private static File writeBlock(int[] array, int bound) {
		File tempFile = null;
		try {
			tempFile = File.createTempFile("blockFile", null);
			Writer writer = new BufferedWriter(new FileWriter(tempFile));
			for (int i = 0; i <= bound; i++) {
				writer.append(String.valueOf(array[i]) + "\n");
			}
			writer.close();
		} catch (IOException e) {
			System.err.println("Error writing to file: ");
			e.printStackTrace();
		}
		return tempFile;
	}

	/**
	 * A method to perform in place quick sort on an array. This method and
	 * {@link #partition(int[], int, int)} use a total of four extra integers (
	 * <code>pivot, temp, i, j</code>).
	 * 
	 * @param array
	 *            Array of integers being sorted.
	 * @param left
	 *            Left index of the array section to be recursively sorted.
	 * @param right
	 *            Right index of the array section to be recursively sorted.
	 */
	private static void quickSort(int[] array, int left, int right) {
		if (left < right) { // base condition
			int pivot = partition(array, left, right);
			quickSort(array, left, pivot - 1); // sort left part of the array
			quickSort(array, pivot + 1, right); // sort right part of the array
		}
	}

	/**
	 * Rearranges the elements of the array so that the pivot has a value
	 * greater than all the elements to its left, and smaller than all the
	 * elements to its right.
	 * 
	 * @param array
	 *            Array of integers being sorted.
	 * @param left
	 *            Left index of the section of the array being sorted.
	 * @param right
	 *            Right index of the section of the array being sorted.
	 * @return Integer representing the index of the pivot in the
	 *         <code>array</code>.
	 */
	private static int partition(int[] array, int left, int right) {
		int i = left - 1;
		// A temp int is used for swapping.
		// I could have used swapping without temp but would run
		// the risk of overflow if the numbers were big enough.
		int temp;
		for (int j = left; j < right; j++) {
			if (array[j] <= array[right]) {
				i++;
				temp = array[i];
				array[i] = array[j];
				array[j] = temp;
			}
		}
		temp = array[i + 1];
		array[i + 1] = array[right];
		array[right] = temp;
		return i + 1;
	}

}
