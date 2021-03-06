import java.util.ArrayList;

public class Instruction
{
   private String inst;
   private String arg1;
   private String arg2;
   private String offset = "-1";
   private boolean first;

   public Instruction(String inst, String arg1, String arg2, String offset, boolean first)
   {
      this.inst = inst;
      this.arg1 = arg1;
      this.arg2 = arg2;
      this.offset = offset;
      this.first = first;
   }
   public Instruction(String inst, String arg1, String arg2)
   {
      this.inst = inst;
      this.arg1 = arg1;
      this.arg2 = arg2;
   }

   public Instruction(String inst, String arg1)
   {
      this.inst = inst;
      this.arg1 = arg1;
      arg2 = "";
   }

   public Instruction(String inst)
   {
      this.inst = inst;
      arg1 = arg2 = "";
   }

   public String getInst()
   {
      return inst;
   }

   public String arg1()
   {
      return arg1;
   }

   public String arg2()
   {
      return arg2;
   }

   public boolean uselessMove()
   {
      if (arg1.equals(arg2) && offset.equals("-1") && inst.equals("movq"))
        return true;
      return false;
   }

   public ArrayList<String> getSource()
   {
      ArrayList<String> ret = new ArrayList<String>();

      switch (inst)
      {
        case "call":
           break;

        case "ret":
        case "je":
        case "jne":
        case "jmp":
           break;

        case "cmp":
           if (arg1.charAt(0) != '$')
              ret.add(arg1);
           if (arg2.charAt(0) != '$')
              ret.add(arg2);
           break;

        case "cmovl":
        case "cmovle":
        case "cmovg":
        case "cmovge":
        case "cmove":
        case "cmovne":
        case "addq":
        case "subq":
        case "imulq":
        case "or":
        case "and":
           ret.add(arg1);
           ret.add(arg2);
           break;

         case "movq":
           if (!offset.equals("-1") && !first) {
             if (arg1.charAt(0) != '$')
               ret.add(arg1);
             ret.add(arg2);
             break;
           }

        default:
           if (!arg1.isEmpty() && arg1.charAt(0) != '$' && !arg1.equals("$scan") && !arg1.contains("scanVar") && !arg1.equals("$print"))
              ret.add(arg1);
      }

      return ret;
   }

   public ArrayList<String> getTarget()
   {
      ArrayList<String> ret = new ArrayList<String>();

      switch (inst)
      {
        case "call":
           ret.add("%rax");
           ret.add("%rdx");
           ret.add("%rcx");
           ret.add("%rsi");
           ret.add("%rdi");
           ret.add("%r8");
           ret.add("%r9");
           ret.add("%r10");
           ret.add("%r11");
           break;

        case "ret":
        case "je":
        case "jne":
        case "jmp":
        case "cmp":
           break;

        case "idivq":
           ret.add("%rax");
           ret.add("%rdx");
           break;

        case "neg":
           ret.add(arg1);
           break;

        case "movq":
           if (!offset.equals("-1") && !first) {
             break;
           }

        default:
           if (!arg2.isEmpty() && !arg2.equals("$scan") && !arg2.contains("scanVar") && !arg2.equals("$print"))
              ret.add(arg2);
      }

      return ret;
   }

   public void setArg1(String source)
   {
      arg1 = source;
   }

   public void setArg2(String target)
   {
      arg2 = target;
   }

   public String getArg1()
   {
      return arg1;
   }

   public String getArg2()
   {
      return arg2;
   }

   public boolean KeepInstruction()
   {
      switch (inst)
      {
         case "jmp":
         case "call":
         case "ret":
         case "cmp":
         case "je":
         case "jne":
            return true;

         default:
            for (String target : getTarget())
               if (target.contains("%"))
                  return true;
            return inst.contains(":") || !offset.equals("-1");
      }
   }

   @Override
   public String toString()
   {
      String ret = inst;
      if (offset.equals("-1"))
      {
         if (!arg1.isEmpty())
            ret += " " + arg1;
         if (!arg2.isEmpty())
            ret += ", " + arg2;
      }
      else
      {
         if (first)
            ret += " " + (offset.equals("0") ? "" : offset) +"(" + arg1 + "), " + arg2;
         else
            ret += " " + arg1 + ", " + (offset.equals("0") ? "" : offset) + "(" + arg2 + ")";
      }
      return ret;
   }
}
