package krow.guis.chatroom;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import krow.guis.chatroom.messages.CommandMessage;
import kr�w.connections.Client;
import kr�w.connections.FullClientListener;
import kr�w.connections.Server;
import kr�w.connections.messages.Message;
import kr�w.connections.messages.ReplyMessage;
import kr�w.core.Kr�w;

public class ChatRoomServer extends Server {

	protected List<ChatRoomClient> connections = new ArrayList<>();

	public ChatRoomServer() throws IOException {
		super();
	}

	public ChatRoomServer(final int port) throws IOException {
		super(port);
	}

	@Override
	protected void acceptConnection(final Socket connection) {

		try {
			final ChatRoomClient client = new ChatRoomClient(connection);
			if (Kr�w.DEBUG_MODE)
				System.out.println("Server: Made server-client: " + client);
			connections.add(client);

			client.addListener(new FullClientListener() {

				@Override
				public void connectionClosed() {
					connections.remove(client);
				}

				@Override
				public void connectionEstablished() {

				}

				@Override
				public void connectionLost() {
					connections.remove(client);
				}

				@Override
				public void objectReceived(final Object object) {
					if (Kr�w.DEBUG_MODE)
						System.out.println("Server: Object received");

					try {

						if (object instanceof krow.guis.chatroom.ChatRoomMessage) {
							for (Client c : connections)
								if (c != client)
									c.sendMessage(
											new UserMessage(((krow.guis.chatroom.ChatRoomMessage) object).getText(),
													((krow.guis.chatroom.ChatRoomMessage) object).username));
								else
									c.sendMessage(new ReplyMessage(((krow.guis.chatroom.ChatRoomMessage) object).id));
						}

						if (Client.isEndConnectionMessage((Message) object))
							for (final Client c : connections)
								c.sendObject(new ServerMessage("A user has left the chatroom..."));

						if (object instanceof CommandMessage) {
							final CommandMessage cm = (CommandMessage) object;
							if (cm.getCommand().equalsIgnoreCase("setname")
									|| cm.getCommand().equalsIgnoreCase("set-name") && cm.getArgs() != null
											&& cm.getArgs().length > 0) {
								final String prevName = client.getName();
								client.setName(cm.getArgs()[0]);
								for (final Client cl : connections)
									if (cl != client)
										cl.sendMessage(new ServerMessage(
												prevName + " has changed their name to " + client.getName() + "."));
							}
							return;
						}

						for (final Client cl : connections)
							// Send to all those who didn't send it.
							if (cl != client)
								try {
									if (Kr�w.DEBUG_MODE)
										System.out.println(
												"Server: Sending object to clients via server-client... (Ignore upcoming client send msgs...)");
									cl.sendObject((Serializable) object);
								} catch (final IOException e) {
									e.printStackTrace();
								}

					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (final IOException e) {
			e.printStackTrace();
			return;
		}

	}

	@Override
	protected List<ChatRoomClient> getAllConnections() {
		return connections;
	}

}
