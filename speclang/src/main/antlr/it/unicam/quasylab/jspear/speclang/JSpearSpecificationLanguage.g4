grammar JSpearSpecificationLanguage;

@header {
    package it.unicam.quasylab.jspear.speclang;
}


jSpearSpecificationModel : element* ;

element: constantDeclaration
| parameterDeclaration
| variablesDeclaration
| typeDeclaration
| environmentDeclaration
| controllerDeclaration
| penaltyDeclaration
| functionDeclaration
| systemDeclaration;

functionDeclaration: 'function' name=ID '(' (arguments+=functionArgument (',' arguments+=functionArgument)*)? ')'
 functionBlock;

functionStatement:
      returnStatement
    | ifThenElseStatement
    | functionBlock
    | switchStatement
;

switchStatement: 'switch' name=ID '{'
    (switchCases += caseStatement)+
'}';

caseStatement: 'case' name=ID ':' functionStatement;


ifThenElseStatement: 'if' '(' guard=expression ')' thenStatement=functionStatement ('else' elseStatement=functionStatement)?;

returnStatement: 'return' expression ';';

functionBlock: '{'  functionStatement '}';

functionArgument: type name=ID;

systemDeclaration: 'system' name=ID '=' controllerName=ID '{'
    initialAssignments+=initialAssignment*
'}';

initialAssignment: name=ID '=' value=expression ';';


penaltyDeclaration: 'penalty' name=ID '=' value=expression;

controllerDeclaration: 'controller' '{'
    stateDeclaration*
'}'
;

stateDeclaration: 'state' name=ID stateBody;

stateBody:
    '=' components += ID ('||' components+=ID)* ';' # parallelController
    | body=blockBehaviour                  #sequentialController;

controllerBehaviour:
    variableAssignmentBehaviour
    | ifThenElseBehaviour
    | probabilisticChoiceBehaviour
    | blockBehaviour
    | stepBehaviour
    | execBehaviour
;

variableAssignmentBehaviour:
('when' guard=expression)? target=varExpression '=' value=expression ';'
;

execBehaviour: 'exec' target=ID;

stepBehaviour: (steps=expression '#')? 'step' target=ID;

blockBehaviour: '{'
    (statements += controllerBehaviour)*
'}';

probabilisticChoiceBehaviour: 'choose' '{'
    probabilisticItem*
'}';

probabilisticItem: ('when' guard=expression)? 'with' probability=expression blockBehaviour ;

ifThenElseBehaviour: 'if' guard=expression thenBranch=controllerBehaviour ('else' elseBranch=controllerBehaviour)?;

environmentDeclaration:
    'environment' '{'
        ('let' localVariables+=localVariable
        ('and' localVariables+=localVariable)*
        'in')?
        (assignments += variableAssignment)*
    '}'
;

variableAssignment: ('when' guard=expression)? target=varExpression '=' value=expression ';';

varExpression: name=NEXT_ID ('[' first=expression ('..' last=expression)? ']')?;

localVariable: name=ID '=' expression;

typeDeclaration: 'type' name=ID '=' elements+=ID ('|' elements += ID )* ';';

variablesDeclaration: 'variables' '{'
                         variableDeclaration*
                       '}';

variableDeclaration: type name=ID ('range' '[' from=expression ',' to=expression ']')? ';';

type: 'int' #integerType
| 'real' #realType
| 'array' #arrayType
| 'bool' #booleanType
| name=ID #customType;

parameterDeclaration: 'param' name=ID '=' expression;

constantDeclaration: 'const' name=ID '=' expression;

expression:       left=expression op=('&'|'&&') right=expression                      # andExpression
          | left=expression op=('|'|'||') right=expression                      # orExpression
          | left=expression '^' right=expression                                # exponentExpression
          | left=expression op=('*'|'/'|'//') right=expression               # mulDivExpression
          | left=expression op=('+'|'-'|'%') right=expression                   # addSubExpression
          |  left=expression op=('<'|'<='|'=='|'>='|'>') right=expression          # relationExpression
          | '!' arg=expression                                     # negationExpression
          | guard=expression '?' thenBranch=expression ':' elseBranch=expression             # ifThenElseExpression
          | op=('-'|'+') arg=expression                            # unaryExpression
          | '(' expression ')'                                     # bracketExpression
          | INTEGER                                      # intValue
          | REAL                                         # realValue
          | 'false'                                      # falseValue
          | 'true'                                       # trueValue
          | fun=unaryMathFunction '(' argument=expression ')'                   # unaryMathCallExpression
          | fun=binaryMathFunction '(' left=expression ',' right=expression ')' # binaryMathCallExpression
          | name=ID '(' (callArguments += expression (',' callArguments += expression)*)? ')' #callExpression
          | name=ID ('[' first=expression ('..' last=expression)? ']')? #referenceExpression
          | '[' (elements += expression (',' elements += expression)*) ']' #arrayExpression
          | 'N' '[' mean=expression ',' variance=expression ']' #normalExpression
          | 'U' '[' values += expression (',' values += expression)* ']' #uniformExpression
          | 'R' ('[' from = expression ',' to = expression ']')?     #randomExpression
          ;


binaryMathFunction:
    'atan2'
    | 'hypot'
    | 'max'
    | 'min'
    | 'pow'
;

unaryMathFunction: 'abs'
    | 'acos'
    | 'asin'
    | 'atan'
    | 'cbrt'
    | 'ceil'
    | 'cos'
    | 'cosh'
    | 'exp'
    | 'expm1'
    | 'floor'
    | 'log'
    | 'log10'
    | 'log1p'
    | 'signum'
    | 'sin'
    | 'sinh'
    | 'sqrt'
    | 'tan'
    ;



fragment DIGIT  :   [0-9];
fragment LETTER :   [a-zA-Z_];

ID              :   LETTER (DIGIT|LETTER)*;
NEXT_ID         :   ID '\'';
INTEGER         :   DIGIT+;
REAL            :   ((DIGIT* '.' DIGIT+)|DIGIT+ '.')(('E'|'e')('-')?DIGIT+)?;


COMMENT
    : '/*' .*? '*/' -> channel(HIDDEN) // match anything between /* and */
    ;

WS  : [ \r\t\u000C\n]+ -> channel(HIDDEN)
    ;