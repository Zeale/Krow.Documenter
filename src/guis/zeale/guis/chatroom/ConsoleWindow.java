package zeale.guis.chatroom;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.WeakHashMap;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import krow.guis.chatroom.ChatRoomMessage;
import krow.guis.chatroom.ChatRoomServer;
import krow.guis.chatroom.UserMessage;
import kr�w.annotations.AutoLoad;
import kr�w.annotations.LoadTime;
import kr�w.connections.Client;
import kr�w.connections.ClientListener;
import kr�w.connections.Server;
import kr�w.connections.messages.Message;
import kr�w.connections.messages.ReplyMessage;
import kr�w.gui.Application;

public abstract class ConsoleWindow extends Application {

	private static final short DEFAULT_SERVER_PORT = 25000;

	private static Client client;
	private static Server server;

	public Client getClient() {
		return client;
	}

	public Server getServer() {
		return server;
	}

	@AutoLoad(LoadTime.PROGRAM_EXIT)
	private static void stopServer_Impl() {
		if (server != null)
			try {
				server.stop();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private final ClientListener listener = object -> {
		if (object instanceof Message)
			receivedMsg((Message) object);
		else
			receivedObj((Serializable) object);
	};

	/**
	 * Checks if there is a server being hosted. If there is, {@link #server} will
	 * not be null.
	 * 
	 * @return <code>true</code> if there is a server being hosted.
	 *         <code>false</code> otherwise.
	 */
	public boolean hostingServer() {
		return server != null;
	}

	/**
	 * Checks if there is a connection to a server. If there is, {@link #client}
	 * will not be null.
	 * 
	 * @return Returns <code>true</code> if there is an active client.
	 *         <code>false</code> otherwise.
	 */
	public boolean connected() {
		return client != null;
	}

	public boolean connect(String hostname, short port) throws UnknownHostException, IOException {
		if (connected())
			return false;
		client = new Client(hostname, port);
		client.addListener(listener);
		return true;
	}

	public boolean connect(String hostname) throws UnknownHostException, IOException {
		return connect(hostname, DEFAULT_SERVER_PORT);
	}

	public boolean startServer() throws IOException {

		boolean success = startServer(DEFAULT_SERVER_PORT);
		if (!success)
			return false;

		// If we're not already connected to a server, connect to the one we just made.
		if (!connected())
			connect(null);// null makes this computer connect to itself.
		return true;
	}

	public boolean startServer(int port) throws IOException {
		if (hostingServer())
			return false;
		server = new ChatRoomServer(port);
		return true;
	}

	public boolean stopServer() throws IOException {
		if (!hostingServer())
			return false;
		server.stop();
		server = null;
		return true;
	}

	/**
	 * Attempts to disconnect the client and set its field to null.
	 * 
	 * @return <code>true</code> if there was a client connected. <code>false</code>
	 *         otherwise.
	 */
	public boolean disconnect() {
		if (!connected())
			return false;
		client.closeConnection();
		client = null;
		return true;
	}

	protected String username = "Unnamed";

	@FXML
	protected TextFlow chatPane;
	@FXML
	protected TextArea chatBox;
	@FXML
	protected ScrollPane chatPaneWrapper;

	@FXML
	protected AnchorPane pane;

	@FXML
	protected Button sendButton;

	/**
	 * Prints a line of text with the default color of {@link Color#WHITE}.
	 * 
	 * @param text
	 *            The text to print.
	 */
	protected void println(final String text) {
		println(text, Color.WHITE);
	}

	protected void println(final String line, final Color color) {
		print(line + "\n", color);
	}

	protected void println() {
		println("");
	}

	protected void printNode(final Node node) {
		chatPane.getChildren().add(node);
	}

	protected void print(final String text) {
		print(text, Color.WHITE);
	}

	protected void print(final String text, final Color color) {
		final Text t = new Text(text);
		t.setFill(color);
		formatTextSize(t);
		chatPane.getChildren().add(t);
	}

	/**
	 * Called when an object <b>that isn't a {@link Message}</b> is received.
	 * 
	 * @param obj
	 *            The received object.
	 */
	protected void receivedObj(Serializable obj) {
	}

	/**
	 * Called when a {@link Message} of any type is received.
	 * 
	 * @param msg
	 *            The received {@link Message}.
	 */
	protected void receivedMsg(Message msg) {
		if (msg instanceof ReplyMessage)
			if (SENT_MESSAGES_MAP.containsKey(((ReplyMessage) msg).getId()))
				SENT_MESSAGES_MAP.get(((ReplyMessage) msg).getId()).receivedReply();
		if (msg instanceof UserMessage) {
			Text name = new Text(((UserMessage) msg).sender), arrow = new Text(" > "),
					text = new Text(((UserMessage) msg).getText());
			// Since this message is coming from the server, we pass true.
			formatBasicMessage(name, arrow, text, true);
			printNode(name);
			printNode(arrow);
			printNode(text);
			println();
		}
	}

	/**
	 * Formats a basic message, either from or to the server. This also calls
	 * {@link #formatTextSize(Text)} on each given object to size the text
	 * correctly.
	 * 
	 * @param name
	 *            The name text.
	 * @param arrow
	 *            The arrow text.
	 * @param message
	 *            The message text.
	 * @param received
	 *            Whether or not this basic message was received from or sent to the
	 *            server. <code>true</code> if received, <code>false</code> if sent.
	 */
	protected void formatBasicMessage(Text name, Text arrow, Text message, boolean received) {
		name.setFill(Color.RED);
		arrow.setFill(Color.WHITE);
		message.setFill(Color.BLUE);
		formatTextSize(name);
		formatTextSize(arrow);
		formatTextSize(message);
		if (name.getText().equals("Kr�w")) {
			if (received)
				name.setFill(Color.PINK);
			else
				name.setFill(Color.BLACK);
			name.setFont(Font.font("Brush Script MT", 22));
			message.setFill(Color.WHITE);
			message.setFont(
					Font.font(Font.getDefault().getFamily(), FontWeight.EXTRA_BOLD, Font.getDefault().getSize() + 2));
		}
	}

	/**
	 * This readies the given text to be shown in the console, but also clears any
	 * font weight and font posture applied to the text. This method will apply a
	 * new {@link Font} to the text object with the family of the old font and a
	 * size of 16.
	 * 
	 * @param text
	 *            The text object to format.
	 */
	protected void formatTextSize(Text text) {
		text.setFont(Font.font(text.getFont().getFamily(), 16));
	}

	protected final void handleUserInput(String input) {
		input = input.trim();
		final boolean del = false;

		if (input == null || input.isEmpty())
			return;

		if (input.startsWith("/")) {

			String temp = "";
			for (final char c : input.toCharArray())
				if (c == ' ')
					if (del)
						continue;
					else
						temp += c;
				else if (c == '\n')
					continue;
				else
					temp += c;
			input = temp;

			String cmd;
			String[] args;
			// If there are arguments...
			if (input.contains(" ")) {
				// Get the actual command
				cmd = input.substring(1, input.indexOf(" "));
				// Get everything else
				input = input.substring(input.indexOf(" ") + 1);
				// Split "everything else" by spaces, and return the array.
				args = input.split(" ");
			} else {
				cmd = input.substring(1);
				args = new String[0];
			}
			commandInput(cmd, args);
		} else {
			textInput(input);
		}
		chatBox.setText("");
	}

	/**
	 * <p>
	 * Called by <b>{@link #handleUserInput(String)}</b>. This method will handle a
	 * command and its arguments however its subclass desires.
	 * <p>
	 * If there are no arguments, <code>args</code> is an array of length zero.
	 * <code>args</code> will not be null.
	 * 
	 * @param name
	 *            The command's name. For example,
	 * 
	 *            <pre>
	 * <code>/test arg1 arg2 arg3</code>
	 *            </pre>
	 * 
	 *            would give the name "test".
	 * @param args
	 *            Each argument of the command.
	 */
	protected abstract void commandInput(String name, String... args);

	/**
	 * Called by <b>{@link #handleUserInput(String)}</b>. Handles a basic message
	 * sent by the user.
	 * 
	 * @param message
	 *            The text that the user submitted.
	 */
	protected abstract void textInput(String message);

	public void send(String text) {
		new ChatRoomText(text).send();
	}

	private static final WeakHashMap<UUID, ChatRoomText> SENT_MESSAGES_MAP = new WeakHashMap<>();

	protected final int getReferencedTexts() {
		return SENT_MESSAGES_MAP.size();
	}

	/**
	 * An instance of this class should be created to give basic messages that the
	 * user sends to a server.
	 * 
	 * @author Zeale
	 *
	 */
	private class ChatRoomText {
		/**
		 * <p>
		 * I can't figure out how to get rid of all the references to this {@link UUID},
		 * so {@link ChatRoomText}s will just deliberately remove themselves from the
		 * {@link ConsoleWindow#SENT_MESSAGES_MAP} when they are ready to be garbage
		 * collected. This will also help prevent the issue that arises when the same
		 * {@link UUID} appears twice, even though the chance of that happening is
		 * astronomically small.
		 * <p>
		 * {@link ConsoleWindow#SENT_MESSAGES_MAP} will remain a {@link WeakHashMap} so
		 * that it doesn't prevent garbage collection once I fix what currently does.
		 */
		private UUID id = UUID.randomUUID();

		{
			SENT_MESSAGES_MAP.put(id, this);
		}

		/**
		 * Sets this {@link ChatRoomText} ready for garbage collection. Unless some
		 * weird stuff takes place, this {@link ChatRoomText} will only be prevented
		 * from garbage collection by being shown on the screen, so if the user does
		 * /cls and clicks Free RAM in the side menu, this {@link ChatRoomText} should
		 * be garbage collected.
		 * 
		 * @see {@link #id} for more info.
		 */
		private void readyForGC() {
			SENT_MESSAGES_MAP.remove(id);
			id = null;
		}

		private class ReplyText extends Text {
			{
				setOpacity(0.5);
			}

			public void receivedReply() {
				setOpacity(1);
				readyForGC();
			}

			public void noReply() {
				setStrikethrough(true);
				readyForGC();
			}
		}

		public void receivedReply() {
			message.receivedReply();
		}

		private ReplyText message = new ReplyText();
		private Text name = new Text(username), arrow = new Text(" > ");

		/**
		 * Prints each text node contained to the console with the correct coloring and
		 * sends a message to the server. Once a reply has been received, the message
		 * that was printed to the console will become fully opaque.
		 */
		public void send() {
			// Since this message is going out to the server, we pass false.
			formatBasicMessage(name, arrow, message, false);
			printNode(name);
			printNode(arrow);
			printNode(message);
			println();
			rawSend(new ChatRoomMessage(message.getText(), name.getText(), id));
		}

		public ChatRoomText(String message) {
			if (!connected())
				throw new RuntimeException();
			this.message.setText(message);
		}

	}

	public boolean rawSend(Serializable object) {
		if (connected())
			try {
				getClient().sendObject(object);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		return false;
	}

}
