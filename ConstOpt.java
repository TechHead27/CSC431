import java.util.*;

// do this optimization on iloc
public class ConstOpt
{
    public static void AddMapping(HashMap<String, Long> constReg, Iloc iloc)
    {
       switch (iloc.getInst())
       {
           case "storeret":
               if (constReg.containsKey(iloc.getReg(0)))
               {
                   constReg.put("%rax", constReg.get(iloc.getReg(0)));
               }
               else
                   constReg.remove("%rax");
               break;
           case "read":
               constReg.remove(iloc.getReg(0));
               break;
           case "add":
               if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) + constReg.get(iloc.getReg(1)));
               }
               else
                   constReg.remove(iloc.getReg(2));
               break;
           case "addi":
               if (constReg.containsKey(iloc.getReg(0)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) + Long.parseLong(iloc.getReg(1)));
               }
               else
                   constReg.remove(iloc.getReg(2));
               break;
           case "mov":
               if (constReg.containsKey(iloc.getReg(0)))
               {
                   constReg.put(iloc.getReg(1), constReg.get(iloc.getReg(0)));
               }
               else
                   constReg.remove(iloc.getReg(1));
               break;
           case "loadi":
               constReg.put(iloc.getReg(1), Long.parseLong(iloc.getReg(0)));
               break;
           case "loadret":
               if (constReg.containsKey("%rax"))
               {
                   constReg.put(iloc.getReg(0), constReg.get("%rax"));
               }
               else
                   constReg.remove(iloc.getReg(0));
               break;
           case "loadglobal":
               constReg.remove(iloc.getReg(0));
               break;
           case "new":
               constReg.remove(iloc.getReg(1));
               break;
           case "sub":
               if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) - constReg.get(iloc.getReg(1)));
               }
               else
                   constReg.remove(iloc.getReg(2));
               break;
           case "mult":
               if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) * constReg.get(iloc.getReg(1)));
               }
               else
                   constReg.remove(iloc.getReg(2));
               break;
           case "div":
               if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) / constReg.get(iloc.getReg(1)));
               }
               else
                   constReg.remove(iloc.getReg(2));
               break;
           case "loadinargument":
               constReg.remove(iloc.getReg(2));
               break;
           case "loadai":
               constReg.remove(iloc.getReg(2));
               break;
           case "movlti":
               constReg.remove(iloc.getReg(2));
              break;
           case "movgti":
               constReg.remove(iloc.getReg(2));
              break;
           case "movnei":
               constReg.remove(iloc.getReg(2));
              break;
           case "movlei":
               constReg.remove(iloc.getReg(2));
              break;
           case "movgei":
               constReg.remove(iloc.getReg(2));
              break;
           case "moveqi":
               constReg.remove(iloc.getReg(2));
              break;
           case "xori":
               if (constReg.containsKey(iloc.getReg(0)))
               {
                   constReg.put(iloc.getReg(0), constReg.get(iloc.getReg(0)) ^ 1);
               }
               break;
           case "and":
               if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) & constReg.get(iloc.getReg(1)));
               }
               else
                   constReg.remove(iloc.getReg(2));
              break;
           case "or":
               if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) | constReg.get(iloc.getReg(1)));
               }
               else
                   constReg.remove(iloc.getReg(2));
              break;
           default:
              break;
       }
    }

    public static void replaceConstant(HashMap<String, Long> constReg, Iloc iloc)
    {
       switch (iloc.getInst())
       {
           case "storeret":
               if (constReg.containsKey(iloc.getReg(0)))
               {
                   constReg.put("%rax", constReg.get(iloc.getReg(0)));
                   iloc.setArg(0, "$"+constReg.get(iloc.getReg(0)));
               }
               else
                   constReg.remove("%rax");
               break;
           case "print":
               if (constReg.containsKey(iloc.getReg(0)))
                   iloc.setArg(0, "$" + constReg.get(iloc.getReg(0)));
               break;
           case "read":
               constReg.remove(iloc.getReg(0));
               break;
           case "add":
               if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) + constReg.get(iloc.getReg(1)));
                   iloc.swapIloc("loadi", "" + constReg.get(iloc.getReg(2)), iloc.getReg(2));
               }
               else
                   constReg.remove(iloc.getReg(2));
               break;
           case "addi":
               if (constReg.containsKey(iloc.getReg(0)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) + Long.parseLong(iloc.getReg(1)));
                   iloc.swapIloc("loadi", "" +constReg.get(iloc.getReg(2)), iloc.getReg(2));
               }
               else
                   constReg.remove(iloc.getReg(2));
               break;
           case "mov":
               if (constReg.containsKey(iloc.getReg(0)))
               {
                   constReg.put(iloc.getReg(1), constReg.get(iloc.getReg(0)));
                   iloc.swapIloc("loadi", "" + constReg.get(iloc.getReg(1)), iloc.getReg(1));
               }
               else
                   constReg.remove(iloc.getReg(1));
               break;
           case "storeai":
               if (constReg.containsKey(iloc.getReg(0)))
                   iloc.setArg(0, "$" + constReg.get(iloc.getReg(0)));
               break;
           case "storeglobal":
               if (constReg.containsKey(iloc.getReg(0)))
                   iloc.setArg(0, "$" + constReg.get(iloc.getReg(0)));
               break;
           case "loadi":
               constReg.put(iloc.getReg(1), Long.parseLong(iloc.getReg(0)));
               break;
           case "storeoutargument":
               if (constReg.containsKey(iloc.getReg(0)))
                   iloc.setArg(0, "$" + constReg.get(iloc.getReg(0)));
               break;
           case "loadret":
               if (constReg.containsKey("%rax"))
               {
                   constReg.put(iloc.getReg(0), constReg.get("%rax"));
                   iloc.swapIloc("loadi", "" + constReg.get("%rax"), iloc.getReg(0));
               }
               else
                   constReg.remove(iloc.getReg(0));
               break;
           case "loadglobal":
               constReg.remove(iloc.getReg(0));
               break;
           case "new":
               constReg.remove(iloc.getReg(1));
               break;
           case "del":
               break;
           case "sub":
               if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) - constReg.get(iloc.getReg(1)));
                   iloc.swapIloc("loadi", "" +constReg.get(iloc.getReg(2)), iloc.getReg(2));
               }
               else
                   constReg.remove(iloc.getReg(2));
               break;
           case "mult":
               if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) * constReg.get(iloc.getReg(1)));
                   iloc.swapIloc("loadi", "" +constReg.get(iloc.getReg(2)), iloc.getReg(2));
               }
               else
                   constReg.remove(iloc.getReg(2));
               break;
           case "div":
               if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) / constReg.get(iloc.getReg(1)));
                   iloc.swapIloc("loadi", "" +constReg.get(iloc.getReg(2)), iloc.getReg(2));
               }
               else
                   constReg.remove(iloc.getReg(2));
               break;
           case "loadinargument":
               constReg.remove(iloc.getReg(2));
               break;
           case "loadai":
               constReg.remove(iloc.getReg(2));
               break;
           case "movlti":
               constReg.remove(iloc.getReg(2));
              break;
           case "movgti":
               constReg.remove(iloc.getReg(2));
              break;
           case "movnei":
               constReg.remove(iloc.getReg(2));
              break;
           case "movlei":
               constReg.remove(iloc.getReg(2));
              break;
           case "movgei":
               constReg.remove(iloc.getReg(2));
              break;
           case "moveqi":
               constReg.remove(iloc.getReg(2));
              break;
           case "xori":
               if (constReg.containsKey(iloc.getReg(0)))
               {
                   constReg.put(iloc.getReg(0), constReg.get(iloc.getReg(0)) ^ 1);
                   iloc.swapIloc("loadi", "" +constReg.get(iloc.getReg(0)), iloc.getReg(0));
               }
               break;
           case "and":
               if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) & constReg.get(iloc.getReg(1)));
                   iloc.swapIloc("loadi", "" +constReg.get(iloc.getReg(2)), iloc.getReg(2));
               }
               else
                   constReg.remove(iloc.getReg(2));
              break;
           case "or":
               if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
               {
                   constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) | constReg.get(iloc.getReg(1)));
                   iloc.swapIloc("loadi", "" +constReg.get(iloc.getReg(2)), iloc.getReg(2));
               }
               else
                   constReg.remove(iloc.getReg(2));
              break;
           default: // for labels
              break;
       }
    }

    public static void OptimizeConstants(Block head)
    {
       for (Block b : head)
          b.calculateConstantOut();

       boolean unchanged = false;
       while (!unchanged)
       {
          for (Block b : head)
          {
             unchanged = true;
             unchanged = unchanged && b.calculateConstantIn();
          }
       }

       for (Block b : head)
          b.propagateConstants();
    }
}
