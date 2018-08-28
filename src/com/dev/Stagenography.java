package com.dev;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/***
 * 
 * @author Devendra_Yadav
 * 
 */
public class Stagenography {

	private static final byte END_OF_MESSAGE_BYTE=(byte)223;
	
	public static void main(String[] args) {
		cmdLineHandling(args);
	}

	/***
	 * This method shows usgae information and handles the cmd line inputs and take appropriate actions
	 * @param args input arguments related to encoding or decoding 
	 */
	private static void cmdLineHandling(String[] args) {
		if(args==null) {
			showHelpMessage();
		}else if(args[0].equalsIgnoreCase("-encode")&&args.length==4) {
			if(args[1]==null||args[2]==null||args[3]==null) {
				showHelpMessage();
			}else {
				String inputImageFile=args[1];
				String message=args[2];
				String outputImageFile=args[3];
				boolean isEncodingSuccessfull=encode(inputImageFile, message, outputImageFile);
				if(isEncodingSuccessfull) {
					System.out.println("Message encoded. Output image file : \""+outputImageFile+"\". Message length : "+message.length());
				}else {
					System.err.println("Could not encode!!");
				}
				
			}
		}else if(args[0].equalsIgnoreCase("-decode")&&args.length==2) {
			if(args[1]==null) {
				showHelpMessage();
			}else {
				String inputImageFile=args[1];
				try {
					String message=decode(inputImageFile);
					if(message==null) {
						System.out.println("No decoded message.");
					}else {
						System.out.println("Decoded Message >>"+message+"<< found in \""+inputImageFile+"\"");
					}
					
				} catch (Exception e) {
					System.err.println("Some unexpected exception. "+e.getMessage());
					showHelpMessage();
				}
				
			}
		}else {
			showHelpMessage();
		}
	}

	/***
	 * This method shows the usgage info on how to execute the cmd for Stagenography (encoding or decoding)
	 */
	private static void showHelpMessage() {
		System.out.println("\n------------>Usage<--------------");
		System.out.println("To Encode : java -jar Stagenography.jar -encode \"<input-image-fullpath>\" \"<hidden-text>\" \"<output-image-fullpath>\"");
		System.out.println("To Decode : java -jar Stagenography.jar -decode \"<image-file-fullpath>\"");
	}

	/***
	 * Decode the hidden message from the given image file
	 * @param inputImageFile input image file name
	 * @param messageLength length of the expected message
	 * @return decoded string message
	 */
	private static String decode(String inputImageFile) {
		//initialize the StribgBuilder that will store the decoded message from image
		StringBuilder message=new StringBuilder();
		
		//get the BufferedImage Object from the input image file.
		BufferedImage theBufferedImage=getBufferedImageFromFile(inputImageFile);
		
		if(theBufferedImage==null) {
			return null;
		}
		
		//get width and height of image 
		int imageWidth=theBufferedImage.getWidth();
		int imageHeight=theBufferedImage.getHeight();

		//Boolean value to track if all message bytes are decoded or not. Used to break out of loops.
		boolean isMessageFinished=false;
		
		//Read pixels of image (1 pixel = 4bytes. Alpha, Red Green Blue)
		for(int i=0;i<imageWidth;i++) {
			for(int j=0;j<imageHeight;j++) {
				
				/*
				 * We have 4 bytes from a single pixel. Last bit of each byte (Alpha, Red, Green and Blue) contains the bit of our message. 
				 * Therefore we need 2 pixels to get 1 byte(8 bits) of data.  
				 */
				
				//get current pixel value
				int thePixel=theBufferedImage.getRGB(i, j);
				
				//Extract the first half of the message byte
				int firstHalfByteOfMessage=extractHalfByteOfMessage(thePixel);
				
				//Increment j to get next pixel
				j++;
				
				//get next pixel for 2nd half of byte
				thePixel=theBufferedImage.getRGB(i, j);
				
				//Extract the second half of the message byte
				int secondHalfByteOfMessage=extractHalfByteOfMessage(thePixel);
				
				//Combine both halves of the message byte
				byte theMessageByte=(byte)((firstHalfByteOfMessage<<4) | secondHalfByteOfMessage);
				
				if(theMessageByte==END_OF_MESSAGE_BYTE) {
					isMessageFinished=true;
					break;
				}
				
				message.append((char)(theMessageByte));
			}
			
			//if all bytes of message extracted from image then break out of the loop.
			if(isMessageFinished) {
				break;
			}
		}
		
		return message.toString();
	}

	/***
	 * This method extract the last bit of each of the 4 bytes of the given pixel
	 * @param thePixel input pixel value
	 * @return extracted 4 bits from the given pixel
	 */
	private static int extractHalfByteOfMessage(int thePixel) {
		//Declare the variable to store the 4 bits of data that will be extracted from input pixel. 
		int messageHalfByte;
		
		//1 pixel contains 4 Bytes. 1 byte for alpha info, 3 bytes for color information (1 for each color, red, green and blue).
		//Get all 4 bytes from pixel.
		int alpha=(thePixel>>24) & 0xFF;
		int red=(thePixel>>16) & 0xFF;
		int green=(thePixel>>8) & 0xFF;
		int blue=thePixel & 0xFF;

		//Get last bit from each of the 4 Bytes (Alpha, Red, Green, Blue)
		int bitFromAlpha=alpha & 1;
		int bitFromRed=red & 1;
		int bitFromGreen=green & 1;
		int bitFromBlue=blue & 1;
		
		//Combine the 4 bits to form half byte
		messageHalfByte=(bitFromAlpha<<3) | (bitFromRed<<2) | (bitFromGreen<<1) | (bitFromBlue);
		

		return messageHalfByte;
	}

	/***
	 * This method initiate the encoding process (hiding message in the image)
	 * @param inputImageFile input image file name
	 * @param message text that we want to hide in the image
	 * @param outputImageFile output image file name. this image will contain the hidden message
	 * @return true/false for success/failure
	 */
	private static boolean encode(String inputImageFile, String message, String outputImageFile) {
		
		//get the BufferedImage object to start the work
		BufferedImage theBufferedImage=getBufferedImageFromFile(inputImageFile);
		
		if(theBufferedImage==null) {
			return false;
		}
		
		//Convert hidden text in string format to byte[]
		byte[] messageBytes=message.getBytes();

		//get width and height of image 
		int imageWidth=theBufferedImage.getWidth();
		int imageHeight=theBufferedImage.getHeight();

		//This counter will be used to check how many bytes of message has been encoded.
		int messageByteCounter=0;

		//Boolean value to check when all message bytes are encoded.
		boolean isMessageFinished=false;
		
		//Read pixels of image (1 pixel = 4bytes. Alpha, Red Green Blue)
		for(int i=0;i<imageWidth;i++) {
			for(int j=0;j<imageHeight;j++) {
				
				//Check if all message bytes are processed or not.
				if(messageByteCounter>message.length()-1) {
					isMessageFinished=true;
					
					//Add an extra byte at the end to mark the end of the message.
					//We will use END_OF_MESSAGE_BYTE static final variable to use as end of the message.
					
					//Get the pixel from image
					int thePixel=theBufferedImage.getRGB(i, j);
					
					/*
					 * We have 4 bytes from a single pixel. We will replace that last bit of each byte (Alpha, Red, Green and Blue) with the bit from our message byte.
					 * We need 2 pixels to hide 1 byte(8 bits) of data. hence we will work on 4 bit of message byte at a time. 
					 */
					
					//get 1st half of message byte 
					int firstHalfEndOfMessageByte=(END_OF_MESSAGE_BYTE & 0xF0) >> 4;
			
					//add this half byte to current pixel and get modified pixel
					int modifiedPixel=getModifiedPixel(thePixel, firstHalfEndOfMessageByte);
					
					//update the current pixel of the image
					theBufferedImage.setRGB(i, j, modifiedPixel);
					
					//Increment j to get next pixel
					j++;

					//get next pixel to put 2nd half of the message byte
					thePixel=theBufferedImage.getRGB(i, j);
					
					//get 2nd half of message byte
					int secondHalfEndOfMessagebyte=(END_OF_MESSAGE_BYTE & 0x0F);
					
					//add this half byte to current pixel and get modified pixel
					modifiedPixel=getModifiedPixel(thePixel, secondHalfEndOfMessagebyte);
					
					//update the current pixel of the image
					theBufferedImage.setRGB(i, j, modifiedPixel);
					
					break;
				}
				
				//Get the pixel from image
				int thePixel=theBufferedImage.getRGB(i, j);
				
				//get current messagebyte
				byte theMessageByte=messageBytes[messageByteCounter++];
				
				/*
				 * We have 4 bytes from a single pixel. We will replace that last bit of each byte (Alpha, Red, Green and Blue) with the bit from our message byte.
				 * We need 2 pixels to hide 1 byte(8 bits) of data. hence we will work on 4 bit of message byte at a time. 
				 */
				
				//get 1st half of message byte 
				int firstHalfMessageByte=(theMessageByte & 0xF0) >> 4;
		
				//add this half byte to current pixel and get modified pixel
				int modifiedPixel=getModifiedPixel(thePixel, firstHalfMessageByte);
				
				//update the current pixel of the image
				theBufferedImage.setRGB(i, j, modifiedPixel);
				
				//Increment j to get next pixel
				j++;

				//get next pixel to put 2nd half of the message byte
				thePixel=theBufferedImage.getRGB(i, j);
				
				//get 2nd half of message byte
				int secondHalfMessagebyte=(theMessageByte & 0x0F);
				
				//add this half byte to current pixel and get modified pixel
				modifiedPixel=getModifiedPixel(thePixel, secondHalfMessagebyte);
				
				//update the current pixel of the image
				theBufferedImage.setRGB(i, j, modifiedPixel);
			}
			
			//If all message bytes finished then break out of loop.
			if(isMessageFinished) {
				break;
			}
		}

		//Write the updated bufferedImage object to output image file. Currently only png output image file is accepted.
		boolean output=writeToImageFile(outputImageFile, theBufferedImage);

		if(output) {
			return true;
		}else {
			return false;
		}
		
	}

	/***
	 * Create an image file from BufferedImage object
	 * @param outputImageFile name of the image file to be created
	 * @param theBufferedImage BufferedImage object. This is the modified BufferedImage object after the message has been added to the original input image
	 * @return true/false for success/failure
	 */
	private static boolean writeToImageFile(String outputImageFile, BufferedImage theBufferedImage) {
		//Write the updated bufferedImage object to output image file. Currently only png output image file is accepted.
		try {
			if(Utility.isValidoutputImage(outputImageFile)) {
				ImageIO.write(theBufferedImage, "png", new File(outputImageFile));
				return true;
			}else {
				System.err.println("Only png output is accepted!!!");
				return false;
			}
		} catch (IOException e) {
			System.out.println("Some exception in creating output image file. "+e.getMessage());
			return false;
		}
	}

	/***
	 * This method takes an input pixel and replace the last bit of 4bytes (ARGB) with the 4 bits given as input(half byte from message)  
	 * @param thePixel input pixel value
	 * @param halfByte 4 bits of a Byte from message
	 * @return modified value of the pixel
	 */
	private static int getModifiedPixel(int thePixel, int halfByte) {
		
		//1 pixel contains 4 Bytes. 1 byte for alpha info, 3 bytes for color information (1 for each color, red, green and blue).
		int alpha=(thePixel>>24) & 0xFF;
		int red=(thePixel>>16) & 0xFF;
		int green=(thePixel>>8) & 0xFF;
		int blue=thePixel & 0xFF;

		//add the half byte of message to the 4 bytes of of the pixel and get modified bytes
		//Last bit of the 4 bytes are replaced with the 4 bits of input halfByte
		int modifiedAlpha=(halfByte >> 3 & 1) | (alpha>>1<<1);
		int modifiedRed=(halfByte >> 2 & 1) | (red>>1<<1);
		int modifiedGreen=(halfByte >> 1 & 1) | (green>>1<<1);
		int modifiedBlue=(halfByte & 1) | (blue>>1<<1);

		//Construct the pixel using the 4 bytes of Alpha, Red, green and blue.
		thePixel= (modifiedAlpha << 24) | (modifiedRed << 16) | (modifiedGreen << 8) | (modifiedBlue);

		return thePixel;
	}

	/***
	 * This method creates a local BufferedImage object from the given image file name 
	 * @param inputFile input image file name
	 * @return BufferedImage object of the input image file.
	 */
	private static BufferedImage getBufferedImageFromFile(String inputFile) {

		BufferedImage localBufferedImage=null;

		if(Utility.isNull(inputFile)) {
			return null;
		}else {
			if(Utility.isValidInputImage(inputFile)) {
				
				//get the local copy (BufferedImage object) of the image
				localBufferedImage=createLocalCopyImage(inputFile);
			}else {
				System.err.println("Wrong format image");
				return null;
			}
		}

		return localBufferedImage;
	}

	/***
	 * This method creates a local copy of the input image to work on.
	 * @param inputFile input image file name
	 * @return BufferedImage object of the input image file.
	 */
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

}
