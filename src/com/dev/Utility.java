package com.dev;

public class Utility {
	
	public static boolean isNull(Object theObject) {
		if(theObject==null) {
			return true;
		}else {
			return false;
		}
		
	}
	
	/***
	 * This method checks if the input image is a valid image or not. Currently on jpg and png files are acceptable.
	 * @param inputFile input image file name.
	 * @return true/false 
	 */
	public static boolean isValidInputImage(String inputFile) {
		return inputFile.endsWith("jpg")||inputFile.endsWith("png");
	}
	
	/***
	 * This method checks if the output image is a valid image or not. Currently on png files are acceptable.
	 * @param inputFile output image file name.
	 * @return true/false 
	 */
	public static boolean isValidoutputImage(String inputFile) {
		return inputFile.endsWith("png");
	}
}
