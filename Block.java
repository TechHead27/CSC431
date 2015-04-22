import java.util.ArrayList;
import java.util.LinkedList;

public class Block
{
   private ArrayList<Block> pred;
   private ArrayList<Block> succ;
   private String label;
   private boolean visited = false;
   ArrayList<Iloc> ilocs = new ArrayList<Iloc>();
   private boolean if_fix = false;

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

   public String printIloc()
   {
      String ret = "";
      for (Iloc iloc: ilocs) {
         ret += iloc.toString() + "\n";
      }
      return ret;
   }

   public String getGraph()
   {
      String ret = "";
      LinkedList<Block> toVisit = new LinkedList<Block>();
      toVisit.offer(this);

      while (!toVisit.isEmpty())
      {
         Block current = toVisit.poll();
         if (!(current.visited))
         {
            current.visited = true;
            String[] parts = current.label.split(":");
            if (parts[1].equals("ifend") && !current.if_fix)
            {
               current.if_fix = true;
               current.visited = false;
               toVisit.push(current);
               System.err.println("Pushing " + current.succ.get(1).label);
               toVisit.push(current.succ.get(1));
               System.err.println("Pushing " + current.succ.get(0).label);
               toVisit.push(current.succ.get(0));
            }
            else
            {
               if (parts[1].equals("whiletest"))
               {
                  System.err.println("Pushing " + current.succ.get(1).label);
                  toVisit.push(current.succ.get(1));
                  System.err.println("Pushing " + current.succ.get(0).label);
                  toVisit.push(current.succ.get(0));
               }
               else
               {
                  for (Block b : current.succ)
                  {
                     toVisit.push(b);
                  }
               }
               ret += current.toString();
               ret += current.printIloc();
            }
         }
      }
      return ret;
   }

   public void addIloc(String instruction) {
      System.out.println(ilocs.size());
      //ilocs.add(new Iloc());
      //ilocs.add(new Iloc(instruction));
   }
}
