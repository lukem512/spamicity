import java.io.*;
import java.util.*;

class train
{
  private File trainDir, stopWordFile;
  private HashMap<String,tuple<Integer, Integer>> wordFreq;
  private List<String> stopWords;
  private int hamFileCount, spamFileCount;
  private int hamWordCount, spamWordCount;
  private int hamUniqueWordCount, spamUniqueWordCount;
  private double prS, prH;

  public train(File trainDir, File stopWordFile)
  {
    this.trainDir = trainDir;
    this.stopWordFile = stopWordFile;
    stopWords = new ArrayList<String>(Arrays.asList(readFile(stopWordFile).split(",")));
  }

  private String trainSubjectLine(String fileText, File f)
  {
    int i = 0;
    // Split file into lines
    String[] fileLines = fileText.split("\n");
    
    for (; i < fileLines.length; i++)
    {
      // Find the subject line (assume this always starts the same
      if (fileLines[i].indexOf("Subject:") == 0)
      {
        String[] line = fileLines[i].split("[^A-Za-z0-9']");        
        tuple<Integer, Integer> t;     
        trainWords(line, 55, f);        
        break;
      }
    }
    
    if (i < fileLines.length - 1)
    {
      String afterSubjectLines[] = Arrays.copyOfRange(fileLines, i+1, fileLines.length);
      StringBuilder builder = new StringBuilder();
      
      for(String s : afterSubjectLines)
      {
          builder.append(s);
          builder.append(' ');
      }
      
      return builder.toString();
    }
    else
    {
      return fileText;
    }
  }
 
  private void trainWords(String[] words, int increment, File f)
  {
    for (String s : words)
    {
      boolean newWord = false;
      tuple<Integer, Integer> t;
     
      t = wordFreq.get(s);

     if (t == null)
     {
       t = new tuple<Integer, Integer>(0,0);
       wordFreq.put(s, t);
       newWord = true;
     }

     if (isFileTypeHam(f))
     {
       t.ham+=increment;
       if (newWord) hamUniqueWordCount++;
       hamWordCount+=increment;
     }
     else
     {
       t.spam+=increment;
       if (newWord) spamUniqueWordCount++;
       spamWordCount+=increment;
     }
    }
  }

  private void trainFile(File f)
  {
    String fileText = readFile(f);   
    String[] words = fileText.split("[^A-Za-z0-9']");
    trainWords(words, 1, f);
    trainSubjectLine(fileText, f); 
  }

  public void train(File[] files)
  {
    // reset state
    wordFreq = new HashMap<String,tuple<Integer, Integer>>();
    hamFileCount = 0;
    spamFileCount = 0;
    hamWordCount = 0;
    spamWordCount = 0;
    hamUniqueWordCount = 0;
    spamUniqueWordCount = 0;
    prS = 0.0;
    prH = 0.0;
    
    // read each file and store the word
    // frequencies in the HashMap
    for (File f : files)
    {
      trainFile(f);

      if (isFileTypeHam(f))
      {
        hamFileCount++;
      } else {
        spamFileCount++;
      }
    }

    // compute pr(spam) and pr(ham)
    // the probability that an email is either ham or spam
    prS = spamFileCount / (double)(spamFileCount + hamFileCount);
    prH = hamFileCount / (double)(spamFileCount + hamFileCount);

    // output to classifier file
    try
    {
      PrintWriter writer = new PrintWriter("classifier.csv", "UTF-8");
      
      // Write number of spam emails so prior can be obtained in filter
      writer.println(spamFileCount / (double)(spamFileCount + hamFileCount));
      
      for (String s : wordFreq.keySet())
      {
        double spamicity = spamminess(s);
        double hamicity = hamminess(s);
        
        if (!stopWords.contains(s) && !s.matches(".*\\d.*"))
        {
           // only use words that contribute a high confidence
           // to either spam or ham
           if (Math.abs(spamicity - hamicity) > 2.0)
           {
             writer.println(s+","+spamicity+","+hamicity);
           }
        }
      }
      writer.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  public static boolean isFileTypeHam(File f)
  {
    if (f.getName().contains("ham"))
    {
      return true;
    }

    return false;
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

  private double spamminess(String s)
  {
    tuple<Integer, Integer> t = wordFreq.get(s);
    
    double prior = spamFileCount / (double)(spamFileCount + hamFileCount);    
    double prWS = (t.spam + 1) / (double) (wordFreq.size() + spamWordCount);
    
    return Math.log(prWS);
  }
  
  private double hamminess(String s)
  {
    tuple<Integer, Integer> t = wordFreq.get(s);
    
    double prior = hamFileCount / (double)(spamFileCount + hamFileCount);    
    double prWH = (t.ham + 1) / (double) (wordFreq.size() + hamWordCount);
    
    return Math.log(prWH);
  }

  public static void usage()
  {
    System.out.println("Usage: java train traindir");
    System.out.println();
  }

  public static void error(String msg)
  {
    System.out.println("Train Error: " + msg);
    System.out.println();
  }

  public static void main(String[] args)
  {
    File trainDir, stopWordFile;

    if (args.length < 1)
    {
      usage();
      return;
    }

    trainDir = new File(args[0]);
    if (!trainDir.exists())
    {
      error("could not open training directory " + trainDir);
      return;
    }
    
    stopWordFile = new File("stop-words.csv");
    if (!stopWordFile.exists())
    {
      error("could not open stop words file " + stopWordFile);
      return;
    }

    File[] files = trainDir.listFiles();
    train t = new train(trainDir, stopWordFile);
    t.train(files);
  }
}
