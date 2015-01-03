package uk.ac.bham.util;

import java.awt.Point;

public class Vector2 {
	
	private double x, y;
	
	
	public Vector2()
	{

		
	}
	
	public Vector2(double x, double y)
	{
		this.x = x;
		this.y = y;
		
	}
	
	public Vector2(Point other)
	{
		this.setVector2(other);
	}
	
	
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
	
	public void setXY(double x, double y)
	{
		this.setX(x);
		this.setY(y);
	}
	
	public void setVector2(Vector2 other)
	{
		this.setXY(other.getX(), other.getY());
	}
	
	public void setVector2(Point other)
	{
		this.setXY(other.getX(), other.getY());
	}
	
	public Point toPoint()
	{
		return new Point((int)this.x,(int)this.y);
	}
	

	public Vector2 multCpy(double k)
	{
		Vector2 cpy = new Vector2(this.x*k,this.y*k);
		
		
		return cpy;
	}
	
	public Vector2 mult(double k)
	{
		this.x *= k;
		this.y *= k;
		
		
		return this;
	}
	
	public Vector2 divCpy(double k)
	{
		Vector2 cpy = new Vector2(this.x/k,this.y/k);
		
		
		return cpy;
	}
	
	public Vector2 div(double k)
	{
		this.x /= k;
		this.y /= k;
		
		
		return this;
	}
	
	public Vector2 addCpy(Vector2 other)
	{
		Vector2 cpy = new Vector2(this.x + other.getX(),this.y + other.getY());
		
		return cpy;
	}
	
	public Vector2 add(double tx, double ty)
	{
		this.setX(this.x + tx);
		this.setY(this.y + ty);
		
		
		return this;
	}
	
	public Vector2 add(Vector2 other)
	{
		this.setX(this.x + other.getX());
		this.setY(this.y + other.getY());
		
		
		return this;
	}
	
	public Vector2 subCpy(Vector2 other)
	{
		Vector2 cpy = new Vector2(this.x - other.getX(),this.y - other.getY());
		
		return cpy;
	}
	
	public Vector2 sub(Vector2 other)
	{
		this.setX(this.x - other.getX());
		this.setY(this.y - other.getY());
		
		
		return this;
	}
	
	public double dot(Vector2 other)
	{
		return this.x*other.getX() + this.y*other.getY();		
	}
	
	public double norm()
	{
		return Math.sqrt(this.dot(this));
	}
	
	
	public double radiansAngle(Vector2 other)
	{
		return Math.acos(this.dot(other)/(this.norm()*other.norm()));
	}
	
	public double degreesAngle(Vector2 other)
	{
		return Math.toDegrees(this.radiansAngle(other));
	}
	
	
	public double dist(Vector2 other)
	{
		Vector2 diff = this.subCpy(other);
		
		return Math.sqrt(diff.dot(diff));
	}
	
	public Vector2 getCpy()
	{
		return new Vector2(this.x,this.y);
	}
	
	public String toString()
	{
		return "(" + this.x + ", " + this.y + ")";
	}
	


}
