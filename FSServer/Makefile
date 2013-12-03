JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
				$(JC) $(JFLAGS) $*.java

CLASSES = \
       	src/RequestProcessor.java \
        src/SQLManager.java \
        src/FSServer.java \
        
default: classes

classes: $(CLASSES:.java=.class)
				mv src/*.class bin

clean:
				$(RM) *.class

