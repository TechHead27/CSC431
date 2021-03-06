import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import org.antlr.stringtemplate.*;

import java.io.*;
import java.util.Vector;
import java.util.HashMap;
import java.util.ArrayList;
import javax.json.JsonValue;
import javax.json.Json;
import java.util.Collections;

public class Compile
{
   public static void main(String[] args)
   {
      parseParameters(args);

      CommonTokenStream tokens = new CommonTokenStream(createLexer());
      MiniParser parser = new MiniParser(tokens);
      CommonTree tree = parse(parser);
      IlocToAsm converter = new IlocToAsm();

      if (_displayAST && tree != null)
      {
         DOTTreeGenerator gen = new DOTTreeGenerator();
         StringTemplate st = gen.toDOT(tree);
         System.out.println(st);
      }
      else if (!parser.hasErrors())
      {
         try
         {
            ArrayList<Block> functionBlocks = translate(tree, tokens);
            System.err.println("No type errors.");

            // Optimizations
            if (_copy)
            {
               for (Block b : functionBlocks)
               {
                  CopyPropagator.analyze(b);
                  CopyPropagator.propagateCopies(b);
               }
            }

            if (_const)
            {
               for (Block b : functionBlocks)
               {
                  ConstOpt.OptimizeConstants(b);
               }
            }

            if (_dumpIL)
            {
               for (Block b : functionBlocks)
               {
                  System.out.println(b.getGraph());
               }
            }

               converter.Convert(functionBlocks);

            if (_useless)
            {
               for (Block b : functionBlocks)
               {
                  b.RemoveUseless();
               }
            }

            if (!_debug)
               converter.AllocateRegisters(functionBlocks);

            for (Block b : functionBlocks)
            {
               ArrayList<String> asm = b.printAssembly();
               if (_useless)
               {
                  ArrayList<String> temp = reduceCode(asm, b.getLabel(), true);
                  while (asm.size() != temp.size())
                  {
                     asm = temp;
                     temp = reduceCode(asm, b.getLabel(), false);
                  }
               }
               for (int i = 0; i < asm.size(); i++)
                  System.out.println(asm.get(i));
            }
         }
         catch (SyntaxException e)
         {
            System.out.println("Syntax error: " + e.getMessage());
         }
      }
   }

   private static ArrayList<String> reduceCode(ArrayList<String> insts, String label, boolean first)
   {
      ArrayList<String> asm = new ArrayList<String>(insts);
      HashMap<String, Integer> labels = new HashMap<String, Integer>();
      ArrayList<String> usedLabels = new ArrayList<String>();
      if (!label.contains("header"))
      {
         if (first && asm.get(6).split(" ")[1].contains("$0"))
         {
            asm.remove(6);
            asm.remove(asm.size()-8);
         }
         boolean rbx, r12, r13, r14, r15; 
         rbx = r12 = r13 = r14 = r15 = true; // true means able to remove
      
         for (int i = (first ? 6 : 1); i < asm.size()-(first ? 7 : 0); i++)
         {
            String inst = asm.get(i);
            if (inst.contains("jmp"))
            {
               if (asm.get(i+1).contains(inst.split(" ")[1]))
               {
                  asm.remove(i);
                  i--;
               }
               else
               {
                  labels.remove(inst.split(" ")[1] + ":");
                  usedLabels.add(inst.split(" ")[1] + ":");
               }
            }
            else if (inst.contains("je") || inst.contains("jne"))
            {
               labels.remove(inst.split(" ")[1] + ":");
               usedLabels.add(inst.split(" ")[1] + ":");
            }
            else if (inst.contains(":"))
            {
               if (!usedLabels.contains(inst))
               {
                  labels.put(inst, i);
               }
            }
            else
            {
               if (inst.contains("%rbx"))
                  rbx = false;
               if (inst.contains("%r12"))
                  r12 = false;
               if (inst.contains("%r13"))
                  r13 = false;
               if (inst.contains("%r14"))
                  r14 = false;
               if (inst.contains("%r15"))
                  r15 = false;
            }
         }

         ArrayList<Integer> lines = new ArrayList<Integer>();
         for (String key : labels.keySet())
            lines.add(labels.get(key));

         Collections.sort(lines);
         Collections.reverse(lines);

         for (int i: lines)
         {
            asm.remove(i);
         }

         if (first)
         {
            if (r15)
            {
               asm.remove(5);
               asm.remove(asm.size()-7);
            }
            if (r14)
            {
               asm.remove(4);
               asm.remove(asm.size()-6);
            }
            if (r13)
            {
               asm.remove(3);
               asm.remove(asm.size()-5);
            }
            if (r12)
            {
               asm.remove(2);
               asm.remove(asm.size()-4);
            }
            if (rbx)
            {
               asm.remove(1);
               asm.remove(asm.size()-3);
            }
         }
      }

      return asm;
   }

   private static final String DISPLAYAST = "-displayAST";
   private static final String DUMPIL = "-dumpIL";
   private static final String COPIES = "-copy";
   private static final String USELESS = "-useless";
   private static final String DEBUG = "-debug";
   private static final String CONST = "-const";
   private static final String CLEAN = "-clean";

   private static String _inputFile = null;
   private static boolean _displayAST = false;
   private static boolean _dumpIL = false;
   private static boolean _copy = false;
   private static boolean _useless = false;
   private static boolean _debug = false;
   private static boolean _const = false;

   private static void parseParameters(String [] args)
   {
      for (int i = 0; i < args.length; i++)
      {
         if (args[i].equals(DISPLAYAST))
         {
            _displayAST = true;
         }
         else if (args[i].equals(DUMPIL))
         {
            _dumpIL = true;
         }
         else if (args[i].equals(COPIES))
         {
            _copy = true;
         }
         else if (args[i].equals(USELESS))
         {
            _useless = true;
         }
         else if (args[i].equals(DEBUG))
         {
            _debug = true;
         }
         else if (args[i].equals(CONST))
         {
            _const = true;
         }
         else if (args[i].charAt(0) == '-')
         {
            System.err.println("unexpected option: " + args[i]);
            System.exit(1);
         }
         else if (_inputFile != null)
        {
            System.err.println("too many files specified");
            System.exit(1);
         }
         else
         {
            _inputFile = args[i];
         }
      }
   }

   private static CommonTree parse(MiniParser parser)
   {
      try
      {
         MiniParser.program_return ret = parser.program();

         return (CommonTree)ret.getTree();
      }
      catch (org.antlr.runtime.RecognitionException e)
      {
         error(e.toString());
      }
      catch (Exception e)
      {
         System.exit(-1);
      }

      return null;
   }

   private static ArrayList<Block> translate(CommonTree tree, CommonTokenStream tokens) throws SyntaxException
   {
      try
      {
         CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
         nodes.setTokenStream(tokens);
         TypeCheck tparser = new TypeCheck(nodes);

         nodes = new CommonTreeNodeStream(tree);
         nodes.setTokenStream(tokens);
         BuildCFG builder = new BuildCFG(nodes);

         tparser.translate();
         return builder.translate();
      }
      catch (org.antlr.runtime.RecognitionException e)
      {
         error(e.toString());
      }
      return null;
   }

   private static void error(String msg)
   {
      System.err.println(msg);
      System.exit(1);
   }

   private static MiniLexer createLexer()
   {
      try
      {
         ANTLRInputStream input;
         if (_inputFile == null)
         {
            input = new ANTLRInputStream(System.in);
         }
         else
         {
            input = new ANTLRInputStream(
               new BufferedInputStream(new FileInputStream(_inputFile)));
         }
         return new MiniLexer(input);
      }
      catch (java.io.IOException e)
      {
         System.err.println("file not found: " + _inputFile);
         System.exit(1);
         return null;
      }
   }
}
