import java.io.*;
import java.util.*;

class crossValidation
{
  public final static int K = 10;

  public double[] crossValidate(train t, File trainDir)
  {  
    List<File> files = Arrays.asList(trainDir.listFiles());
    ArrayList<tuple<Integer, Integer>> foldIndexes = new ArrayList<tuple<Integer, Integer>>(K);
    int foldSize = files.size() / K;
    int r = files.size() % K;    
    double sumTruePos = 0;
    double sumTrueNeg = 0;
    double sumFalsePos = 0;
    double sumFalseNeg = 0;
    double sumSqTruePos = 0;
    double sumSqTrueNeg = 0;
    double sumSqFalsePos = 0;
    double sumSqFalseNeg = 0;
    
    // shuffle file list
    long seed = System.nanoTime();
    Collections.shuffle(files, new Random(seed));
        
    // populate fold array
    for (int i = 0; i < K; i++)
    {
      foldIndexes.add(i, new tuple<Integer, Integer>(i * foldSize, (i + 1) * foldSize));
    }
    foldIndexes.get(K-1).ham = (K * foldSize) + r;

    // perform k-fold cross-validation
    for (int i = 0; i < K; i++)
    {
      List<File> firstSet = files.subList(0, foldIndexes.get(i).spam);
      List<File> secondSet = files.subList(foldIndexes.get(i).ham - 1, files.size() - 1);
      List<File> all = new ArrayList<File>(firstSet);
      all.addAll(secondSet);

      File[] newTrainFiles = new File[files.size() - (foldIndexes.get(i).ham - foldIndexes.get(i).spam)];
      all.toArray(newTrainFiles);

      t.train(newTrainFiles);
      
      List<File> testList = files.subList(foldIndexes.get(i).spam, foldIndexes.get(i).ham);
      
      File[] testArray = new File[foldIndexes.get(i).ham - foldIndexes.get(i).spam];
      testList.toArray(testArray);
      
      double[] results = validate(testArray);
      
      sumTruePos += results[0];
      sumTrueNeg += results[1];
      sumFalsePos += results[2];
      sumFalseNeg += results[3];
      
      sumSqTruePos += Math.pow(results[0], 2);
      sumSqTrueNeg += Math.pow(results[1], 2);;
      sumSqFalsePos += Math.pow(results[2], 2);
      sumSqFalseNeg += Math.pow(results[3], 2);
    }
    
    double truePosResult = sumTruePos / (double) K;
    double trueNegResult = sumTrueNeg / (double) K;
    double falsePosResult = sumFalsePos / (double) K;
    double falseNegResult = sumFalseNeg / (double) K;
    
    double truePosSD = Math.sqrt((sumSqTruePos / (double) K) - Math.pow(truePosResult, 2));
    double trueNegSD = Math.sqrt((sumSqTrueNeg / (double) K) - Math.pow(trueNegResult, 2));
    double falsePosSD = Math.sqrt((sumSqFalsePos / (double) K) - Math.pow(falsePosResult, 2));
    double falseNegSD= Math.sqrt((sumSqFalseNeg / (double) K) - Math.pow(falseNegResult, 2));
    
    return new double[]{truePosResult, trueNegResult, falsePosResult, falseNegResult, truePosSD, trueNegSD, falsePosSD, falseNegSD};
  }

  public double[] validate(File[] files)
  {
    double truePos = 0;
    double trueNeg = 0;
    double falsePos = 0;
    double falseNeg = 0;
    int spamCount = 0;
    
    filter f = new filter(filter.defaultClassifier);
    
    for (int i = 0; i < files.length; i++)
    {
      boolean spam = f.run(files[i]);

      if (train.isFileTypeHam(files[i]))
      {
        if (!spam)
        {
          trueNeg++;
        } else {
          falsePos++;
        }
      }
      else
      {
        // increase file counter
        spamCount++;
        
        if (spam)
        {
          truePos++;
        } else {
          falseNeg++;
        }
      }     
    }
    
    //System.out.println("true pos count: " + truePos);
    //System.out.println("true neg count: " + trueNeg);
    //System.out.println("false pos count: " + falsePos);
    //System.out.println("false neg count: " + falseNeg);
    
    // compute hamCount
    // the + 4 term is to accomodate for the additional
    // 'files' being added by using Laplace correction
    int hamCount = (files.length) - spamCount;
    
    //System.out.println("\nSpamcount: " + spamCount);
    //System.out.println("Hamcount: " + hamCount);
    
    // compute percentages
    truePos /= (double) spamCount;
    trueNeg /= (double) hamCount;
    falsePos /= (double) hamCount;
    falseNeg /= (double) spamCount;
    
    return new double[]{truePos, trueNeg, falsePos, falseNeg};
  }
  
  public double[] run(File trainDir, File stopWordFile)
  {    
    train t = new train(trainDir, stopWordFile);
    return crossValidate(t, trainDir);
  }
  
  public static void usage()
  {
    System.out.println("Usage: java crossValidation trainDir");
    System.out.println();
  }

  public static void error(String msg)
  {
    System.out.println("Cross Validation Error: " + msg);
    System.out.println();
  }
  
  public static void main(String[] args)
  {
    File trainDir, stopWordFile;
    double sumTruePos = 0;
    double sumTrueNeg = 0;
    double sumFalsePos = 0;
    double sumFalseNeg = 0;
    double sumTruePosSD = 0;
    double sumTrueNegSD = 0;
    double sumFalsePosSD = 0;
    double sumFalseNegSD = 0;
    
    int iterations = Integer.parseInt(args[1]);;

    if (args.length != 2)
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

    crossValidation crossValidator = new crossValidation();
    
    for (int i = 0; i < iterations; i++)
    {
      double[] results = crossValidator.run(trainDir, stopWordFile);
      sumTruePos += results[0];
      sumTrueNeg += results[1];
      sumFalsePos += results[2];
      sumFalseNeg += results[3];
      sumTruePosSD += results[4];
      sumTrueNegSD += results[5];
      sumFalsePosSD += results[6];
      sumFalseNegSD += results[7];
      
      System.out.println("Iteration " + (i + 1) + " of " + iterations + " complete");
    }
    
    double truePosResult = sumTruePos / (double) iterations;
    double trueNegResult = sumTrueNeg / (double) iterations;
    double falsePosResult = sumFalsePos / (double) iterations;
    double falseNegResult = sumFalseNeg / (double) iterations;
    double truePosResultSD = sumTruePosSD / (double) iterations;
    double trueNegResultSD = sumTrueNegSD / (double) iterations;
    double falsePosResultSD = sumFalsePosSD / (double) iterations;
    double falseNegResultSD = sumFalseNegSD / (double) iterations;

    System.out.println("\nTrue Positives: " + truePosResult);
    System.out.println("True Negatives: " + trueNegResult);
    System.out.println("False Positives: " + falsePosResult);
    System.out.println("False Negatives: " + falseNegResult);
    System.out.println("\nStandard Deviation\nTrue Positives: " + truePosResultSD);
    System.out.println("True Negatives: " + trueNegResultSD);
    System.out.println("False Positives: " + falsePosResultSD);
    System.out.println("False Negatives: " + falseNegResultSD);
  }
}
