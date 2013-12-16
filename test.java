import java.io.*;
import java.util.*;

class test
{
  private File testDir;

  public test(File testDir)
  {
    this.testDir = testDir;
  }

  public void run(String expectedOutput)
  {
    int total = 0;
    int correct = 0;
    filter f = new filter(filter.defaultClassifier);
    File[] files = testDir.listFiles();
    boolean spam;
    boolean expected = expectedOutput.equals("spam") ? true : false;
    
    for (int i = 0; i < files.length; i++)
    {
      spam = f.run(files[i]);
      
      if (spam == expected)
      {
        correct++;
      }
      
      total++;
    }
    
    System.out.println("result: " + (correct / (double)total));
  }
  
  public static void usage()
  {
    System.out.println("Usage: java crossValidation TestDir ExpectedOutput");
    System.out.println();
  }

  public static void error(String msg)
  {
    System.out.println("Test Error: " + msg);
    System.out.println();
  }

  public static void main(String[] args)
  {
    File testDir;
    
    if (args.length != 2)
    {
      usage();
      return;
    }

    testDir = new File(args[1]);
    if (!testDir.exists())
    {
      error("could not open testing directory " + testDir);
      return;
    }
    
    String expectedOutput = args[0];
    test t = new test(testDir);
    t.run(expectedOutput); 
  }
}
