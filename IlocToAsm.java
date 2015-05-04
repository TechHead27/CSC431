public class IlocToAsm
{
   private ArrayList<String> globals;
   private Block header;

   private String ConvertInst(Iloc input)
   {
      switch (input.getInst())
      {
         case "ret":
            return "ret";
         case "storeret":
            return "mov " + input.getReg(0) + "%rax";
         case "add":
            String ret = "movq " + input.getReg(0) + " " + input.getReg(2);
            ret += "\naddq " + input.getReg(1) + " " + input.getReg(2);
            return ret;
         case "addi":
            String ret = "movq " + input.getReg(0) + " " + input.getReg(2);
            ret += "\naddq $" + input.getReg(1) + " " + input.getReg(2);
            return ret;
         case "mov":
            return "mov " + input.getReg(0) + " " + input.getReg(1);
         case "storeai":
            // need to replace field name with offset
            return "mov " + input.getReg(0) + " " + input.getReg(2) + "(" + input.getReg(1) + ")";
         case "storeglobal":
            if (!globals.contains(input.getReg(1)))
            {
               header.addIloc(new Iloc(".comm " + input.getReg(1) + ",8,8"));
               globals.add(input.getReg(1));
            }
            return "mov " + input.getReg(0) + " " + input.getReg(1) + "(%rip)";
         case "loadi":
            return "mov $" + input.getReg(0) + " " + input.getReg(1);
      }
   }


}
