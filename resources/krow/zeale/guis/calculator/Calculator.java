package krow.zeale.guis.calculator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import krow.zeale.guis.calculator.Calculator.Parser.EmptyEquationException;
import krow.zeale.guis.calculator.Calculator.Parser.UnmatchedParenthesisException;
import wolf.zeale.guis.Window;

public class Calculator {

	private Stage stage = new Stage();
	private CalculatorController controller;
	private Parser parser = new Parser();

	public Calculator() {
		FXMLLoader loader = new FXMLLoader(Calculator.class.getResource("Calculator.fxml"));
		try {
			stage.setScene(new Scene(loader.load()));
			stage.initStyle(StageStyle.UNDECORATED);
		} catch (IOException e) {
			// This block should never run. Just like in the FileManager class.
		}
		controller = loader.<CalculatorController>getController();
		controller.setOnClose(this::hide);
		controller.setParser(parser);
		Window.setPaneDraggableByNode(stage, stage.getScene().getRoot());
		Window.setPaneDraggableByNode(stage, controller.menuBar);
	}

	public final double getHeight() {
		return stage.getHeight();
	}

	public final String getTitle() {
		return stage.getTitle();
	}

	public final double getWidth() {
		return stage.getWidth();
	}

	public final double getX() {
		return stage.getX();
	}

	public final double getY() {
		return stage.getY();
	}

	public void hide() {
		stage.hide();
	}

	public final boolean isFocused() {
		return stage.isFocused();
	}

	public final boolean isFullScreen() {
		return stage.isFullScreen();
	}

	public final boolean isIconified() {
		return stage.isIconified();
	}

	public final boolean isMaximized() {
		return stage.isMaximized();
	}

	public final boolean isShowing() {
		return stage.isShowing();
	}

	public final void setFullScreen(boolean value) {
		stage.setFullScreen(value);
	}

	public final void setIconified(boolean value) {
		stage.setIconified(value);
	}

	public final void setMaximized(boolean value) {
		stage.setMaximized(value);
	}

	public final void setTitle(String value) {
		stage.setTitle(value);
	}

	public final void setX(double value) {
		stage.setX(value);
	}

	public final void setY(double value) {
		stage.setY(value);
	}

	public final void show() {
		stage.show();
	}

	public double calculate() throws EmptyEquationException, UnmatchedParenthesisException {
		double result = parser.evaluate(controller.getEquation());
		controller.setEquation(Double.toString(result));
		return result;
	}

	public static class Parser {

		public class EmptyEquationException extends Exception {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		}

		public class UnmatchedParenthesisException extends Exception {

			/**
			 * SVUID
			 */
			private static final long serialVersionUID = 1L;

		}

		private static class Equation extends ArrayList<Object> {
			/**
			 * SVUID
			 */
			private static final long serialVersionUID = 1L;
			private boolean started;

			@Override
			public boolean add(Object e) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void add(int index, Object element) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean addAll(Collection<? extends Object> c) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean addAll(int index, Collection<? extends Object> c) {
				throw new UnsupportedOperationException();
			}

			public void start(Element element) {
				if (started)
					throw new UnsupportedOperationException();
				super.add(element);
				started = true;
			}

			public void add(Operation operation, Element element) {
				if (!started)
					throw new UnsupportedOperationException();
				super.add(operation);
				super.add(element);
			}

			public double evaluate() throws EmptyEquationException, UnmatchedParenthesisException {

				for (byte precedence = 3; precedence > -1; precedence--)
					for (int i = 2; i < size() && i > 0; i += 2)
						if (((Operation) get(--i)).getPrecedence() == precedence) {
							popin(new Element.Number(((Operation) remove(i))
									.evaluate(((Element) remove(--i)).evaluate(), ((Element) remove(i)).evaluate())),
									i);
							i -= 2;
						} else
							i++;
				return ((Element) get(0)).evaluate();
			}

			private void popin(Element element, int location) {
				super.add(location, element);
			}

			private void popin(Operation operation, int location) {
				super.add(location, operation);
			}
		}

		private static interface Operation {

			default byte getPrecedence() {
				return 2;
			}

			final Operation ADD = new Operation() {
				@Override
				public byte getPrecedence() {
					return 0;
				}

				@Override
				public double evaluate(double input1, double input2) {
					return input1 + input2;
				}

			};

			final Operation NONE = new Operation() {

				@Override
				public double evaluate(double input1, double input2) {
					return input1;
				}

			};

			final Operation SUBTRACT = new Operation() {
				@Override
				public byte getPrecedence() {
					return 0;
				}

				@Override
				public double evaluate(double input1, double input2) {
					return input1 - input2;
				}

			};

			final Operation MULTIPLY = new Operation() {
				@Override
				public byte getPrecedence() {
					return 1;
				}

				@Override
				public double evaluate(double input1, double input2) {
					return input1 * input2;
				}

			};

			final Operation DIVIDE = new Operation() {
				@Override
				public byte getPrecedence() {
					return 1;
				}

				@Override
				public double evaluate(double input1, double input2) {
					return input1 / input2;
				}

			};

			final Operation POWER = new Operation() {
				@Override
				public byte getPrecedence() {
					return 2;
				}

				@Override
				public double evaluate(double input1, double input2) {
					return Math.pow(input1, input2);
				}
			};

			final Operation MODULUS = new Operation() {

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

			public static Operation getOperation(char c) {
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

			public static Operation getOperation(String c) {
				return getOperation(c.charAt(0));
			}

		}

		private static interface Element {
			public static abstract class Function implements Element {

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

				public static Function getFunction(String name, String input) {
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

				public static final class Log extends Function {

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

				public static final class Log10 extends Function {

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

				public static final class Sqrt extends Function {

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

			static class Number implements Element {

				@Deprecated
				public void chain(Operation operation, Element nextElement) {
					this.operation = operation;
					this.nextElement = nextElement;
				}

				private double value;
				@Deprecated
				private Operation operation;
				@Deprecated
				private Element nextElement;

				public Number(double value) {
					this.value = value;
				}

				@SuppressWarnings("unused")
				@Deprecated
				public Number(double value, Operation operation, Element nextElement) {
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
			Element e = getElement();
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

		private Element getElement() throws UnmatchedParenthesisException {
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

		private Operation getOperation() {
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
			Operation operation = Operation.getOperation(equation.substring(position + blen, position + flen));
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

}
