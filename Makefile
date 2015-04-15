SOURCES=$(shell echo *.java)
CLASSES=$(SOURCES:.java=.class)

all: $(CLASSES)

%.class : %.java antlr.generated 
	javac $<

antlr.generated: antlr.generated.mini antlr.generated.json antlr.generated.typecheck antlr.generated.buildcfg
	touch antlr.generated

antlr.generated.mini : Mini.g
	java org.antlr.Tool Mini.g
	touch antlr.generated.mini

antlr.generated.json : ToJSON.g
	java org.antlr.Tool ToJSON.g
	touch antlr.generated.json

antlr.generated.typecheck : TypeCheck.g
	java org.antlr.Tool TypeCheck.g
	touch antlr.generated.typecheck

antlr.generated.buildcfg : BuildCFG.g
	java org.antlr.Tool BuildCFG.g
	touch antlr.generated.buildcfg

clean:
	\rm *generated* MiniParser.java MiniLexer.java ToJSON.java TypeCheck.java Mini.tokens ToJSON.tokens TypeCheck.tokens *.class
