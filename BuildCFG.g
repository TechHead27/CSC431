tree grammar BuildCFG;

options
{
   tokenVocab=Mini;
   ASTLabelType=CommonTree;
}

/*
   Tree Parser -- typechecks given program
*/
@header
{
   import java.util.ArrayList;
   import java.util.HashMap;
}

@members
{
   private HashMap<String, Type> structs = new HashMap<String, Type>();
   private HashMap<String, Type> sTable = new HashMap<String, Type>();
   ArrayList<Block> functionStarts = new ArrayList<Block>();
   int lastLabel = 0;
}

translate
   returns [ArrayList<Block> blocks = null;]
   :  ^(PROGRAM t=types d=declarations f=functions)
      {
         blocks = functionStarts;
      }
   ;

types
   :  ^(TYPES (t=type_decl)*)
   ;

type_decl
   :  ^(ast=STRUCT id=ID {structs.put($id.text, new RecType());} n=nested_decl)
      {
         structs.put($id.text, new StructType($n.hash, $n.fields));
      }
   ;

nested_decl
   returns [HashMap<String, Type> hash, ArrayList<String> fields]
   @init {$hash = new HashMap<String, Type>(); $fields = new ArrayList<String>();}
   :  (f=field_decl { $hash.put($f.name, $f.fieldType); $fields.add($f.name); })+
   ;

field_decl
   returns [String name, Type fieldType]
   :  ^(DECL ^(TYPE t=type) id=ID)
      {
         $name = $id.text;
         $fieldType = $t.typeName;
      }
   ;

type
   returns [Type typeName = null;]
   :  INT { $typeName = new IntType(); }
   |  BOOL { $typeName = new BoolType(); }
   |  ^(STRUCT id=ID) { $typeName = structs.get($id.text); }
   ;

declarations
   :  ^(DECLS (decl_list)*)
   ;

decl_list
   :  ^(DECLLIST ^(TYPE t=type) (id=ID {sTable.put($id.text, $t.typeName);})+)
   ;

functions
   :  ^(FUNCS (f=function {functionStarts.add($f.start);} )*)
   ;

function 
   returns [Block start = null;]
   scope { String name;
           int count;
           Block end;
           ArrayList<String> regValues;
           HashMap<String, Type> localHash;}
   @init { $function::count = 0; $function::regValues = new ArrayList<String>();
           $function::localHash = new HashMap<String, Type>(sTable);}
   :  ^(ast=FUN id=ID { start = new Block($id.text + ":start");
                        start.addIloc(new Iloc($id.text + ":"));
                        $function::name = $id.text;
                        $function::end = new Block($id.text + ":end"); } p=parameters[start] r=return_type
         d=fun_decls s=statement_list[$p.end])
         {
            $function::end.addIloc(new Iloc($function::name + "end:"));
            $function::end.addIloc(new Iloc("ret"));
            $s.end.connect($function::end);
         }
   ;

fun_decls
   :  ^(DECLS (fun_decl_list)*)
   ;

fun_decl_list
   :  ^(DECLLIST ^(TYPE t=type) (id=ID
         {
            $function::localHash.put($id.text, $t.typeName);
            if (!$function::regValues.add($id.text))
            {
               System.out.println("error :[");
            }
         } )+)
      
   ;

parameters[Block currentBlock]
   returns [Block end = null;]
   scope {Block blk;}
   @init{$parameters::blk = currentBlock;}
   :  ^(PARAMS (p=param_decl)*)
      {
         end = $parameters::blk;
      }
   ;

param_decl
   :  ^(DECL ^(TYPE t=type) id=ID)
      {
         $function::localHash.put($id.text, $t.typeName);
         $function::regValues.add($id.text);

         //loadinargument num, 0, r0
         int temp = $function::regValues.indexOf($id.text);
         $parameters::blk.addIloc(new Iloc("loadinargument", $id.text, ""+temp, "r"+temp));
      }
   ;

return_type
   :  ^(RETTYPE rtype) 
   ;

rtype
   :  t=type 
   |  VOID 
   ;

statement[Block currentBlock]
   returns [Block end = null, boolean returns = false]
   scope { Block block; }
   @init { $statement::block = currentBlock; $end = currentBlock;}
   :  (s=block[currentBlock]
      |  s=assignment
      |  s=print //
      |  s=invocation_stmt //
      |  s=delete
      |  s=read
      |  s=return_stmt {$returns = true;} //
      |  s=conditional //
      |  s=loop //
      )
   {
      $end = $s.end;
   }
   ;

block[Block block]
   returns [Block end = null;]
   :  ^(BLOCK s=statement_list[block]) {$end = $s.end;}
   ;

statement_list[Block block]
   returns [Block end = null, boolean returns = false]
   @init {$end = block;}
   :  ^(STMTS (s=statement[$end] {if ($s.returns) $returns = true; $end = $s.end;})*)
   ;

assignment
   returns [Block end = null;]
   :  ^(ast=ASSIGN e=expression[$statement::block] l=lvalue[false])
      {
         end = $statement::block;
         if ($l.dot)
         {
            if ($l.isGlobal)
            {
               $end.addIloc(new Iloc("storeglobal", "r" + $e.reg, $l.field_name));
            }
            else
               $end.addIloc(new Iloc("storeai", "r" + $e.reg,  "r" + $l.reg, ""+((StructType)$l.typeName).getFieldOffset($l.field_name)));
         }
         else
         {
            $end.addIloc(new Iloc("mov", "r" + $e.reg, "r" + $l.reg));
         }
      }
   ;

print
   returns [Block end = null;]
   :  ^(ast=PRINT e=expression[$statement::block] (ENDL)?)
      {
         end = $statement::block;
         $end.addIloc(new Iloc("print", "r" + $e.reg));
      }
   ;

read
   returns [Block end = null;]
   :  ^(ast=READ l=lvalue[false])
      {
         end = $statement::block;
         $function::regValues.add("::read");
         end.addIloc(new Iloc("read", "r" + $l.reg));
      }
   ;

conditional
   returns [Block end = null]
   @init { end = new Block($function::name + ":ifend:" + $function::count);
           Block thenBlock = new Block($function::name + ":then:" + $function::count);
           Block elseBlock = new Block($function::name + ":else:" + $function::count);
           thenBlock.connect(end);
           elseBlock.connect(end);
           $function::count++; }
   :  ^(ast=IF
            {
               int then_cond = ++lastLabel;
               int else_cond = ++lastLabel;
               int finally_cond = ++lastLabel;
            } g=expression[$statement::block]
                  {
                     int reg = $function::regValues.size();
                     $function::regValues.add("::cond");
                     $statement::block.addIloc(new Iloc("loadi", "1", "r" + reg));
                     $statement::block.addIloc(new Iloc("comp", "r" + $g.reg, "r" + reg, "ccr"));
                     $statement::block.addIloc(new Iloc("cbreq", "ccr", "L" + then_cond, "L" + else_cond));
                     thenBlock.addIloc(new Iloc("L" + then_cond + ":"));
                     end.addIloc(new Iloc("L" + finally_cond + ":"));
                  }
              t=block[thenBlock] {elseBlock.addIloc(new Iloc("jumpi", "L" + finally_cond)); elseBlock.addIloc(new Iloc("L" + else_cond + ":")); $statement::block.connect(thenBlock); $statement::block.connect(elseBlock);}
              (e=block[elseBlock])?)
   ;

loop
   returns [Block end = null;]
   @init { end = new Block($function::name + ":whileend:" + $function::count);
           Block expBlock = new Block($function::name + ":whiletest:" + $function::count);
           $statement::block.connect(expBlock);
           Block bodyBlock = new Block($function::name + ":whilebody:" + $function::count);
           $function::count++;
           
           }
   :  ^(ast=WHILE
               {
                  int in = ++lastLabel;
                  int out = ++lastLabel;
               } e=expression[expBlock]
                  {
                     expBlock.addIloc(new Iloc("brz", "r" + $e.reg, "L" + out, "L" + in));
                     expBlock.addIloc(new Iloc("L" + in + ":"));
                  } b=block[bodyBlock] ex=expression[$b.end]
                     {
                        $b.end.addIloc(new Iloc("brz", "r" + $ex.reg, "L" + out, "L" + in));
                        expBlock.connect(bodyBlock);
                        expBlock.connect(end);
                        bodyBlock.connect($b.end);
                        $b.end.connect(expBlock);
                        end.addIloc(new Iloc("L" + out + ":"));
                     })
   ;

delete
   returns [Block end = null;]
   :  ^(ast=DELETE e=expression[$statement::block])
      {
         end = $statement::block;
         int reg = $function::regValues.size();
         $function::regValues.add("::delete");
         end.addIloc(new Iloc("mov", "r" + $e.reg, "r" + reg));
         end.addIloc(new Iloc("del", "r" + reg));
      }
   ;

return_stmt
   returns [Block end = null;]
   :  ^(ast=RETURN (e=expression[$statement::block])?)
      {
         if ($e.reg != -1)
            $statement::block.addIloc(new Iloc("storeret", "r" + $e.reg));
         $statement::block.addIloc(new Iloc("jumpi", $function::name + "end"));
         end = new Block($function::name + ":afterreturn:" + $function::count);
         $statement::block.connect(end);
         $function::count++;
      }
   ;

invocation_stmt
   returns [Block end = null;]
   @init {ArrayList<Integer> argRegs = new ArrayList<Integer>();}
   :  ^(ast=INVOKE id=ID ^(ARGS (e=expression[$statement::block]
                                    {
                                       argRegs.add($e.reg);
                                    })*))
      {
         for (int i = 0; i < argRegs.size(); i++)
            $statement::block.addIloc(new Iloc("storeoutargument", "r" + argRegs.get(i), "" + i));
         $statement::block.addIloc(new Iloc("call", $id.text, "" + argRegs.size()));
         end = $statement::block;
      }
   ;

lvalue[boolean rec]
   returns [int reg = -1, boolean dot = false, String field_name = null, boolean isGlobal = false, Type typeName = null]
   :  id=ID
         {
            if ($function::localHash.get($id.text) != null)
            {
               $reg = $function::regValues.indexOf($id.text);
               $typeName = $function::localHash.get($id.text);
            }
            else if (sTable.get($id.text) != null)
            {
               $reg = $function::regValues.size();
               $function::regValues.add("::global");
               $statement::block.addIloc(new Iloc("loadglobal", $id.text, "r" + $reg));
               $isGlobal = true;
               $field_name = $id.text;
               $typeName = sTable.get($id.text);
            }
         }
   |  ^(ast=DOT l=lvalue[true] id=ID)
         {
            $dot = true;
            $field_name = $id.text;
            
            if (rec)
            {
               $reg = $function::regValues.size();
               $function::regValues.add("::struct");
               $statement::block.addIloc(new Iloc("loadai", "r" + $l.reg, ""+((StructType)$l.typeName).getFieldOffset($field_name), "r" + $reg));
               $typeName = ((StructType)$l.typeName).getFieldType($field_name);
            }
            else
            {  
               $reg = $l.reg;
               $typeName = $l.typeName;
            }
         }
   ;

expression[Block currentBlock] 
   returns [int reg = -1, Type typeName = null;]
   :  ^(ast=LT lft=expression[currentBlock] rht=expression[currentBlock])
         {
            int temp = $function::regValues.size();
            $function::regValues.add("::1");
            $reg = $function::regValues.size();
            $function::regValues.add("::0");
            currentBlock.addIloc(new Iloc("loadi", "0", "r" + $reg));
            currentBlock.addIloc(new Iloc("comp", "r" + $lft.reg, "r" + $rht.reg, "ccr"));
            currentBlock.addIloc(new Iloc("movlti", "r" + temp, "1", "r" + $reg));
         }
   |  ^(ast=GT lft=expression[currentBlock] rht=expression[currentBlock])
         {
            int temp = $function::regValues.size();
            $function::regValues.add("::1");
            $reg = $function::regValues.size();
            $function::regValues.add("::0");
            currentBlock.addIloc(new Iloc("loadi", "0", "r" + $reg));
            currentBlock.addIloc(new Iloc("comp", "r" + $lft.reg, "r" + $rht.reg, "ccr"));
            currentBlock.addIloc(new Iloc("movgti", "r" + temp, "1", "r" + $reg));
         }
   |  ^(ast=NE lft=expression[currentBlock] rht=expression[currentBlock])
         {
            int temp = $function::regValues.size();
            $function::regValues.add("::1");
            $reg = $function::regValues.size();
            $function::regValues.add("::0");
            currentBlock.addIloc(new Iloc("loadi", "0", "r" + $reg));
            currentBlock.addIloc(new Iloc("comp", "r" + $lft.reg, "r" + $rht.reg, "ccr"));
            currentBlock.addIloc(new Iloc("movnei", "r" + temp, "1", "r" + $reg));
         }
   |  ^(ast=LE lft=expression[currentBlock] rht=expression[currentBlock])
         {
            int temp = $function::regValues.size();
            $function::regValues.add("::1");
            $reg = $function::regValues.size();
            $function::regValues.add("::0");
            currentBlock.addIloc(new Iloc("loadi", "0", "r" + $reg));
            currentBlock.addIloc(new Iloc("comp", "r" + $lft.reg, "r" + $rht.reg, "ccr"));
            currentBlock.addIloc(new Iloc("movlei", "r" + temp, "1", "r" + $reg));
         }
   |  ^(ast=GE lft=expression[currentBlock] rht=expression[currentBlock])
         {
            int temp = $function::regValues.size();
            $function::regValues.add("::1");
            $reg = $function::regValues.size();
            $function::regValues.add("::0");
            currentBlock.addIloc(new Iloc("loadi", "0", "r" + $reg));
            currentBlock.addIloc(new Iloc("comp", "r" + $lft.reg, "r" + $rht.reg, "ccr"));
            currentBlock.addIloc(new Iloc("movgei", "r" + temp, "1", "r" + $reg));
         }
   |  ^(ast=PLUS lft=expression[currentBlock] rht=expression[currentBlock])
         {
            $reg = $function::regValues.size();
            $function::regValues.add("::plus");
            currentBlock.addIloc(new Iloc("add", "r" + $lft.reg, "r" + $rht.reg, "r" + $reg));
         }
   |  ^(ast=MINUS lft=expression[currentBlock] rht=expression[currentBlock])
         {
            $reg = $function::regValues.size();
            $function::regValues.add("::sub");
            currentBlock.addIloc(new Iloc("sub", "r" + $lft.reg, "r" + $rht.reg, "r" + $reg));
         }
   |  ^(ast=TIMES lft=expression[currentBlock] rht=expression[currentBlock])
         {
            $reg = $function::regValues.size();
            $function::regValues.add("::mult");
            currentBlock.addIloc(new Iloc("mult", "r" + $lft.reg, "r" + $rht.reg, "r" + $reg));
         }
   |  ^(ast=DIVIDE lft=expression[currentBlock] rht=expression[currentBlock])
         {
            $reg = $function::regValues.size();
            $function::regValues.add("::div");
            currentBlock.addIloc(new Iloc("div", "r" + $lft.reg, "r" + $rht.reg, "r" + $reg));
         }
   |  ^(ast=EQ lft=expression[currentBlock] rht=expression[currentBlock])
         {
            int temp = $function::regValues.size();
            $function::regValues.add("::1");
            $reg = $function::regValues.size();
            $function::regValues.add("::eq");
            currentBlock.addIloc(new Iloc("loadi", "0", "r" + $reg));
            currentBlock.addIloc(new Iloc("comp", "r" + $lft.reg, "r" + $rht.reg, "ccr"));
            currentBlock.addIloc(new Iloc("moveqi", "r" + temp, "1", "r" + $reg));
         }
   |  ^(ast=AND lft=expression[currentBlock] rht=expression[currentBlock])
         {
            $reg = $function::regValues.size();
            $function::regValues.add("::and");
            currentBlock.addIloc(new Iloc("and", "r" + $lft.reg, "r" + $rht.reg, "r" + $reg));
         }
   |  ^(ast=OR lft=expression[currentBlock] rht=expression[currentBlock])
         {
            $reg = $function::regValues.size();
            $function::regValues.add("::or");
            currentBlock.addIloc(new Iloc("or", "r" + $lft.reg, "r" + $rht.reg, "r" + $reg));
         }
   |  ^(ast=NOT e=expression[currentBlock])
         {
            $reg = $function::regValues.size();
            $function::regValues.add("::not");
            currentBlock.addIloc(new Iloc("xori", "r" + $e.reg, "1","r" + $reg));
         }
   |  ^(ast=NEG e=expression[currentBlock])
         {
            int immed = $function::regValues.size();
            $function::regValues.add("::-1");
            currentBlock.addIloc(new Iloc("loadi", "-1", "r" + immed));
            $reg = $function::regValues.size();
            $function::regValues.add("::mult");
            currentBlock.addIloc(new Iloc("mult", "r" + $e.reg, "r" + immed, "r" + $reg));
         }
   |  ^(ast=DOT e=expression[currentBlock] id=ID)
         {
            $reg = $function::regValues.size();
            $function::regValues.add($id.text);
            $typeName = ((StructType)$e.typeName).getFieldType($id.text);
            $currentBlock.addIloc(new Iloc("loadai", "r" + $e.reg, ""+((StructType)$e.typeName).getFieldOffset($id.text), "r" + $reg));
         }
   |  ie=invocation_exp[currentBlock] {$reg = $ie.reg;}
   |  id=ID
         {
            int oldReg = $function::regValues.indexOf($id.text);
            $reg = $function::regValues.size();
            $function::regValues.add($id.text);

            if ($function::localHash.get($id.text) != null)
            {
               currentBlock.addIloc(new Iloc("mov", "r" + oldReg, "r" + $reg));
               $typeName = $function::localHash.get($id.text);
            }
            else if (sTable.get($id.text) != null)
            {
               currentBlock.addIloc(new Iloc("loadglobal", $id.text, "r" + $reg));
               $typeName = sTable.get($id.text);
            }
         }
   |  i=INTEGER
         {
            $reg = $function::regValues.size();
            $function::regValues.add($i.text);
            currentBlock.addIloc(new Iloc("loadi", $i.text, "r" + $reg));
         }
   |  ast=TRUE
         {
            $reg = $function::regValues.size();
            $function::regValues.add("::true");
            currentBlock.addIloc(new Iloc("loadi", "1", "r" + $reg));
         }
   |  ast=FALSE
         {
            $reg = $function::regValues.size();
            $function::regValues.add("::false");
            currentBlock.addIloc(new Iloc("loadi", "0", "r" + $reg));
         }
   |  ^(ast=NEW id=ID)
         {
            $reg = $function::regValues.size();
            $function::regValues.add("::new");
            currentBlock.addIloc(new Iloc("new", ""+((StructType)structs.get($id.text)).size(), "r" + $reg));
         }
   |  ast=NULL
         {
            $reg = $function::regValues.size();
            $function::regValues.add("::null");
            currentBlock.addIloc(new Iloc("loadi", "0", "r" + $reg));
         }
   ;

invocation_exp[Block currentBlock]
   returns[int reg = -1]
   @init {ArrayList<Integer> argRegs = new ArrayList<Integer>();}
   :  ^(ast=INVOKE id=ID ^(ARGS (e=expression[currentBlock]
                                    {
                                       argRegs.add($e.reg);
                                    })*))
         {
            for (int i = 0; i < argRegs.size(); i++)
               currentBlock.addIloc(new Iloc("storeoutargument", "r" + argRegs.get(i), "" + i));
            currentBlock.addIloc(new Iloc("call", $id.text, "" + argRegs.size()));
            $reg = $function::regValues.size();
            $function::regValues.add("::invoc");
            currentBlock.addIloc(new Iloc("loadret", "r" + $reg));
         }
   ;
