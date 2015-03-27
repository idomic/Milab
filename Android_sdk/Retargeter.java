package edu.cg;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Retargeter {

	private BufferedImage originImg;
	private boolean vertical;
	private int[][] imgCostMat, imgSeamMat, originPosMat, grayImgMat;
	private int newParam, newWidth, height, width;
	private static final int INF = 2000000000;

	// Create Retargeter and set parameters accordingly
	public Retargeter(BufferedImage m_img, boolean m_isVertical, int newSize) {

		// Initialize case for vertical == false
		originImg = m_img;
		BufferedImage retImg = originImg;
		height = originImg.getHeight();
		width = originImg.getWidth();
		vertical = m_isVertical;

		// Change params for vertical == true
		if (vertical) {
			retImg = new BufferedImage(height, width, originImg.getType());

			// Transpose originImg
			for (int tempHeight = 0; tempHeight < height; tempHeight++) {
				for (int tempWidth = 0; tempWidth < width; tempWidth++) {
					retImg.setRGB(tempHeight, tempWidth, originImg.getRGB(tempWidth, tempHeight));
				}
			}
			height = retImg.getHeight();
			width = retImg.getWidth();
		}

		// Set remaining params
		BufferedImage grImg = ImageProc.grayScale(retImg);
		originImg = retImg;
		imgCostMat = new int[height][width];
		imgSeamMat = new int[height][width];
		originPosMat = new int[height][width];
		grayImgMat = new int[width][height];
		newWidth = newSize;

		// Update matrices
		for (int tempHeight = 0; tempHeight < height; tempHeight++) {
			for (int tempWidth = 0; tempWidth < width; tempWidth++) {
				imgSeamMat[tempHeight][tempWidth] = INF;
				originPosMat[tempHeight][tempWidth] = tempWidth;
				grayImgMat[tempWidth][tempHeight] = new Color(grImg.getRGB(tempWidth, tempHeight)).getBlue();
			}
		}

		// find the number of seams we need to calculate and send it to calculation
		newParam = Math.abs(newWidth - originImg.getWidth());
		calculateSeamsOrderMatrix();
	}

	// Returns the seamOrderMatrix of the Retargeter
	public int[][] getSeamsOrderMatrix() {
		int[][] retSeamsOrder;

		// set the size of the matrix retSeamsOrder according to if vertical == true or not.
		if (vertical) {
			retSeamsOrder = new int[height][width];
		} else {
			retSeamsOrder = new int[width][height];
		}
		for (int tempHeight = 0; tempHeight < retSeamsOrder.length; tempHeight++) {
			for (int tempWidth = 0; tempWidth < retSeamsOrder[0].length; tempWidth++) {
				retSeamsOrder[tempHeight][tempWidth] = imgSeamMat[tempHeight][tempWidth];
			}
		}
		return retSeamsOrder;
	}

	// Returns the originPosMat of the Retargeter
	public int[][] getOriginPosMat() {

		int updatedWidth = Math.abs(originImg.getWidth() - newParam);
		int[][] retPosMat = new int[height][updatedWidth];

		for (int tempHeight = 0; tempHeight < height; tempHeight++) {
			for (int tempWidth = 0; tempWidth < updatedWidth; tempWidth++) {
				retPosMat[tempHeight][tempWidth] = originPosMat[tempHeight][tempWidth];
			}
		}
		return retPosMat;
	}

	// Change the matrices according to newSize after calculating the existing matrices.
	public BufferedImage retarget(int newSize) {
		BufferedImage retResultedRetargeter = new BufferedImage(newSize, height, width);

		// Implement this if the newSize is greater than the original images width
		if (newSize > width) {
			for (int tempHeight = 0; tempHeight < height; tempHeight++) {
				int clrCounter = 0;

				// If a seem uses a pixel checked then double the seam, otherwise copy it to retResultedRetargeter.
				for (int tempWidth = 0; tempWidth < width; tempWidth++) {
					int RGBColor = originImg.getRGB(tempWidth, tempHeight);

					// Check if there is a seam connected to the pixel
					if (imgSeamMat[tempHeight][tempWidth] <= newParam) {
						retResultedRetargeter.setRGB(clrCounter, tempHeight, RGBColor);
						clrCounter++;
						retResultedRetargeter.setRGB(clrCounter, tempHeight, RGBColor);
					} else {
						retResultedRetargeter.setRGB(clrCounter, tempHeight, RGBColor);
					}
					clrCounter++;
				}
			}
		} else {
			// If a seem uses a pixel checked then do nothing to the seam, otherwise copy it to retResultedRetargeter.
			for (int tempHeight = 0; tempHeight < height; tempHeight++) {
				int clrCounter = 0;
				for (int tempWidth = 0; tempWidth < width; tempWidth++) {
					int RGBColor = originImg.getRGB(tempWidth, tempHeight);
					if (imgSeamMat[tempHeight][tempWidth] > newParam) {
						retResultedRetargeter.setRGB(clrCounter, tempHeight, RGBColor);
						clrCounter++;
					}
				}
			}
		}
		return retResultedRetargeter;
	}

	// Calculating the seam order matrix according to the formula learned in class.
	private void calculateSeamsOrderMatrix() {
		calculateCostsMatrix(width);
		int colCounter;
		int[] seamMatPos = new int[height];

		// find newParam number of seams
		for (int counter = 1; counter <= newParam; counter++) {
			int startingPos = MinIndex(imgCostMat[height - 1], width - counter + 1);

			// find the original position in origPostMatrix
			int posInOrigin = originPosMat[height - 1][startingPos];
			imgSeamMat[height - 1][posInOrigin] = counter;
			seamMatPos[height - 1] = startingPos;

			// This loop making the Backtracking to find the seam pixels
			for (int tempHeight = height - 2; tempHeight >= 0; tempHeight--) {

				// If there is one column remaining
				if ((startingPos == 0) && ((startingPos + 1) > (width - counter))) {
					colCounter = 1;
				}

				// If this is the first column
				else if ((startingPos == 0) && ((startingPos + 1) <= (width - counter))) {
					int[] minArr = {imgCostMat[tempHeight][startingPos], imgCostMat[tempHeight][startingPos + 1]};
					colCounter = MinIndex(minArr, 2) + 1;
				}

				// If this is the last column
				else if ((startingPos + 1) > (width - counter)) {
					int[] MinArr = {imgCostMat[tempHeight][startingPos - 1], imgCostMat[tempHeight][startingPos]};
					colCounter = MinIndex(MinArr, 2);
				} else {
					int[] MinArr = {imgCostMat[tempHeight][startingPos - 1], imgCostMat[tempHeight][startingPos], imgCostMat[tempHeight][startingPos + 1]};
					colCounter = MinIndex(MinArr, 3);
				}

				int newPos = colCounter + startingPos - 1;
				posInOrigin = originPosMat[tempHeight][newPos];
				imgSeamMat[tempHeight][posInOrigin] = counter;
				seamMatPos[tempHeight] = newPos;
				startingPos = newPos;
			}

			// Delete the seem found and calculate the new cost matrix that results.
			SeamDeletion(width - counter, seamMatPos);
			calculateCostsMatrix(width - counter);
		}
	}

	// Delete a seam according to a provided location Index, also delete this pixel from origPostMatrix and grayImgMat.
	private void SeamDeletion(int imgWidth, int[] Index) {
		Boolean check;
		int dWidth = imgWidth;
		for (int tempHeight = 0; tempHeight < height; tempHeight++) {
			check = false;
			for (int tempWidth = 0; tempWidth < dWidth; tempWidth++) {

				// when at index position, move the pixel a column to the left
				if (Index[tempHeight] == tempWidth) {
					check = true;
				}
				if (check) {
					grayImgMat[tempWidth][tempHeight] = grayImgMat[tempWidth + 1][tempHeight];
					originPosMat[tempHeight][tempWidth] = originPosMat[tempHeight][tempWidth + 1];
				}
			}
		}
	}

	// Returns the minimum index in the input array
	private int MinIndex(int[] givenArr, int Index) {
		int minimumIndex = 0;
		int minValInArr = givenArr[0];
		for (int counter = 1; counter < Index; counter++) {
			if (givenArr[counter] < minValInArr) {
				minimumIndex = counter;
				minValInArr = givenArr[counter];
			}
		}
		return minimumIndex;
	}

	// Using what we learned in class, calculate the cost matrix
	private void calculateCostsMatrix(int w) {
		int right, left, diag, cl, cv, cr;
		imgCostMat[0][0] = 1000;
		imgCostMat[0][w - 1] = 1000;

		// calculate the cost value of each pixel
		for (int tempHeight = 0; tempHeight < height; tempHeight++) {
			for (int tempWidth = 0; tempWidth < w; tempWidth++) {

				// In case this is the first row
				if (tempHeight == 0) {
					if (tempWidth > 0 && tempWidth < w - 1) {
						imgCostMat[tempHeight][tempWidth] = Math.abs(grayImgMat[tempWidth - 1][tempHeight] - grayImgMat[tempWidth + 1][tempHeight]);
					}
				}

				// In case this is the first column
				else if (tempWidth == 0) {
					right = grayImgMat[tempWidth + 1][tempHeight];
					left = 0;
					diag = grayImgMat[tempWidth][tempHeight - 1];
					cv = Math.abs(right - left);
					cr = Math.abs(right - left) + Math.abs(diag - right);
					imgCostMat[tempHeight][tempWidth] = Math.min(imgCostMat[tempHeight - 1][tempWidth] + cv, imgCostMat[tempHeight - 1][tempWidth + 1] + cr);
				}

				// In case this is the last column
				else if (tempWidth == (w - 1)) {
					right = 0;
					left = grayImgMat[tempWidth - 1][tempHeight];
					diag = grayImgMat[tempWidth][tempHeight - 1];
					cl = Math.abs(right - left) + Math.abs(diag - left);
					cv = Math.abs(right - left);
					imgCostMat[tempHeight][tempWidth] = Math.min(imgCostMat[tempHeight - 1][tempWidth - 1] + cl, imgCostMat[tempHeight - 1][tempWidth] + cv);
				} else {
					right = grayImgMat[tempWidth + 1][tempHeight];
					left = grayImgMat[tempWidth - 1][tempHeight];
					diag = grayImgMat[tempWidth][tempHeight - 1];
					cl = Math.abs(right - left) + Math.abs(diag - left);
					cv = Math.abs(right - left);
					cr = Math.abs(right - left) + Math.abs(diag - right);
					imgCostMat[tempHeight][tempWidth] = Math.min(imgCostMat[tempHeight - 1][tempWidth - 1] + cl, Math.min(imgCostMat[tempHeight - 1][tempWidth] + cv, imgCostMat[tempHeight - 1][tempWidth + 1] + cr));
				}
			}
		}
	}
}