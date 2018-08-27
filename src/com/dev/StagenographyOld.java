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

		String inputImageFile="D:\\test.jpg";
		String outputImageFile="D:\\raj.jpg";

		String secretText="AC";

		//Encode message
		encode(inputImageFile,secretText, outputImageFile);

		//Decode Message
		String message=decode(outputImageFile,secretText.length());
		System.out.println("MEssafge : >>"+message+"<<");

		System.out.println("DONE");
	}

	private static String decode(String inputImageFile, int messageLength) {
		String message=null;
		byte[] messageBytes=new byte[messageLength];
		int messageByteCounter=0;
		int currentByte=0;
		
		boolean exitAllLoops=false;
		
		BufferedImage temp=getBufferedImageFromFile(inputImageFile);
		int width=temp.getWidth();
		int height=temp.getHeight();
		
		int internalLoop=0;
		
		//Read pixels of image (1 pixel = 4bytes. Alpha, Red Green Blue)
		for(int i=0;i<width;i++) {
			for(int j=0;j<height;j++) {
				int pixel=temp.getRGB(i, j);
				
				int alpha=(pixel>>24) & 0xFF;
				int red=(pixel>>16) & 0xFF;
				int green=(pixel>>8) & 0xFF;
				int blue=pixel & 0xFF;

				if(internalLoop==2) {
					internalLoop=0;
					currentByte=0;
				}
				
				int bitFromAlpha=(alpha>>7)|0;
				int bitFromRed=(red>>7)|0;
				int bitFromGreen=(green>>7)|0;
				int bitFromBlue=(blue>>7)|0;
				
				
				
				if(internalLoop==0) {
					currentByte=(bitFromAlpha<<7)|(bitFromRed<<6)|(bitFromGreen<<5)|(bitFromBlue<<4);
				}else if(internalLoop==1) {
					currentByte=currentByte|(bitFromAlpha<<3)|(bitFromRed<<2)|(bitFromGreen<<1)|(bitFromBlue);
					messageBytes[messageByteCounter++]=(byte) currentByte;
					System.out.println("Current Byte : "+currentByte+" "+Integer.toBinaryString(currentByte));
				}
				
				if(messageByteCounter==messageLength) {
					exitAllLoops=true;
					break;
				}
				
				internalLoop++;
			}
			
			if(exitAllLoops) {
				break;
			}
		}
		
		message=new String(messageBytes);
		
		return message;
	}

	private static void encode(String inputImageFile, String message, String outputImageFile) {

		BufferedImage temp=getBufferedImageFromFile(inputImageFile);
		byte[] messageBytes=message.getBytes();

		System.out.println("Message bytes : "+Arrays.toString(messageBytes));
		
		int width=temp.getWidth();
		int height=temp.getHeight();

		byte theMessageByte=messageBytes[0];
		int messageByteCounter=0;
		int internalLoop=1;
		boolean exitAllLoops=false;


		//Read pixels of image (1 pixel = 4bytes. Alpha, Red Green Blue)
		for(int i=0;i<width;i++) {
			for(int j=0;j<height;j++) {

				int pixel=temp.getRGB(i, j);
				System.out.println("Pixel --> "+pixel+" .. "+Integer.toBinaryString(pixel));

				int alpha=(pixel>>24) & 0xFF;
				int red=(pixel>>16) & 0xFF;
				int green=(pixel>>8) & 0xFF;
				int blue=pixel & 0xFF;
				
				System.out.println("A : "+Integer.toBinaryString(alpha));
				System.out.println("R : "+Integer.toBinaryString(red));
				System.out.println("G : "+Integer.toBinaryString(green));
				System.out.println("B : "+Integer.toBinaryString(blue));

				if(messageByteCounter>message.length()-1) {
					exitAllLoops=true;
					break;
				}

				
				
				int modifiedAlpha=(theMessageByte>>7 & 1) | alpha;
				int modifiedRed=(theMessageByte>>7 & 1) | red;
				int modifiedGreen=(theMessageByte>>7 & 1) | green;
				int modifiedBlue=(theMessageByte>>7 & 1) | blue;

				System.out.println("A : "+Integer.toBinaryString(modifiedAlpha));
				System.out.println("R : "+Integer.toBinaryString(modifiedRed));
				System.out.println("G : "+Integer.toBinaryString(modifiedGreen));
				System.out.println("B : "+Integer.toBinaryString(modifiedBlue));
				
				pixel=(modifiedAlpha<<24)|(modifiedRed)<<16|(modifiedGreen<<8)|(modifiedBlue);
				temp.setRGB(i, j, pixel);

				if(internalLoop==2) {
					internalLoop=0;
					if(messageByteCounter>message.length()-1) {
						exitAllLoops=true;
						break;
					}
					theMessageByte=messageBytes[++messageByteCounter];
				}

				internalLoop++;
				//System.out.println(Integer.toBinaryString(alpha)+" : "+Integer.toBinaryString(red)+" : "+Integer.toBinaryString(green)+" : "+Integer.toBinaryString(blue));
			}

			if(exitAllLoops) {
				break;
			}

		}

		//Add a new Pixel. Ending Pixel;
		
		
		//Write the file.
		try {
			ImageIO.write(temp, "jpg", new File(outputImageFile));
		} catch (IOException e) {
			e.printStackTrace();
		}

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
				localBufferedImage=new BufferedImage(theBufferedImage.getWidth(), theBufferedImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
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
