package uk.ac.bham.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public abstract class Assets {
	
	//Background
	public static BufferedImage background;
	
	//Robot sprites
	public static BufferedImage head;
	public static BufferedImage body;
	public static BufferedImage leftArm;
	public static BufferedImage rightArm;
	
	
	//Table 
	
	public static BufferedImage table;
	
	//Objects
	public static BufferedImage container;
	
	
	
	
	public static void loadAssets() throws IOException
	{
		background = ImageIO.read(new File("assets/bg.png"));
		
		head = ImageIO.read(new File("assets/robot_head.png"));
		body = ImageIO.read(new File("assets/robot_body.png"));

		rightArm = ImageIO.read(new File("assets/robot_right_arm.png"));
		leftArm = ImageIO.read(new File("assets/robot_left_arm.png"));
		
		table = ImageIO.read(new File("assets/table.png"));
		
		container = ImageIO.read(new File("assets/container.png"));
		
	}

}
