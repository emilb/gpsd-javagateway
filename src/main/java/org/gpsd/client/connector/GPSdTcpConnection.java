package org.gpsd.client.connector;

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
import org.gpsd.client.GPSd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GPSdTcpConnection implements Runnable, GPSdConnection {

	private GPSd gpsd;
	private SocketChannel socketChannel;
	private Selector readSelector;
	private ByteBuffer readBuffer;
	private CharsetDecoder asciiDecoder;
	private boolean running = false;

	@Autowired
	public GPSdTcpConnection(GPSd gpsd) {
		this.gpsd = gpsd;
	}

	/* (non-Javadoc)
	 * @see org.gpsd.client.connector.IGPSdConnection#connect(java.lang.String, int)
	 */
	@Override
	public void connect(String host, int port) throws IOException {

		socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress(host, port));

		readSelector = Selector.open();
		socketChannel.configureBlocking(false);
		socketChannel.register(readSelector, SelectionKey.OP_READ,
				new StringBuilder());

		readBuffer = ByteBuffer.allocateDirect(512);
		asciiDecoder = Charset.forName("US-ASCII").newDecoder();
		running = true;
		Thread t = new Thread(this);
		t.start();
	}

	/* (non-Javadoc)
	 * @see org.gpsd.client.connector.IGPSdConnection#disconnect()
	 */
	@Override
	public void disconnect() {
		running = false;

		try {
			socketChannel.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.gpsd.client.connector.IGPSdConnection#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return running;
	}

	/* (non-Javadoc)
	 * @see org.gpsd.client.connector.IGPSdConnection#send(java.lang.String)
	 */
	@Override
	public void send(String msg) throws IOException {
		ByteBuffer buf = ByteBuffer.allocate(512);
		buf.clear();
		buf.put(msg.getBytes());

		buf.flip();

		while (buf.hasRemaining()) {
			socketChannel.write(buf);
		}
	}
	
	@Override
	public void run() {
		try {
			while (running) {

				// non-blocking select, returns immediately regardless of how
				// many
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
						disconnect();
					} else {
						// grab the StringBuffer we stored as the attachment
						StringBuilder sb = (StringBuilder) key.attachment();

						// use a CharsetDecoder to turn those bytes into a
						// string
						// and append to our StringBuffer
						readBuffer.flip();
						String str = asciiDecoder.decode(readBuffer).toString();
						sb.append(str);
						readBuffer.clear();

						// check for a full line and write to STDOUT
						String line = sb.toString();

						while (StringUtils.containsAny(line, new char[] { '\n',
								'\r' })) {
							int posN = StringUtils.indexOf(line, '\n');
							int posR = StringUtils.indexOf(line, '\r');
							int pos = 0;
							if (posN < 0)
								pos = posR;
							else if (posR < 0)
								pos = posN;
							else
								pos = Math.min(posN, posR);

							// Get the rest
							line = StringUtils.substring(line, 0, pos);

							// Trim trailing and prefixed newlines
							line = StringUtils.trim(line);

							// Notify gpsd
							if (StringUtils.isNotBlank(line))
								gpsd.messageReceived(line);

							sb.delete(0, pos + 1);
							line = sb.toString();
						}
					}
					tryToSleep(50);
				}
				tryToSleep(50);
			}
		} catch (Exception e) {
			e.printStackTrace();
			disconnect();
		}
	}

	private void tryToSleep(long ms) {
		try {
			Thread.sleep(50);
		} catch (InterruptedException ie) {
			System.out.println("interrupted");
		}
	}
}
