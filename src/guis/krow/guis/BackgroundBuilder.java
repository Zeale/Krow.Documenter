package krow.guis;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.StrokeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import kr�w.core.Kr�w;

public final class BackgroundBuilder {

	public static abstract class BackgroundManager {

		private double animationDuration = SHAPE_MOVE_DURATION;

		protected Pane currentPane;

		private Color startColor = SHAPE_COLOR;

		private EventHandler<MouseEvent> mouseMovementHandler;

		private final ArrayList<Node> mouseDetectionNodes = new ArrayList<>();

		protected BackgroundManager(final Pane pane) {
			currentPane = pane;
			pane.setOnMouseMoved(getMouseMovementHandler());
		}

		protected BackgroundManager(final Pane currentPane, final Color startColor) {
			this(currentPane);
			this.startColor = startColor;
		}

		protected BackgroundManager(final Pane currentPane, final Color startColor, final Node... detectionNodes) {
			this(currentPane);
			this.startColor = startColor;
			for (final Node n : detectionNodes)
				n.setOnMouseMoved(getMouseMovementHandler());
		}

		protected BackgroundManager(final Pane currentPane, final Node... detectionNodes) {
			this(currentPane);
			for (final Node n : detectionNodes)
				n.setOnMouseMoved(getMouseMovementHandler());
		}

		public void addMouseDetectionNodes(final Node... nodes) {
			for (final Node n : nodes) {
				n.setOnMouseMoved(mouseMovementHandler);
				mouseDetectionNodes.add(n);
			}
		}

		public abstract void disable();

		public void disableMouseInteraction() {
			currentPane.setOnMouseMoved(null);
		}

		public abstract void enable();

		public void enableMouseInteraction() {
			currentPane.setOnMouseMoved(getMouseMovementHandler());
		}

		public abstract void fadeIn();

		public abstract void fadeOut();

		/**
		 * @return the animationDuration
		 */
		public double getAnimationDuration() {
			return animationDuration;
		}

		/**
		 * @return the currentPane
		 */
		public Pane getCurrentPane() {
			return currentPane;
		}

		protected EventHandler<MouseEvent> getMouseMovementHandler() {
			return mouseMovementHandler;
		}

		public Color getStartColor() {
			return startColor;
		}

		public void removeMouseDetectionNodes(final Node... nodes) {
			for (final Node n : nodes)
				n.setOnMouseMoved(null);
		}

		/**
		 * @param animationDuration
		 *            the animationDuration to set
		 */
		public void setAnimationDuration(final double animationDuration) {
			this.animationDuration = animationDuration;
		}

		/**
		 * @param currentPane
		 *            the currentPane to set
		 */
		public void setCurrentPane(final Pane currentPane) {
			if (this.currentPane == currentPane)
				return;
			for (final Node n : mouseDetectionNodes)
				removeMouseDetectionNodes(n);
			this.currentPane.setOnMouseMoved(null);
			this.currentPane = currentPane;
			currentPane.setOnMouseMoved(getMouseMovementHandler());
		}

		protected void setMouseMovementHandler(final EventHandler<MouseEvent> mouseMovementHandler) {
			this.mouseMovementHandler = mouseMovementHandler;
			currentPane.setOnMouseMoved(mouseMovementHandler);
		}

	}

	public static class ShapeBackgroundManager extends BackgroundManager {

		public static class ColorAnimation {

			public static ColorAnimation generateRandomColorAnimation() {
				return generateRandomColorAnimation(random.nextInt(19) + 1);
			}

			public static ColorAnimation generateRandomColorAnimation(final int colorCount) {
				final Color[] colors = new Color[colorCount];
				for (int i = 0; i < colorCount; i++)
					colors[i] = generateRandomColor();
				return new ColorAnimation(random.nextInt(random.nextInt(5) + 2) + 1, random.nextBoolean(),
						random.nextBoolean(), colors);
			}

			public static ColorAnimation[] generateRandomColorAnimations() {
				return generateRandomColorAnimations(random.nextInt(10) + 1);
			}

			public static ColorAnimation[] generateRandomColorAnimations(final int count) {
				final ColorAnimation[] animations = new ColorAnimation[count];
				for (int i = 0; i < count; i++)
					animations[i] = generateRandomColorAnimation();
				return animations;
			}

			public static ColorAnimation rainbow(final int runCount, final boolean synchronous, final boolean even) {
				return new ColorAnimation(runCount, synchronous, even, Color.RED, Color.ORANGE, Color.YELLOW,
						Color.GREEN, Color.BLUE, Color.INDIGO, Color.VIOLET);
			}

			private final Color[] colors;
			private int runCount = 1;
			private boolean synchronous, even;

			public ColorAnimation(final boolean synchronous, final boolean even, final Color... colors) {
				this.colors = colors;
				this.synchronous = synchronous;
				this.even = even;
			}

			public ColorAnimation(final boolean synchronous, final Color... colors) {
				this.colors = colors;
				this.synchronous = synchronous;
			}

			public ColorAnimation(final Color... colors) {
				this.colors = colors;
			}

			public ColorAnimation(final int runCount, final boolean synchronous, final boolean even,
					final Color... colors) {
				this.colors = colors;
				this.runCount = runCount;
				this.synchronous = synchronous;
				this.even = even;
			}

			public ColorAnimation(final int runCount, final boolean synchronous, final Color... colors) {
				this.colors = colors;
				this.runCount = runCount;
				this.synchronous = synchronous;
			}

			public ColorAnimation(final int runCount, final Color... colors) {
				this.colors = colors;
				this.runCount = runCount;
			}

			/**
			 * @return the colors
			 */
			public Color[] getColors() {
				return colors;
			}

			/**
			 * @return the runCount
			 */
			public int getRunCount() {
				return runCount;
			}

			/**
			 * @return the even
			 */
			public boolean isEven() {
				return even;
			}

			/**
			 * @return the synchronous
			 */
			public boolean isSynchronous() {
				return synchronous;
			}

		}

		private static Color generateRandomColor() {
			return new Color(random.nextDouble(), random.nextDouble(), random.nextDouble(), 1);
		}

		private final Stack<ColorAnimation> animation = new Stack<>();

		private boolean repeatColorAnimation;

		private int shapesColored = 0, runCount = 0;

		private final ArrayList<Shape> shapes = new ArrayList<>();

		private double customShapeSize = SHAPE_RADIUS;

		{
			setMouseMovementHandler(event -> {
				final double mouseX = event.getSceneX(), mouseY = event.getSceneY();
				for (final Shape s : getShapes())
					if (!s.getProperties().containsKey(IS_BEING_SHOVED_KEY)
							&& Kr�w.getProgramSettings().isShapeBackgroundRespondToMouseMovement() && !isDisabled()) {
						final double shapeX = s.getLayoutX() + s.getTranslateX(),
								shapeY = s.getLayoutY() + s.getTranslateY();

						final double distX = mouseX - shapeX, distY = mouseY - shapeY;
						final double distance = Math.sqrt(distX * distX + distY * distY);

						if (distance <= customShapeSize) {

							final TranslateTransition translator = (TranslateTransition) s.getProperties()
									.get(TRANSLATOR_KEY);

							translator.stop();

							translator.setByX(-distX * 3);
							translator.setByY(-distY * 3);
							translator.setDuration(
									Duration.seconds((1 - random.nextDouble() / 8) * getAnimationDuration() / 3));

							final EventHandler<ActionEvent> onFinished = translator.getOnFinished();
							s.getProperties().put(IS_BEING_SHOVED_KEY, true);
							final Interpolator interpolator = translator.getInterpolator();
							translator.setOnFinished(event1 -> {
								s.getProperties().remove(IS_BEING_SHOVED_KEY);
								translator.setOnFinished(onFinished);
								translator.setInterpolator(interpolator);
								onFinished.handle(event1);
							});
							translator.setInterpolator(Interpolator.EASE_OUT);
							translator.play();
							Kr�w.getSoundManager().playSound(Kr�w.getSoundManager().POP, 1f);
						}
					}
			});
		}

		private boolean rotatable;

		private boolean disabled;

		protected ShapeBackgroundManager(final Pane pane) {
			super(pane);
		}

		protected ShapeBackgroundManager(final Pane currentPane, final Color startColor) {
			super(currentPane, startColor);
		}

		public void addColorAnimations(final ColorAnimation... animations) {
			for (final ColorAnimation ca : animations)
				animation.add(0, ca);
		}

		public void addRandomShape() {

			buildShape(ShapeFactory.buildRegularShape(100, random.nextInt(14) + 3));

			/*
			 * switch (random.nextInt(3)) {
			 *
			 * case 1: buildShape(new Circle(customShapeSize / 2)); break; case
			 * 2: buildShape(new Rectangle(customShapeSize, customShapeSize));
			 * break;
			 *
			 * default: case 0: buildShape(ShapeFactory.buildTriangle()); break;
			 * }
			 */

		}

		public void addRandomShapes(int count) {
			for (; count > 0; count--)
				addRandomShape();
		}

		public void addShape(final Shape shape) {
			buildShape(shape);
		}

		public void addShapes(final Shape... shapes) {
			for (final Shape s : shapes)
				buildShape(s);
		}

		private void addShapeToPane(final Shape s) {
			currentPane.getChildren().add(0, s);
		}

		private void animate(final Shape c) {

			final TranslateTransition translator = (TranslateTransition) c.getProperties().get(TRANSLATOR_KEY);

			final double multiplier = generateRandomMultiplier();
			translator.setByX(calculateByX(c.getLayoutX(), c.getTranslateX()) * multiplier);
			translator.setByY(calculateByY(c.getLayoutY(), c.getTranslateY()) * multiplier);
			translator.setDuration(Duration.seconds(getAnimationDuration() * multiplier));
			translator.play();
		}

		private void animateColors() {
			shapesColored = 0;

			int i = 0;

			// Get first color animation.
			ColorAnimation currentAnimation = animation.peek();
			if (currentAnimation.getRunCount() <= runCount)
				currentAnimation = animation.pop();

			// Apply it to each shape.
			for (final Shape s : getShapes()) {
				// Get transition
				final StrokeTransition st = (StrokeTransition) s.getProperties().get(COLORER_KEY);

				// Handle time.
				st.setDuration(Duration.seconds(currentAnimation.isSynchronous() ? getAnimationDuration()
						: getAnimationDuration() * generateRandomMultiplier()));

				// Handle color distribution.
				if (currentAnimation.isEven()) {
					st.setToValue(currentAnimation.getColors()[i % currentAnimation.getColors().length]);
					i++;
				} else
					st.setToValue(
							currentAnimation.getColors()[(int) (Math.random() * currentAnimation.getColors().length)]);
			}

			// If the animation loops.
			if (currentAnimation.getRunCount() <= runCount) {
				if (repeatColorAnimation)
					animation.add(0, currentAnimation);// Add this to the front.
				runCount = 0;
			}
			for (final Shape s : getShapes())
				((StrokeTransition) s.getProperties().get(COLORER_KEY)).play();

			runCount++;

		}

		public void animateShapes() {
			for (final Shape s : shapes)
				((TranslateTransition) s.getProperties().get(TRANSLATOR_KEY)).play();
		}

		public void buildRotator(final RotateTransition rt) {
			rt.stop();
			rt.setToAngle(Double.NaN);
			rt.setDuration(Duration.seconds(getAnimationDuration() * generateRandomMultiplier()));
			rt.setByAngle(random.nextDouble() * 720);
			rt.setOnFinished(event -> {
				rt.setDuration(Duration.seconds(getAnimationDuration() * generateRandomMultiplier()));
				rt.setByAngle((random.nextDouble() - 0.5) * 720);
				rt.play();
			});
		}

		private void buildShape(final Shape s) {
			if (s.getProperties().containsKey(BUILT_KEY) && s.getProperties().get(BUILT_KEY).equals(true))
				return;

			s.setStroke(SHAPE_COLOR);
			s.setFill(Color.TRANSPARENT);

			final Effect effect = new DropShadow(BlurType.GAUSSIAN, SHAPE_COLOR, 20, 0.5, 0, 0);
			s.setEffect(effect);
			s.getProperties().put(EFFECT_KEY, effect);

			s.setLayoutX(0);
			s.setLayoutY(0);

			s.setTranslateX(random.nextInt((int) Kr�w.getSystemProperties().getScreenWidth()) - customShapeSize);
			s.setTranslateY(random.nextInt((int) Kr�w.getSystemProperties().getScreenHeight()) - customShapeSize);

			final TranslateTransition translator = new TranslateTransition();
			final RotateTransition rotator = new RotateTransition();
			final StrokeTransition colorer = new StrokeTransition();
			final FadeTransition fader = new FadeTransition();
			final ScaleTransition scaler = new ScaleTransition();

			translator.setNode(s);
			rotator.setNode(s);
			colorer.setShape(s);
			fader.setNode(s);
			scaler.setNode(s);

			s.getProperties().put(TRANSLATOR_KEY, translator);
			s.getProperties().put(ROTATOR_KEY, rotator);
			s.getProperties().put(COLORER_KEY, colorer);
			s.getProperties().put(FADER_KEY, fader);
			s.getProperties().put(SCALER_KEY, scaler);

			translator.setDuration(Duration.seconds(generateRandomMultiplier() * getAnimationDuration()));
			translator.setByX(calculateByX(s.getLayoutX(), s.getTranslateX()));
			translator.setByY(calculateByY(s.getLayoutY(), s.getTranslateY()));
			translator.setInterpolator(Interpolator.EASE_OUT);
			translator.setOnFinished(event -> animate(s));

			s.setCache(true);
			s.setCacheHint(CacheHint.SPEED);

			shapes.add(s);
			addShapeToPane(s);

			s.getProperties().put(BUILT_KEY, true);
		}

		private double calculateByX(final double layoutX, final double translateX) {
			double numb = generateRand();
			if (numb + layoutX + translateX > Kr�w.getSystemProperties().getScreenWidth())
				numb -= 100;

			if (numb + layoutX + translateX < 0)
				numb += 100;

			return numb;
		}

		private double calculateByY(final double layoutY, final double translateY) {
			double numb = generateRand();
			if (numb + layoutY + translateY > Kr�w.getSystemProperties().getScreenHeight())
				numb -= 100;

			if (numb + layoutY + translateY < 0)
				numb += 100;

			return numb;
		}

		@Override
		public void disable() {
			disabled = true;
			stopAnimations();
		}

		public void disableGlow() {
			for (final Shape s : getShapes())
				disableGlow(s);
		}

		public void disableGlow(final Shape s) {
			if (s.getProperties().containsKey(EFFECT_KEY))
				s.setEffect(null);
		}

		@Override
		public void enable() {
			disabled = false;
			for (final Shape s : getShapes()) {
				final TranslateTransition translator = (TranslateTransition) s.getProperties().get(TRANSLATOR_KEY);
				final RotateTransition rotator = (RotateTransition) s.getProperties().get(ROTATOR_KEY);
				final StrokeTransition colorer = (StrokeTransition) s.getProperties().get(COLORER_KEY);
				translator.play();
				rotator.play();
				colorer.play();
			}
		}

		public void enableGlow() {
			for (final Shape s : getShapes())
				enableGlow(s);
		}

		public void enableGlow(final Shape s) {
			if (s.getProperties().containsKey(EFFECT_KEY))
				s.setEffect((Effect) s.getProperties().get(EFFECT_KEY));
		}

		@Override
		public void fadeIn() {
			fadeIn(Duration.seconds(generateRandomMultiplier() * getAnimationDuration() / 6));
		}

		public void fadeIn(final Duration time) {

			new Object() {
				private volatile int amountFaded = 0;
				{

					for (final Shape s : getShapes()) {
						final FadeTransition fader = (FadeTransition) s.getProperties().get(FADER_KEY);
						fader.stop();
						fader.setOnFinished(event -> {
							amountFaded++;
							fader.setOnFinished(null);
							if (amountFaded == getShapes().size())
								enable();
						});
						fader.setDuration(time);
						fader.setToValue(1);
						fader.play();
					}
				}
			};
		}

		@Override
		public void fadeOut() {
			fadeOut(Duration.seconds(generateRandomMultiplier() * getAnimationDuration() / 6));
		}

		public void fadeOut(final Duration time) {
			disable();
			for (final Shape s : getShapes()) {
				final FadeTransition fader = (FadeTransition) s.getProperties().get(FADER_KEY);
				fader.stop();
				fader.setOnFinished(null);
				fader.setDuration(time);
				fader.setToValue(0);
				fader.play();
			}
		}

		private void fadeShapesToColor(final Shape s, final Color color) {
			fadeShapeToColor(s, color, Duration.seconds(getAnimationDuration() * generateRandomMultiplier()));
		}

		private void fadeShapeToColor(final Shape s, final Color color, final Duration duration) {
			final StrokeTransition st = (StrokeTransition) s.getProperties().get(COLORER_KEY);
			st.stop();
			st.setToValue(color);
			st.setDuration(duration);
			st.play();
		}

		private double generateRand() {
			return generateRand(random.nextBoolean());
		}

		private double generateRand(final boolean positive) {
			return (random.nextInt(50) + 50) * (positive ? 1 : -1);
		}

		private double generateRandomMultiplier() {
			return 1 - random.nextDouble() / 8;
		}

		/**
		 * @return the customShapeSize
		 */
		public double getCustomShapeSize() {
			return customShapeSize;
		}

		private ArrayList<Shape> getShapes() {
			return shapes;
		}

		/**
		 * @return the disabled
		 */
		public final boolean isDisabled() {
			return disabled;
		}

		public boolean isRotatable() {
			return rotatable;
		}

		public void playColorAnimations() {
			for (final Shape s : getShapes())
				((StrokeTransition) s.getProperties().get(COLORER_KEY)).setOnFinished(event -> {
					setGlowColor(s, (Color) s.getStroke());
					shapesColored++;
					if (shapesColored >= getShapes().size() && !animation.isEmpty())
						animateColors();
				});
			animateColors();
		}

		public void realignRotations() {
			for (final Shape s : shapes) {
				final RotateTransition rt = (RotateTransition) s.getProperties().get(ROTATOR_KEY);
				rt.stop();
				rt.setToAngle(0);
				rt.setDuration(Duration.seconds(generateRandomMultiplier() * getAnimationDuration() / 2));
				rt.setOnFinished(null);
				rt.play();
			}
		}

		public void removeAllShapes() {
			shapes.clear();
		}

		public void resetColor() {
			setColor(getStartColor());
		}

		public void setColor(final Color newColor) {
			for (final Shape s : getShapes())
				fadeShapesToColor(s, newColor);
		}

		public void setColorAnimations(final boolean repeat, final ColorAnimation... animations) {
			stopColorAnimations();
			repeatColorAnimation = repeat;

			animation.clear();
			for (final ColorAnimation ca : animations)
				animation.add(0, ca);

		}

		public void setColorsEvenly(final Color... colors) {
			if (colors.length == 0)
				return;
			int i = 0;
			for (final Shape s : getShapes())
				fadeShapesToColor(s, colors[i++ % colors.length]);

		}

		public void setColorsRandomly(final Color... colors) {
			if (colors.length == 0)
				return;
			for (final Shape s : getShapes())
				fadeShapesToColor(s, colors[(int) (Math.random() * colors.length)]);
		}

		@Override
		public void setCurrentPane(final Pane currentPane) {
			if (this.currentPane == currentPane)
				return;
			for (final Shape s : getShapes())
				getCurrentPane().getChildren().remove(s);
			super.setCurrentPane(currentPane);
			for (final Shape s : getShapes())
				addShapeToPane(s);
		}

		/**
		 * @param customShapeSize
		 *            the customShapeSize to set
		 */
		public void setCustomShapeSize(final double customShapeSize) {
			this.customShapeSize = customShapeSize;
		}

		/**
		 * @param disabled
		 *            the disabled to set
		 */
		public final void setDisabled(final boolean disabled) {
			this.disabled = disabled;
		}

		public void setEvenShapeColorsSynchronously(final Color... colors) {
			final int i = 0;
			for (final Shape s : getShapes())
				fadeShapeToColor(s, colors[i % colors.length], Duration.seconds(getAnimationDuration()));
		}

		private void setGlowColor(final Shape s, final Color c) {
			if (s.getEffect() != null && s.getEffect() instanceof DropShadow)
				((DropShadow) s.getEffect()).setColor(c);
		}

		public void setRandomColor() {
			setColor(generateRandomColor());
		}

		public void setRandomColors() {
			for (final Shape s : getShapes())
				fadeShapesToColor(s, generateRandomColor());
		}

		public void setRandomShapeColorsSynchronously() {
			for (final Shape s : getShapes())
				fadeShapeToColor(s, generateRandomColor(),
						Duration.seconds(generateRandomMultiplier() * getAnimationDuration()));
		}

		public void setRandomShapeColorsSynchronously(int count) {
			if (count > shapes.size())
				count = shapes.size();
			final Color[] colors = new Color[count];
			for (int i = 0; i < count; i++)
				colors[i] = generateRandomColor();
			setEvenShapeColorsSynchronously(colors);
		}

		public void setRepeatColorAnimations(final boolean repeat) {
			repeatColorAnimation = repeat;
		}

		public void setRotatable(final boolean rotatable) {
			final boolean wasRot = isRotatable();
			this.rotatable = rotatable;

			if (wasRot && !rotatable)
				realignRotations();
			else if (rotatable && !wasRot)
				for (final Shape s : shapes) {
					final RotateTransition rt = (RotateTransition) s.getProperties().get(ROTATOR_KEY);
					rt.stop();
					buildRotator(rt);
					rt.play();
				}
		}

		public void setShapeColor(final Paint color) {
			for (final Shape s : shapes)
				s.setStroke(color);
		}

		public void stopAnimations() {
			for (final Shape s : getShapes()) {
				final TranslateTransition translator = (TranslateTransition) s.getProperties().get(TRANSLATOR_KEY);
				final RotateTransition rotator = (RotateTransition) s.getProperties().get(ROTATOR_KEY);
				final StrokeTransition colorer = (StrokeTransition) s.getProperties().get(COLORER_KEY);
				translator.stop();
				rotator.stop();
				colorer.stop();
			}
		}

		public void stopColorAnimations() {
			for (final Shape s : getShapes())
				((StrokeTransition) s.getProperties().get(COLORER_KEY)).stop();
		}

		public void zoomIn() {
			stopAnimations();
			for (final Shape s : getShapes()) {
				final ScaleTransition scaler = (ScaleTransition) s.getProperties().get(SCALER_KEY);
				final TranslateTransition translator = (TranslateTransition) s.getProperties().get(TRANSLATOR_KEY);
				final FadeTransition fader = (FadeTransition) s.getProperties().get(FADER_KEY);
				scaler.stop();
				translator.stop();
				fader.stop();

				final Duration duration = Duration.seconds(1.5);
				scaler.setDuration(duration);
				translator.setDuration(duration);
				fader.setDuration(Duration.seconds(0.95));

				final Interpolator interpolator = Interpolator.SPLINE(0.9, 0.9, 0.3, 0.3);

				scaler.setInterpolator(interpolator);
				translator.setInterpolator(interpolator);
				fader.setInterpolator(interpolator);

				scaler.setToX(s.getScaleX() * 5);
				scaler.setToY(s.getScaleY() * 5);

				final double distX = s.getLayoutX() + s.getTranslateX()
						- Kr�w.getSystemProperties().getScreenWidth() / 2,
						distY = s.getLayoutY() + s.getTranslateY() - Kr�w.getSystemProperties().getScreenHeight() / 2;
				// Subtracting half the screen size sets the coord (0,0) in the
				// center of the screen, rather than the top left,
				// (conceptually, of course...).

				translator.setByX(distX * 5);
				translator.setByY(distY * 5);

				fader.setToValue(0);

				scaler.setOnFinished(null);
				translator.setOnFinished(null);
				fader.setOnFinished(null);

				scaler.play();
				translator.play();
				fader.play();

			}
		}

		public void zoomOut() {
			for (final Shape s : getShapes()) {
				final ScaleTransition scaler = (ScaleTransition) s.getProperties().get(SCALER_KEY);
				final TranslateTransition translator = (TranslateTransition) s.getProperties().get(TRANSLATOR_KEY);
				final FadeTransition fader = (FadeTransition) s.getProperties().get(FADER_KEY);
				scaler.stop();
				translator.stop();
				fader.stop();

				final Duration duration = Duration.seconds(1.5);
				scaler.setDuration(duration);
				translator.setDuration(duration);
				fader.setDuration(Duration.seconds(1.3));

				scaler.setInterpolator(Interpolator.TANGENT(Duration.seconds(4), 0.9, Duration.seconds(9), 0.3));
				translator.setInterpolator(Interpolator.TANGENT(Duration.seconds(4), 0.9, Duration.seconds(9), 0.3));
				fader.setInterpolator(Interpolator.TANGENT(Duration.seconds(4), 0.9, Duration.seconds(9), 0.3));

				scaler.setToX(s.getScaleX() / 5);
				scaler.setToY(s.getScaleY() / 5);

				final double distX = s.getLayoutX() + s.getTranslateX()
						- Kr�w.getSystemProperties().getScreenWidth() / 2,
						distY = s.getLayoutY() + s.getTranslateY() - Kr�w.getSystemProperties().getScreenHeight() / 2;

				translator.setByX(distX / 5);
				translator.setByY(distY / 5);

				fader.setToValue(1);

				scaler.setOnFinished(null);
				translator.setOnFinished(event -> animate(s));
				fader.setOnFinished(null);

				scaler.play();
				translator.play();
				fader.play();
			}

		}
	}

	private static final Random random = new Random();
	private static final int SHAPE_COUNT = 50;
	private static final double SHAPE_RADIUS = 100;
	private static final Color SHAPE_COLOR = Color.BLACK;
	private static final double SHAPE_MOVE_DURATION = 8;

	private static final Object TRANSLATOR_KEY = new Object(), ROTATOR_KEY = new Object(), COLORER_KEY = new Object(),
			FADER_KEY = new Object(), SCALER_KEY = new Object();

	private static final Object IS_BEING_SHOVED_KEY = new Object(), BUILT_KEY = new Object(), EFFECT_KEY = new Object();

	public static ShapeBackgroundManager shapeBackground(final Pane pane, final Node... mouseDetectionNodes) {
		final ShapeBackgroundManager manager = new ShapeBackgroundManager(pane);
		manager.addRandomShapes(SHAPE_COUNT);

		for (final Node n : mouseDetectionNodes)
			n.addEventFilter(MouseEvent.MOUSE_MOVED, manager.getMouseMovementHandler());

		return manager;

	}

	private BackgroundBuilder() {
	}

}
