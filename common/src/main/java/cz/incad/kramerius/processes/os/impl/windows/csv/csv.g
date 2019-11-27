/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * CVS parser  
 */
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
RECORD  : '"' (~('\r'|'\n'|'"'))* '"'  ;
COMMA   : ',' ;
NEWLINE : ('\r''\n')=> '\r''\n' //DOS
        | '\r'                  //MAC
        | '\n'                  //UNIX
        { newline(); }
        ;
WS      : (' '|'\t') { $setType(Token.SKIP); } ;