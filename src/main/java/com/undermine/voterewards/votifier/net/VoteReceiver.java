package com.undermine.voterewards.votifier.net;


import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import javax.crypto.BadPaddingException;

import org.apache.logging.log4j.Level;

import com.undermine.voterewards.VoteRewardsMod;
import com.undermine.voterewards.votifier.Votifier;
import com.undermine.voterewards.votifier.crypto.RSA;
import com.undermine.voterewards.votifier.model.*;

import cpw.mods.fml.common.FMLLog;

/**
 * The vote receiving server.
 * 
 * @author Blake Beaupain
 * @author Kramer Campbell
 * 
 * modified by
 * @author kadzu
 */
public class VoteReceiver extends Thread {
	/** The host to listen on. */
	private final String host;

	/** The port to listen on. */
	private final int port;

	/** The server socket. */
	private ServerSocket server;

	/** The running flag. */
	private boolean running = true;

	/**
	 * Instantiates a new vote receiver.
	 * 
	 * @param host
	 *            The host to listen on
	 * @param port
	 *            The port to listen on
	 */
	public VoteReceiver(String host, int port)
			throws Exception {
		this.host = host;
		this.port = port;

		initialize();
	}

	private void initialize() throws Exception {
		try {
			server = new ServerSocket();
			server.bind(new InetSocketAddress(host, port));
		} catch (Exception ex) {
			FMLLog.log(Level.ERROR, "Error initializing vote receiver. Please verify that the configured");
			FMLLog.log(Level.ERROR, "IP address and port are not already in use. This is a common problem");
			FMLLog.log(Level.ERROR, "with hosting services and, if so, you should check with your hosting provider.");
			throw new Exception(ex);
		}
	}

	/**
	 * Shuts the vote receiver down cleanly.
	 */
	public void shutdown() {
		running = false;
		if (server == null)
			return;
		try {
			server.close();
		} catch (Exception ex) {
			FMLLog.log(Level.WARN, "Unable to shut down vote receiver cleanly.");
		}
	}

	@Override
	public void run() {

		// Main loop.
		while (running) {
			try {
				Socket socket = server.accept();
				socket.setSoTimeout(5000); // Don't hang on slow connections.
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(socket.getOutputStream()));
				InputStream in = socket.getInputStream();

				// Send them our version.
				writer.write("VOTIFIER " + Votifier.getInstance().getVersion());
				writer.newLine();
				writer.flush();

				// Read the 256 byte block.
				byte[] block = new byte[256];
				in.read(block, 0, block.length);

				// Decrypt the block.
				block = RSA.decrypt(block, Votifier.getInstance().getKeyPair()
						.getPrivate());
				int position = 0;

				// Perform the opcode check.
				String opcode = readString(block, position);
				position += opcode.length() + 1;
				if (!opcode.equals("VOTE")) {
					// Something went wrong in RSA.
					throw new Exception("Unable to decode RSA");
				}

				// Parse the block.
				String serviceName = readString(block, position);
				position += serviceName.length() + 1;
				String username = readString(block, position);
				position += username.length() + 1;
				String address = readString(block, position);
				position += address.length() + 1;
				String timeStamp = readString(block, position);
				position += timeStamp.length() + 1;

				// Create the vote.
				final Vote vote = new Vote();
				vote.setServiceName(serviceName);
				vote.setUsername(username);
				vote.setAddress(address);
				vote.setTimeStamp(timeStamp);

				//Just push the vote onto a synchonized queue
				VoteRewardsMod.CurrentVotes.add(vote);
				
				// Clean up.
				writer.close();
				in.close();
				socket.close();
			} catch (SocketException ex) {
				FMLLog.log(Level.WARN, "Protocol error. Ignoring packet - "
						+ ex.getLocalizedMessage());
			} catch (BadPaddingException ex) {
				FMLLog.log(Level.WARN, 
						"Unable to decrypt vote record. Make sure that that your public key");
				FMLLog.log(Level.WARN, 
						"matches the one you gave the server list.");
			} catch (Exception ex) {
				FMLLog.log(Level.WARN, 
						"Exception caught while receiving a vote notification");
			}
		}
	}

	/**
	 * Reads a string from a block of data.
	 * 
	 * @param data
	 *            The data to read from
	 * @return The string
	 */
	private String readString(byte[] data, int offset) {
		StringBuilder builder = new StringBuilder();
		for (int i = offset; i < data.length; i++) {
			if (data[i] == '\n')
				break; // Delimiter reached.
			builder.append((char) data[i]);
		}
		return builder.toString();
	}
}