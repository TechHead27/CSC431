import java.util.*;

// do this optimization on iloc
public class ConstOpt
{
    private static HashMap<String, Long> constReg; // only put reg holding const in map

    public static void transform(Block head)
    {
        for (Block b : head)
        {
            constReg = new HashMap<String, Long>();
            for (Iloc iloc : b.getIlocs())
            {
                switch (iloc.getInst())
                {
                    case "ret":
                    //ret.add(new Instruction("ret"));
                    break;
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
                    case "call":
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
                    // ret.add(new Instruction("cmp", iloc.getReg(1), iloc.getReg(0)));
                        break;
                    case "cbreq":
                    /*ret.add(new Instruction("je", iloc.getReg(1)));
                    ret.add(new Instruction("jne", iloc.getReg(2)));
                    */break;
                    case "movlti":
                        constReg.remove(iloc.getReg(2));
                    /*ret.add(new Instruction("movq", "$" + iloc.getReg(1), iloc.getReg(0)));
                    ret.add(new Instruction("cmovl", iloc.getReg(0), iloc.getReg(2)));
                    */break;
                    case "brz":
                    /*ret.add(new Instruction("cmp", "$0", iloc.getReg(0)));
                    ret.add(new Instruction("je", iloc.getReg(1)));
                    ret.add(new Instruction("jne", iloc.getReg(2)));
                    */break;
                    case "movgti":
                        constReg.remove(iloc.getReg(2));
                    /*ret.add(new Instruction("movq", "$" + iloc.getReg(1), iloc.getReg(0)));
                    ret.add(new Instruction("cmovg", iloc.getReg(0), iloc.getReg(2)));
                    */break;
                    case "movnei":
                        constReg.remove(iloc.getReg(2));
                    /*ret.add(new Instruction("movq", "$" + iloc.getReg(1), iloc.getReg(0)));
                    ret.add(new Instruction("cmovne", iloc.getReg(0), iloc.getReg(2)));
                    */break;
                    case "movlei":
                        constReg.remove(iloc.getReg(2));
                    /*ret.add(new Instruction("movq", "$" + iloc.getReg(1), iloc.getReg(0)));
                    ret.add(new Instruction("cmovle", iloc.getReg(0), iloc.getReg(2)));
                    */break;
                    case "movgei":
                        constReg.remove(iloc.getReg(2));
                    /*ret.add(new Instruction("movq", "$" + iloc.getReg(1), iloc.getReg(0)));
                    ret.add(new Instruction("cmovge", iloc.getReg(0), iloc.getReg(2)));
                    */break;
                    case "moveqi":
                        constReg.remove(iloc.getReg(2));
                    /*ret.add(new Instruction("movq", "$" + iloc.getReg(1), iloc.getReg(0)));
                    ret.add(new Instruction("cmove", iloc.getReg(0), iloc.getReg(2)));
                    */break;
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
                    /*ret.add(new Instruction("mov", iloc.getReg(0), iloc.getReg(2)));
                    ret.add(new Instruction("and", iloc.getReg(1), iloc.getReg(2)));
                    */break;
                    case "or":
                        if (constReg.containsKey(iloc.getReg(0)) && constReg.containsKey(iloc.getReg(1)))
                        {
                            constReg.put(iloc.getReg(2), constReg.get(iloc.getReg(0)) | constReg.get(iloc.getReg(1)));
                            iloc.swapIloc("loadi", "" +constReg.get(iloc.getReg(2)), iloc.getReg(2));
                        }
                        else
                            constReg.remove(iloc.getReg(2));
                    /*ret.add(new Instruction("mov", iloc.getReg(0), iloc.getReg(2)));
                    ret.add(new Instruction("or", iloc.getReg(1), iloc.getReg(2)));
                    */break;
                    default: // for labels
                    // ret.add(new Instruction(iloc.getInst()));
                }
            }
        }
    }
}