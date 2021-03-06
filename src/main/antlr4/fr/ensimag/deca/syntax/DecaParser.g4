parser grammar DecaParser;

options {
    // Default language but name it anyway
    //
    language  = Java;

    // Use a superclass to implement all helper
    // methods, instance variables and overrides
    // of ANTLR default methods, such as error
    // handling.
    //
    superClass = AbstractDecaParser;

    // Use the vocabulary generated by the accompanying
    // lexer. Maven knows how to work out the relationship
    // between the lexer and parser and will build the
    // lexer before the parser. It will also rebuild the
    // parser if the lexer changes.
    //
    tokenVocab = DecaLexer;

}

// which packages should be imported?
@header {
    import fr.ensimag.deca.tree.*;
    import fr.ensimag.deca.tools.SymbolTable;
    import java.io.PrintStream;
}

@members {
    @Override
    protected AbstractProgram parseProgram() {
        return prog().tree;
    }
}

prog returns[AbstractProgram tree]
    : list_classes main EOF {
            assert($list_classes.tree != null);
            assert($main.tree != null);
            $tree = new Program($list_classes.tree, $main.tree);
            setLocation($tree, $list_classes.start);
	    setLocation($list_classes.tree, $list_classes.start);
	    setLocation($main.tree, $main.start);
        }
    ;

main returns[AbstractMain tree]
    : /* epsilon */ {
            $tree = new EmptyMain();
        }
    | block {
            assert($block.decls != null);
            assert($block.insts != null);
            $tree = new Main($block.decls, $block.insts);
        }
    ;

block returns[ListDeclVar decls, ListInst insts]
    : OBRACE list_decl list_inst CBRACE {
            assert($list_decl.tree != null);
            assert($list_inst.tree != null);
            $decls = $list_decl.tree;
            $insts = $list_inst.tree;
	    setLocation($decls, $list_decl.start);
	    setLocation($insts, $list_inst.start);
        }
    ;

list_decl returns[ListDeclVar tree]
@init   {
            $tree = new ListDeclVar();
        }
    : decl_var_set[$tree]*
    ;

decl_var_set[ListDeclVar l]
    : type list_decl_var[$l,$type.tree] SEMI
    ;

list_decl_var[ListDeclVar l, AbstractIdentifier t]
    : dv1=decl_var[$t] {
            $l.add($dv1.tree);
            setLocation($dv1.tree, $dv1.start);
        } (COMMA dv2=decl_var[$t] {
            $l.add($dv2.tree);
            setLocation($dv2.tree, $dv2.start);
        }
      )*
    ;

decl_var[AbstractIdentifier t] returns[AbstractDeclVar tree]
@init   {
            AbstractInitialization init = new NoInitialization();
        }
    : i=ident {
            setLocation($i.tree, $i.start);
        }
      (EQUALS e=expr {
            setLocation($e.tree, $e.start);
            init = new Initialization($e.tree);
        }
      )? {
            $tree = new DeclVar($t, $i.tree, init);
            setLocation(init, $i.start);
        }
    ;

list_inst returns[ListInst tree]
@init {
        $tree = new ListInst();
    }
    : (inst { 
            $tree.add($inst.tree);
	    setLocation($inst.tree, $inst.start);
        }
      )*
    ;

inst returns[AbstractInst tree]
    : e1=expr SEMI {
            assert($e1.tree != null);
            $tree = $e1.tree;
        }
    | SEMI { $tree = new NoOperation();
        }
    | PRINT OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Print(false, $list_expr.tree);
        }
    | PRINTLN OPARENT list_expr CPARENT SEMI { 
            assert($list_expr.tree != null);
            $tree = new Println(false, $list_expr.tree);
        }
    | PRINTX OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Print(true, $list_expr.tree);
        }
    | PRINTLNX OPARENT list_expr CPARENT SEMI {
            assert($list_expr.tree != null);
            $tree = new Println(true, $list_expr.tree);
        }
    | if_then_else {
            assert($if_then_else.tree != null);
            $tree = $if_then_else.tree;
            setLocation($tree, $if_then_else.start);
        }
    | WHILE OPARENT condition=expr CPARENT OBRACE body=list_inst CBRACE {
            assert($condition.tree != null);
            assert($body.tree != null);
            $tree = new While($condition.tree, $body.tree);
            setLocation($condition.tree, $condition.start);
            setLocation($body.tree, $body.start);
        }
    | RETURN expr SEMI {
            assert($expr.tree != null);
            $tree = new Return($expr.tree);
            setLocation($expr.tree, $expr.start);
        }
    ;

if_then_else returns[IfThenElse tree]
@init {
    ListInst elseBranch = new ListInst();
    IfThenElse elsifInstr;
    ListInst elsifBranch;
}
    : if1=IF OPARENT condition=expr CPARENT OBRACE li_if=list_inst CBRACE {
            $tree = new IfThenElse($condition.tree, $li_if.tree, elseBranch); 
            setLocation($tree, $if1);
            setLocation($li_if.tree, $li_if.start);
        }
      (ELSE elsif=IF OPARENT elsif_cond=expr CPARENT OBRACE elsif_li=list_inst CBRACE {
            elsifBranch = elseBranch;
            setLocation(elseBranch, $ELSE);
            elseBranch = new ListInst();
            elsifInstr = new IfThenElse($elsif_cond.tree, $elsif_li.tree, elseBranch);
            setLocation(elsifInstr, $CBRACE);
            elsifBranch.add(elsifInstr);
            setLocation($elsif_li.tree, $elsif_li.start);
        }
      )*
      (ELSE OBRACE li_else=list_inst CBRACE {
            setLocation(elseBranch, $ELSE);
            for (AbstractInst i : $li_else.tree.getList()) {
                elseBranch.add(i);
                setLocation(i, $list_inst.start);
            }
            setLocation($li_else.tree, $list_inst.start);
       
        }
      )?
    ;

list_expr returns[ListExpr tree]
@init   { 
            $tree = new ListExpr();
        }
    : (e1=expr { 
            $tree.add($e1.tree);
	    setLocation($tree, $e1.start);
        }
       (COMMA e2=expr { 
            $tree.add($e2.tree); 
        }
       )* )?
    ;

expr returns[AbstractExpr tree]
    : assign_expr {
            assert($assign_expr.tree != null);
            $tree = $assign_expr.tree;
	        setLocation($tree, $assign_expr.start);
        }
    ;

assign_expr returns[AbstractExpr tree]
    : e=or_expr
    (
        /* condition: expression e must be a "LVALUE" */ {
            if (! ($e.tree instanceof AbstractLValue)) {
                throw new InvalidLValue(this, $ctx);
            }
        }
        EQUALS e2=assign_expr {
            assert($e.tree != null);
            assert($e2.tree != null);
            setLocation($e2.tree, $e2.start);
            $tree = new Assign((AbstractLValue) $e.tree, $e2.tree);
        }
      | /* epsilon */ {
            assert($e.tree != null);
            $tree = $e.tree;
        }
      )
    ;

or_expr returns[AbstractExpr tree]
    : e=and_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=or_expr OR e2=and_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Or($e1.tree, $e2.tree);
            setLocation($e1.tree, $e1.start);
            setLocation($e2.tree, $e2.start);
       }
    ;

and_expr returns[AbstractExpr tree]
    : e=eq_neq_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    |  e1=and_expr AND e2=eq_neq_expr {
            assert($e1.tree != null);                         
            assert($e2.tree != null);
            $tree = new And($e1.tree, $e2.tree);
            setLocation($e1.tree, $e1.start);
            setLocation($e2.tree, $e2.start);
        }
    ;

eq_neq_expr returns[AbstractExpr tree]
    : e=inequality_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=eq_neq_expr EQEQ e2=inequality_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Equals($e1.tree,$e2.tree);
            setLocation($e1.tree, $e1.start);
            setLocation($e2.tree, $e2.start);
        }
    | e1=eq_neq_expr NEQ e2=inequality_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new NotEquals($e1.tree,$e2.tree);
            setLocation($e1.tree, $e1.start);
            setLocation($e2.tree, $e2.start);
        }
    ;

inequality_expr returns[AbstractExpr tree]
    : e=sum_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=inequality_expr LEQ e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new LowerOrEqual($e1.tree,$e2.tree);
            setLocation($e1.tree, $e1.start);
            setLocation($e2.tree, $e2.start);
        }
    | e1=inequality_expr GEQ e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new GreaterOrEqual($e1.tree,$e2.tree);
            setLocation($e1.tree,$e1.start);
            setLocation($e2.tree,$e2.start);
        }
    | e1=inequality_expr GT e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Greater($e1.tree,$e2.tree);
            setLocation($e1.tree,$e1.start);
            setLocation($e2.tree,$e2.start);
        }
    | e1=inequality_expr LT e2=sum_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Lower($e1.tree,$e2.tree);
            setLocation($e1.tree,$e1.start);
            setLocation($e2.tree,$e2.start);
        }
    | e1=inequality_expr INSTANCEOF type {
            assert($e1.tree != null);
            assert($type.tree != null);
            $tree = new InstanceOf($e1.tree, $type.tree);
            setLocation($e1.tree,$e1.start);
            setLocation($type.tree,$type.start);
        }
    ;


sum_expr returns[AbstractExpr tree]
    : e=mult_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=sum_expr PLUS e2=mult_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Plus($e1.tree, $e2.tree);
            setLocation($e1.tree, $e1.start);
            setLocation($e2.tree, $e2.start);

        }
    | e1=sum_expr MINUS e2=mult_expr {
            assert($e1.tree != null);
            assert($e2.tree != null);
            $tree = new Minus($e1.tree, $e2.tree);
            setLocation($e1.tree, $e1.start);
            setLocation($e2.tree, $e2.start);
        }
    ;

mult_expr returns[AbstractExpr tree]
    : e=unary_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=mult_expr TIMES e2=unary_expr {
            assert($e1.tree != null);                                        
            assert($e2.tree != null);
            $tree = new Multiply($e1.tree, $e2.tree);
            setLocation($e1.tree, $e1.start);
            setLocation($e2.tree, $e2.start);
            
        }
    | e1=mult_expr SLASH e2=unary_expr {
            assert($e1.tree != null);                                         
            assert($e2.tree != null);
            $tree = new Divide($e1.tree, $e2.tree);
            setLocation($e1.tree, $e1.start);
            setLocation($e2.tree, $e2.start);
        }
    | e1=mult_expr PERCENT e2=unary_expr {
            assert($e1.tree != null);                                                                          
            assert($e2.tree != null);
            $tree = new Modulo($e1.tree, $e2.tree);
            setLocation($e1.tree, $e1.start);
            setLocation($e2.tree, $e2.start);
        }
    ;

unary_expr returns[AbstractExpr tree]
    : op=MINUS e=unary_expr {
            assert($e.tree != null);
            $tree = new UnaryMinus($e.tree);
            setLocation($e.tree, $e.start);
        }
    | op=EXCLAM e=unary_expr {
            assert($e.tree != null);
            $tree = new Not($e.tree);
            setLocation($e.tree, $e.start);
        }
    | select_expr {
            assert($select_expr.tree != null);
            $tree = $select_expr.tree;
            setLocation($select_expr.tree, $select_expr.start);
        }
    ;

select_expr returns[AbstractExpr tree]
    : e=primary_expr {
            assert($e.tree != null);
            $tree = $e.tree;
        }
    | e1=select_expr DOT i=ident {
            assert($e1.tree != null);
            assert($i.tree != null);
        }
        (o=OPARENT args=list_expr CPARENT {
            // we matched "e1.i(args)"
            assert($args.tree != null);
            $tree = new MethodCall($e1.tree, $i.tree, $args.tree);
            setLocation($tree, $e1.start);
            setLocation($args.tree, $list_expr.start);
        }
        | /* epsilon */ {
            $tree = new Selection($e1.tree, $i.tree);
            setLocation($tree, $e1.start);
        }
        )
    ;

primary_expr returns[AbstractExpr tree]
    : ident {
            assert($ident.tree != null);
            $tree = $ident.tree;
            setLocation($ident.tree, $ident.start);
        }
    | m=ident OPARENT args=list_expr CPARENT {
            assert($args.tree != null);
            assert($m.tree != null);
            AbstractExpr calledObject = new This();
            $tree = new MethodCall(calledObject, $m.tree, $args.tree);
            setLocation($tree, $m.start);
            setLocation(calledObject, $m.start);
        }
    | OPARENT expr CPARENT {
            assert($expr.tree != null);
            $tree = $expr.tree;
        }
    | READINT OPARENT CPARENT {
            $tree = new ReadInt();
        }
    | READFLOAT OPARENT CPARENT {
            $tree = new ReadFloat();
        }
    | NEW ident OPARENT CPARENT {
            assert($ident.tree != null);
            $tree = new New($ident.tree);
            setLocation($tree, $NEW);
        }
    | cast=OPARENT type CPARENT OPARENT expr CPARENT {
            assert($type.tree != null);
            assert($expr.tree != null);
            $tree = new Cast($type.tree, $expr.tree);
            setLocation($type.tree, $type.start);
            setLocation($expr.tree, $expr.start);
        }
    | literal {
            assert($literal.tree != null);
            $tree = $literal.tree;
	    setLocation($tree, $literal.start);
        }
    ;

type returns[AbstractIdentifier tree]
    : ident {
            assert($ident.tree != null);
            $tree = $ident.tree;
            setLocation($ident.tree, $ident.start);
        }
    ;

literal returns[AbstractExpr tree]
    : INT {
            try {
            $tree = new IntLiteral(Integer.parseInt($INT.text));
        }
            catch (NumberFormatException e) {
                System.out.prinln("ddesdq");
                $tree = null;
}
}
    | FLOAT {
            $tree = new FloatLiteral(Float.parseFloat($FLOAT.text));
        }
    | STRING {
      	    $tree = new StringLiteral($STRING.text);
        }
    | TRUE {
	    $tree = new BooleanLiteral(true);
        }
    | FALSE {
      	    $tree = new BooleanLiteral(false);
        }
    | THIS { $tree = new This();
        }
    | NULL {
        $tree = new NullLiteral();
        }
    ;

ident returns[AbstractIdentifier tree]
    : IDENT {
            $tree = new Identifier(new SymbolTable().create($IDENT.text));
            setLocation($tree, $IDENT);
        }
    ;

/****     Class related rules     ****/

list_classes returns[ListDeclClass tree]
@init   {
            $tree = new ListDeclClass();
        }
    :
      (c1=class_decl {
          $tree.add($c1.tree);
        }
      )*
    ;

class_decl returns[DeclClass tree]
    : CLASS name=ident superclass=class_extension OBRACE body=class_body CBRACE {
            $tree = new DeclClass($superclass.tree, $body.meths, $ident.tree, $body.flds);
            setLocation($tree, $CLASS);
            setLocation($body.meths, $body.start);
            setLocation($body.flds, $body.start);
            setLocation($superclass.tree, $superclass.start);
        }
    ;

class_extension returns[AbstractIdentifier tree]
    : EXTENDS ident {
            $tree = $ident.tree;
            setLocation($tree, $EXTENDS);
        }
    | /* epsilon */ {
            $tree = new Identifier(new SymbolTable().create("Object"));
        }
    ;

class_body returns[ListDeclMethod meths, ListDeclField flds]
@init   {
            $meths = new ListDeclMethod();
            $flds = new ListDeclField();
        }   
    : (m=decl_method {
            $meths.add($m.tree);
        }
      | (f=decl_field_set[$flds])
      )*
    ;

decl_field_set[ListDeclField flds]
    : v=visibility t=type list_decl_field[$v.v, $t.tree, flds]
      SEMI 
    ;

visibility returns[Visibility v]
    : /* epsilon */ {
            $v = Visibility.PUBLIC;
        }
    | PROTECTED {
            $v = Visibility.PROTECTED;
        }
    ;

list_decl_field[Visibility v, AbstractIdentifier t, ListDeclField flds]
    : dv1=decl_field[v, t] {
        flds.add($dv1.tree);
    }
        (COMMA dv2=decl_field[v, t] {
        flds.add($dv2.tree);
    }
      )*
    ;

decl_field[Visibility v, AbstractIdentifier t] returns[DeclField tree]
@init   {
    AbstractInitialization init = new NoInitialization();
    }
    : i=ident {
            setLocation($i.tree, $i.start);
        }
      (EQUALS e=expr {
          init = new Initialization($e.tree);
        }
      )? {
          $tree = new DeclField(v, t, $i.tree, init);
          setLocation($tree, $ident.start);
          setLocation(init,$ident.start);
        }
    ;

decl_method returns[DeclMethod tree]
@init { AbstractMethodBody body;
        
}
    : type ident OPARENT params=list_params CPARENT (block {
        body = new MethodBody($block.decls, $block.insts);
            setLocation($block.decls, $block.start);
            setLocation($block.insts, $block.start);
            setLocation(body, $block.start);
        }
      | ASM OPARENT code=multi_line_string CPARENT SEMI {
            body = new MethodBodyASM($code.text);
            setLocation(body, $code.start);
        }
      ) {
            $tree = new DeclMethod($type.tree, $ident.tree, $params.tree, body);
            setLocation($tree, $type.start);
            setLocation($type.tree, $type.start);
            setLocation($ident.tree, $ident.start);
            setLocation($params.tree, $params.start);
        }
    ;

list_params returns[ListDeclParam tree]
@init { $tree = new ListDeclParam(); }

    : (p1=param { 
                    $tree.add($p1.tree);
                    setLocation($p1.tree, $p1.start);
        } (COMMA p2=param { 
                    $tree.add($p2.tree);
                    setLocation($p2.tree, $p2.start);
        }
      )*)?
    ;
    
multi_line_string returns[String text, Location location]
    : s=STRING {
            $text = $s.text;
            $location = tokenLocation($s);
        }
    | s=MULTI_LINE_STRING {
            $text = $s.text;
            $location = tokenLocation($s);
        }
    ;

param returns[AbstractDeclParam tree] 
    : type ident {
            $tree = new DeclParam($type.tree, $ident.tree);
            setLocation($type.tree, $type.start);
            setLocation($ident.tree, $ident.start);
        }
    ;
