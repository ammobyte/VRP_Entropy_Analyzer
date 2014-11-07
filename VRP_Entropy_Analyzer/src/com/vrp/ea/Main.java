package com.vrp.ea;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class Main {
	public static void main(String[] args) {

		if (args.length < 3) {
			System.out.println("PARAMETERS:");
			System.out.println("1.- Black and white(1) or Color(2) images.");
			System.out.println("2.- Black and white threshold (1-250). Ignored if color is chosen");
			System.out.println("3.- Keywords. Example: Hello world.");
			System.out.println();
			System.out.println("Run the program like this: java -jar VRP.jar 1 100 Bill Gates");
			return;
		}

		String imageColor = args[0];
		String threshold = args[1];

		String keyword = "";

		for (int i = 2; i < args.length; i++) {
			keyword = keyword + args[i] + " ";
		}

		try {
			BufferedImage image = ImageUtils.getImageFromKeyword(keyword);

			image = ImageUtils.getResizedImage(image, 500d);

			if (imageColor.equals("1")) {
				image = ImageUtils.colorImageToBlackAndWhite(image, Integer.parseInt(threshold));
			} else if (!imageColor.equals("2")) {
				System.out.println("INVALID COLOR OPTION. Please choose (1) for black and white or (2) for color.");
			}

			Double entropyResult = ImageUtils.calculateShannonEntropy(ImageUtils.imageToValues(image));
			System.out.println("Entropy result:" + entropyResult);

			JOptionPane.showMessageDialog(null, "", "", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(image));
			JOptionPane.showMessageDialog(null, "Entropy result: " + entropyResult, "", JOptionPane.INFORMATION_MESSAGE, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
