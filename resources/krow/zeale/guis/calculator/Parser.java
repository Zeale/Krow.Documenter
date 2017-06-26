package krow.zeale.guis.calculator;

import krow.zeale.guis.calculator.exceptions.EmptyEquationException;
import krow.zeale.guis.calculator.exceptions.UnmatchedParenthesisException;

public class Parser {

	static interface Operation {

		default byte getPrecedence() {
			return 2;
		}

		final Parser.Operation ADD = new Operation() {
			@Override
			public byte getPrecedence() {
				return 0;
			}

			@Override
			public double evaluate(double input1, double input2) {
				return input1 + input2;
			}

		};

		final Parser.Operation NONE = new Operation() {

			@Override
			public double evaluate(double input1, double input2) {
				return input1;
			}

		};

		final Parser.Operation SUBTRACT = new Operation() {
			@Override
			public byte getPrecedence() {
				return 0;
			}

			@Override
			public double evaluate(double input1, double input2) {
				return input1 - input2;
			}

		};

		final Parser.Operation MULTIPLY = new Operation() {
			@Override
			public byte getPrecedence() {
				return 1;
			}

			@Override
			public double evaluate(double input1, double input2) {
				return input1 * input2;
			}

		};

		final Parser.Operation DIVIDE = new Operation() {
			@Override
			public byte getPrecedence() {
				return 1;
			}

			@Override
			public double evaluate(double input1, double input2) {
				return input1 / input2;
			}

		};

		final Parser.Operation POWER = new Operation() {
			@Override
			public byte getPrecedence() {
				return 2;
			}

			@Override
			public double evaluate(double input1, double input2) {
				return Math.pow(input1, input2);
			}
		};

		final Parser.Operation MODULUS = new Operation() {

			@Override
			public double evaluate(double input1, double input2) {
				return input1 % input2;
			}

			@Override
			public byte getPrecedence() {
				return 1;
			}
		};

		double evaluate(double input1, double input2);

		public static Parser.Operation getOperation(char c) {
			if (!isOperator(c))
				throw new NumberFormatException();
			switch (c) {
			case '+':
				return ADD;
			case '-':
				return SUBTRACT;
			case '*':
			case 'x':
				return MULTIPLY;
			case '/':
			case '�':
				return DIVIDE;
			case '^':
				return POWER;
			case '%':
				return MODULUS;
			default:
				throw new NumberFormatException();
			}
		}

		public static Parser.Operation getOperation(String c) {
			return getOperation(c.charAt(0));
		}

	}

	static interface Element {
		public static abstract class Function implements Parser.Element {

			protected final String params;

			public Function(String input) {
				this.params = input;
			}

			/**
			 * <p>
			 * Automatically takes the string input and parses it using the
			 * default parser. If your Function does not read its parameter
			 * contents abnormally, you should use this method to evaluate
			 * your parameters so you can read it as a number, rather than
			 * parse it yourself as a String.
			 * <p>
			 * This method takes {@link #params} and parses it as a
			 * mathematical equation. The result is returned.
			 * <p>
			 * {@link #params} isn't modified through this method so
			 * repeated calls to {@link #autoParse()} should return the same
			 * value. (So long as {@link #params} isn't modified
			 * externally.)
			 * 
			 * @return The parsed value of {@link #params}. (Assuming that
			 *         {@link #params} is a valid equation.)
			 * @throws UnmatchedParenthesisException
			 *             Incase {@link #params} has unmatched parentheses
			 * @throws EmptyEquationException
			 *             Incase {@link #params} is empty.
			 */
			protected double autoParse() throws EmptyEquationException, UnmatchedParenthesisException {
				return new Parser().evaluate(params);
			}

			public static Element.Function getFunction(String name, String input) {
				switch (name.toLowerCase()) {
				case "log":
					return new Log(input);
				case "log10":
					return new Log10(input);
				case "loge":
					return new Log(input);
				case "sqrt":
					return new Sqrt(input);
				default:
					return null;
				}
			}

			public static final class Log extends Element.Function {

				public Log(String input) {
					super(input);
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * krow.zeale.guis.calculator.Calculator.Parser.Element#
				 * evaluate()
				 */
				@Override
				public double evaluate() throws EmptyEquationException, UnmatchedParenthesisException {
					return Math.log(autoParse());
				}

			}

			public static final class Log10 extends Element.Function {

				public Log10(String input) {
					super(input);
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * krow.zeale.guis.calculator.Calculator.Parser.Element#
				 * evaluate()
				 */
				@Override
				public double evaluate() throws EmptyEquationException, UnmatchedParenthesisException {
					return Math.log10(autoParse());
				}
			}

			public static final class Sqrt extends Element.Function {

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * krow.zeale.guis.calculator.Calculator.Parser.Element#
				 * evaluate()
				 */
				@Override
				public double evaluate() throws EmptyEquationException, UnmatchedParenthesisException {
					return Math.sqrt(autoParse());
				}

				public Sqrt(String input) {
					super(input);
				}

			}

		}

		static class Number implements Parser.Element {

			@Deprecated
			public void chain(Parser.Operation operation, Parser.Element nextElement) {
				this.operation = operation;
				this.nextElement = nextElement;
			}

			private double value;
			@Deprecated
			private Parser.Operation operation;
			@Deprecated
			private Parser.Element nextElement;

			public Number(double value) {
				this.value = value;
			}

			@SuppressWarnings("unused")
			@Deprecated
			public Number(double value, Parser.Operation operation, Parser.Element nextElement) {
				this.value = value;
				this.operation = operation;
				this.nextElement = nextElement;
			}

			@Override
			public double evaluate() throws EmptyEquationException, UnmatchedParenthesisException {
				if (!(operation == null || nextElement == null)) {
					return operation.evaluate(value, nextElement.evaluate());
				}
				return value;
			}

		}

		public double evaluate() throws EmptyEquationException, UnmatchedParenthesisException;
	}

	public double evaluate(String equation) throws EmptyEquationException, UnmatchedParenthesisException {
		if (equation.isEmpty())
			throw new EmptyEquationException();
		reset();
		this.equation = equation;

		Equation equ = new Equation();
		Parser.Element e = getElement();
		equ.start(e);
		while (position < equation.length()) {
			equ.add(getOperation(), getElement());
		}

		return equ.evaluate();
	}

	private volatile String equation;
	private int position;

	private Element.Number getNumber() {
		if (!isNumb())
			throw new NumberFormatException();
		// Forward length and backward length.
		int flen = 0, blen = 0;
		while (position + --blen > -1 && isNumb(position + blen))
			;
		blen++;
		while (position + ++flen < equation.length() && isNumb(position + flen))
			;
		double value = Double.valueOf(equation.substring(position + blen, position + flen));
		position += flen;
		return new Element.Number(value);

	}

	private Parser.Element getElement() throws UnmatchedParenthesisException {
		if (isOperator(getCurrChar()))
			throw new NumberFormatException();
		if (isNumb())
			return getNumber();
		if (isFunc(position))
			return getFunction();
		throw new NumberFormatException();
	}

	private Element.Function getFunction() throws UnmatchedParenthesisException {
		if (!isFunc(position))
			throw new NumberFormatException();
		int flen = -1, blen = 0;
		while (position + --blen > -1 && isFunc(position + blen))
			;
		blen++;
		while (position + ++flen < equation.length() && isFunc(position + flen))
			;
		String name = equation.substring(position + blen, position + flen);
		position += flen;// This covers the opening parenthesis as well as
							// the function's name...
		int posSubEquOpen = ++position;

		for (int parentheses = 1; parentheses > 0; nextChar()) {

			if (position >= equation.length())
				throw new UnmatchedParenthesisException();
			else if (getCurrChar().equals("("))
				parentheses++;
			else if (getCurrChar().equals(")"))
				parentheses--;
		}
		return Element.Function.getFunction(name, equation.substring(posSubEquOpen, (position) - 1));
	}

	private boolean isFunc(int pos) {
		// TODO Implement an iteration technique once we add constants.
		return !(isNumb(pos) || isOperator(equation.charAt(pos)) || equation.charAt(pos) == '('
				|| equation.charAt(pos) == ')');
	}

	private Parser.Operation getOperation() {
		if (isNumb())
			throw new NumberFormatException();
		// Forward length and backward length.
		int flen = 0, blen = 0;
		while (position + --blen > -1 && isOperator(equation.charAt(position + blen)))
			;
		blen++;
		while (position + ++flen < equation.length() && isOperator(equation.charAt(position + flen)))
			;
		// For now each operation should be one character long, but better
		// safe than sorry.
		Parser.Operation operation = Operation.getOperation(equation.substring(position + blen, position + flen));
		position += flen;
		return operation;
	}

	private static boolean isOperator(char c) {
		return c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == 'x' || c == '�' || c == '%';
	}

	private static boolean isOperator(String c) {
		return isOperator(c.charAt(0));
	}

	private boolean isNumb(int position) {
		if (position < 0 || position >= equation.length())
			return false;
		int flen = 0, blen = 0;
		if (isOperator(equation.charAt(position)))
			return false;
		// Check behind the given position by iterating through each
		// character.
		while (position + --blen > -1) {
			// Get the character at the current position.
			char c = equation.charAt(position + blen);

			if (Character.isDigit(c) || c == '.')// If its part of a numb
				continue;
			else if (c == 'x')// Both operator and part of func name
				continue;
			else if (Character.isAlphabetic(c) || c == '(')// It's a
															// function.
				return false;
			else if (isOperator(c))// No letters behind the
									// given position. Now to
									// check infront.
				break;
		}
		// Check infront of the given position...
		while (true) {
			if (!(position + ++flen < equation.length())) {
				return true;
			}
			char c = equation.charAt(position + flen);

			if (Character.isDigit(c) || c == '.')// If its part of a numb
				continue;
			else if (c == 'x')// Both an operator and part of func name; we
								// can't determine anything with this.
				continue;
			else if (Character.isAlphabetic(c) || c == ')')// It's a
															// function.
				return false;
			else if (isOperator(c))// No letters infront or
									// behind the given
									// position. This is
									// definitely a number...
				return true;
		}
	}

	private boolean isNumb() {
		return isNumb(position);
	}

	private String getNextChar() {
		return equation.substring(position + 1, position + 1);
	}

	private String getCurrChar() {
		return equation.substring(position, position + 1);
	}

	/**
	 * Increments the position <b>after</b> returning the current character.
	 * 
	 * @return The current char.
	 */
	private String nextChar() {
		return equation.substring(position, ++position);
	}

	/**
	 * <p>
	 * Much like the {@link #nextChar()} method, this method will return the
	 * current character and move the pinhead (position) down to the
	 * previous character.
	 * 
	 * 
	 * 
	 * @return
	 */
	private String previousChar() {
		return equation.substring(position, position-- + 1);
	}

	private String getPreviousChar() {
		return equation.substring(position - 1, position);
	}

	private void reset() {
		position = 0;
	}

}