import java.util.Comparator;

public class RegisterComparator implements Comparator<String>
{
   private RegisterGraph graph;

   public RegisterComparator(RegisterGraph g)
   {
      graph = g;
   }

   @Override
   public int compare(String s1, String s2)
   {
      Integer n1 = graph.countNeighbors(s1);
      Integer n2 = graph.countNeighbors(s2);

      return n2.compareTo(n1);
   }

}
