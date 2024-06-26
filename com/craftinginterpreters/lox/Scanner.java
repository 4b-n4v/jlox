package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	private int start = 0;
	private int current = 0;
	private int line = 1;
	private static final Map<String, TokenType> keywords;

	static {
		keywords = new HashMap<>();
		keywords.put("and", AND);
		keywords.put("class", CLASS);
		keywords.put("else", ELSE);
		keywords.put("false", FALSE);
		keywords.put("for", FOR);
		keywords.put("fun", FUN);
		keywords.put("if", IF);
		keywords.put("nil", NIL);
		keywords.put("or", OR);
		keywords.put("print", PRINT);
		keywords.put("return", RETURN);
		keywords.put("super", SUPER);
		keywords.put("this", THIS);
		keywords.put("true", TRUE);
		keywords.put("var", VAR);
		keywords.put("while", WHILE);
	}

	Scanner(String source) {
		this.source = source;
	}

	List<Token> scanTokens() {
		while (!isAtEnd()) {
			// We are at the beginning of the next lexeme.
			start = current;
			scanToken();
		}

		tokens.add(new Token(EOF, "", null, line));
		return tokens;
	}

	private void scanToken() {
		char c = advance();
		switch (c) {
			// First the single character tokens.
			case '(':
				addToken(LEFT_PAREN);
				break;
			case ')':
				addToken(RIGHT_PAREN);
				break;
			case '{':
				addToken(LEFT_BRACE);
				break;
			case '}':
				addToken(RIGHT_BRACE);
				break;
			case ',':
				addToken(COMMA);
				break;
			case '.':
				addToken(DOT);
				break;
			case '-':
				addToken(MINUS);
				break;
			case '+':
				addToken(PLUS);
				break;
			case ';':
				addToken(SEMICOLON);
				break;
			case '*':
				addToken(STAR);
				break;
			// Case for two character length tokens.
			case '!':
				addToken(match('=') ? BANG_EQUAL : BANG);
				break;
			case '=':
				addToken(match('=') ? EQUAL_EQUAL : EQUAL);
				break;
			case '<':
				addToken(match('=') ? LESS_EQUAL : LESS);
				break;
			case '>':
				addToken(match('=') ? GREATER_EQUAL : GREATER);
				break;
			case '/':
				if (match('/')) {
					// A comment goes until the end of the line.
					while (peek() != '\n' && !isAtEnd())
						advance(); // Keep advancing until the end of the comment line
				} else {
					addToken(SLASH);
				}
				break;
			// Whitespace characters
			case ' ':
			case '\r':
			case '\t':
				// Ignore whitespace
				break;
			case 'n':
				line++;
				break;
			// String literals
			case '"':
				string();
				break;
			default:
				if (isDigit(c)) {
					number();
				} else if (isAlpha(c)) {
					identifier();
				} else {
					Lox.error(line, "Unexpected character.");
				}

				break;
		}
	}

	// HELPER: Scans for identifiers
	private void identifier() {
		while (isAlphaNumeric(peek()))
			advance();

		String text = source.substring(start, current);
		TokenType type = keywords.get(text);
		if (type == null)
			type = IDENTIFIER;
		addToken(IDENTIFIER);
	}

	// HELPER: Looks for integers and floating point numbers
	private void number() {
		while (isDigit(peek()))
			advance();
		// Look a fractional part.
		if (peek() == '.' && isDigit(peekNext())) {
			// Consume the "."
			advance();

			while (isDigit(peek()))
				advance();
		}

		addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
	}

	// HELPER: Looks for characters encased in " ".
	private void string() {
		while (peek() != '"' && !isAtEnd()) { // While the character is not an end quation nor at the end of the source
			if (peek() == '\n') // If character is a new line, then increment line
				line++;
			advance(); // Keep going through text
		}

		if (isAtEnd()) { // If we're at the end of the source, the string has not been terminated.
			Lox.error(line, "Unterminated string.");
			return; // Early return
		}

		// The closing ".
		advance();

		// Trim the quotation marks
		String value = source.substring(start + 1, current - 1);
		addToken(STRING, value);
	}

	// HELPER: Looks at the character but does not consume it as a lexeme.
	private char peek() {
		if (isAtEnd())
			return '\0';

		return source.charAt(current);
	}

	// HELPER: Looks at the next character but does not consume it as a lexeme.
	private char peekNext() {
		if (current + 1 >= source.length())
			return '\0';

		return source.charAt(current + 1);
	}

	// HELPER: Looks if character is alphabetic
	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') ||
				(c >= 'A' && c <= 'Z') ||
				c == '_';

	}

	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}

	// HELPER: check if character is a digit.
	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	// HELPER: Look at the next character return true if character matches current.
	private boolean match(char expected) {
		if (isAtEnd())
			return false;
		if (source.charAt(current) != expected)
			return false;

		current++;
		return true;
	}

	// HELPER: return true if current is at or exceeded the length of the source
	// text.
	private boolean isAtEnd() {
		return current >= source.length();
	}

	// HELPER: Return next character, increment current.
	private char advance() {
		return source.charAt(current++);
	}

	// Add Token
	// Function Overloading
	private void addToken(TokenType type) {
		addToken(type, null);
	}

	// Add token with specific type.
	private void addToken(TokenType type, Object literal) {
		String text = source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}
}
