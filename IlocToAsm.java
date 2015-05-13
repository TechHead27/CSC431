import java.util.*;

public class IlocToAsm
{
   private ArrayList<String> globals;
   private Block header;

   public IlocToAsm()
   {
      globals = new ArrayList<String>();
      header = new Block("header: ");
      header.addIloc(new Iloc("print:\n.string \"%d\\n\""));
      header.addIloc(new Iloc(".text"));
      header.addIloc(new Iloc("scan:\n.string \"%d\""));
      header.addIloc(new Iloc(".text"));
      header.addIloc(new Iloc(".comm scanVar,8,8"));
      header.addIloc(new Iloc(".globl main"));
   }

   private ArrayList<Instruction> ConvertInst(Iloc input)
   {
      ArrayList<Instruction> ret = new ArrayList<Instruction>();
      switch (input.getInst())
      {
         case "ret":
            ret.add(new Instruction("ret"));
            break;
         case "storeret":
            ret.add(new Instruction("movq", input.getReg(0), "%rax"));
            break;
         case "print":
            ret.add(new Instruction("movq", "$print", "%rdi"));
            ret.add(new Instruction("movq", input.getReg(0), "%rsi"));
            ret.add(new Instruction("xorq", "%rax", "%rax"));
            ret.add(new Instruction("call", "printf"));
            ret.add(new Instruction("movq", "$0", "%rax"));
            break;
         case "read":
            ret.add(new Instruction("movq", "$scan", "%rdi"));
            ret.add(new Instruction("leaq", "(scanVar)", "%rsi"));
            ret.add(new Instruction("xorq", "%rax", "%rax"));
            ret.add(new Instruction("call", "scanf"));
            ret.add(new Instruction("movq", "scanVar", input.getReg(0)));
            break;
         case "jumpi":
            ret.add(new Instruction("jmp", input.getReg(0)));
            break;
         case "neg":
            ret.add(new Instruction("neg", input.getReg(0)));
            break;
         case "add":
            ret.add(new Instruction("movq", input.getReg(0), input.getReg(2)));
            ret.add(new Instruction("addq", input.getReg(1), input.getReg(2)));
            break;
         case "addi":
            ret.add(new Instruction("movq", input.getReg(0), input.getReg(2)));
            ret.add(new Instruction("addq", "$" + input.getReg(1), input.getReg(2)));
            break;
         case "mov":
            ret.add(new Instruction("movq", input.getReg(0), input.getReg(1)));
            break;
         case "storeai":
            ret.add(new Instruction("movq", input.getReg(0), input.getReg(1), input.getReg(2), false));
            break;
         case "storeglobal":
            if (!globals.contains(input.getReg(1)))
            {
               header.addIloc(new Iloc(".comm " + input.getReg(1) + ",8,8"));
               globals.add(input.getReg(1));
            }
            ret.add(new Instruction("movq", input.getReg(0), input.getReg(1) + "(%rip)"));
            break;
         case "loadi":
            ret.add(new Instruction("movq", "$" + input.getReg(0), input.getReg(1)));
            break;
         case "storeoutargument":
            switch (Integer.parseInt(input.getReg(1)))
            {
               case 0:
                  ret.add(new Instruction("movq", input.getReg(0), "%rdi"));
                  break;
               case 1:
                  ret.add(new Instruction("movq", input.getReg(0), "%rsi"));
                  break;
               case 2:
                  ret.add(new Instruction("movq", input.getReg(0), "%rdx"));
                  break;
               case 3:
                  ret.add(new Instruction("movq", input.getReg(0), "%rcx"));
                  break;
               case 4:
                  ret.add(new Instruction("movq", input.getReg(0), "%r8"));
                  break;
               case 5:
                  ret.add(new Instruction("movq", input.getReg(0), "%r9"));
                  break;
               default:
                  ret.add(new Instruction("movq", input.getReg(0), "stack"));
            }
            break;
         case "call":
            ret.add(new Instruction("call", input.getReg(0)));
            break;
         case "loadret":
            ret.add(new Instruction("movq", "%rax", input.getReg(0)));
            break;
         case "loadglobal":
            ret.add(new Instruction("movq", input.getReg(0) + "(%rip)", input.getReg(1)));
            break;
         case "new":
            ret.add(new Instruction("movq", "$" + input.getReg(0), "%rdi"));
            ret.add(new Instruction("call", "malloc"));
            ret.add(new Instruction("movq", "%rax", input.getReg(1)));
            break;
         case "del":
            ret.add(new Instruction("movq", input.getReg(0), "%rdi"));
            ret.add(new Instruction("call", "free"));
            break;
         case "sub":
            ret.add(new Instruction("movq", input.getReg(0), input.getReg(2)));
            ret.add(new Instruction("subq", input.getReg(1), input.getReg(2)));
            break;
         case "mult":
            ret.add(new Instruction("movq", input.getReg(0), input.getReg(2)));
            ret.add(new Instruction("imulq", input.getReg(1), input.getReg(2)));
            break;
         case "div":
            ret.add(new Instruction("movq", input.getReg(0), "%rax"));
            ret.add(new Instruction("movq", "%rax", "%rdx"));
            ret.add(new Instruction("sarq", "$63", "%rdx"));
            ret.add(new Instruction("idviq", input.getReg(1)));
            ret.add(new Instruction("movq", "%rax", input.getReg(2)));
            break;
         case "loadinargument":
            switch (input.getReg(1))
            {
               case "0":
                  ret.add(new Instruction("movq", "%rdi", input.getReg(2)));
                  break;
               case "1":
                  ret.add(new Instruction("movq", "%rsi", input.getReg(2)));
                  break;
               case "2":
                  ret.add(new Instruction("movq", "%rdx", input.getReg(2)));
                  break;
               case "3":
                  ret.add(new Instruction("movq", "%rcx", input.getReg(2)));
                  break;
               case "4":
                  ret.add(new Instruction("movq", "%r8", input.getReg(2)));
                  break;
               case "5":
                  ret.add(new Instruction("movq", "%r9", input.getReg(2)));
                  break;
               default:
                  ret.add(new Instruction("movq", "stack", input.getReg(2)));
            }
            break;
         case "loadai":
            ret.add(new Instruction("movq", input.getReg(0), input.getReg(2), input.getReg(1), true));
            break;
         case "comp":
            ret.add(new Instruction("cmp", input.getReg(0), input.getReg(1)));
            break;
         case "cbreq":
            ret.add(new Instruction("je", input.getReg(1)));
            ret.add(new Instruction("jne", input.getReg(2)));
            break;
         case "movlti":
            ret.add(new Instruction("movq", "$1", input.getReg(0)));
            ret.add(new Instruction("cmovl", input.getReg(0), input.getReg(2)));
            break;
         case "brz":
            ret.add(new Instruction("cmp", "$0", input.getReg(0)));
            ret.add(new Instruction("je", input.getReg(1)));
            ret.add(new Instruction("jne", input.getReg(2)));
            break;
         case "movgti":
            ret.add(new Instruction("movq", "$1", input.getReg(0)));
            ret.add(new Instruction("cmovg", input.getReg(0), input.getReg(2)));
            break;
         case "movnei":
            ret.add(new Instruction("movq", "$1", input.getReg(0)));
            ret.add(new Instruction("cmovne", input.getReg(0), input.getReg(2)));
            break;
         case "movlei":
            ret.add(new Instruction("movq", "$1", input.getReg(0)));
            ret.add(new Instruction("cmovle", input.getReg(0), input.getReg(2)));
            break;
         case "movgei":
            ret.add(new Instruction("movq", "$1", input.getReg(0)));
            ret.add(new Instruction("cmovge", input.getReg(0), input.getReg(2)));
            break;
         case "movqi":
            ret.add(new Instruction("movq", "$1", input.getReg(0)));
            ret.add(new Instruction("cmove", input.getReg(0), input.getReg(2)));
            break;
         case "xori":
            ret.add(new Instruction("xorq", "$1", input.getReg(0)));
            break;
         case "and":
            ret.add(new Instruction("mov", input.getReg(0), input.getReg(2)));
            ret.add(new Instruction("and", input.getReg(1), input.getReg(2)));
            break;
         case "or":
            ret.add(new Instruction("mov", input.getReg(0), input.getReg(2)));
            ret.add(new Instruction("or", input.getReg(1), input.getReg(2)));
            break;
         default: // for labels
            ret.add(new Instruction(input.getInst()));
      }

      return ret;
   }

   public void Convert(ArrayList<Block> blocks)
   {
      for (Block head : blocks)
      {
         for (Block b : head)
            for (Iloc i : b.getIlocs())
               b.addInstructions(ConvertInst(i));
      }
      
      for (Iloc i : header.getIlocs())
         header.addInstructions(ConvertInst(i));

      AllocateRegisters(blocks);
      blocks.add(0, header);
   }

   public void AllocateRegisters(ArrayList<Block> blocks)
   {
      for (Block b : blocks)
         AllocateFunction(b);
   }

   private void AllocateFunction(Block head)
   {
      RegisterGraph g = new RegisterGraph();
      Iterator<Block> iter = head.iterator();

      while (iter.hasNext())
         iter.next().calculateGenKillSets();

      iter = head.iterator();
      boolean unchanged = true;

      while (!unchanged)
      {
         unchanged = true;
         while (iter.hasNext())
            unchanged = unchanged && iter.next().calculateLiveOut();
      }

      iter = head.iterator();

      while (iter.hasNext())
         iter.next().calculateInterference(g);

      System.err.print(g.toString());
      HashMap<String, String> allocations = new HashMap<String, String>();
      LinkedList<String> stack = constructStack(g);
      ArrayList<String> spilled = new ArrayList<String>();

      while (!stack.isEmpty())
      {
         String register = stack.pop();
         
         if (register.charAt(0) == '%')
            allocations.put(register, register);
         else
         {
            HashSet<String> candidates = (HashSet<String>)UsableRegisters.clone();
            for (String neighbor : g.getNeighbors(register))
            {
               String used = allocations.get(neighbor);
               candidates.remove(used);
            }

            if (candidates.isEmpty()) // spill on to stack
            {
               spilled.add(register);
            }
            else
               allocations.put(register, candidates.iterator().next());
         }
      }
      
      // Now every virtual register has been given a real location
      ReplaceRegisters(head, allocations, spilled);

      // Add stack reservation code to head
      ArrayList<Instruction> insts = head.getAssembly();
      insts.add(1, new Instruction("subq", "$" + spilled.size()*8, "%rsp"));
      head.setAssembly(insts);

      for (Block b : head)
      {
         if (b.getLabel().contains(":end"))
         {
            insts = b.getAssembly();
            insts.add(insts.size() - 1, new Instruction("addq", "$" + spilled.size()*8, "%rsp"));
            b.setAssembly(insts);
         }
      }
   }

   private LinkedList<String> constructStack(RegisterGraph g)
   {
      Set<String> registers = g.getRegisters();
      LinkedList<String> stack = new LinkedList<String>();
      PriorityQueue<String> constrained = new PriorityQueue<String>(20, new RegisterComparator(g));

      for (String register : registers)
      {
         if (register.charAt(0) != '%' && g.countNeighbors(register) <= UsableRegisters.size())
            stack.push(register);
         else if (register.charAt(0) != '%')
            constrained.offer(register);
      }

      while (!constrained.isEmpty())
         stack.push(constrained.poll());

      for (String register : registers)
         if (register.charAt(0) == '%')
            stack.push(register);

      return stack;
   }

   private void ReplaceRegisters(Block head, HashMap<String, String> allocations, ArrayList<String> spilled)
   {
      for (Block b : head)
      {
         ArrayList<Instruction> insts = b.getAssembly();
         for (int i = 0; i < insts.size(); i++)
         {
            String arg1 = insts.get(i).getArg1();
            String arg2 = insts.get(i).getArg2();
            
            if (spilled.contains(arg1))
            {
               insts.get(i).setArg1("%r14");
               insts.add(i, new Instruction("movq", "%rsp", "%r14", ""+spilled.indexOf(arg1)*8, true));
               i++;
            }
            else if (allocations.containsKey(arg1))
            {
               insts.get(i).setArg1(allocations.get(arg1));
            }
            if (spilled.contains(arg2))
            {
               insts.get(i).setArg2("%r15");
               insts.add(i, new Instruction("movq", "%r15", "%rsp", ""+spilled.indexOf(arg2)*8, false));
               i++;
            }
            else if (allocations.containsKey(arg2))
            {
               insts.get(i).setArg2(allocations.get(arg2));
            }
         }
         b.setAssembly(insts);
      }
   }

   private static final String[] SET_VALUES = new String[] {"%rax", "%rbx", "%rcx", "%rdx", "%rbp", "%rsi", "%r8", "%r9", "%r10", "%r11", "%rdi", "%r12", "%r13"};
   public static final HashSet<String> UsableRegisters = new HashSet<String>(Arrays.asList(SET_VALUES));
}
