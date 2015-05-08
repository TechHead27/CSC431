public class Instruction
{
   private String inst;
   private String arg1;
   private String arg2;

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
}
