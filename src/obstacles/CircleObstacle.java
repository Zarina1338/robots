package obstacles;

import java.awt.Point;

import gui.Line;

public class CircleObstacle extends AbstractObstacle {


	public  CircleObstacle(Point p, int size) {
		type = "circle";
        position = p;
        anchorDistance = 20;

        Point topleftCorner = new Point();
        topleftCorner.setLocation(p.x - size/2, p.y - size/2);

        anchorPoints.add(new Point(p.x-size/2 - anchorDistance, 
                p.y-size/2 - anchorDistance));
        anchorPoints.add(new Point(p.x-size/2 - anchorDistance, 
               p.y+ size/2 + anchorDistance));
        anchorPoints.add(new Point(p.x+size/2 + anchorDistance, 
               p.y- size/2 - anchorDistance));
        anchorPoints.add(new Point(p.x+size/2 + anchorDistance, 
               p.y+ size/2 + anchorDistance));

        Point temp = anchorPoints.get(0);
        collisionPoints.add(new Point(temp.x + anchorDistance/2, temp.y + anchorDistance/2));

        temp = anchorPoints.get(1);
        collisionPoints.add(new Point(temp.x + anchorDistance/2, temp.y - anchorDistance/2));

        temp = anchorPoints.get(2);
        collisionPoints.add(new Point(temp.x - anchorDistance/2, temp.y + anchorDistance/2));

        temp = anchorPoints.get(3);
        collisionPoints.add(new Point(temp.x - anchorDistance/2, temp.y - anchorDistance/2));


        Line line = new Line(collisionPoints.get(0), collisionPoints.get(2)); //up
        collisionPairs.add(line);
        line = new Line(collisionPoints.get(1), collisionPoints.get(3)); // down
        collisionPairs.add(line);
        line = new Line(collisionPoints.get(0), collisionPoints.get(1)); // left
        collisionPairs.add(line);
        line = new Line(collisionPoints.get(2), collisionPoints.get(3)); // right
        collisionPairs.add(line);
	}

}
