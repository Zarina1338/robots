package robot;

import log.Logger;
import obstacles.AbstractObstacle;


import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import gui.GameWindow;
import gui.Line;
import gui.MainApplicationFrame;

public class RobotMovement extends Observable {

    public RobotMovement(GameWindow gw) {
        gameWindow = gw;
    }

    private final GameWindow gameWindow;

    private double m_robotPositionX = 100;
    private double m_robotPositionY = 100;
    private double m_robotDirection = 0;

    private double m_targetPositionX = 150;
    private double m_targetPositionY = 100;

    private static final double maxVelocity = 0.1;
    private static final double maxAngularVelocity = 0.002;

     public CopyOnWriteArrayList<Point> path = new CopyOnWriteArrayList<>();
    public AtomicInteger pointsReached = new AtomicInteger(0);
    private static final double targetReachDist = 5;

    public boolean setTarget(int x, int y) {
        setM_targetPositionX(x);
        setM_targetPositionY(y);
        Point start = new Point((int)getM_robotPositionX(), (int)getM_robotPositionY());
        Point target = new Point(x, y);

        //path
        path.clear();
        if (getGameWindow().getVisualizer().obstacles.size() == 0)
            path.add(new Point(x, y));
        else
        {
            HashMap<Point, ArrayList<Point>> graph = new HashMap<>();
            ArrayList<Point> graphPoints = new ArrayList<>();
            ArrayList<Line> collisionLines = new ArrayList<>();



            Boolean additionalLogging = false;
            for (AbstractObstacle obs : getGameWindow().getVisualizer().obstacles) {
                graphPoints.add(start);
                graphPoints.addAll(obs.getAnchors());
                graphPoints.add(target);
                if (additionalLogging)
                    System.out.println("Points:" + graphPoints.size());

                collisionLines.addAll(obs.getCollisionPairs());
                if (collisionLines.isEmpty())
                    try {
                        throw new Exception("Obstacles are present, but have no collision");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                if (additionalLogging)
                    System.out.println("Collision: " + collisionLines.size());
            }



            for (Point p : graphPoints) {
                graph.put(p, new ArrayList<>());
            }


            for (int i = 0; i < graphPoints.size(); i++) {
                secondPoint:
                for (Point graphPoint : graphPoints) {
                    Point a = graphPoints.get(i);
                    if (!a.equals(graphPoint)) {
                        for (Line col : collisionLines) {
                            if (col.intersectsLine(new Line(a, graphPoint))) {
                                continue secondPoint;
                            }
                        }
                        if (!graph.get(a).contains(graphPoint)) {
                            ArrayList<Point> next = graph.get(a);
                            next.add(graphPoint);
                            graph.replace(a, next);
                        }
                    }
                }
            }

            if (graph.get(target).size() == 0)
                return false;

            if (additionalLogging) {
                int size = 0;
                for (Point point : graph.keySet())
                    size += graph.get(point).size();
                System.out.println("Graph size: " + size);

                for (Point each : graph.keySet()) {
                    System.out.print(each.toString().replace("java.awt.Point", ""));
                    System.out.print(": ");
                    for (Point each2 : graph.get(each)) {
                        System.out.print(each2.toString().replace("java.awt.Point", ""));
                    }
                    System.out.println();
                }
                System.out.println();
            }

            //BFS
            ArrayList<Point> queue = new ArrayList<>();
            ArrayList<Point> used = new ArrayList<>();
            HashMap<Point, Double> dist = new HashMap<>();
            HashMap<Point, Point> prev = new HashMap<>();
            queue.add(start);
            for(Point each : graphPoints) {
                dist.put(each, Double.POSITIVE_INFINITY);
            }
            dist.replace(start, 0.0);

            while (!queue.isEmpty()) {
                Point current = queue.get(0);
                used.add(current);
                ArrayList<Point> adjacents = graph.get(current);
                for(Point adjacent : graph.get(current)) {
                    if ((!used.contains(adjacent)) && (!queue.contains(adjacent)))
                        queue.add(adjacent);
                }
                for(Point adjacent : adjacents) {
                    double currentDist = dist.get(adjacent);
                    double newDist = dist.get(current) + distance(current.x, current.y, adjacent.x, adjacent.y);
                    if (newDist < currentDist) {
                        dist.replace(adjacent, newDist);
                        prev.put(adjacent, current);
                    }
                }
                queue.remove(0);
            }

            if(additionalLogging) {
                for(Point each : prev.keySet()) {
                    System.out.print(each + ": ");
                    System.out.println(prev.get(each));
                }
                System.out.println();
            }


            Point next = target;
            while (prev.containsKey(next)) {
                path.add(0, next);
                next = prev.get(next);
            }
            path.add(0, next);

            if (additionalLogging) {
                for (Point each : path) {
                    if (each == start)
                        System.out.print("Start: ");
                    System.out.println(each.toString().replace("java.awt.Point", ""));
                }
                System.out.println();
            }
        }
        updateTarget();
        return true;
    }

    private void updateTarget() {
        this.setM_targetPositionX(path.get(0).x);
        this.setM_targetPositionY(path.get(0).y);
    }

    private static double distance(double x1, double y1, double x2, double y2)
    {
        double diffX = x1 - x2;
        double diffY = y1 - y2;
        return Math.sqrt(diffX * diffX + diffY * diffY);
    }

    private static double angleTo(double fromX, double fromY, double toX, double toY)
    {
        return asNormalizedRadians(Math.atan2(toY - fromY, toX - fromX));
    }

    public void onModelUpdateEvent()
    {

        double distance = distance(getM_robotPositionX(), getM_robotPositionY(), getM_targetPositionX(), getM_targetPositionY());
        if (distance < targetReachDist) {
            if (path.size() > 0) {
                path.remove(0);
                if (path.size() > 0) {
                    updateTarget();
                } else {
                	getGameWindow().getVisualizer().setTargetPosition(this, randomPoint());
                    pointsReached.incrementAndGet();
                    Logger.debug("Цель достигнута.");
                }
            } else {
            	getGameWindow().getVisualizer().setTargetPosition(this, randomPoint());
                pointsReached.incrementAndGet();
                Logger.debug("Цель достигнута.");
            }

        } else {
            if (lookingAtTarget()) {
                moveRobot(getMaxVelocity(), 0);
            } else {
                rotateRobot();
            }
        }
    }

    public Point randomPoint() {
        double x = Math.random() * (getGameWindow().getWidth() - 100) + 50;
        double y = Math.random() * (getGameWindow().getHeight() - 100) + 50;
        Point result = new Point();
        result.setLocation(x, y);
        return result;
    }

    private void rotateRobot() {
        double angularVelocity;
        double angle = angleFromRobot();
        if (angle < Math.PI)
            angularVelocity = getMaxangularVelocity();
        else
            angularVelocity = -getMaxangularVelocity();
         moveRobot(0, angularVelocity);
    }

    private void moveRobot(double velocity, double angularVelocity)
    {
        velocity = applyLimits(velocity, 0, getMaxVelocity());
        angularVelocity = applyLimits(angularVelocity, -getMaxangularVelocity(), getMaxangularVelocity());

        int duration = MainApplicationFrame.globalTimeConst;
        double newX = getM_robotPositionX() + velocity / angularVelocity *
                (Math.sin(getM_robotDirection()  + angularVelocity * duration) -
                        Math.sin(getM_robotDirection()));
        if (!Double.isFinite(newX))
        {
            newX = getM_robotPositionX() + velocity * duration * Math.cos(getM_robotDirection());
        }
        double newY = getM_robotPositionY() - velocity / angularVelocity *
                (Math.cos(getM_robotDirection()  + angularVelocity * duration) -
                        Math.cos(getM_robotDirection()));
        if (!Double.isFinite(newY))
        {
            newY = getM_robotPositionY() + velocity * duration * Math.sin(getM_robotDirection());
        }
        setM_robotPositionX(newX);
        setM_robotPositionY(newY);
        setM_robotDirection(asNormalizedRadians(getM_robotDirection() + angularVelocity * duration));

        notifyObservers();
        setChanged();
    }

    private static double applyLimits(double value, double min, double max)
    {
        if (value < min)
            return min;
        if (value > max)
            return max;
        return value;
    }

    private double angleFromRobot() {
        double angleToTarget = angleTo(getM_robotPositionX(), getM_robotPositionY(), getM_targetPositionX(), getM_targetPositionY());
        return asNormalizedRadians(angleToTarget - getM_robotDirection()); //angle from robots perspective
    }

    private boolean lookingAtTarget()  { return rounded(angleFromRobot(), 1) == 0; }

    private static double asNormalizedRadians(double angle)
    {
        //convert any angle to [0, 2PI)
        while (angle < 0)
            angle += 2*Math.PI;
        while (angle >= 2*Math.PI)
            angle -= 2*Math.PI;
        return angle;
    }

    public static double rounded(double num, int accuracy) {
        num = Math.floor(num * Math.pow(10,accuracy));
        return num / Math.pow(10,accuracy);
    }

	public double getM_robotPositionX() {
		return m_robotPositionX;
	}

	public void setM_robotPositionX(double m_robotPositionX) {
		this.m_robotPositionX = m_robotPositionX;
	}

	public double getM_robotPositionY() {
		return m_robotPositionY;
	}

	public void setM_robotPositionY(double m_robotPositionY) {
		this.m_robotPositionY = m_robotPositionY;
	}

	public double getM_robotDirection() {
		return m_robotDirection;
	}

	public void setM_robotDirection(double m_robotDirection) {
		this.m_robotDirection = m_robotDirection;
	}

	public double getM_targetPositionX() {
		return m_targetPositionX;
	}

	public void setM_targetPositionX(double m_targetPositionX) {
		this.m_targetPositionX = m_targetPositionX;
	}

	public double getM_targetPositionY() {
		return m_targetPositionY;
	}

	public void setM_targetPositionY(double m_targetPositionY) {
		this.m_targetPositionY = m_targetPositionY;
	}

	public GameWindow getGameWindow() {
		return gameWindow;
	}

	public static double getMaxVelocity() {
		return maxVelocity;
	}

	public static double getMaxangularVelocity() {
		return maxAngularVelocity;
	}

}
