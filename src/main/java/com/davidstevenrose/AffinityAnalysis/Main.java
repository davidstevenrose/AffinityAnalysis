package com.davidstevenrose.AffinityAnalysis;

import com.opencsv.CSVIterator;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * System of determining the confidence and support of association rule X -> Y
 * Precondition: the user inputs at least one value for X and inputs only one value for Y
 *                The user also does not include value of Y as element in X.
 * Postcondition: A value of confidence and a value of support, both in range 0<= i <= 1
 *
 * @author drose
 */
public class Main {

  private static HashSet<String> genres;

  private static void verifyItem(String item){
    if (!genres.contains(item)) {
      System.out.println(
          "We do not have the item " + item + ". Please restart the program and try again.");
      System.exit(0);
    }
  }

  public static void main(String[] args) throws IOException, CsvValidationException {
    FileReader file = new FileReader("src/Data/sampleFile.csv");
    Scanner sc = new Scanner(System.in);
    CSVReader reader = new CSVReader(file);
    String[] metadata = reader.readNext();
    genres = new HashSet<>(Arrays.asList(metadata));

    System.out.println("Enter comma separated values for the items your consumer purchased.");
    String[] setXInfo = sc.nextLine().trim().split(",");
    //store the elements of setXInfo here, which are prerecorded transactions of customers
    List<List<String>> setXElements = new LinkedList<>();
    //verify user input
    Arrays.stream(setXInfo).forEach(Main::verifyItem);

    System.out.println(
        "What item do you want to infer about that the consumer whom purchased the previously"
            + " entered item(s) will also but this item?");
    String itemY = sc.next();
    verifyItem(itemY);

    //we now have setXInfo and item Y. Compute support and confidence.
    //N is total number of transactions
    int N = 0;
    //XSize is num of transactions containing X1 and X2 and ... and XN
    //CVSReader reader is currently on the second line, so this construction should start on the
    // second line instead of the first.
    CSVIterator current = new CSVIterator(reader);
    String[] data;
    try {
      while (current.hasNext()) {
        data = current.next();
        N++;
        //get list of items in current transaction
        List<String> itemsInTransaction = new LinkedList<>();
        for (int index = 0; index < data.length; index++) {
          if (data[index].equals("1")) {
            itemsInTransaction.add(metadata[index]);
          }
        }
        //if setXInfo is subset of items in transaction, increment XSize and add to set X
        if (itemsInTransaction.containsAll(Arrays.asList(setXInfo))) {
          setXElements.add(itemsInTransaction);
        }
      }

      //now, find the number of transactions in setXTransactions that also contain our infered object itemY
      //we just need the size of this subset for now
      int YSize = (int) setXElements.stream().filter((x) -> x.contains(itemY)).count();

      //let the confidence be YSize / XSize
      float confidence = setXElements.size()==0? 0 : (float) YSize / (float) setXElements.size();
      //let the support be YSize / N
      float support = (float) YSize / (float) N;

      System.out.println("Confidence: " + confidence + "; support: " + support);
    } catch (Exception e){
      System.out.println("Sorry, an unexpected error occurred.");
      e.printStackTrace();
    }finally {
      reader.close();
      file.close();
      sc.close();
    }
  }
}
