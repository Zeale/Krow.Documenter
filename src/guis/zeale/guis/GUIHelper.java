package zeale.guis;

import java.io.IOException;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javafx.animation.FillTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import kr�w.core.Kr�w;
import kr�w.libs.guis.Window;
import kr�w.libs.guis.Window.NotSwitchableException;

final class GUIHelper {

	private static final Color MENU_BAR_SHADOW_COLOR = Color.BLACK;
	private static final int MENU_BAR_SHADOW_RADIUS = 7;
	private static final Color MENU_BACKGROUND_COLOR = new Color(0, 0, 0, 0.3);
	private static final double MENU_WIDTH_FRACTION = 4.5;
	private static final double MENU_ITEM_SPACING = 15 / 1080 * Kr�w.SCREEN_HEIGHT;
	private static final Interpolator MENU_BUTTON_TRANSITION_INTERPOLATOR = Interpolator.EASE_OUT,
			MENU_CHILD_TRANSITION_INTERPOLATOR = MENU_BUTTON_TRANSITION_INTERPOLATOR;
	private static final Duration MENU_BUTTON_ANIMATION_DURATION = Duration.seconds(0.8);
	private static final double MENU_BUTTON_RECTANGLE_WIDTH = 25, MENU_BUTTON_RECTANGLE_HEIGHT = 5,
			MENU_BUTTON_RECTANGLE_X = Kr�w.SCREEN_WIDTH - (double) 50 / 1920 * Kr�w.SCREEN_WIDTH,
			MENU_BUTTON_Y = (double) 33 / 1080 * Kr�w.SCREEN_HEIGHT,
			MENU_BUTTON_RECTANGLE_SPACING = (double) 13 / 1080 * Kr�w.SCREEN_HEIGHT;
	private static final int SIDE_MENU_PADDING_TOP = (int) ((double) 80 / 1080 * Kr�w.SCREEN_HEIGHT);
	private static final byte MENU_BAR_ANIMATION_ROTATION_COUNT = 1;
	private static final Color MENU_BUTTON_START_COLOR = MENU_BAR_SHADOW_COLOR, MENU_BUTTON_END_COLOR = Color.WHITE;
	private static final Duration MENU_CHILD_NODE_HOVER_ANIMATION_DURATION = Duration.seconds(0.6);
	private static final Color MENU_CHILD_NODE_START_COLOR = Color.BLACK,
			MENU_CHILD_NODE_HOVER_ANIMATION_END_COLOR = Color.WHITE;

	public static VBox buildCloseButton(Pane pane) {

		Shape menubarTop = new Rectangle(MENU_BUTTON_RECTANGLE_WIDTH, MENU_BUTTON_RECTANGLE_HEIGHT),
				menubarBottom = new Rectangle(MENU_BUTTON_RECTANGLE_WIDTH, MENU_BUTTON_RECTANGLE_HEIGHT);
		menubarTop.setLayoutX(MENU_BUTTON_RECTANGLE_X);
		menubarBottom.setLayoutX(MENU_BUTTON_RECTANGLE_X);
		menubarTop.setLayoutY(MENU_BUTTON_Y);
		menubarBottom.setLayoutY(MENU_BUTTON_RECTANGLE_SPACING + MENU_BUTTON_Y);

		menubarTop.setStroke(MENU_BUTTON_START_COLOR);
		menubarTop.setFill(MENU_BUTTON_START_COLOR);
		menubarBottom.setStroke(MENU_BUTTON_START_COLOR);
		menubarBottom.setFill(MENU_BUTTON_START_COLOR);

		menubarTop.setEffect(new DropShadow(MENU_BAR_SHADOW_RADIUS, MENU_BAR_SHADOW_COLOR));
		menubarBottom.setEffect(new DropShadow(MENU_BAR_SHADOW_RADIUS, MENU_BAR_SHADOW_COLOR));

		FillTransition topFill = new FillTransition(MENU_BUTTON_ANIMATION_DURATION, menubarTop),
				bottomFill = new FillTransition(MENU_BUTTON_ANIMATION_DURATION, menubarBottom);
		topFill.setInterpolator(MENU_BUTTON_TRANSITION_INTERPOLATOR);
		bottomFill.setInterpolator(MENU_BUTTON_TRANSITION_INTERPOLATOR);

		RotateTransition topRot = new RotateTransition(MENU_BUTTON_ANIMATION_DURATION, menubarTop),
				bottomRot = new RotateTransition(MENU_BUTTON_ANIMATION_DURATION, menubarBottom);
		topRot.setInterpolator(MENU_BUTTON_TRANSITION_INTERPOLATOR);
		bottomRot.setInterpolator(MENU_BUTTON_TRANSITION_INTERPOLATOR);

		TranslateTransition topTrans = new TranslateTransition(MENU_BUTTON_ANIMATION_DURATION, menubarTop),
				bottomTrans = new TranslateTransition(MENU_BUTTON_ANIMATION_DURATION, menubarBottom);

		VBox menu = new VBox(MENU_ITEM_SPACING);
		menu.setPrefSize(Kr�w.SCREEN_WIDTH / MENU_WIDTH_FRACTION, Kr�w.SCREEN_HEIGHT);
		menu.setLayoutX(Kr�w.SCREEN_WIDTH);
		menu.setLayoutY(0);
		menu.setBackground(new Background(new BackgroundFill(MENU_BACKGROUND_COLOR, null, null)));
		menu.setId("GUIH-SideMenu");
		menu.setStyle("-fx-padding: " + SIDE_MENU_PADDING_TOP + "px 0px 0px 0px; -fx-alignment: top-center;");
		menu.getChildren().addListener(new ListChangeListener<Node>() {

			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Node> c) {
				while (c.next())
					if (c.wasAdded())
						for (Node n : c.getAddedSubList())
							try {
								assert n instanceof Text;// This is caught in
															// actual runtime
															// below if it's
															// thrown.
								Text t = (Text) n;

								t.setTextAlignment(TextAlignment.CENTER);

								t.setFill(MENU_CHILD_NODE_START_COLOR);
								t.setFont(Font.font(Font.getDefault().getName(), FontWeight.BOLD,
										(int) ((double) 16 / 1920 * Kr�w.SCREEN_WIDTH)));

								FillTransition ft = new FillTransition(MENU_CHILD_NODE_HOVER_ANIMATION_DURATION, t);
								ft.setInterpolator(MENU_CHILD_TRANSITION_INTERPOLATOR);
								t.setOnMouseEntered(new EventHandler<Event>() {

									@Override
									public void handle(Event event) {
										ft.stop();
										ft.setFromValue((Color) t.getFill());
										ft.setToValue(MENU_CHILD_NODE_HOVER_ANIMATION_END_COLOR);
										ft.play();
									}
								});
								t.setOnMouseExited(new EventHandler<Event>() {

									@Override
									public void handle(Event event) {
										ft.stop();
										ft.setFromValue((Color) t.getFill());
										ft.setToValue(MENU_CHILD_NODE_START_COLOR);
										ft.play();
									}

								});
							} catch (ClassCastException e) {
							}
			}

		});

		TranslateTransition menuSlide = new TranslateTransition(MENU_BUTTON_ANIMATION_DURATION, menu);
		menuSlide.setInterpolator(MENU_BUTTON_TRANSITION_INTERPOLATOR);

		AnchorPane cover = new AnchorPane();

		cover.setPrefSize(MENU_BUTTON_RECTANGLE_WIDTH * 2 + MENU_BUTTON_RECTANGLE_SPACING,
				MENU_BUTTON_RECTANGLE_WIDTH * 2 + MENU_BUTTON_RECTANGLE_SPACING);
		cover.setLayoutX(Kr�w.SCREEN_WIDTH - cover.getPrefWidth());
		cover.setLayoutY(0);

		new Object() {
			private boolean closing, opening;

			private void close() {
				closing = true;
				opening = false;
				topFill.stop();
				bottomFill.stop();
				topRot.stop();
				bottomRot.stop();
				topTrans.stop();
				bottomTrans.stop();
				menuSlide.stop();

				topFill.setFromValue((Color) menubarTop.getFill());
				bottomFill.setFromValue((Color) menubarBottom.getFill());
				topFill.setToValue(MENU_BUTTON_START_COLOR);
				bottomFill.setToValue(MENU_BUTTON_START_COLOR);

				topRot.setFromAngle(menubarTop.getRotate());
				bottomRot.setFromAngle(menubarBottom.getRotate());
				topRot.setToAngle(0);
				bottomRot.setToAngle(0);

				topTrans.setFromY(menubarTop.getTranslateY());
				bottomTrans.setFromY(menubarBottom.getTranslateY());
				topTrans.setToY(0);
				bottomTrans.setToY(0);

				menuSlide.setFromX(menu.getTranslateX());
				menuSlide.setToX(0);

				topFill.play();
				bottomFill.play();
				topRot.play();
				bottomRot.play();
				topTrans.play();
				bottomTrans.play();
				menuSlide.play();
			}

			private void open() {
				closing = false;
				opening = true;
				topFill.stop();
				bottomFill.stop();
				topRot.stop();
				bottomRot.stop();
				topTrans.stop();
				bottomTrans.stop();
				menuSlide.stop();

				topFill.setFromValue((Color) menubarTop.getFill());
				bottomFill.setFromValue((Color) menubarBottom.getFill());
				topFill.setToValue(MENU_BUTTON_END_COLOR);
				bottomFill.setToValue(MENU_BUTTON_END_COLOR);

				topRot.setFromAngle(menubarTop.getRotate());
				bottomRot.setFromAngle(menubarBottom.getRotate());
				double rotationCount = 45 + MENU_BAR_ANIMATION_ROTATION_COUNT * 180;
				topRot.setToAngle(rotationCount);
				bottomRot.setToAngle(-rotationCount);

				topTrans.setFromY(menubarTop.getTranslateY());
				bottomTrans.setFromY(menubarBottom.getTranslateY());
				topTrans.setToY(MENU_BUTTON_RECTANGLE_SPACING / 2);
				bottomTrans.setToY(-MENU_BUTTON_RECTANGLE_SPACING / 2);

				menuSlide.setFromX(menu.getTranslateX());
				menuSlide.setToX(-menu.getPrefWidth());

				topFill.play();
				bottomFill.play();
				topRot.play();
				bottomRot.play();
				topTrans.play();
				bottomTrans.play();
				menuSlide.play();
			}

			{

				EventHandler<? super MouseEvent> enterHandler = event -> {
					if (event.getPickResult().getIntersectedNode() == cover && opening)
						return;
					try {
						Kr�w.getSoundManager().playSound(Kr�w.getSoundManager().TICK);
					} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
						e.printStackTrace();
					}
					open();
				};

				EventHandler<MouseEvent> exitHandler = new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (event.getPickResult().getIntersectedNode() == menu
								|| event.getPickResult().getIntersectedNode() == cover
								|| event.getPickResult().getIntersectedNode().getParent() == menu || closing)
							return;

						close();

					}
				};

				EventHandler<MouseEvent> coverClickHandler = new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						if (opening) {
							close();
							try {
								Kr�w.getSoundManager().playSound(Kr�w.getSoundManager().TICK);
							} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
								e.printStackTrace();
							}
							return;
						}
						if (closing) {
							open();
							try {
								Kr�w.getSoundManager().playSound(Kr�w.getSoundManager().TICK);
							} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
								e.printStackTrace();
							}
						}

					}
				};

				cover.setOnMouseEntered(enterHandler);

				cover.setOnMouseExited(exitHandler);
				menu.setOnMouseExited(exitHandler);

				cover.setOnMousePressed(coverClickHandler);

			}
		};

		// Children will be added at the end.

		pane.getChildren().add(menu);
		pane.getChildren().add(menubarTop);
		pane.getChildren().add(menubarBottom);
		pane.getChildren().add(cover);

		// Test Text object.
		// menu.getChildren().add(new Text("ABCDEFGHIJKLMNOP"));

		return menu;

	}

	public static void addDefaultSettings(VBox vbox) {
		List<Node> children = vbox.getChildren();
		Text goHome = new Text("Go Home");
		goHome.setOnMouseClicked(new EventHandler<Event>() {

			@Override
			public void handle(Event event) {
				try {
					Window.setScene(Home.class);
				} catch (InstantiationException | IllegalAccessException | IOException e) {
					e.printStackTrace();
				} catch (NotSwitchableException e) {
					if (Window.getNamePropertyFromParent(e.getCurrentParent()) == Window
							.getNamePropertyFromParent(e.getNewParent()))
						Window.spawnLabelAtMousePos("You are already at this Window.", Color.FIREBRICK);
					else
						Window.spawnLabelAtMousePos("You can't go there right now...", Color.FIREBRICK);
				}
			}

		});

		children.add(goHome);
	}

}