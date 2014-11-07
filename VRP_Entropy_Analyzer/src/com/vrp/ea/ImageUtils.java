package com.vrp.ea;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ImageUtils {

	public static void main(String[] args) {
		
		if(args.length < 3){
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
		
		for(int i=2;i<args.length;i++){
			keyword = keyword + args[i] +" ";
		}
		
		try {
			BufferedImage image = getImageFromKeyword(keyword);

			image = getResizedImage(image, 500d);

			if(imageColor.equals("1")){
				image = colorImageToBlackAndWhite(image, Integer.parseInt(threshold));
			}else if(!imageColor.equals("2")){
				System.out.println("INVALID COLOR OPTION. Please choose (1) for black and white or (2) for color.");
			}
			
			Double entropyResult = calculateShannonEntropy(imageToValues(image));
			System.out.println("Entropy result:"+entropyResult);
			
			JOptionPane.showMessageDialog(null, "", "", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(image));
			JOptionPane.showMessageDialog(null, "Entropy result: "+entropyResult, "", JOptionPane.INFORMATION_MESSAGE, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> imageToValues(BufferedImage originalImage) throws Exception {
		int pixelColor;
		
		ArrayList<String> values = new ArrayList<String>();
		
		for (int i = 0; i < originalImage.getWidth(); i++) {
			for (int j = 0; j < originalImage.getHeight(); j++) {

				// Get pixels
				pixelColor = new Color(originalImage.getRGB(i, j)).getRGB();

				values.add(String.valueOf(pixelColor));
			}
		}
		
		return values;
	}
	
	public static BufferedImage getImageFromKeyword(String keyword) throws Exception {
		BufferedImage image = null;
		String[] urls = getImageURLsFromKeyword(URLEncoder.encode(keyword, "UTF-8"));
		int i = 0;
		do {
			try {
				image = ImageIO.read(new URL(urls[i]));
			} catch (Exception e) {
				image = null;
			}
			i++;

			if (i > urls.length) {
				throw new Exception("NO IMAGES FOUND");
			}
		} while (image == null);

		return image;
	}

	public static BufferedImage getResizedImage(BufferedImage originalImage, double maximumSideLength) {
		int biggerSide = 0;

		if (originalImage.getWidth() > originalImage.getHeight()) {
			biggerSide = 1;
		} else if (originalImage.getWidth() < originalImage.getHeight()) {
			biggerSide = 2;
		} else {
			biggerSide = 3;
		}

		if (originalImage.getWidth() > maximumSideLength && biggerSide == 1) {
			int newWidth = (int) maximumSideLength;
			int newHeight = (int) (originalImage.getHeight() * (maximumSideLength / originalImage.getWidth()));

			originalImage = resizeImage(originalImage, originalImage.getType(), newWidth, newHeight);
		}

		if (originalImage.getHeight() > maximumSideLength && biggerSide == 2) {
			int newHeight = (int) maximumSideLength;
			int newWidth = (int) (originalImage.getWidth() * (maximumSideLength / originalImage.getHeight()));

			originalImage = resizeImage(originalImage, originalImage.getType(), newWidth, newHeight);
		}

		if (originalImage.getHeight() > maximumSideLength && biggerSide == 3) {
			originalImage = resizeImage(originalImage, originalImage.getType(), (int) maximumSideLength, (int) maximumSideLength);
		}

		return originalImage;
	}

	public static BufferedImage colorImageToBlackAndWhite(BufferedImage originalImage, int threshold) throws Exception {
		BufferedImage binarized = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

		int red;
		int newPixel;

		for (int i = 0; i < originalImage.getWidth(); i++) {
			for (int j = 0; j < originalImage.getHeight(); j++) {

				// Get pixels
				red = new Color(originalImage.getRGB(i, j)).getRed();

				int alpha = new Color(originalImage.getRGB(i, j)).getAlpha();

				if (red > threshold) {
					newPixel = 0;
				} else {
					newPixel = 255;
				}
				newPixel = colorToRGB(alpha, newPixel, newPixel, newPixel);
				binarized.setRGB(i, j, newPixel);

			}
		}
		// ImageIO.write(binarized, "jpg", new File("blackwhiteimage"));
		return binarized;
	}

	public static int colorToRGB(int alpha, int red, int green, int blue) {
		int newPixel = 0;
		newPixel += alpha;
		newPixel = newPixel << 8;
		newPixel += red;
		newPixel = newPixel << 8;
		newPixel += green;
		newPixel = newPixel << 8;
		newPixel += blue;

		return newPixel;
	}

	public static String[] getImageURLsFromKeyword(String keyword) throws Exception {
		URL url = new URL("https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=" + keyword);
		URLConnection connection = url.openConnection();

		String line;
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}

		JsonParser parser = new JsonParser();

		JsonElement jsonE = parser.parse(builder.toString());

		JsonObject jsonO = jsonE.getAsJsonObject();

		JsonObject responseData = jsonO.getAsJsonObject("responseData");
		JsonArray results = responseData.getAsJsonArray("results");

		String[] imageURLs = new String[results.size()];
		for (int i = 0; i < results.size(); i++) {
			JsonObject resObj = results.get(i).getAsJsonObject();
			imageURLs[i] = resObj.get("url").getAsString();
		}

		return imageURLs;
	}

	public static BufferedImage resizeImage(BufferedImage originalImage, int type, int width, int height) {
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, width, height, null);
		g.dispose();

		return resizedImage;
	}

	public static Double calculateShannonEntropy(List<String> values) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		// count the occurrences of each value
		for (String sequence : values) {
			if (!map.containsKey(sequence)) {
				map.put(sequence, 0);
			}
			map.put(sequence, map.get(sequence) + 1);
		}

		// calculate the entropy
		Double result = 0.0;
		for (String sequence : map.keySet()) {
			Double frequency = (double) map.get(sequence) / values.size();
			//result -= frequency * (Math.log(frequency) / Math.log(2));
			result -=  frequency * (Math.log(frequency) / Math.log(2));
		}

		return result;
	}
}
