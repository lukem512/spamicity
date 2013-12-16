import java.io.*;
import java.util.*;

public class filter
{
  public static final File defaultClassifier = new File ("classifier.csv");
  private HashMap<String,tuple<Double, Double>> wordProbs;
  private double prior;

  public filter (File classifierFile)
  {
    wordProbs = new HashMap<String,tuple<Double, Double>>();
    loadClassifier(classifierFile);
  }

  public boolean run(File testFile)
  {
    // load test file
    String fileText = readFile(testFile);
    String[] words = fileText.split("[^A-Za-z0-9']");
    
    // Unknown words should contribute the average as the are 50/50
    double hamLogPrior = Math.log(1 - prior);
    double spamLogPrior = Math.log(prior);
    double sumSpaminess = 0;
    double sumHaminess = 0;
    
    // compute spamminess of email
    for (String s : words)
    {
      tuple<Double, Double> t = wordProbs.get(s);
      
      if (t != null)
      {
        //System.out.println("word: " + s + " spamicity: " + t.spam + " hamicity: " + t.ham);
        sumSpaminess += t.spam;
        sumHaminess += t.ham;
      }
    }
    
    double prSpam = spamLogPrior + sumSpaminess;
    double prHam = hamLogPrior + sumHaminess;
    
    if (prSpam > prHam)
    {    
      return true;
    } else {
      return false;
    }
  }
  
  private String[] atomicWords(String[] words)
  {
    String prev = "";
    ArrayList<String> atomicWords = new ArrayList<String>(Arrays.asList(words));
    Collections.sort(atomicWords);
    
    for (int i = 0; i < atomicWords.size(); i++)
    {
      if (atomicWords.get(i).equals(""))
      {
        atomicWords.remove(i);
        i--;
      } else {
        if (atomicWords.get(i).equals(prev))
        {
          atomicWords.remove(i);
          i--;
        }
        prev = atomicWords.get(i);
      }
    }
    
    String[] atomicArray = new String[atomicWords.size()];
    atomicWords.toArray(atomicArray);
    return atomicArray;
  }
  
  private String readFile(File f)
  {
    String txt = "";

    try
    {
      FileReader fr = new FileReader(f);
      char[] chars = new char[(int) f.length()];
      fr.read(chars);
      txt = new String(chars);
      fr.close();
    }
    catch (IOException e)
    {
      error("could not read file " + f);
      e.printStackTrace();
    }

    return txt;
  }

  private void loadClassifier(File classifierFile)
  {
    if (!classifierFile.exists())
    {
      error("could not open classifier file " + classifierFile);
      return;
    }

    try
    {
      BufferedReader br = new BufferedReader(new FileReader(classifierFile));
      String line = br.readLine();
      this.prior = Double.parseDouble(line);
      
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

         wordProbs.put(data[0], new tuple<Double, Double>(spamicity,hamicity));
      }
      br.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public static void usage()
  {
    System.out.println("Usage: java filter testfile");
    System.out.println();
  }

  public static void error(String msg)
  {
    System.out.println("Filter Error: " + msg);
    System.out.println();
  }

  public static void main (String[] args)
  {
    File testFile;
    
    if (args.length != 1)
    {
      usage();
      return;
    }

    testFile = new File(args[0]);

    if (!testFile.exists())
    {
      error("could not open test file " + testFile);
      return;
    }

    filter f = new filter(defaultClassifier);
    boolean spam = f.run(testFile);
    System.out.println(spam ? "spam" : "ham");
  }
}
