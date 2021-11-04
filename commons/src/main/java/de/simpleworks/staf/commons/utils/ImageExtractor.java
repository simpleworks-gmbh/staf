package de.simpleworks.staf.commons.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImageExtractor {
	private final static Logger logger = LogManager.getLogger(ImageExtractor.class);

	public static boolean createImage(final File file, final String imageDataBytes) {
		if (file == null) {
			throw new IllegalArgumentException("file can't be null.");
		}

		if (Convert.isEmpty(imageDataBytes)) {
			throw new IllegalArgumentException("imageDataBytes can't be null or empty string.");
		}

		final BufferedImage image = ImageExtractor.createBuffredImage(imageDataBytes);
		if (image == null) {
			if (ImageExtractor.logger.isDebugEnabled()) {
				ImageExtractor.logger.debug(String.format("image is null."));
			}

			return false;
		}

		try {
			ImageIO.write(image, "png", file);

			return true;
		} catch (final IOException e) {
			ImageExtractor.logger.error(String.format("Cannot save image into file: '%s'.", file), e);
			// FIXME throw an exception.
		}

		return false;
	}

	private static BufferedImage createBuffredImage(final String imageDataBytes) {
		if (Convert.isEmpty(imageDataBytes)) {
			throw new IllegalArgumentException("imageDataBytes can't be null or empty string.");
		}

		final byte[] imageAsBytes = Base64.getDecoder().decode(imageDataBytes);

		BufferedImage result = null;
		try (ByteArrayInputStream input = new ByteArrayInputStream(imageAsBytes)) {
			result = ImageIO.read(input);
		} catch (final IOException ex) {
			ImageExtractor.logger.error("Cannot cretae buffreed Image.", ex);
			// FIXME throw an exception.
		}

		return result;
	}
}
