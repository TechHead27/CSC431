SOURCES=Block.java BoolType.java Check.java Compile.java FunType.java IntType.java Iloc.java NullType.java RecType.java StructType.java SyntaxException.java Type.java VoidType.java
CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES)

%.class : %.java
	javac $<

TypeCheck.java BuildCFG.java : %.java : %.g
	java org.antlr.Tool $<

MiniLexer.java : Mini.g
	java org.antlr.Tool $<

Check.class Compile.class : MiniLexer.class TypeCheck.class BuildCFG.class

clean:
	rm -f BuildCFG.java MiniParser.java MiniLexer.java TypeCheck.java *.tokens *.class
