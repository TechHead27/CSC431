import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
   private HashSet<String> LiveNow;
   private HashMap<String, String> CopyIn = new HashMap<String, String>();
   private HashMap<String, String> CopyGen = new HashMap<String, String>();
   private HashSet<String> CopyKill = new HashSet<String>();
   private HashMap<String, Long> ConstantIn = new HashMap<String, Long>();
   private HashMap<String, Long> ConstantOut = new HashMap<String, Long>();

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
      ArrayList<String> elses = new ArrayList<String>();

      while (!toVisit.isEmpty())
      {
         Block current = toVisit.pop();
         if (!(current.visited))
         {
            String[] parts = current.label.split(":");
            
            if(parts[1].equals("ifend") && elses.indexOf(parts[2]) != -1)
            {
               String search = parts[0] + ":else:" + parts[2];
               Block elseBlock = null;
               for (Block b: toVisit)
               {
                  if (b.label.equals(search))
                     elseBlock = b;
               }
               if (elseBlock != null)
               { 
                  toVisit.remove(elseBlock);
                  toVisit.push(current);
                  toVisit.push(elseBlock);
               }
               else
               {
                  System.err.println("I'm broken. fix me");
               }
            }
            else
            {
               current.visited = true;
               if (parts[1].equals("then"))
               {
                  elses.add(parts[2]);
                  for (int i = current.succ.size()-1; i >= 0; i--)
                     toVisit.push(current.succ.get(i));
               }
               else if (parts[1].equals("else"))
               {
                  elses.remove(parts[2]);
                  if (!current.succ.isEmpty())
                  {
                     for (int i = current.succ.size()-1; i >= 0; i--)
                        toVisit.push(current.succ.get(i));
                  }
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
      }
      return ret;
   }

   
   public String printAssembly()
   {
      String ret = "";
      LinkedList<Block> toVisit = new LinkedList<Block>();
      toVisit.offer(this);
      ArrayList<String> elses = new ArrayList<String>();

      while (!toVisit.isEmpty())
      {
         Block current = toVisit.pop();
         if (!(current.visited))
         {
            String[] parts = current.label.split(":");
            
            if(parts[1].equals("ifend") && elses.indexOf(parts[2]) != -1)
            {
               String search = parts[0] + ":else:" + parts[2];
               Block elseBlock = null;
               for (Block b: toVisit)
               {
                  if (b.label.equals(search))
                     elseBlock = b;
               }
               if (elseBlock != null)
               { 
                  toVisit.remove(elseBlock);
                  toVisit.push(current);
                  toVisit.push(elseBlock);
               }
               else
               {
                  System.err.println("I'm broken. fix me");
               }
            }
            else
            {
               current.visited = true;
               if (parts[1].equals("then"))
               {
                  elses.add(parts[2]);
                  for (int i = current.succ.size()-1; i >= 0; i--)
                     toVisit.push(current.succ.get(i));
               }
               else if (parts[1].equals("else"))
               {
                  elses.remove(parts[2]);
                  if (!current.succ.isEmpty())
                  {
                     for (int i = current.succ.size()-1; i >= 0; i--)
                        toVisit.push(current.succ.get(i));
                  }
               }
               else
               {
                  for (int i = current.succ.size()-1; i >= 0; i--)
                     toVisit.push(current.succ.get(i));
               }
               ret += current.printAsm();
            }
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
      Kill.clear();
      Gen.clear();
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

      if (newCopies != null)
         CopyIn = newCopies;
      return oldSize == CopyIn.size();
   }

   // replace sources
   public void propagateCopies()
   {
      HashMap<String, String> reverse = new HashMap<String, String>();
      for (Map.Entry<String, String> entry : CopyIn.entrySet())
         reverse.put(entry.getValue(), entry.getKey());

      for (Iloc current : ilocs)
      {
         if (current.getTarget() != null)
         {
            reverse.remove(current.getTarget());

            Iterator<Map.Entry<String, String>> entries = reverse.entrySet().iterator();
            while (entries.hasNext())
               if (entries.next().getValue().equals(current.getTarget()))
                  entries.remove();
         }

         current.replace(reverse);

         if (current.getInst().equals("mov"))
         {
            reverse.put(current.getReg(1), current.getReg(0));
         }
      }
   }

   public void calculateInterference(RegisterGraph g)
   {
      LiveNow = (HashSet<String>)LiveOut.clone();

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

   public void RemoveUseless()
   {
      for (Block b : this)
      {
         b.calculateGenKillSets();
      }

      boolean unchanged = false;
      while (!unchanged)
      {
         unchanged = true;
         for (Block b : this)
         {
            unchanged = unchanged && b.calculateLiveOut();
         }
      }

      for (Block b : this)
      {
         HashSet<String> LiveNow = (HashSet<String>)b.LiveOut.clone();

         for (int i = b.assembly.size() - 1; i >= 0; i--)
         {
            Instruction current = b.assembly.get(i);

            boolean contains = false;
            for (String target : current.getTarget())
               contains = contains || LiveNow.contains(target);

            if (!contains && !current.KeepInstruction())
               b.assembly.remove(i);
            else
            {
               LiveNow.removeAll(current.getTarget());
               LiveNow.addAll(current.getSource());
            }
         }
      }
   }

   public void calculateConstantOut()
   {
      ConstantOut.putAll(ConstantIn);
      for (int i = 0; i < ilocs.size(); i++)
         ConstOpt.AddMapping(ConstantOut, ilocs.get(i), (i == 0 ? null : ilocs.get(i-1))) ;
   }

   public boolean calculateConstantIn()
   {
      int prevSize = ConstantIn.size();

      for (Block b : pred)
      {
         if (ConstantIn.isEmpty())
            ConstantIn.putAll(b.ConstantOut);
         else
         {
            Iterator<Map.Entry<String, Long>> iter = ConstantIn.entrySet().iterator();
            while (iter.hasNext())
            {
               Map.Entry<String, Long> current = iter.next();
               
               if (current.getValue() != b.ConstantOut.get(current.getKey()))
                  iter.remove();
            }
         }
      }

/*      for (Map.Entry<String, Long> entry : ConstantIn.entrySet())
         if (!ConstantOut.containsKey(entry.getKey()))
         ConstantOut.put(entry.getKey(), entry.getValue());*/

      return prevSize == ConstantIn.size();
   }

   public void propagateConstants()
   {
      HashMap<String, Long> ConstantNow = (HashMap<String, Long>)ConstantIn.clone();

      for (int i = 0; i < ilocs.size(); i++)
         i += ConstOpt.replaceConstant(ConstantNow, ilocs.get(i), ilocs, label);
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

   public HashSet<String> getLiveOut()
   {
      return LiveOut;
   }

   public HashSet<String> getLiveNow()
   {
      return LiveNow;
   }

   public Set<String> getConstOut()
   {
      return ConstantOut.keySet();
   }

   public Set<String> getConstIn()
   {
      return ConstantIn.keySet();
   }
}
