import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

public class Block implements Iterable<Block>
{
   private ArrayList<Block> pred;
   private ArrayList<Block> succ;
   private String label;
   private boolean visited = false;
   private boolean ASMvisited = false;
   private ArrayList<Iloc> ilocs = new ArrayList<Iloc>();
   private ArrayList<Instruction> assembly = new ArrayList<Instruction>();
   private HashSet<String> Gen = new HashSet<String>();
   private HashSet<String> Kill = new HashSet<String>();
   private HashSet<String> LiveOut = new HashSet<String>();
   private HashMap<String, String> CopyIn = new HashMap<String, String>();
   private HashMap<String, String> CopyGen = new HashMap<String, String>();
   private HashSet<String> CopyKill = new HashSet<String>();

   private class BlockIterator implements Iterator<Block>
   {
      LinkedList<Block> toVisit;
      HashSet<Block> visited;

      public BlockIterator(Block start)
      {
         toVisit = new LinkedList<Block>();
         visited = new HashSet<Block>();

         toVisit.offer(start);
      }

      @Override
      public boolean hasNext()
      {
         return !toVisit.isEmpty();
      }

      @Override
      public Block next()
      {
         Block current = toVisit.poll();
         visited.add(current);

         for (Block b : current.getSucc())
         {
            if (!visited.contains(b) && !toVisit.contains(b))
               toVisit.offer(b);
         }

         return current;
      }

      @Override
      public void remove()
      {
         throw new UnsupportedOperationException();
      }
   }

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

   @Override
   public Iterator<Block> iterator()
   {
      return new BlockIterator(this);
   }

   public ArrayList<Block> getPred()
   {
      return (ArrayList<Block>)pred.clone();
   }

   public ArrayList<Block> getSucc()
   {
      return (ArrayList<Block>)succ.clone();
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

   
   public String printAssembly()
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
      return ret + "\n";
   }

   public void addIloc(Iloc instruction)
   {
      ilocs.add(instruction);
   }

   public ArrayList<Iloc> getIlocs()
   {
      ArrayList<Iloc> ret = new ArrayList<Iloc>();
      ret.addAll(this.ilocs);
      return ret;
   }

   public void calculateGenKillSets()
   {
      for (Instruction i : assembly)
      {
         for (String source : i.getSource())
         {
            if (!Kill.contains(source))
               Gen.add(source);
         }
         Kill.addAll(i.getTarget());
      }
   }
   
   public void calculateCopySets()
   {
      for (int i = ilocs.size() - 1; i >= 0; i--)
      {
         Iloc inst = ilocs.get(i);
         if (inst.getInst().equals("mov"))
         {
            if (!CopyKill.contains(inst.getReg(1)))
            {
               CopyGen.put(inst.getReg(0), inst.getReg(1));
            }

            if (inst.getTarget() != null)
               CopyKill.add(inst.getTarget());
         }
      }
   }

   // returns whether LiveOut is unchanged
   public boolean calculateLiveOut()
   {
      int oldSize = LiveOut.size();

      for (Block b : succ)
      {
         HashSet<String> difference = (HashSet<String>)b.LiveOut.clone();
         difference.removeAll(b.Kill);

         LiveOut.addAll(b.Gen);
         LiveOut.addAll(difference);
      }

      return oldSize == LiveOut.size();
   }

   // returns whether unchanged
   public boolean calculateCopyIn()
   {
      int oldSize = CopyIn.size();
      HashMap<String, String> newCopies = null;

      for (Block b : pred)
      {
         HashMap<String, String> difference = (HashMap<String, String>)b.CopyIn.clone();
         Iterator<Map.Entry<String, String>> copies = difference.entrySet().iterator();

         while (copies.hasNext())
         {
            if (b.CopyKill.contains(copies.next().getValue()))
               copies.remove();
         }

         difference.putAll(b.CopyGen);

         // Calculate intersection
         if (newCopies == null)
         {
            newCopies = difference;
         }
         else
         {
            copies = newCopies.entrySet().iterator();
            while (copies.hasNext())
            {
               Map.Entry<String, String> current = copies.next();
               if (!current.getValue().equals(difference.get(current.getKey())))
                  copies.remove();
            }
         }
      }

      CopyIn = newCopies;
      return oldSize == CopyIn.size();
   }

   public void calculateInterference(RegisterGraph g)
   {
      HashSet<String> LiveNow = (HashSet<String>)LiveOut.clone();

      for (int i = assembly.size() - 1; i >= 0; i--)
      {
         Instruction current = assembly.get(i);
         if (LiveNow.isEmpty())
            for (String t : current.getTarget())
               g.addVertex(t);
         else
         {
            for (String s : LiveNow)
            {
               for (String t : current.getTarget())
               {
                  g.addEdge(s, t);
               }
            }
         }

         LiveNow.removeAll(current.getTarget());
         LiveNow.addAll(current.getSource());
      }
   }

   public void addInstructions(ArrayList<Instruction> insts)
   {
      assembly.addAll(insts);
   }

   public ArrayList<Instruction> getAssembly()
   {
      return (ArrayList<Instruction>)assembly.clone();
   }

   public void setAssembly(ArrayList<Instruction> insts)
   {
      assembly = insts;
   }
}
