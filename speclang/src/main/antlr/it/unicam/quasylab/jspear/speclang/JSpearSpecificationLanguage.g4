grammar JSpearSpecificationLanguage;

@header {
    package it.unicam.quasylab.jspear.speclang;
}


model : element* ;

element: constant_declaration
| parameter_declartion
| variables_declaration
| type_declaration
| environment_declaration
| controller_declaration
| penalty_declaration
| function_declaration
| system_declaration;

function_declaration: 'function' name=ID '(' (arguments+=function_argument (',' arguments+=function_argument)*)? ')'
 function_block;

function_statement: 
      return_statement
    | if_then_else_statement
    | function_block
    | switch_statement
;

switch_statement: 'switch' name=ID '{'
    switch_cases += case_statement
'}';

case_statement: 'case' name=ID ':' function_statement;


if_then_else_statement: 'if' '(' guard=expr ')' thenStatement=function_statement ('else' elseStatement=function_statement)?;

return_statement: 'return' expr ';';

function_block: '{' (statements += function_statement)* '}';

function_argument: type name=ID;

system_declaration: 'system' name=ID '=' controllerName=ID '{' 
    initialAssignment+=initial_assignment*
'}';

initial_assignment: name=ID '=' value=expr ';';


penalty_declaration: 'penalty' name=ID '=' value=expr;

controller_declaration: 'controller' '{'
    state_declaration*
'}'
;

state_declaration: 'state' name=ID state_body;

state_body:
    components += ID ('||' components+=ID)* # parallelController
    | body=statement_block                  #sequentialController;

controller_behaviour:
    variable_assignment
    | if_then_else
    | probabilistic_choice
    | statement_block
    | step_statement
    | exec_statement
;

exec_statement: 'exec' target=ID;

step_statement: (steps=expr '#')? 'step' target=ID;

statement_block: '{'
    (statements += controller_behaviour)*
'}';

probabilistic_choice: 'choose' '{'
    probabilistic_item*
'}';

probabilistic_item: ('when' guard=expr)? 'with' probability=expr statement_block ;

if_then_else: 'if' guard=expr controller_behaviour ('else' controller_behaviour)?;

environment_declaration:
    'environment' '{'
        ('let' localVariables+=local_variable
        ('and' localVariables+=local_variable)*
        'in')?
        variable_assignment*
    '}'
;

variable_assignment: ('when' guard=expr)? name = var_expression '=' expr ';';

var_expression: NEXT_ID ('[' first=expr ('..' last=expr)? ']');

local_variable: name=ID '=' expr;

type_declaration: 'type' name=ID '=' elements+=ID ('|' elements += ID )* ';';

variables_declaration: 'variables' '{'
                         variable_declaration*
                       '}';

variable_declaration: type name=ID ';';

type: 'int' '[' from=expr ',' to=expr ']' #integerType
| 'real' '[' from=expr ',' to=expr ']' #realType
| 'bool' #booleanType
| name=ID #customType;

parameter_declartion: 'param' name=ID '=' expr;

constant_declaration: 'const' name=ID '=' expr;

expr:       left=expr op=('&'|'&&') right=expr                      # andExpression
          | left=expr op=('|'|'||') right=expr                      # orExpression
          | left=expr '^' right=expr                                # exponentExpression
          | left=expr op=('*'|'/'|'//') right=expr               # mulDivExpression
          | left=expr op=('+'|'-'|'%') right=expr                   # addSubExpression
          |  left=expr op=('<'|'<='|'=='|'>='|'>') right=expr          # relationExpression
          | '!' arg=expr                                     # negationExpression
          | guard=expr '?' thenBranch=expr ':' elseBranch=expr             # ifThenElseExpression
          | op=('-'|'+') arg=expr                            # unaryExpression
          | '(' expr ')'                                     # bracketExpression
          | INTEGER                                      # intValue
          | REAL                                         # realValue
          | 'false'                                      # falseValue
          | 'true'                                       # trueValue
          | fun=unaryMathFunction '(' argument=expr ')'                   # unaryMathCallExpression
          | fun=binaryMathFunction '(' left=expr ',' right=expr ')' # binaryMathCallExpression
          | name=ID '(' (callArguments += expr (',' callArguments += expr)*)? ')' #callExpression
          | name=ID ('[' first=expr ('..' last=expr)? ']')? #referenceExpression
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