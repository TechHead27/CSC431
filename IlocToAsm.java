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

   private String ConvertInst(Iloc input)
   {
      String ret;
      switch (input.getInst())
      {
         case "ret":
            return "ret";
         case "storeret":
            return "movq " + input.getReg(0) + ", %rax";
         case "print":
            ret = "movq print, " + "%rdi\n";
            ret += "movq " + input.getReg(0) + "%rsi\n";
            ret += "call printf\n";
            ret += "movq $0, $rax";
            return ret;
         case "read":
            ret = "movq scan, " + "%rdi\n";
            ret += "lea scanVar, %rsi";
            ret += "call scanf\n";
            ret += "movq scanVar, " + input.getReg(0);
            return ret;
         case "jumpi":
            return "jmp " + input.getReg(0);
         case "neg":
            return "neg " + input.getReg(0);
         case "add":
            ret = "movq " + input.getReg(0) + ", " + input.getReg(2);
            ret += "\naddq " + input.getReg(1) + ", " + input.getReg(2);
            return ret;
         case "addi":
            ret = "movq " + input.getReg(0) + ", " + input.getReg(2);
            ret += "\naddq $" + input.getReg(1) + ", " + input.getReg(2);
            return ret;
         case "mov":
            return "movq " + input.getReg(0) + ", " + input.getReg(1);
         case "storeai":
            // need to replace field name with offset
            return "movq " + input.getReg(0) + ", " + input.getReg(2) + "(" + input.getReg(1) + ")";
         case "storeglobal":
            if (!globals.contains(input.getReg(1)))
            {
               header.addIloc(new Iloc(".comm " + input.getReg(1) + ",8,8"));
               globals.add(input.getReg(1));
            }
            return "movq " + input.getReg(0) + ", " + input.getReg(1) + "(%rip)";
         case "loadi":
            return "movq $" + input.getReg(0) + ", " + input.getReg(1);
         case "storeoutargument":
            ret = "movq " + input.getReg(0) + ", ";
            switch (Integer.parseInt(input.getReg(1)))
            {
               case 0:
                  ret += "%rdi";
                  break;
               case 1:
                  ret += "%rsi";
                  break;
               case 2:
                  ret += "%rdx";
                  break;
               case 3:
                  ret += "%rcx";
                  break;
               case 4:
                  ret += "%r8";
                  break;
               case 5:
                  ret += "%r9";
                  break;
               default:
                  ret += "stack";
            }
            return ret;
         case "call":
            return "call " + input.getReg(0);
         case "loadglobal":
            return "movq " + input.getReg(0) + "(%rip) " + input.getReg(1);
         case "new":
            ret = "movq " + input.getReg(0) + ", " + "%rdi\n";
            ret += "call malloc\n";
            ret += "movq %rax, " + input.getReg(1);
            return ret;
         case "del":
            ret = "movq " + input.getReg(0) + ", " + "%rdi\n";
            ret += "call free";
            return ret;
         case "sub":
            ret = "movq " + input.getReg(0) + ", " + input.getReg(2);
            ret += "\nsubq " + input.getReg(1) + ", " + input.getReg(2);
            return ret;
         case "mult":
            ret = "movq " + input.getReg(0) + ", " + input.getReg(2);
            ret += "\nimulq " + input.getReg(1) + ", " + input.getReg(2);
            return ret;
         case "div":
            ret = "movq " + input.getReg(0) + ", " + "%rax";
            ret += "\nmovq %rax, %rdx";
            ret += "\nsarq $63, %rdx";
            ret += "\nidivq " + input.getReg(1);
            ret += "\nmov %rax, " + input.getReg(2);
            return ret;
         case "loadinargument":
            switch (input.getReg(1))
            {
               case "0":
                  ret = "mov %rdi, ";
                  break;
               case "1":
                  ret = "mov %rsi, ";
                  break;
               case "2":
                  ret = "mov %rdx, ";
                  break;
               case "3":
                  ret = "mov %rcx, ";
                  break;
               case "4":
                  ret = "mov %r8, ";
                  break;
               case "5":
                  ret = "mov %r9, ";
                  break;
               default:
                  ret = "stack";
            }
            ret += input.getReg(2);
            return ret;
         case "loadai":
            return "movq " + input.getReg(1) + "(" + input.getReg(0) + ") " + input.getReg(2);
         case "comp":
            return "cmp " + input.getReg(0) + ", " + input.getReg(1);
         case "cbreq":
            ret = "je " + input.getReg(1);
            ret += "\njne " + input.getReg(2);
            return ret;
         case "movlti":
            ret = "push %rdi";
            ret += "\nmovq $1, %rdi";
            ret += "\ncmovl %rdi, " + input.getReg(2);
            ret += "\npop %rdi";
            return ret;
         case "brz":
            ret = "cmp $0, " + input.getReg(0);
            ret += "\nje " + input.getReg(1);
            ret += "\njne " + input.getReg(2);
            return ret;
         case "movgti":
            ret = "push %rdi";
            ret += "\nmovq $1, %rdi";
            ret += "\ncmovg %rdi, " + input.getReg(2);
            ret += "\npop %rdi";
            return ret;
         case "movnei":
            ret = "push %rdi";
            ret += "\nmovq $1, %rdi";
            ret += "\ncmovne %rdi, " + input.getReg(2);
            ret += "\npop %rdi";
            return ret;
         case "movlei":
            ret = "push %rdi";
            ret += "\nmovq $1, %rdi";
            ret += "\ncmovle %rdi, " + input.getReg(2);
            ret += "\npop %rdi";
            return ret;
         case "movgei":
            ret = "push %rdi";
            ret += "\nmovq $1, %rdi";
            ret += "\ncmovge %rdi, " + input.getReg(2);
            ret += "\npop %rdi";
            return ret;
         case "movqi":
            ret = "push %rdi";
            ret += "\nmovq $1, %rdi";
            ret += "\ncmove %rdi, " + input.getReg(2);
            ret += "\npop %rdi";
            return ret;
         case "xori":
            return "xorq $1, " + input.getReg(0);
         case "and":
            ret = "mov " + input.getReg(0) + ", " + input.getReg(2);
            ret += "\nand " + input.getReg(1) + ", " + input.getReg(2);
            return ret;
         case "or":
            ret = "mov " + input.getReg(0) + ", " + input.getReg(2);
            ret += "\nor " + input.getReg(1) + ", " + input.getReg(2);
            return ret;
         default: // for labels
            return input.getInst();
      }
   }

   public String Convert(ArrayList<Block> blocks)
   {
      StringBuilder ret = new StringBuilder(200);
      ArrayList<Iloc> ilocs;
      int index = 0;

      for (Block b : blocks)
      {
         ilocs = b.getIlocs();
         for (Iloc i : ilocs)
         {
            ret.append(ConvertInst(i) + "\n");
         }
         ret.append("\n");
      }
      
      ilocs = header.getIlocs();
      String headerSt = "";
      for (Iloc i : ilocs)
      {
         headerSt += ConvertInst(i) + "\n";
      }

      return headerSt + "\n" + ret.toString();
   }
}
