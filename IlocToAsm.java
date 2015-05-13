import java.util.ArrayList;

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
            ret.add(new Instruction("movq", "print", "%rdi"));
            ret.add(new Instruction("movq", input.getReg(0), "%rsi"));
            ret.add(new Instruction("call", "printf"));
            ret.add(new Instruction("movq", "$0", "%rax"));
            break;
         case "read":
            ret.add(new Instruction("movq", "scan", "%rdi"));
            ret.add(new Instruction("lea", "scanVar", "%rsi"));
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
            break;
         case "addi":
            ret.add(new Instruction("movq", input.getReg(0), input.getReg(2)));
            ret.add(new Instruction("addq", "$" + input.getReg(1), input.getReg(2)));
            break;
         case "mov":
            ret.add(new Instruction("movq", input.getReg(0), input.getReg(1)));
            break;
         case "storeai":
            // need to replace field name with offset
            ret.add(new Instruction("movq", input.getReg(0), input.getReg(2) + "(" + input.getReg(1) + ")"));
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
            ret.add(new Instruction("movq", input.getReg(0), "%rdi"));
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
            ret.add(new Instruction("movq", input.getReg(1) + "(" + input.getReg(0) + ")", input.getReg(2)));
            break;
         case "comp":
            ret.add(new Instruction("cmp", input.getReg(0), input.getReg(1)));
            break;
         case "cbreq":
            ret.add(new Instruction("je", input.getReg(1)));
            ret.add(new Instruction("jne", input.getReg(2)));
            break;
         case "movlti":
            ret.add(new Instruction("push", "%rdi"));
            ret.add(new Instruction("movq", "$1", "%rdi"));
            ret.add(new Instruction("cmovl", "%rdi", input.getReg(2)));
            ret.add(new Instruction("pop", "%rdi"));
            break;
         case "brz":
            ret.add(new Instruction("cmp", "$0", input.getReg(0)));
            ret.add(new Instruction("je", input.getReg(1)));
            ret.add(new Instruction("jne", input.getReg(2)));
            break;
         case "movgti":
            ret.add(new Instruction("push", "%rdi"));
            ret.add(new Instruction("movq", "$1", "%rdi"));
            ret.add(new Instruction("cmovg", "%rdi", input.getReg(2)));
            ret.add(new Instruction("pop", "%rdi"));
            break;
         case "movnei":
            ret.add(new Instruction("push", "%rdi"));
            ret.add(new Instruction("movq", "$1", "%rdi"));
            ret.add(new Instruction("cmovne", "%rdi", input.getReg(2)));
            ret.add(new Instruction("pop", "%rdi"));
            break;
         case "movlei":
            ret.add(new Instruction("push", "%rdi"));
            ret.add(new Instruction("movq", "$1", "%rdi"));
            ret.add(new Instruction("cmovle", "%rdi", input.getReg(2)));
            ret.add(new Instruction("pop", "%rdi"));
            break;
         case "movgei":
            ret.add(new Instruction("push", "%rdi"));
            ret.add(new Instruction("movq", "$1", "%rdi"));
            ret.add(new Instruction("cmovge", "%rdi", input.getReg(2)));
            ret.add(new Instruction("pop", "%rdi"));
            break;
         case "movqi":
            ret.add(new Instruction("push", "%rdi"));
            ret.add(new Instruction("movq", "$1", "%rdi"));
            ret.add(new Instruction("cmove", "%rdi", input.getReg(2)));
            ret.add(new Instruction("pop", "%rdi"));
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
      for (Block b : blocks)
         for (Iloc i : b.getIlocs())
            b.addInstructions(ConvertInst(i));
      
      for (Iloc i : header.getIlocs())
         header.addInstructions(ConvertInst(i));
   }
}
