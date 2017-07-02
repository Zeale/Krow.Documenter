package kr�w.zeale.math;

import java.util.ArrayList;
import java.util.Collection;

import kr�w.zeale.math.exceptions.EmptyEquationException;
import kr�w.zeale.math.exceptions.IrregularCharacterException;
import kr�w.zeale.math.exceptions.UnmatchedParenthesisException;

class Equation extends ArrayList<Object> {
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

	public double evaluate() throws EmptyEquationException, UnmatchedParenthesisException, IrregularCharacterException {

		for (byte precedence = 3; precedence > -1; precedence--)
			for (int i = 2; i < size() && i > 0; i += 2)
				if (((Operation) get(--i)).getPrecedence() == precedence) {
					popin(new Number(((Operation) remove(i))
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