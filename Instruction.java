public class Instruction
{
   private String inst;
   private String arg1;
   private String arg2;
   private int offset = 0;
   private boolean first;

   public Instruction(String inst, String arg1, String arg2, String offset, boolean first)
   {
      this.inst = inst;
      this.arg1 = arg1;
      this.arg2 = arg2;
      this.offset = Integer.parseInt(offset);
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

   @Override
   public String toString()
   {
      String ret = inst;
      if (offset == 0)
      {
         if (!arg1.isEmpty())
            ret += " " + arg1;
         if (!arg2.isEmpty())
            ret += ", " + arg2;
      }
      else
      {
         if (first)
            ret += " " + offset +"(" + arg1 + "), " + arg2;
         else
            ret += " " + arg1 + ", " + offset + "(" + arg2 + ")";
      }
      return ret;
   }
}
