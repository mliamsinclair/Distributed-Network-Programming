import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

class MultithreadMergeSort implements Runnable {
    private static Thread[] threads = new Thread[4];
    private int threadID;
    private int[] arr;

    // constructor
    public MultithreadMergeSort(int ID, int length) {
        threadID = ID;
        arr = new int[length];
    }

    // run method for threads
    public void run() {
        threadTask(threadID, arr);
        // wait for thread 2 and 4 to finish before merging
        if (threadID == 1 || threadID == 3) {
            try {
                threads[threadID].join();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            mergeFile("part" + threadID + ".txt", "part" + (threadID + 1) + ".txt", "output" + threadID + ".txt");
            System.out.println("output" + threadID + ".txt created!");
        }
        System.out.println("Thread " + threadID + " done!");
    }

    // merge two subarrays of arr[]
    public static void merge(int[] arr, int l, int m, int r) {
        // sizes of both subarrays to be merged
        int size1 = m - l + 1;
        int size2 = r - m;
        // create temp arrays
        int Left[] = new int[size1];
        int Right[] = new int[size2];
        // copy data to temp arrays
        for (int i = 0; i < size1; ++i)
            Left[i] = arr[l + i];
        for (int j = 0; j < size2; ++j)
            Right[j] = arr[m + 1 + j];
        // merge the temp arrays
        int i = 0, j = 0;
        int k = l;
        while (i < size1 && j < size2) {
            if (Left[i] <= Right[j]) {
                arr[k] = Left[i];
                i++;
            } else {
                arr[k] = Right[j];
                j++;
            }
            k++;
        }
        // copy remaining elements of Left[]
        while (i < size1) {
            arr[k] = Left[i];
            i++;
            k++;
        }
        // copy remaining elements of Right[]
        while (j < size2) {
            arr[k] = Right[j];
            j++;
            k++;
        }
    }

    // sort the array
    public static void sort(int[] arr, int l, int r) {
        if (l < r) {
            // find the mid point
            int m = (l + r) / 2;
            // sort left and right halves
            sort(arr, l, m);
            sort(arr, m + 1, r);
            // merge
            merge(arr, l, m, r);
        }
    }

    // create 4 files to store parts of the input file
    public static void createParts(int numParts) {
        for (int i = 1; i <= numParts; i++) {
            File file = new File("part" + i + ".txt");
            if (!file.exists()) {
                try {
                    file.delete();
                    file.createNewFile();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

    // fill parts with data from input file
    public static int[] fillParts(String filename, int numParts) {
        long numLines = -1;
        int[] fileLines = new int[2];
        try (Stream<String> lines = Files.lines(Paths.get(filename))) {
            numLines = (int) lines.count();
            System.out.println("File found: " + filename);
        } catch (IOException e) {
            System.out.println(e);
        }
        // if file is empty, return
        if (numLines == -1) {
            System.out.println("File is empty!");
            return fileLines;
        }
        // find the number of lines per file, with the fourth file having the remainder
        System.out.println("Number of lines: " + numLines);
        int linesPerFile = (int) Math.ceil(numLines / 4);
        fileLines[0] = linesPerFile;
        fileLines[1] = linesPerFile;
        fileLines[1] += (int) numLines % 4;
        BufferedReader reader;
        // fill parts with data from input file
        try {
            int lineCount = 1;
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            for (int i = 1; i <= numParts; i++) {
                FileWriter write = new FileWriter("part" + i + ".txt");
                while (lineCount != linesPerFile + 1) {
                    if (lineCount == linesPerFile) {
                        write.write(line);
                        line = reader.readLine();
                        break;
                    }
                    write.write(line + "\n");
                    line = reader.readLine();
                    lineCount++;
                }
                lineCount = 1;
                write.close();
                System.out.println("File part" + i + ".txt created!");
            }
            reader.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        return fileLines;
    }

    // task to be carried out by each thread
    public static void threadTask(int threadID, int[] arr) {
        // read file
        try {
            BufferedReader reader = new BufferedReader(new FileReader("part" + threadID + ".txt"));
            String line = reader.readLine();
            for (int i = 0; i < arr.length && line != null; i++) {
                arr[i] = Integer.parseInt(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        // sort
        sort(arr, 0, arr.length - 1);
        // write sorted array to file
        try {
            FileWriter write = new FileWriter("part" + threadID + ".txt");
            for (int i = 0; i < arr.length; i++) {
                if (i == arr.length - 1)
                    write.write(arr[i] + "");
                else
                    write.write(arr[i] + "\n");
            }
            write.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    // merge two files into one
    public static void mergeFile(String filename1, String filename2, String output) {
        // if output file exists, delete it
        try {
            BufferedReader reader1 = new BufferedReader(new FileReader(filename1));
            BufferedReader reader2 = new BufferedReader(new FileReader(filename2));
            FileWriter write = new FileWriter(output);
            File file = new File(output);
            if (!file.exists()) {
                file.delete();
                file.createNewFile();
            }
            // merge
            String line1 = reader1.readLine();
            String line2 = reader2.readLine();
            while (line1 != null && line2 != null) {
                if (Integer.parseInt(line1) <= Integer.parseInt(line2)) {
                    write.write(line1 + "\n");
                    line1 = reader1.readLine();
                } else {
                    write.write(line2 + "\n");
                    line2 = reader2.readLine();
                }
            }
            // copy remaining lines
            while (line1 != null) {
                String temp = line1;
                line1 = reader1.readLine();
                if (line1 == null) {
                    write.write(temp);
                    break;
                } else {
                    write.write(temp + "\n");
                }
            }
            while (line2 != null) {
                String temp = line2;
                line2 = reader2.readLine();
                if (line2 == null) {
                    write.write(temp);
                    break;
                } else {
                    write.write(temp + "\n");
                }
            }
            reader1.close();
            reader2.close();
            write.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        // delete the two files that were merged
        File file = new File(filename1);
        file.delete();
        file = new File(filename2);
        file.delete();
        System.out.println("File " + filename1 + " and " + filename2 + " deleted!");
    }

    // main method
    public static void main(String[] args) {
        System.out.println("\nMultithreaded Merge Sort\n");
        // create 4 files to store parts of the input file
        createParts(4);
        // fill parts with data from input file
        int[] length = fillParts("input.txt", 4);
        // create 4 threads and start them
        for (int i = 1; i <= threads.length; i++) {
            if (i == threads.length) {
                threads[i - 1] = new Thread(new MultithreadMergeSort(i, length[1]));
                threads[i - 1].start();
                break;
            }
            threads[i - 1] = new Thread(new MultithreadMergeSort(i, length[0]));
            threads[i - 1].start();
        }
        System.out.println("\nThreads started!");
        // wait for all threads to finish
        try {
            for (int i = 1; i <= threads.length; i++) {
                threads[i - 1].join();
            }
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        // merge the two remaining halves into one
        mergeFile("output1.txt", "output3.txt", "result.txt");
        System.out.println("\nDone! Check result.txt for the sorted file.\n");
    }
}
