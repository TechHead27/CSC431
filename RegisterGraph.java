import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class RegisterGraph
{
   HashMap<String, HashSet<String>> graph; 

   public RegisterGraph()
   {
      graph = new HashMap<String, HashSet<String>>();
   }

   public void addEdge(String r1, String r2)
   {
      HashSet<String> h1, h2;
      if (!graph.containsKey(r1))
      {
         h1 = new HashSet<String>();
         graph.put(r1, h1);
      }
      else
         h1 = graph.get(r1);

      if (!graph.containsKey(r2))
      {
         h2 = new HashSet<String>();
         graph.put(r2, h2);
      }
      else
         h2 = graph.get(r2);

      h1.add(r2);
      h2.add(r1);
   }

   public boolean isConnected(String r1, String r2)
   {
      return graph.get(r1).contains(r2);
   }

   public HashSet<String> getNeighbors(String r1)
   {
      return (HashSet<String>)graph.get(r1).clone();
   }

   public void removeRegister(String r1)
   {
      for (String connected : graph.get(r1))
         graph.get(connected).remove(r1);

      graph.remove(r1);
   }

   @Override
   public String toString()
   {
      StringBuilder ret = new StringBuilder();
      for (String r1 : graph.keySet())
      {
         for (String r2 : graph.get(r1))
            ret.append(r1 + " -> " + r2 + " ");

         ret.append("\n");
      }

      return ret.toString();
   }
}
