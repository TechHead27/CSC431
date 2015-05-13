import java.util.ArrayList;
import java.util.LinkedList;

public class Block
{
   private ArrayList<Block> pred;
   private ArrayList<Block> succ;
   private String label;
   private boolean visited = false;
   private boolean ASMvisited = false;
   private ArrayList<Iloc> ilocs = new ArrayList<Iloc>();
   private ArrayList<Instruction> assembly = new ArrayList<Instruction>();

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
   
   public String printAsm()
   {
      String ret = "";
      for (Instruction inst : assembly)
         ret += inst.toString() + "\n";
      
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
            if (parts[1].equals("then"))
            {
               for (int i = current.succ.size()-1; i > 0; i--)
                  toVisit.push(current.succ.get(i));
            }
            else if (parts[1].equals("else"))
            {
               toVisit.push(current.succ.get(0));
               for (int i = current.succ.size()-1; i > 0; i--)
                  toVisit.push(current.succ.get(i));
            }
            else
            {
               for (int i = current.succ.size()-1; i >= 0; i--)
                  toVisit.push(current.succ.get(i));
            }
            ret += current.toString();
            ret += current.printIloc();
         }
      }
      return ret;
   }

   public String getAssembly()
   {
      String ret = "";
      LinkedList<Block> toVisit = new LinkedList<Block>();
      toVisit.offer(this);

      while (!toVisit.isEmpty())
      {
         Block current = toVisit.poll();
         if (!(current.ASMvisited))
         {
            current.ASMvisited = true;
            String[] parts = current.label.split(":");
            if (parts[1].equals("then"))
            {
               for (int i = current.succ.size()-1; i > 0; i--)
                  toVisit.push(current.succ.get(i));
            }
            else if (parts[1].equals("else"))
            {
               toVisit.push(current.succ.get(0));
               for (int i = current.succ.size()-1; i > 0; i--)
                  toVisit.push(current.succ.get(i));
            }
            else
            {
               for (int i = current.succ.size()-1; i >= 0; i--)
                  toVisit.push(current.succ.get(i));
            }
            ret += current.printAsm();
         }
      }
      return ret;
   }

   public void addIloc(Iloc instruction)
   {
      ilocs.add(instruction);
   }

   public ArrayList<Iloc> getIlocs()
   {
      ArrayList<Iloc> ret = new ArrayList<Iloc>();
      LinkedList<Block> toVisit = new LinkedList<Block>();
      toVisit.offer(this);

      while (!toVisit.isEmpty())
      {
         Block current = toVisit.poll();
         if (!(current.visited))
         {
            current.visited = true;
            String[] parts = current.label.split(":");
            if (parts[1].equals("then"))
            {
               for (int i = current.succ.size()-1; i > 0; i--)
                  toVisit.push(current.succ.get(i));
            }
            else if (parts[1].equals("else"))
            {
               toVisit.push(current.succ.get(0));
               for (int i = current.succ.size()-1; i > 0; i--)
                  toVisit.push(current.succ.get(i));
            }
            else
            {
               for (int i = current.succ.size()-1; i >= 0; i--)
                  toVisit.push(current.succ.get(i));
            }
            ret.addAll(current.ilocs);
         }
      }
      return ret;
   }

   public void addInstructions(ArrayList<Instruction> insts)
   {
      assembly.addAll(insts);
   }
}
