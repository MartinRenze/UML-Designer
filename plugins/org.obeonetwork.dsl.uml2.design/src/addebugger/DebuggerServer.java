package addebugger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class DebuggerServer implements Runnable {

	private DebuggerService service;
	private ServerSocket server;
	private Socket client;

	private boolean connected;

	private DataInputStream inputStream;
	private DataOutputStream outputStream;

	public DebuggerServer(DebuggerService service, int port) {
		try {
			this.service = service;
			server = new ServerSocket(port);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server started");
	}

	public void close() {
		connected = false;
		try {
			if (inputStream != null) {
				inputStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
			if (client != null) {
				client.close();
			}
			if (server != null) {
				server.close();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		System.out.println("Server closed");
	}

	public boolean isConnected() {
		return connected;
	}

	public void open() {
		try {
			inputStream = new DataInputStream(new BufferedInputStream(client.getInputStream()));
			outputStream = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
			connected = true;
		} catch (final IOException e) {
			e.printStackTrace();
			connected = false;
		}
		System.out.println("Opened Server: " + connected);
	}

	public void run() {
		try {
			client = server.accept();
			System.out.println("Client conencted");
			open();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		while (connected) {
			try {
				final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
				inputStream.read(byteBuffer.array());

				final String text = ByteBufferUtils.readString(byteBuffer);
				if (!text.isEmpty()) {
					System.out.println("Server received: " + text);
					service.handle(text);
				}

			} catch (final Exception e) {
				e.printStackTrace();
				connected = false;
			}
		}
		close();
	}
}
