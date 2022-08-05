package de.simpleworks.staf.commons.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UtilsNetwork {

	private static final Logger logger = LogManager.getLogger(UtilsNetwork.class);

	public static boolean isHostAvailable(final String host) {

		if (Convert.isEmpty(host)) {
			throw new IllegalArgumentException("host can't be null or empty string.");
		}

		InetAddress hostAddress = null;

		try {
			hostAddress = InetAddress.getByName(host);
		} catch (UnknownHostException ex) {
			UtilsNetwork.logger.error(String.format("host '%s' can't be converted to '%s'.", host, InetAddress.class),
					ex);
			return false;
		}

		if (hostAddress.isAnyLocalAddress() || hostAddress.isLoopbackAddress()) {
			return true;
		}

		boolean result = false;

		try {
			result = hostAddress.isReachable(10);
		} catch (Exception ex) {
			UtilsNetwork.logger.error(String.format("host '%s' is unavailable.", host), ex);
		}

		return result;
	}

	public static boolean isServerAvailable(URI uri) {

		if (uri == null) {
			throw new IllegalArgumentException("uri ca't be null.");
		}

		boolean flag = isServerAvailable(uri.getHost(), uri.getPort());

		return flag;
	}

	public static boolean isServerAvailable(String hostname, int port) {

		if (port < 0) {
			throw new IllegalArgumentException(
					String.format("port can't be less than 0, bu was '%s.", Integer.toString(port)));
		}

		boolean flag = false;

		if (isHostAvailable(hostname)) {
			try (Socket socket = new Socket()) {
				SocketAddress socketAddress = new InetSocketAddress(hostname, port);
				socket.connect(socketAddress);
			} catch (Exception ex) {
				//ignore error, no software to connect to, means the sw can connect to the rspecting port
				flag = true;
			}
		}

		return flag;
	}
}
