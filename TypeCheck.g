tree grammar TypeCheck;

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
   import java.util.HashMap;
}

@members
{
   private HashMap<String, Type> structs = new HashMap<String, Type>();
   private HashMap<String, Type> sTable = new HashMap<String, Type>();
}

translate throws SyntaxException
   :  ^(PROGRAM t=types d=declarations[sTable] f=functions)
         {
            if ($f.mainFound == false)
            {
               throw new SyntaxException("Main function was not found");
            }
         }
   ;

types
   :  ^(TYPES (t=type_decl)*)
   |
   ;

type_decl
   :  ^(ast=STRUCT id=ID {structs.put($id.text, new RecType());} n=nested_decl)
      {
         structs.put($id.text, new StructType($n.hash, null));
      }
   ;

nested_decl
   returns [HashMap<String, Type> hash = new HashMap<String, Type>();]
   :  (f=field_decl { $hash.put($f.name, $f.fieldType); })+
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

declarations[HashMap<String, Type> hash]
   :  ^(DECLS (decl_list[hash])*)
   ;

decl_list[HashMap<String, Type> hash]
   :  ^(DECLLIST ^(TYPE t=type)
         (id=ID
            {
               hash.put($id.text, $t.typeName);
            }
         )+
      )
   ;

functions
   returns [boolean mainFound = false] throws SyntaxException
   :  ^(FUNCS (f = function {if ($f.mainFound) $mainFound = true;})*)
   ;

function 
   returns [boolean mainFound = false] throws SyntaxException
   scope { HashMap<String, Type> localHash;
            Type ret; }
   @init{ $function::localHash = new HashMap<String, Type>(sTable); }
   :  ^(ast=FUN id=ID p=parameters r=return_type { sTable.put($id.text, new FunType($p.params, $r.typeName));
                                                   $function::localHash.put($id.text, new FunType($p.params, $r.typeName));
                                                   $function::ret = $r.typeName;}
         d=declarations[$function::localHash]
         s=statement_list)
         {
            if ($id.text.equals("main"))
               mainFound = true;
            if ($s.retCheck == false && !$function::ret.equals(new VoidType()))
               throw new SyntaxException("Function " + $id.text + " is missing a return for one or more cases");
         }
   ;

parameters
   returns [ArrayList<Type> params = new ArrayList<Type>()]
   :  ^(PARAMS (p=param_decl {$params.add($p.t2); })*)
   ;

param_decl
   returns [Type t2]
   :  ^(DECL ^(TYPE t=type) id=ID)
      {
         $function::localHash.put($id.text, $t.typeName);
         $t2 = $t.typeName;
      }
   ;

return_type
   returns [Type typeName = null]
   :  ^(RETTYPE rtype) { $typeName = $rtype.typeName; }
   ;

rtype
   returns [Type typeName = null]
   :  t=type { $typeName = $t.typeName; }
   |  VOID { $typeName = new VoidType(); }
   ;

statement
   returns [boolean retCheck = false] throws SyntaxException
   :  (s=block {$retCheck = $s.retCheck; }
      |  s=assignment
      |  s=print
      |  s=invocation_stmt
      |  s=delete
      |  s=read
      |  s=return_stmt {$retCheck = true;}
      |  s=conditional {$retCheck = $s.retCheck;}
      |  s=loop {$retCheck = $s.retCheck;}
      )
   ;

block
   returns [boolean retCheck = false] throws SyntaxException
   :  ^(BLOCK s=statement_list) {$retCheck = $s.retCheck;}
   ;

statement_list
   returns [boolean retCheck = false] throws SyntaxException
   :  ^(STMTS (s=statement {$retCheck = $retCheck || $s.retCheck;})*)
   ;

assignment throws SyntaxException
   :  ^(ast=ASSIGN e=expression l=lvalue)
      {
         if (!($e.typeName.getClass().equals(NullType.class) || $e.typeName.equals($l.typeName)))
         {
            throw new SyntaxException("Assignment type mismatch at " + $ast.line + ": have "
                                 + $e.typeName.getClass().toString() + ", need " + $l.typeName.getClass().toString());
         }
      }
   ;

print throws SyntaxException
   :  ^(ast=PRINT e=expression (ENDL)?)
      {
         if (!$e.typeName.equals(new IntType()))
         {
            throw new SyntaxException("Printing non-integer at " + $ast.line);
         }
      }
   ;

read throws SyntaxException
   :  ^(ast=READ l=lvalue)
      {
         if (!$l.typeName.equals(new IntType()))
         {
            throw new SyntaxException("Read into non-integer at " + $ast.line);
         }
      }
   ;

conditional
   returns [boolean retCheck = false] throws SyntaxException
   :  ^(ast=IF g=expression t=block (e=block {$retCheck = $t.retCheck && $e.retCheck;})?)
      {
         if (!$g.typeName.equals(new BoolType()))
         {
            throw new SyntaxException("Non-boolean condition at " + $ast.line);
         }
      }
   ;

loop
   returns [boolean retCheck = false] throws SyntaxException
   :  ^(ast=WHILE e=expression b=block expression)
      {
         $retCheck = $b.retCheck;
         if (!$e.typeName.equals(new BoolType()))
         {
            throw new SyntaxException("Non-boolean condition at " + $ast.line);
         }
      }
   ;

delete throws SyntaxException
   :  ^(ast=DELETE e=expression)
      {
         if (!$e.typeName.getClass().equals(StructType.class))
         {
            throw new SyntaxException("Deletion of non-struct at " + $ast.line);
         }
      }
   ;

return_stmt throws SyntaxException
   @init { boolean retTypeCheck = false; }
   :  ^(ast=RETURN { if ($function::ret.getClass().equals(VoidType.class))
                         retTypeCheck = true;
                   }
          (e=expression 
           {
            retTypeCheck = $function::ret.equals($e.typeName);
           }
          )?)
      {
         if (!retTypeCheck)
            throw new SyntaxException("Invalid return type at " + $ast.line);
      }
   ;

invocation_stmt throws SyntaxException
   @init { int index = 0;
           Type function;
           ArrayList<Type> params;
         }
   :  ^(ast=INVOKE id=ID {function = sTable.get($id.text);
                      if (function == null || !function.getClass().equals(FunType.class))
                         throw new SyntaxException("Invocation of non-function at "
                          + $ast.line + ": " + $id.text);
                      params = ((FunType)function).getParams();
                     }
               ^(ARGS (e=expression { if (!params.get(index++).equals($e.typeName))
                                          throw new SyntaxException("Non-matching argument for "
                                          + $id.text + " at " + $ast.line + ": " + $e.text);})*))
      {
         if (index < params.size())
         {
            throw new SyntaxException("Too few arguments for " + $id.text + " at " + $ast.line);
         }
      }
   ;
   catch[IndexOutOfBoundsException ex] { throw new SyntaxException("Too many arguments for function " + $id.text + " at " + $ast.line); }

lvalue 
   returns [Type typeName = null] throws SyntaxException
   :  id=ID
      { 
        $typeName = $function::localHash.get($id.text);
        if ($typeName == null)
        {
           throw new SyntaxException("Access of undeclared variable: " + $id.text);
        }
      }
   |  ^(ast=DOT l=lvalue id=ID)
      {
         if (!$l.typeName.getClass().equals(StructType.class))
         {
            throw new SyntaxException("Access of field of non struct at "
            + $ast.line + ": " + $id.text);
         }

         $typeName = ((StructType)($l.typeName)).getFieldType($id.text); 
         if ($typeName == null)
         {
            throw new SyntaxException("Non-existing field" + $id.text + " at " + $ast.line);
         }
      }
   ;

expression
   returns [Type typeName = null] throws SyntaxException
   :  ^((ast=PLUS | ast=MINUS | ast=TIMES | ast=DIVIDE)
         lft=expression rht=expression)
      {
         if (!($lft.typeName.getClass().equals(IntType.class) && $rht.typeName.getClass().equals(IntType.class)))
         {
            throw new SyntaxException("Need int at " + $ast.line);
         }
         $typeName = $lft.typeName;
      }
   |  ^((ast=LT | ast=GT | ast=NE | ast=LE | ast=GE) lft=expression rht=expression)
      {
         if (!($lft.typeName.getClass().equals(IntType.class) && $rht.typeName.getClass().equals(IntType.class)))
         {
            throw new SyntaxException("Need int at " + $ast.line);
         }
         $typeName = new BoolType();
      }
   |  ^(ast=EQ lft=expression rht=expression)
      {
         if (!($lft.typeName.getClass().equals($rht.typeName.getClass())))
         {
            throw new SyntaxException("Need same types at " + $ast.line);
         }
         $typeName = new BoolType();
      }
   |  ^((ast=AND | ast=OR) lft=expression rht=expression)
      {
         if(!($lft.typeName.equals(new BoolType()) && $rht.typeName.equals(new BoolType())))
         {
            throw new SyntaxException("Need boolean at " + $ast.line);
         }
         $typeName = new BoolType();
      }
   |  ^(ast=NOT e=expression)
      {
         if (!$e.typeName.getClass().equals(BoolType.class))
         {
            throw new SyntaxException("Need boolean at " + $ast.line);
         }
         $typeName = $e.typeName;
      }
   |  ^(ast=NEG e=expression)
      {
         if (!$e.typeName.getClass().equals(IntType.class))
         {
            throw new SyntaxException("Need int at " + $ast.line);
         }
         $typeName = $e.typeName;
      }
   |  ^(ast=DOT    e=expression id=ID)
      {
         if (!$e.typeName.getClass().equals(StructType.class))
         {
            throw new SyntaxException("Access of field of non struct at "
            + $ast.line + ": " + $id.text);
         }

         $typeName = ((StructType)($e.typeName)).getFieldType($id.text); 
         if ($typeName == null)
         {
            throw new SyntaxException("Non-existing field " +$id.text+ " at " + $ast.line);
         }
      }
   |  ie=invocation_exp { $typeName = $ie.typeName; }
   |  id=ID
      {
         $typeName = $function::localHash.get($id.text);
         if ($typeName == null)
         {
            throw new SyntaxException("Undeclared variable: " + $id.text);
         }
      }
   |  i=INTEGER
      {
         $typeName = new IntType();
      }
   |  ast=TRUE
      {
         $typeName = new BoolType();
      }
   |  ast=FALSE
      {
         $typeName = new BoolType();
      }
   |  ^(ast=NEW id=ID)
      {
         $typeName = structs.get($id.text);
         if ($typeName == null || !$typeName.getClass().equals(StructType.class))
         {
            throw new SyntaxException("Unknown type: " + $id.text + " at " + $ast.line);
         }
      }
   |  ast=NULL
      {
         $typeName = new NullType();
      }
   ;

invocation_exp
   returns [Type typeName = null] throws SyntaxException
   @init { int index = 0;
           Type function;
           ArrayList<Type> params;
         }
   :  ^(ast=INVOKE id=ID {function = sTable.get($id.text);
                      if (function == null || !function.getClass().equals(FunType.class))
                         throw new SyntaxException("Invocation of non-function: " + $id.text);
                      params = ((FunType)function).getParams();
                      }
               ^(ARGS (e=expression { if (!params.get(index++).equals($e.typeName))
                                          throw new SyntaxException("Non-matching argument for "
                                          + $id.text + " at " + $ast.line + ": " + $e.text);})*))
      {
         if (index < params.size())
         {
            throw new SyntaxException("Too few arguments for " + $id.text + " at " + $ast.line);
         }
         $typeName = ((FunType)function).getReturnType();
      }
   ;
   catch[IndexOutOfBoundsException ex] { throw new SyntaxException("Too many arguments for function " + $id.text + " at " + $ast.line); }
