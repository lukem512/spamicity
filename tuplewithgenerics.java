public class tuplewithgenerics<T extends Comparable<T>, E> implements Comparable
{
  public T spam;
  public E ham;
 
  public tuplewithgenerics(T spam, E ham)
  {
    this.spam = spam;
    this.ham = ham;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public int compareTo(Object other)
  {
	  tuplewithgenerics<T, E> otherTuple = (tuplewithgenerics<T, E>) other;
	  return this.spam.compareTo(otherTuple.spam); 
  }
}