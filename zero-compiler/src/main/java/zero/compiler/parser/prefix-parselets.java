/* Copyright (C) 2012 Matt O'Connor <thegreendragon@gmail.com> */
package zero.compiler.parser;

import static zero.compiler.parser.Token.Type.*;

import java.util.List;
import java.util.LinkedList;

// public interface PrefixParselet<T> {
//   Expression parse(Parser parser, T token);
// }

class NameParselet implements PrefixParselet<Token> {
  @Override
  public NameExpression parse(final Parser<Token> parser, final Token token) {
    return new NameExpression(token);
  }
}

class IntegerParselet implements PrefixParselet<Token> {
  @Override
  public IntegerExpression parse(final Parser<Token> parser, final Token token) {
    return new IntegerExpression(token);
  }
}

class GroupingParselet implements PrefixParselet<Token> {
  @Override
  public Expression parse(final Parser<Token> parser, final Token token) {
    final Expression grouped;
    if(parser.lookAhead(0).getType().isNameSymbol()) {
      grouped = new NameExpression(parser.consume());
    } else {
      grouped = parser.parseExpression();
    }
    parser.consume(RPAREN);
    return grouped;
  }
}

class ValDeclParselet implements PrefixParselet<Token> {
  @Override
  public ValDeclExpression parse(final Parser<Token> parser, final Token token) {
    final NameExpression name;
    if(parser.lookAhead(0).getType() == LPAREN) {
      parser.consume(LPAREN);
      name = new NameExpression(parser.consume());
      parser.consume(RPAREN);
    } else {
      name = new NameExpression(parser.consume(NAME));
    }
    parser.consume(EQUALS);
    final Expression val = parser.parseExpression();
    return new ValDeclExpression(name, val);
    // if(parser.lookAhead(0, NAME, EQUALS)) {
    //   final Token name = parser.consume();
    //   final Token eq = parser.consume();
    //   final Expression val = parser.parseExpression();
    //   return new ValDeclExpression(new NameExpression(name), val);
    // } else {
    //   throw new ParseException(parser.lookAhead(0).getType(), NAME);
    // }
  }
}

class FnParselet implements PrefixParselet<Token> {
  @Override
  public FnExpression parse(final Parser<Token> parser, final Token token) {
    final List<NameExpression> names = new LinkedList<>();
    while(parser.lookAhead(0).getType() == NAME) {
      names.add(new NameExpression(parser.consume()));
    }
    parser.consume(RARROW);
    final Expression body = parser.parseExpression();
    return new FnExpression(body, names.toArray(new NameExpression[names.size()]));
  }
}

class MatchParselet implements PrefixParselet<Token> {
  @Override
  public MatchExpression parse(final Parser<Token> parser, final Token token) {
    final Expression val = parser.parseExpression();
    parser.consume(WITH);
    final List<CaseExpression> cases = new LinkedList<>();
    while(parser.lookAhead(0).getType() == VBAR) {
      parser.consume(VBAR);
      final Expression pattern = parser.parseExpression();
      parser.consume(RARROW);
      final Expression result = parser.parseExpression();
      cases.add(new CaseExpression(new PatternExpression(pattern), result));
    }
    if(parser.lookAhead(0).getType() == ELSE) {
      parser.consume(ELSE);
      final Expression result = parser.parseExpression();
      cases.add(new CaseExpression(PatternExpression.WILDCARD, result));
    }
    parser.consume(END);
    return new MatchExpression(val, cases.toArray(new CaseExpression[cases.size()]));
  }
}
