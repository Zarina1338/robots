package obstacles;

import java.awt.*;
import java.util.ArrayList;
import gui.Line;

public abstract class AbstractObstacle {
    String type;
    Point position = new Point();
    int size = 30;
    int anchorDistance= 30;
  
	ArrayList<Point> verticies = new ArrayList<>();
    ArrayList<Point> anchorPoints = new ArrayList<>();
    ArrayList<Point> collisionPoints = new ArrayList<>();
    ArrayList<Line> collisionPairs = new ArrayList<>();

    public String getType() { return type; }
    public ArrayList<Point> getVerticies() {return verticies; }
    public ArrayList<Point> getAnchors() { return anchorPoints; }
    public ArrayList<Line> getCollisionPairs() {return collisionPairs; }
    public Point getPosition() {
    	return position;
    }
    
    public int getSize() {return size;}
    
    public void setSize(int size) {
    	this.size=size;
    }
    public void setAnchors(ArrayList<Point> anchors) {
    	this.anchorPoints=anchors;
    }
    
    public int getAnchorDistance() {
  		return anchorDistance;
  	}
  	public void setAnchorDistance(int anchorDistance) {
  		this.anchorDistance = anchorDistance;
  	}
}
