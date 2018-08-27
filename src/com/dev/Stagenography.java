package com.dev;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

public class Stagenography {

	public static void main(String[] args) {
		System.out.println("Starting");

		String inputImageFile="D:\\test1.jpg";
		String outputImageFile="D:\\raj.png";

		String secretText="Devendra";

		//Encode message
		encode(inputImageFile,secretText, outputImageFile);

		//Decode Message
		System.out.println("---------------------DECODING STARTED -------------------");
		String message=decode(outputImageFile,secretText.length());
		System.out.println("MEssafge : >>"+message+"<<");

		System.out.println("DONE");
	}

	private static String decode(String inputImageFile, int messageLength) {
		byte[] messageBytes=new byte[messageLength];
		
		BufferedImage theBufferedImage=getBufferedImageFromFile(inputImageFile);
				
		//get width and height of image 
		int imageWidth=theBufferedImage.getWidth();
		int imageHeight=theBufferedImage.getHeight();

		int messageByteCounter=0;

		boolean isMessageFinished=false;
		
		//Read pixels of image (1 pixel = 4bytes. Alpha, Red Green Blue)
		for(int i=0;i<imageWidth;i++) {
			for(int j=0;j<imageHeight;j++) {
				
				if(messageByteCounter>messageLength-1) {
					isMessageFinished=true;
					break;
				}
				
				int thePixel=theBufferedImage.getRGB(i, j);
				System.out.println("Image pixel : "+Integer.toBinaryString(thePixel));
				j++;
				
				int firstHalfByteOfMessage=extractHalfByteOfMessage(thePixel);
				System.out.println("first half message byte "+Integer.toBinaryString(firstHalfByteOfMessage));
				
				//get next pixel for 2nd half of byte
				thePixel=theBufferedImage.getRGB(i, j);
				System.out.println("Image pixel : "+Integer.toBinaryString(thePixel));
				
				int secondHalfByteOfMessage=extractHalfByteOfMessage(thePixel);
				System.out.println("Second half message byte "+Integer.toBinaryString(secondHalfByteOfMessage));
				
				//Combine both halves of the message byte
				System.out.println("Combined message byte : "+(byte)((firstHalfByteOfMessage<<4) | secondHalfByteOfMessage));
				messageBytes[messageByteCounter++]=(byte)((firstHalfByteOfMessage<<4) | secondHalfByteOfMessage);
			}
			
			if(isMessageFinished) {
				break;
			}
		}
		System.out.println("Message bytes : "+Arrays.toString(messageBytes));
		return new String(messageBytes);
	}

	private static int extractHalfByteOfMessage(int thePixel) {
		
		int messageHalfByte;
		
		//1 pixel contains 4 Bytes. 1 byte for alpha info, 3 bytes for color information (1 for each color, red, green and blue).
		int alpha=(thePixel>>24) & 0xFF;
		int red=(thePixel>>16) & 0xFF;
		int green=(thePixel>>8) & 0xFF;
		int blue=thePixel & 0xFF;

		System.out.println("A : "+Integer.toBinaryString(alpha));
		System.out.println("R : "+Integer.toBinaryString(red));
		System.out.println("G : "+Integer.toBinaryString(green));
		System.out.println("B : "+Integer.toBinaryString(blue));

		int bitFromAlpha=alpha & 1;
		int bitFromRed=red & 1;
		int bitFromGreen=green & 1;
		int bitFromBlue=blue & 1;
		messageHalfByte=(bitFromAlpha<<3) | (bitFromRed<<2) | (bitFromGreen<<1) | (bitFromBlue);
		

		return messageHalfByte;
	}

	private static void encode(String inputImageFile, String message, String outputImageFile) {
		BufferedImage theBufferedImage=getBufferedImageFromFile(inputImageFile);
		byte[] messageBytes=message.getBytes();

		System.out.println("Message bytes : "+Arrays.toString(messageBytes));

		//get width and height of image 
		int imageWidth=theBufferedImage.getWidth();
		int imageHeight=theBufferedImage.getHeight();

		int messageByteCounter=0;

		boolean isMessageFinished=false;
		
		//Read pixels of image (1 pixel = 4bytes. Alpha, Red Green Blue)
		for(int i=0;i<imageWidth;i++) {
			for(int j=0;j<imageHeight;j++) {
				
				if(messageByteCounter>message.length()-1) {
					isMessageFinished=true;
					break;
				}
				
				int thePixel=theBufferedImage.getRGB(i, j);
				System.out.println("Image pixel : "+Integer.toBinaryString(thePixel));
				
				//get current messagebyte
				byte theMessageByte=messageBytes[messageByteCounter++];
				
				System.out.println("Message byte to encode : "+theMessageByte+" : "+Integer.toBinaryString(theMessageByte));
				
				
				//get 1st half of message byte
				int firstHalfMessageByte=(theMessageByte & 0xF0) >> 4;
				System.out.println("first half message byte "+Integer.toBinaryString(firstHalfMessageByte));
		
				//add this half byte to current pixel and get modified pixel
				int modifiedPixel=getModifiedPixel(thePixel, firstHalfMessageByte);
				System.out.println("Modified pixel : "+Integer.toBinaryString(modifiedPixel));
				
				//update the current pixel of the image
				theBufferedImage.setRGB(i, j, modifiedPixel);
				j++;

				//get next pixel to put 2nd half of the message byte
				thePixel=theBufferedImage.getRGB(i, j);
				System.out.println("Image pixel : "+Integer.toBinaryString(thePixel));
				
				//get 2nd half of message byte
				int secondHalfMessagebyte=(theMessageByte & 0x0F);
				System.out.println("2nd half message byte "+Integer.toBinaryString(secondHalfMessagebyte));
				
				//add this half byte to current pixel and get modified pixel
				modifiedPixel=getModifiedPixel(thePixel, secondHalfMessagebyte);
				System.out.println("Modified pixel : "+Integer.toBinaryString(modifiedPixel));
				
				//update the current pixel of the image
				theBufferedImage.setRGB(i, j, modifiedPixel);
			}
			
			if(isMessageFinished) {
				break;
			}
		}


		//Write the file.
		try {
			ImageIO.write(theBufferedImage, "png", new File(outputImageFile));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static int getModifiedPixel(int thePixel, int halfByte) {
		
		//1 pixel contains 4 Bytes. 1 byte for alpha info, 3 bytes for color information (1 for each color, red, green and blue).
		int alpha=(thePixel>>24) & 0xFF;
		int red=(thePixel>>16) & 0xFF;
		int green=(thePixel>>8) & 0xFF;
		int blue=thePixel & 0xFF;

		System.out.println("A : "+Integer.toBinaryString(alpha));
		System.out.println("R : "+Integer.toBinaryString(red));
		System.out.println("G : "+Integer.toBinaryString(green));
		System.out.println("B : "+Integer.toBinaryString(blue));

		//add the halfbyte of message to the 4 bytes of of the pixel and get modified bytes
		int modifiedAlpha=(halfByte >> 3 & 1) | (alpha>>1<<1);
		int modifiedRed=(halfByte >> 2 & 1) | (red>>1<<1);
		int modifiedGreen=(halfByte >> 1 & 1) | (green>>1<<1);
		int modifiedBlue=(halfByte & 1) | (blue>>1<<1);

		System.out.println("Modified A : "+Integer.toBinaryString(modifiedAlpha));
		System.out.println("Modified R : "+Integer.toBinaryString(modifiedRed));
		System.out.println("Modified G : "+Integer.toBinaryString(modifiedGreen));
		System.out.println("Modified B : "+Integer.toBinaryString(modifiedBlue));
		
		//Construct the pixel using the 4 bytes of Alpha, Red, green and blue.
		thePixel= (modifiedAlpha << 24) | (modifiedRed << 16) | (modifiedGreen << 8) | (modifiedBlue);

		return thePixel;
	}

	private static BufferedImage getBufferedImageFromFile(String inputFile) {

		BufferedImage localBufferedImage=null;

		if(Utility.isNull(inputFile)) {
			return null;
		}else {
			if(isValidImage(inputFile)) {
				localBufferedImage=createLocalCopyImage(inputFile);
			}else {
				System.err.println("Wrong format image");
				return null;
			}
		}

		return localBufferedImage;
	}

	private static BufferedImage createLocalCopyImage(String inputFile) {

		BufferedImage localBufferedImage=null;

		if(Utility.isNull(inputFile)) {
			return null;
		}else {
			File theInputFile=new File(inputFile);
			try {
				BufferedImage theBufferedImage=ImageIO.read(theInputFile);
				localBufferedImage=new BufferedImage(theBufferedImage.getWidth(), theBufferedImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
				Graphics2D graphics=localBufferedImage.createGraphics();
				graphics.drawRenderedImage(theBufferedImage, null);
				graphics.dispose();

			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}


		return localBufferedImage;
	}

	private static boolean isValidImage(String inputFile) {
		return inputFile.endsWith("jpg")||inputFile.endsWith("png");
	}

}
