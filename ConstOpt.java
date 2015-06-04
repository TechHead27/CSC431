import java.util.*;

// do this optimization on iloc
public class ConstOpt
{
    public static void AddMapping(HashMap<String, Long> constReg, Iloc iloc, Iloc prev)
    {
        Long left, right;
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
                if (constReg.containsKey(prev.getReg(0)) && constReg.containsKey(prev.getReg(1)))
                    if (constReg.get(prev.getReg(0)) < constReg.get(prev.getReg(1)))
                        constReg.put(iloc.getReg(2), Long.parseLong(iloc.getReg(1)));
                else
                    constReg.remove(iloc.getReg(2));
                break;
            case "movgti":
                if (constReg.containsKey(prev.getReg(0)) && constReg.containsKey(prev.getReg(1)))
                    if (constReg.get(prev.getReg(0)) > constReg.get(prev.getReg(1)))
                        constReg.put(iloc.getReg(2), Long.parseLong(iloc.getReg(1)));
                else
                    constReg.remove(iloc.getReg(2));
                break;
            case "movnei":
                if (constReg.containsKey(prev.getReg(0)) && constReg.containsKey(prev.getReg(1)))
                    if (constReg.get(prev.getReg(0)) != constReg.get(prev.getReg(1)))
                        constReg.put(iloc.getReg(2), Long.parseLong(iloc.getReg(1)));
                else
                    constReg.remove(iloc.getReg(2));
                break;
            case "movlei":
                if (constReg.containsKey(prev.getReg(0)) && constReg.containsKey(prev.getReg(1)))
                    if (constReg.get(prev.getReg(0)) <= constReg.get(prev.getReg(1)))
                        constReg.put(iloc.getReg(2), Long.parseLong(iloc.getReg(1)));
                else
                    constReg.remove(iloc.getReg(2));
                break;
            case "movgei":
                if (constReg.containsKey(prev.getReg(0)) && constReg.containsKey(prev.getReg(1)))
                    if (constReg.get(prev.getReg(0)) >= constReg.get(prev.getReg(1)))
                        constReg.put(iloc.getReg(2), Long.parseLong(iloc.getReg(1)));
                else
                    constReg.remove(iloc.getReg(2));
                break;
            case "moveqi":
                if (constReg.containsKey(prev.getReg(0)) && constReg.containsKey(prev.getReg(1)))
                    if (constReg.get(prev.getReg(0)) == constReg.get(prev.getReg(1)))
                        constReg.put(iloc.getReg(2), Long.parseLong(iloc.getReg(1)));
                else
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

    public static int replaceConstant(HashMap<String, Long> constReg, Iloc iloc, ArrayList<Iloc> ilocs, String blockLabel)
    {
        int skip = 0;
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
            case "comp":
                long flag = -1;
                int ndx = ilocs.indexOf(iloc);
                Iloc next = ilocs.get(ndx+1);
                boolean cont = true;
                if (ndx + 2 < ilocs.size())
                    cont = !ilocs.get(ndx+2).getInst().equals("brz");
                cont = cont || blockLabel.contains("whiletest"); 
                // either in whiletest or brz is not 2 away from comp
                if (cont)
                {
                    if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
                    {
                        long left = constReg.get(iloc.getReg(0)), right = constReg.get(iloc.getReg(1));
                        if (left == right)
                            flag = 1;
                        else if (left < right)
                            flag = 2;
                        else if (left > right)
                            flag = 3;
                        ilocs.remove(iloc);
                        skip++;
                     
                        switch (next.getInst())
                        {
                            case "cbreq":
                                if (flag == 1)
                                    next.swapIloc("jumpi", next.getReg(1));
                                else
                                    next.swapIloc("jumpi", next.getReg(2));
                                break;
                            case "movlti":
                                if (flag == 2)
                                {
                                    constReg.put(next.getReg(2), Long.parseLong(next.getReg(1)));
                                    next.swapIloc("loadi", next.getReg(1), next.getReg(2));
                                }
                                else
                                    ilocs.remove(next);  
                                break;
                            case "movgti":
                                if (flag == 3)
                                {
                                    constReg.put(next.getReg(2), Long.parseLong(next.getReg(1)));
                                    next.swapIloc("loadi", next.getReg(1), next.getReg(2));
                                }
                                else
                                    ilocs.remove(next);  
                                break;
                            case "movnei":
                                if (flag == 2 || flag == 3)
                                {
                                    constReg.put(next.getReg(2), Long.parseLong(next.getReg(1)));
                                    next.swapIloc("loadi", next.getReg(1), next.getReg(2));
                                }
                                else
                                    ilocs.remove(next);  
                                break;
                            case "movlei":
                                if (flag == 2 || flag == 1)
                                {
                                    constReg.put(next.getReg(2), Long.parseLong(next.getReg(1)));
                                    next.swapIloc("loadi", next.getReg(1), next.getReg(2));
                                }
                                else
                                    ilocs.remove(next);  
                                break;
                            case "movgei":
                                if (flag == 3 || flag == 1)
                                {
                                    constReg.put(next.getReg(2), Long.parseLong(next.getReg(1)));
                                    next.swapIloc("loadi", next.getReg(1), next.getReg(2));
                                }
                                else
                                    ilocs.remove(next);  
                                break;
                            case "moveqi":
                                if (flag == 1)
                                {
                                    constReg.put(next.getReg(2), Long.parseLong(next.getReg(1)));
                                    next.swapIloc("loadi", next.getReg(1), next.getReg(2));
                                }
                                else
                                    ilocs.remove(next);  
                                break;
                        }
                    }
                }
                break;
            case "brz":
                if (blockLabel.contains("whiletest"))
                {
                    if (constReg.containsKey(iloc.getReg(0)))
                    {
                        if (constReg.get(iloc.getReg(0)) == 0)
                            iloc.swapIloc("jumpi", iloc.getReg(1));
                        else
                            iloc.swapIloc("jumpi", iloc.getReg(2));
                    }
                }
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
        return skip;
    }

    public static void OptimizeConstants(Block head)
    {
        for (Block b : head)
        {
            b.calculateConstantIn();
            b.calculateConstantOut();
        }

        for (Block b : head)
            b.propagateConstants();

        for (Block b : head)
        {
            System.err.println(b.getLabel());
            System.err.println(b.printIloc());
            String constin = "";
            String constout = "";
            for (String n : b.getConstIn())
                constin += " " + n;
            for (String n : b.getConstOut())
                constout += " " + n;
            System.err.println("constIn: " + constin);
            System.err.println("constOut: " + constout);
            System.err.println("\n==============================");
        }
    }
}
