import java.io.*;
import java.util.*;

public class topWords
{
  private List<tuple<Double, String>> spamData;
  private List<tuple<Double, String>> hamData;

  private void loadClassifier(File classifierFile)
  {
    spamData = new ArrayList<tuple<Double, String>>();
    hamData = new ArrayList<tuple<Double, String>>();
  
    if (!classifierFile.exists())
    {
      error("could not open classifier file " + classifierFile);
      return;
    }

    try
    {
      BufferedReader br = new BufferedReader(new FileReader(classifierFile));
      String line = br.readLine();
      
      while ((line = br.readLine()) != null) {
         String[] data = line.split(",");
         double spamicity = 0, hamicity = 0;

         if (data.length != 3)
         {
            error("incorrect line length in classifier");
            return;
         }

         try
         {
           spamicity = Double.parseDouble(data[1]);
           hamicity = Double.parseDouble(data[2]);
         }
         catch (NumberFormatException e)
         {
           e.printStackTrace();
         }

         spamData.add(new tuple<Double, String>(spamicity, data[0]));
         hamData.add(new tuple<Double, String>(hamicity, data[0]));
      }
      br.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  private void outputTop(List<tuple<Double, String>> data, int n, String dataType)
  {
    System.out.println();
    System.out.println(dataType);
    
    for (int i = 0; i < n && i < data.size(); i++)
    {
      System.out.println("Word: " + data.get(i).ham + " value: " + data.get(i).spam);
    }
  }

  public void run(int n)
  {
    this.loadClassifier(filter.defaultClassifier);
    Sort sort = new Sort();	
    sort.insertionSort(spamData);
    this.outputTop(spamData, n, "Spam");
    sort.insertionSort(hamData);
    this.outputTop(hamData, n, "Ham");
  }
  
  public static void usage()
  {
    System.out.println("Usage: java topWords NumberofWords");
    System.out.println();
  }

  public static void error(String msg)
  {
    System.out.println("Top Words Error: " + msg);
    System.out.println();
  }

  public static void main(String[] args)
  {
    if (args.length != 1)
    {
      usage();
      return;
    }
    
    int n = Integer.parseInt(args[0]);
    topWords tw = new topWords();
    tw.run(n);
  }
}
