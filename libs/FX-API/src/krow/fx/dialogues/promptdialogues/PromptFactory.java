package krow.fx.dialogues.promptdialogues;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import krow.fx.dialogues.promptdialogues.prompts.IncrementablePrompt;

public final class PromptFactory {
	public static String promptString(String prompt, String hint) {

		Dialog<String> dialog = new Dialog<>();

		Text promptText = new Text(prompt);
		TextField field = new TextField();
		field.setPromptText(hint);
		Button doneButton = new Button("Continue");

		doneButton.setOnAction(event -> {
			String result = field.getText();
			dialog.setResult(result);
			dialog.close();
		});

		VBox wrapper = new VBox(5);
		wrapper.getChildren().addAll(promptText, field, doneButton);

		dialog.getDialogPane().setContent(wrapper);

		dialog.setResultConverter(param -> {
			if (!param.getButtonData().isDefaultButton())
				return null;
			return field.getText();
		});

		return dialog.showAndWait().orElse(null);

	}

	public static String promptString(String string) {
		return promptString(string, null);
	}

	public static class NumberPrompt<K> extends IncrementablePrompt<K, Number> {

		public void setValue(String value) {
			setValue(value);
		}

		@Override
		public void setValue(Number number) {
			field.setText("" + number);
		}

		{
			field.setOnKeyTyped(event -> {
				for (char c : event.getCharacter().toCharArray()) {
					if (c == '.')
						if (field.getText().contains("."))
							event.consume();
						else
							;
					else if (c == '-') {
						// Cache the caret position.
						int carPos = field.getCaretPosition();

						if (field.getText().contains("-")) {
							// Remove the '-' in front.
							field.setText(field.getText().substring(1));
							field.positionCaret(carPos - 1);// A char was removed.
						} else {
							field.setText('-' + field.getText());
							field.positionCaret(carPos + 1);// A char was added.
						}

						event.consume();
					} else if (!Character.isDigit(c))
						event.consume();

				}
			});

			field.caretPositionProperty().addListener((ChangeListener<Number>) (observable, oldValue, newValue) -> {
				if (field.getText().contains("-") && newValue.intValue() < 1)
					field.positionCaret(1);
			});

		}

		public NumberPrompt(PromptDialogue<? super K, ? super Number> owner, K key, String description) {
			super(owner, key, description);
		}

		public NumberPrompt(PromptDialogue<? super K, ? super Number> owner, K key, String description,
				Number defaultValue) {
			super(owner, key, description);
			setValue(defaultValue);
		}

		public NumberPrompt(PromptDialogue<? super K, ? super Number> owner, K key, String description,
				String defaultValue) {
			super(owner, key, description);
			setValue(defaultValue);
		}

		@Override
		public Number getValue() {
			return Double.parseDouble(field.getText());
		}

		@Override
		protected boolean verifyValue() {
			try {
				Double.parseDouble(field.getText());
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		protected void increment(double amount) {
			double numb = 0;
			CALC: try {
				numb = Double.parseDouble(getText());

				if (numb >= -1 && numb <= 0.1) {
					numb = (int) numb + 1;
					break CALC;
				}

				numb += (numb > 0 ? Math.pow(10, Math.floor(Math.log10(Math.abs(numb))))
						: Math.pow(10, Math.ceil(Math.log10(Math.abs(numb))) - 1));
			} catch (Exception e) {
			} finally {
				field.setText("" + numb);
			}
		}

		@Override
		protected void decrement(double amount) {

			double numb = 0;
			CALC: try {
				numb = Double.parseDouble(getText());

				if (numb <= 1 && numb >= -0.1) {
					numb = (int) numb - 1;
					break CALC;
				}

				numb -= (numb < 0 ? Math.pow(10, Math.floor(Math.log10(Math.abs(numb))))
						: Math.pow(10, Math.ceil(Math.log10(Math.abs(numb))) - 1));

			} catch (Exception e) {
			} finally {
				field.setText("" + numb);
			}

		}

	}

	public static class DateStringPrompt<K> extends PromptDialogue<? super K, ? super Date>.TextFieldPrompt<Date> {

		private DateFormat formatter = new SimpleDateFormat();
		private Button currentDate = new Button("�");

		// Used to display the input field and the current date button next to each
		// other.
		private HBox content = new HBox(5, field, currentDate);
		{
			addContent(content);
			currentDate.setOnAction(event -> setCurrentDate());
		}

		/**
		 * Sets the value of this {@link DateStringPrompt} as the current date.
		 */
		public void setCurrentDate() {
			setValue(new Date());
		}

		public DateStringPrompt(PromptDialogue<? super K, ? super Date> owner, K key, String description) {
			owner.super(key, description);
		}

		public DateStringPrompt(PromptDialogue<? super K, ? super Date> owner, K key, String description,
				Date defaultValue) {
			owner.super(key, description);
			setValue(defaultValue);
		}

		public DateStringPrompt(PromptDialogue<? super K, ? super Date> owner, K key, String description,
				String defaultValue) {
			owner.super(key, description);
			setValue(defaultValue);
		}

		public void setValue(String value) {
			setText(value);
		}

		@Override
		public void setValue(Date value) {
			setValue(value.toString());
		}

		@Override
		protected boolean verifyValue() {
			try {
				formatter.parse(getText());
				return true;
			} catch (ParseException e) {
				return false;
			}
		}

		@Override
		protected Date getValue() {
			try {
				return formatter.parse(getText());
			} catch (ParseException e) {
			}
			return null;
		}

	}

}
