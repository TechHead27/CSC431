import java.util.ArrayList;
import java.util.LinkedList;

public class Block
{
   private ArrayList<Block> pred;
   private ArrayList<Block> succ;
   private String label;
   private boolean visited = false;

   public Block()
   {
      pred = new ArrayList<Block>();
      succ = new ArrayList<Block>();
      label = "";
   }

   public Block(String label)
   {
      pred = new ArrayList<Block>();
      succ = new ArrayList<Block>();
      this.label = label;
   }

   public void connect(Block other)
   {
      succ.add(other);
      other.pred.add(this);
   }

   public Block[] getPred()
   {
      return (Block[])pred.toArray();
   }

   public Block[] getSucc()
   {
      return (Block[])succ.toArray();
   }

   public String getLabel()
   {
      return label;
   }

   @Override
   public String toString()
   {
      return label + "\n";
   }

   public String getGraph()
   {
      String ret = "";
      LinkedList<Block> toVisit = new LinkedList<Block>();
      toVisit.push(this);

      while (!toVisit.isEmpty())
      {
         Block current = toVisit.pop();
         if (!current.visited)
         {
            current.visited = true;
            toVisit.addAll(current.succ);
            ret += current.toString();
         }
      }
      return ret;
   }
}
