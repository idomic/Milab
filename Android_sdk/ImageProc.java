/*
 * This class defines some static methods of image processing.
 */

package edu.cg;

import java.awt.*;
import java.awt.image.BufferedImage;



public class ImageProc {

	// Change the image size
	public static BufferedImage scaleDown(BufferedImage img, int factor) {
		if (factor <= 0)
			throw new IllegalArgumentException();
		int height = img.getHeight() / factor;
		int width = img.getWidth() / factor;
		BufferedImage returnImg = new BufferedImage(width, height, img.getType());
		for (int tempWidth = 0; tempWidth < width; tempWidth++)
			for (int tempHeight = 0; tempHeight < height; tempHeight++)
				returnImg.setRGB(tempWidth, tempHeight, img.getRGB(tempWidth * factor, tempHeight * factor));
		return returnImg;
	}

	// Create GrayScale of the image.
	public static BufferedImage grayScale(BufferedImage img) {
		int Height = img.getHeight();
		int Width = img.getWidth();

		// Change every pixel in the image to grayscale.
		for (int tempWidth = 0; tempWidth < Width; tempWidth++) {
			for (int tempHeight = 0; tempHeight < Height; tempHeight++) {
				Color grayColor = new Color(img.getRGB(tempWidth, tempHeight));
				int grayInt = (grayColor.getRed() + grayColor.getGreen() + grayColor.getBlue()) / 3;
				img.setRGB(tempWidth, tempHeight, new Color(grayInt, grayInt, grayInt).getRGB());
			}
		}
		return img;
	}

	// Create the horizontal derivative for the image
	public static BufferedImage horizontalDerivative(BufferedImage img) {

		// Set the parameters for the derivative
		BufferedImage grayImg = grayScale(img);
		int Height = grayImg.getHeight();
		int Width = grayImg.getWidth();
		BufferedImage returnImg = new BufferedImage(Width, Height, grayImg.getType());

		for (int tempHeight = 0; tempHeight < Height; tempHeight++) {
			for (int tempWidth = 0; tempWidth < Width; tempWidth++) {
				if (tempWidth == Width - 1 || tempWidth == 0) {
					returnImg.setRGB(tempWidth, tempHeight, new Color(127, 127, 127).getRGB());
					continue;
				}
				int newColor = ((((grayImg.getRGB(tempWidth - 1, tempHeight) & 0xff) - (grayImg.getRGB(tempWidth + 1, tempHeight) & 0xff)) / 2) + 255) / 2;
				returnImg.setRGB(tempWidth, tempHeight, new Color(newColor, newColor, newColor).getRGB());
			}
		}
		return returnImg;
	}

	// Create the vertical derivative for the image
	public static BufferedImage verticalDerivative(BufferedImage img) {

		// Set the parameters for the derivative
		BufferedImage grayImg = grayScale(img);
		int Height = img.getHeight();
		int Width = img.getWidth();
		BufferedImage returnImg = new BufferedImage(Width, Height, img.getType());

		for (int tempWidth = 0; tempWidth < Width; tempWidth++) {
			for (int tempHeight = 0; tempHeight < Height; tempHeight++) {
				if (tempHeight == 0 || tempHeight == Height - 1) {
					returnImg.setRGB(tempWidth, tempHeight, new Color(127, 127, 127).getRGB());
					continue;
				}
				int newColor = ((((grayImg.getRGB(tempWidth, tempHeight - 1) & 0xff) - (grayImg.getRGB(tempWidth, tempHeight + 1) & 0xff)) / 2) + 255) / 2;

				returnImg.setRGB(tempWidth, tempHeight, new Color(newColor, newColor, newColor).getRGB());
			}
		}
		return returnImg;

	}

	// create the gradiant of the image
	public static BufferedImage gradientMagnitude(BufferedImage img) {
		int[][] colorAverage = new int[img.getWidth()][img.getHeight()];
		BufferedImage returnImage = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

		for (int height = 0; height < img.getHeight(); ++height) {
			for (int width = 0; width < img.getWidth(); ++width) {
				colorAverage[width][height] = img.getRGB(width, height);
				Color currentColor = new Color(colorAverage[width][height]);
				colorAverage[width][height] = (currentColor.getRed() + currentColor.getBlue() + currentColor.getGreen()) / 3;
			}
		}
		for (int width = 0; width < img.getWidth(); ++width) {
			for (int height = 0; height < img.getHeight(); ++height) {
				if (width != 0 && height != 0 && width != img.getWidth() - 1 && height != img.getHeight() - 1) {
					int horizontalDer = colorAverage[width - 1][height] - colorAverage[width + 1][height];
					int verticalDer = colorAverage[width][height - 1] - colorAverage[width][height + 1];
					int newColor = (int) Math.sqrt((double) (horizontalDer * horizontalDer + verticalDer * verticalDer));
					if (newColor > 255) {
						newColor = 255;
					}
					returnImage.setRGB(width, height, (new Color(newColor, newColor, newColor)).getRGB());
				} else {
					returnImage.setRGB(width, height, (new Color(127, 127, 127)).getRGB());
				}
			}
		}
		return returnImage;
	}

	// retarget the size of the image according to the provided parameters.
	public static BufferedImage retargetSize(BufferedImage img, int Width, int Height) {

		// If the image is has its Height changed
		if (Width == img.getWidth()) {
			if (Height == img.getHeight()) {
				return img;
			} else {
				// Update the transposed image accordingly
				Retargeter retParam = new Retargeter(img, true, Height);
				return Rotate(retParam.retarget(Height));
			}
		} else {
			if (img.getHeight() == Height) {
				// Update the image in accordance with the new width
				Retargeter reParam = new Retargeter(img, false, Width);
				return reParam.retarget(Width);
			} else {
				// Update the image in both height and width
				Retargeter retParam = new Retargeter(img, true, Height);
				BufferedImage newImg = Rotate(retParam.retarget(Height));
				retParam = new Retargeter(newImg, false, Width);
				return retParam.retarget(Width);
			}
		}
	}

	// Rotate the provided image
	private static BufferedImage Rotate(BufferedImage img) {
		int height = img.getHeight();
		int width = img.getWidth();
		BufferedImage retImg = new BufferedImage(height, width, img.getType());

		// Transpose retImg
		for (int tempWidth = 0; tempWidth < width; tempWidth++) {
			for (int tempHeight = 0; tempHeight < height; tempHeight++) {
				retImg.setRGB(tempHeight, tempWidth, img.getRGB(tempWidth, tempHeight));
			}
		}
		return retImg;
	}

	// Reveal the seams using the methods we learned in class
	public static BufferedImage showSeams(BufferedImage img, int width, int height) {

		// Create method params
		Retargeter retParam, verRet, horRet;
		BufferedImage retImg;
		int currWidth, currHeight;

		// If the image is has its Height changed
		if (img.getWidth() == width && img.getHeight() == height) {
			return img;
		} else if (img.getWidth() == width && img.getHeight() != height) {
			// Update the transposed image accordingly
			retParam = new Retargeter(img, true, height);
			return Rotate(SeamCreater(retParam, img, height, width, true));
		} else if (img.getWidth() != width && img.getHeight() == height) {
			// Show the seams of the new image according to the width and height
			retParam = new Retargeter(img, false, width);
			return SeamCreater(retParam, img, width, height, false);
		} else {
			// Show the seams of the new image according to the width and height
			currWidth = img.getWidth();
			currHeight = img.getHeight();
			verRet = new Retargeter(img, true, height);
			horRet = new Retargeter(img, false, width);

			if ((img.getHeight() < height && img.getWidth() > width) || (img.getHeight() > height && img.getWidth() > width)) {
				retImg = Rotate(SeamCreater(verRet, img, width, currHeight, true));
				return SeamCreater(horRet, retImg, width, retImg.getHeight(), false);
			} else {
				retImg = SeamCreater(horRet, img, width, currHeight, false);
				return Rotate(SeamCreater(verRet, retImg, height, currWidth, true));
			}

		}
	}


	// Color the red and green seems on the image
	private static BufferedImage SeamCreater(Retargeter retargeter, BufferedImage img, int width, int height, Boolean vertical) {
		int[][] seamMatrix = retargeter.getSeamsOrderMatrix();
		int colorRGB, coloring, diff, currWidth;

		// if (vertical == true) color according to the transposed (update the (params for the vertical coloring)
		if (vertical) {
			coloring = 0x00FF00;
			diff = Math.abs(width - img.getHeight());
			currWidth = img.getHeight();
		} else {
			coloring = 0xFF0000;
			diff = Math.abs(width - img.getWidth());
			currWidth = img.getWidth();
		}
		BufferedImage retImg = new BufferedImage(currWidth, height, img.getType());

		// Color the pixels accordingly
		for (int tempHeight = 0; tempHeight < height; tempHeight++) {
			for (int tempWidth = 0; tempWidth < currWidth; tempWidth++) {
				if (vertical) {
					colorRGB = img.getRGB(tempHeight, tempWidth);
				} else {
					colorRGB = img.getRGB(tempWidth, tempHeight);
				}
				if (seamMatrix[tempHeight][tempWidth] <= diff) {
					retImg.setRGB(tempWidth, tempHeight, coloring);
				} else {
					retImg.setRGB(tempWidth, tempHeight, colorRGB);
				}
			}
		}
		return retImg;
	}


}
