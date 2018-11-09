lexer grammar DecaLexer;


options {
   language=Java;
   // Tell ANTLR to make the generated lexer class extend the
   // the named class, which is where any supporting code and
   // variables will be placed.
   superClass = AbstractDecaLexer;
}

@header {
    import fr.ensimag.deca.syntax.DecaRecognitionException;
}


//SEPARATEURS
WS: ( ' ' | '\t' | '\r' | '\n') {skip(); };


// MOTS CLES
ASM : ('asm');
CLASS : ('class');
EXTENDS : ('extends');
ELSE : ('else');
IF : ('if');
FALSE : ('false');
INSTANCEOF : ('instanceof');
NEW : ('new');
NULL : ('null');
READINT : ('readInt');
READFLOAT : ('readFloat');
PRINT : ('print');
PRINTLN : ('println');
PRINTLNX : ('printlnx');
PRINTX : ('printx');
PROTECTED : ('protected');
RETURN : ('return');
THIS : ('this');
TRUE : ('true');
WHILE : ('while');


// CARACTERES SPECIAUX
LT : ('<');
GT : ('>');
EQUALS : ('=');
PLUS : ('+');
MINUS : ('-');
TIMES : ('*');
SLASH : ('/');
PERCENT : ('%');
DOT : ('.');
COMMA : (',');
OPARENT : ('(');
CPARENT : (')');
OBRACE : ('{');
CBRACE : ('}');
EXCLAM : ('!');
SEMI : (';');
EQEQ : ('==');
NEQ: ('!=');
GEQ : ('>=');
LEQ : ('<=');
OR : ('||');
AND : ('&&');

 
// IDENTIFICATEURS
fragment LETTER: ('a' .. 'z' | 'A' .. 'Z');
fragment DIGIT: '0' .. '9';
IDENT: (LETTER | '$' | '_') (LETTER | DIGIT | '$' | '_')*;


// ENTIER 
fragment POSITIVE_DIGIT: '1' .. '9';
INT: ('0' | POSITIVE_DIGIT DIGIT*);


// FLOTTANTS
fragment NUM: DIGIT+;
fragment SIGN: ('+' | '-' | /* epsilon */);
fragment EXP: ('E' | 'e') SIGN NUM;
fragment DEC: NUM '.' NUM;
fragment FLOATDEC: (DEC | DEC EXP) ('F' | 'f' | /* epsilon */);
fragment DIGITHEX: ('0' .. '9' | 'A' .. 'F'| 'a' .. 'f');
fragment NUMHEX: DIGITHEX+;
fragment FLOATHEX: ('0x' | '0X') NUMHEX '.' NUMHEX ('P' | 'p') SIGN NUM ('F' | 'f' | /* epsilon */);
FLOAT: FLOATDEC | FLOATHEX;


// CHAINE DE CARACTERES
fragment STRING_CAR: ~('"' | '\\' | '\n');
STRING: '"' (STRING_CAR | '\\"' | '\\\\')* '"' {setText(getText().substring(1, getText().length()-1));};
MULTI_LINE_STRING: '"' (STRING_CAR | '\n' | '\\"' | '\\\\')* '"';


//COMMENTAIRES
COMMENT: '/*' .*? '*/' {skip(); };
COMMENTML: '//' ~('\n')* {skip(); };


//INCLUSION
fragment FILENAME: (LETTER | DIGIT | '.' | '-' | '_')+;
INCLUDE: '#include' (' ')* '"' FILENAME '"' {doInclude(getText()); };


DEFAULT: .  {    if(true) {
                    throw new DecaRecognitionException(this, getInputStream(),
                        "l'entrée n'a pas été reconnue -> DEFAULT");
                }
            };