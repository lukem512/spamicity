import java.util.List;

public class Sort {
    static void insertionSort(List<tuple<Double, String>> arr)
    {
    	int i = 0;
    	int j;
    	tuple<Double, String> mv;
    	tuple<Double, String> mvVal = new tuple<Double, String>(arr.get(i).spam, arr.get(i).ham);
        for(i = 1; i < arr.size(); i++) {
        mv = arr.get(i);
        j = i;
        while(j > 0 && arr.get(j-1).spam < mv.spam) {
            arr.set(j, arr.get(j-1));
            j--;
        }
        arr.set(j, mv);
        }
    }
}
