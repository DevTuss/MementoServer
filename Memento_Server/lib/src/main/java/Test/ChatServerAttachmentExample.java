package Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 * <p>
 * Shows how to use the attachment for a WebSocket. This example just uses a simple integer as ID.
 * Setting an attachment also works in the WebSocketClient
 */
public class ChatServerAttachmentExample extends WebSocketServer {

  Integer index = 0;

  public ChatServerAttachmentExample(int port) throws UnknownHostException {
    super(new InetSocketAddress(port));
  }

  public ChatServerAttachmentExample(InetSocketAddress address) {
    super(address);
  }

  @Override
  public void onOpen(WebSocket conn, ClientHandshake handshake) {
    conn.setAttachment(index); //Set the attachment to the current index
    index++;
    // Get the attachment of this connection as Integer
    System.out.println(
        conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room! ID: "
            + conn.<Integer>getAttachment());
  }

  @Override
  public void onClose(WebSocket conn, int code, String reason, boolean remote) {
    // Get the attachment of this connection as Integer
    System.out.println(conn + " has left the room! ID: " + conn.<Integer>getAttachment());
  }

  @Override
  public void onMessage(WebSocket conn, String message) {
    System.out.println(conn + ": " + message);
  }

  @Override
  public void onMessage(WebSocket conn, ByteBuffer message) {
    System.out.println(conn + ": " + message);
  }

  public static void main(String[] args) throws InterruptedException, IOException {
    int port = 8887; // 843 flash policy port
    try {
      port = Integer.parseInt(args[0]);
    } catch (Exception ex) {
    }
    ChatServerAttachmentExample s = new ChatServerAttachmentExample(port);
    s.start();
    System.out.println("ChatServer started on port: " + s.getPort());

    BufferedReader sysin = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      String in = sysin.readLine();
      s.broadcast(in);
      if (in.equals("exit")) {
        s.stop(1000);
        break;
      }
    }
  }

  @Override
  public void onError(WebSocket conn, Exception ex) {
    ex.printStackTrace();
    if (conn != null) {
      // some errors like port binding failed may not be assignable to a specific websocket
    }
  }

  @Override
  public void onStart() {
    System.out.println("Server started!");
  }

}