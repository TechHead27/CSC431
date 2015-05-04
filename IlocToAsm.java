import java.util.ArrayList;

public class IlocToAsm
{
   private ArrayList<String> globals;
   private Block header;

   public IlocToAsm()
   {
      globals = new ArrayList<String>();
      header = new Block();
   }

   private String ConvertInst(Iloc input)
   {
      String ret;
      switch (input.getInst())
      {
         case "ret":
            return "ret";
         case "storeret":
            return "mov " + input.getReg(0) + "%rax";
         case "add":
            ret = "movq " + input.getReg(0) + " " + input.getReg(2);
            ret += "\naddq " + input.getReg(1) + " " + input.getReg(2);
            return ret;
         case "addi":
            ret = "movq " + input.getReg(0) + " " + input.getReg(2);
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
      }
      
      ilocs = header.getIlocs();
      for (Iloc i : ilocs)
      {
         ret.insert(index++, ConvertInst(i));
      }
      return ret.toString();
   }
}
