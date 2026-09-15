grammar FlightPlan;

flightPlan : flight EOF ;

flight : FLIGHT ID LBRACE flightType load legs RBRACE ;

flightType : TYPE ( REGULAR | CHARTER ) SEMI ;

legs : leg+ ;

leg : LEG LBRACE departure arrival route fuel RBRACE ;

departure : DEPARTURE airportCode DATETIME SEMI ;

arrival : ARRIVAL airportCode DATETIME SEMI ;

airportCode : AIRPORT_CODE ;

route : ROUTE LBRACE segment+ RBRACE ;

segment : SEGMENT LPAREN coord RPAREN LPAREN coord RPAREN
          altSlot+
          WIND INTEGER DECIMAL UNIT_SPEED SEMI ;

altSlot : ALT INTEGER UNIT_DIST WIDTH INTEGER UNIT_DIST SEMI ;

fuel : FUEL DECIMAL UNIT_MASS SEMI ;

load : LOAD passengers paxWeight cargoWeight SEMI ;

passengers : PASSENGERS INTEGER ;

paxWeight : PAX_WEIGHT DECIMAL UNIT_MASS ;

cargoWeight : CARGO_WEIGHT DECIMAL UNIT_MASS ;

coord : MINUS? DECIMAL COMMA MINUS? DECIMAL ;

// LEXER RULES
FLIGHT    : [fF][lL][iI][gG][hH][tT] ;
LEG       : [lL][eE][gG] ;
TYPE      : [tT][yY][pP][eE] ;
REGULAR   : [rR][eE][gG][uU][lL][aA][rR] ;
CHARTER   : [cC][hH][aA][rR][tT][eE][rR] ;
DEPARTURE : [dD][eE][pP][aA][rR][tT][uU][rR][eE] ;
ARRIVAL   : [aA][rR][rR][iI][vV][aA][lL] ;
ROUTE     : [rR][oO][uU][tT][eE] ;
SEGMENT   : [sS][eE][gG][mM][eE][nN][tT] ;
ALT       : [aA][lL][tT] ;
WIDTH     : [wW][iI][dD][tT][hH] ;
WIND      : [wW][iI][nN][dD] ;
FUEL      : [fF][uU][eE][lL] ;
LOAD      : [lL][oO][aA][dD] ;
PASSENGERS   : [pP][aA][sS][sS][eE][nN][gG][eE][rR][sS] ;
PAX_WEIGHT   : [pP][aA][xX] '_' [wW][eE][iI][gG][hH][tT] ;
CARGO_WEIGHT : [cC][aA][rR][gG][oO] '_' [wW][eE][iI][gG][hH][tT] ;

UNIT_SPEED : 'm/s' ;
UNIT_DIST  : 'm' ;
UNIT_MASS  : 'kg' | 'l' ;

LBRACE : '{' ;
RBRACE : '}' ;
LPAREN : '(' ;
RPAREN : ')' ;
SEMI  : ';' ;
MINUS : '-' ;
COLON : ':' ;
COMMA : ',' ;

DATETIME : [0-9][0-9][0-9][0-9] '-' [0-9][0-9] '-' [0-9][0-9]
           ' '
           [0-9][0-9] ':' [0-9][0-9] ;

AIRPORT_CODE : [A-Z][A-Z][A-Z][A-Z]? ;

ID      : [a-zA-Z][a-zA-Z0-9_]* ;
INTEGER : [0-9]+ ;
DECIMAL : [0-9]+ ('.' [0-9]+)? ;

WS      : [ \t\r\n]+ -> skip ;
COMMENT : '//' ~[\r\n]* -> skip ;
