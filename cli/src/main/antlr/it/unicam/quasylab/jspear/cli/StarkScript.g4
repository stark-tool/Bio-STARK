grammar StarkScript;

@header {
    package it.unicam.quasylab.jspear.cli;
}

starkScript : (commands+=scriptCommand '\n')* EOF ;

scriptCommand:
    changeDirectoryCommand
    | listCommand
    | cwdCommand
    | loadCommand
    | quitCommand
;

quitCommand: 'quit';

loadCommand: 'load' target=STRING;

changeDirectoryCommand:
    'cd' target=STRING
;

listCommand:
    'ls'
;

cwdCommand:
    'cwd'
;



fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];
fragment FILE_DIGIT : ~[<>:"/\\|?*\n\r];

ID              :   LETTER (DIGIT|LETTER)*;
NEXT_ID         :   ID '\'';
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;
STRING          : '"' ( ~["\n\r] | '\\"')* '"' ;

COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN) // match anything between /* and */
    ;

WS  : [ \r\t\u000C\n]+ -> channel(HIDDEN)
    ;