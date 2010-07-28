header {
	package cz.incad.kramerius.processes.os.impl.windows.csv;
	
	import java.util.*;
}
class CSVParser extends Parser;
	file returns[List file]
		{
			file = new ArrayList();
			List l=null; 		
		}	
	   :  (NEWLINE)* ( l=line {file.add(l);} (NEWLINE (l=line {file.add(l);})? )*  EOF );

	line returns[List line]
		{ 
			line = new ArrayList(); 
			String rec = null;
		}	
	: ( (rec=record {line.add(rec);} )+ ) ;

	record returns [String rec]
		{
			rec = null;
		}
	: ( (r:RECORD  
		{
			rec = r.getText();
		}) (COMMA)? ) ;



class CSVLexer extends Lexer;
options{
		charVocabulary='\u0003'..'\u7FFE';
}
RECORD  : '"' (~(','|'\r'|'\n'))+ ;
COMMA   : ',' ;
NEWLINE : ('\r''\n')=> '\r''\n' //DOS
        | '\r'                  //MAC
        | '\n'                  //UNIX
        { newline(); }
        ;
WS      : (' '|'\t') { $setType(Token.SKIP); } ;