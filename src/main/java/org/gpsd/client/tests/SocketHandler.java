package org.gpsd.client.tests;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class SocketHandler {

	public static void write(SocketChannel socketChannel, String data)
			throws IOException {

		ByteBuffer buf = ByteBuffer.allocate(48);
		buf.clear();
		buf.put(data.getBytes());

		buf.flip();

		while (buf.hasRemaining()) {
			socketChannel.write(buf);
		}

	}

	public static String readLine(SocketChannel socketChannel)
			throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(48);
		int bytesRead = socketChannel.read(buf);
		System.out.println(new String(buf.array()));
		return "";
	}

	public static void main(String[] args) throws IOException {
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress("localhost", 1234));

		// readLine(socketChannel);
		// write(socketChannel, "New String to write to file...\n");

		Selector readSelector = Selector.open();
		socketChannel.configureBlocking(false);
		socketChannel.register(readSelector, SelectionKey.OP_READ,
				new StringBuilder());

		ByteBuffer readBuffer = ByteBuffer.allocateDirect(512);
		CharsetDecoder asciiDecoder = Charset.forName("US-ASCII").newDecoder();
		boolean running = true;

		while (running) {

			// non-blocking select, returns immediately regardless of how many
			// keys are ready
			readSelector.selectNow();

			// fetch the keys
			Set<SelectionKey> readyKeys = readSelector.selectedKeys();

			// run through the keys and process
			Iterator<SelectionKey> i = readyKeys.iterator();
			while (i.hasNext()) {
				SelectionKey key = i.next();
				i.remove();
				SocketChannel channel = (SocketChannel) key.channel();
				readBuffer.clear();

				// read from the channel into our buffer
				long nbytes = channel.read(readBuffer);

				// check for end-of-stream
				if (nbytes == -1) {
					System.out
							.println("disconnected from server: end-of-stream");
					channel.close();
					running = false;
				} else {
					// grab the StringBuffer we stored as the attachment
					StringBuilder sb = (StringBuilder) key.attachment();

					// use a CharsetDecoder to turn those bytes into a string
					// and append to our StringBuffer
					readBuffer.flip();
					String str = asciiDecoder.decode(readBuffer).toString();
					sb.append(str);
					readBuffer.clear();

					// check for a full line and write to STDOUT
					String line = sb.toString();
					
					while (StringUtils.containsAny(line, new char[] {'\n', '\r'})) {
						int posN = StringUtils.indexOf(line, '\n');
						int posR = StringUtils.indexOf(line, '\r');
						int pos = 0;
						if (posN < 0)
							pos = posR;
						else if (posR < 0)
							pos = posN;
						else
							pos = Math.min(posN, posR);
						
						System.out.println(pos);
						
						// Get the rest
						line = StringUtils.substring(line, 0, pos);
						
						// Trim trailing and prefixed newlines
						line = StringUtils.trim(line);
						
						System.out.println("[" + line + "]");
						sb.delete(0, pos+1);
						line = sb.toString();
					}
				}

				try {
//					System.out.println("Sleeping1");
					Thread.sleep(50);
				} catch (InterruptedException ie) { System.out.println("interrupted");}
			}
			
			try {
//				System.out.println("Sleeping2");
				Thread.sleep(50);
			} catch (InterruptedException ie) { System.out.println("interrupted");}

		}
	}
}

// 123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
// 123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890