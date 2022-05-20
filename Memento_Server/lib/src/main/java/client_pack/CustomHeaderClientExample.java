package client_pack;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class shows how to add additional http header like "Origin" or "Cookie".
 * <p>
 * To see it working, start ServerRejectHandshakeExample and then start this example.
 */
public class CustomHeaderClientExample {

  public static void main(String[] args) throws URISyntaxException, InterruptedException {
    Map<String, String> httpHeaders = new HashMap<String, String>();
    httpHeaders.put("Cookie", "test");
    ExampleClient c = new ExampleClient(new URI("ws://localhost:8887"), httpHeaders);
    //We expect no successful connection
    c.connectBlocking();
    httpHeaders.put("Cookie", "username=nemo");
    c = new ExampleClient(new URI("ws://localhost:8887"), httpHeaders);
    //Wer expect a successful connection
    c.connectBlocking();
    c.closeBlocking();
    httpHeaders.put("Access-Control-Allow-Origin", "*");
    c = new ExampleClient(new URI("ws://localhost:8887"), httpHeaders);
    //We expect no successful connection
    c.connectBlocking();
    c.closeBlocking();
    httpHeaders.clear();
    httpHeaders.put("Origin", "localhost:8887");
    httpHeaders.put("Cookie", "username=nemo");
    c = new ExampleClient(new URI("ws://localhost:8887"), httpHeaders);
    //We expect a successful connection
    c.connectBlocking();
    c.closeBlocking();
    httpHeaders.clear();
    httpHeaders.put("Origin", "localhost");
    httpHeaders.put("cookie", "username=nemo");
    c = new ExampleClient(new URI("ws://localhost:8887"), httpHeaders);
    //We expect no successful connection
    c.connectBlocking();
  }
}