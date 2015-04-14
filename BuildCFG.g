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
}

@members
{
   ArrayList<Block> functionStarts = new ArrayList<Block>();
}

translate throws SyntaxException
   :  ^(PROGRAM t=types d=declarations f=functions)
   ;

types
   :  ^(TYPES (t=type_decl)*)
   |
   ;

type_decl
   :  ^(ast=STRUCT id=ID n=nested_decl)
   ;

nested_decl
   :  (f=field_decl)+
   ;

field_decl
   :  ^(DECL ^(TYPE t=type) id=ID)
   ;

type
   :  INT 
   |  BOOL 
   |  ^(STRUCT id=ID) 
   ;

declarations
   :  ^(DECLS (decl_list)*)
   ;

decl_list
   :  ^(DECLLIST ^(TYPE t=type) (id=ID)+)
   ;

functions
   :  ^(FUNCS (f = function {functionStarts.add($f.start);} )*)
   ;

function 
   returns [Block start = null;]
   scope { String name;
           int count; }
   @init{ Block end; $function::count = 0; }
   :  ^(ast=FUN id=ID { start = new Block($id.text + "_start");
                        $function::name = $id.text; } p=parameters r=return_type
         d=declarations
         s=statement_list[start])
         {
            end = new Block($id.text + "_end");
            $s.end.connect(end);
         }
   ;

parameters
   :  ^(PARAMS (p=param_decl)*)
   ;

param_decl
   :  ^(DECL ^(TYPE t=type) id=ID)
      {
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
   returns [Block end = currentBlock;]
   scope { Block block; }
   @init { $statement::block = currentBlock; }
   :  (s=block[currentBlock]
      |  s=assignment
      |  s=print
      |  s=invocation_stmt
      |  s=delete
      |  s=read
      |  s=return_stmt
      |  s=conditional
      |  s=loop
      )
   {
      end = $s.end;
   }
   ;

block[Block block]
   returns [Block end = null;]
   :  ^(BLOCK s=statement_list[block]) {$end = $s.end;}
   ;

statement_list[Block block]
   returns [Block end = block;]
   :  ^(STMTS (s=statement[end] {end = $s.end;})*)
   ;

assignment
   returns [Block end = null;]
   :  ^(ast=ASSIGN e=expression[$statement::block] l=lvalue)
      {
         $end = $e.end;
      }
   ;

print
   returns [Block end = null;]
   :  ^(ast=PRINT e=expression[$statement::block] (ENDL)?)
      {
         $end = $e.end;
      }
   ;

read
   returns [Block end = null;]
   :  ^(ast=READ l=lvalue)
      {
         end = $statement::block;
      }
   ;

conditional
   returns [Block end = null]
   @init { end = new Block($function::name + "_ifend_" + $function::count++);
           Block thenBlock = new Block($function::name + "_then_" + $function::count);
           Block elseBlock = new Block($function::name + "_else_" + $function::count); }
   :  ^(ast=IF g=expression[$statement::block] t=block[thenBlock] {$g.end.connect(thenBlock); thenBlock.connect(end);}
        (e=block[elseBlock] {$g.end.connect(elseBlock); elseBlock.connect(end);} )?)
   ;

loop
   returns [Block end = null;]
   @init { end = new Block($function::name + "_whileend_" + $function::count++);
           Block expBlock = new Block($function::name + "_whiletest_" + $function::count);
           $statement::block.connect(expBlock);
           Block bodyBlock = new Block($function::name + "_whilebody_" + $function::count);
           expBlock.connect(bodyBlock);
           expBlock.connect(end);
           bodyBlock.connect(expBlock); }
   :  ^(ast=WHILE e=expression[expBlock] b=block[bodyBlock] expression[bodyBlock])
   ;

delete
   returns [Block end = null;]
   :  ^(ast=DELETE e=expression[$statement::block])
      {
         end = $e.end;
      }
   ;

return_stmt
   returns [Block end = null;]
   :  ^(ast=RETURN (e=expression[$statement::block] )?)
      {
         end = $e.end;
      }
   ;

invocation_stmt
   returns [Block end = null;]
   :  ^(ast=INVOKE id=ID ^(ARGS (e=expression[$statement::block] )*))
      {
         end = $e.end;
      }
   ;

lvalue 
   :  id=ID
   |  ^(ast=DOT l=lvalue id=ID)
   ;

expression[Block currentBlock] 
   returns [Block end = currentBlock] // I don't think this would ever not be the case
   :  ^((ast=LT | ast=GT | ast=NE | ast=LE | ast=GE | ast=PLUS
         | ast=MINUS | ast=TIMES | ast=DIVIDE)
         lft=expression[currentBlock] rht=expression[currentBlock])
   |  ^(ast=EQ lft=expression[currentBlock] rht=expression[currentBlock])
   |  ^((ast=AND | ast=OR) lft=expression[currentBlock] rht=expression[currentBlock])
   |  ^(ast=NOT e=expression[currentBlock])
   |  ^(ast=NEG e=expression[currentBlock])
   |  ^(ast=DOT    e=expression[currentBlock] id=ID)
   |  ie=invocation_exp[currentBlock]
   |  id=ID
   |  i=INTEGER
   |  ast=TRUE
   |  ast=FALSE
   |  ^(ast=NEW id=ID)
   |  ast=NULL
   ;

invocation_exp[Block currentBlock]
   returns [Block end = currentBlock]
   :  ^(ast=INVOKE id=ID ^(ARGS (e=expression[currentBlock] )*))
   ;
