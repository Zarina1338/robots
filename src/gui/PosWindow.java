package gui;

import java.awt.BorderLayout;
import java.awt.TextArea;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import robot.RobotMovement;


public class PosWindow extends JInternalFrame implements Observer {
    private TextArea m_pos;
    private RobotMovement robotMovement;

    PosWindow(RobotMovement rm) {
        super("Положение робота", true, true, true, true);
        robotMovement = rm;
        m_pos = new TextArea("");
        m_pos.setSize(200, 200);
        rm.addObserver(this);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(m_pos, BorderLayout.CENTER);
        getContentPane().add(panel);
        updateInfo();
    }


    @Override
    public void update(Observable o, Object arg) {
        updateInfo();
    }


    private void updateInfo() {
        String content = "X: " + RobotMovement.rounded(robotMovement.getM_robotPositionX(), 2) + "\n" +
                "Y: " + RobotMovement.rounded(robotMovement.getM_robotPositionY(), 2) + "\n" +
                "Угол: " + (int) (robotMovement.getM_robotDirection() / Math.PI * 180) + "° \n" +
                "X цели: " + robotMovement.getM_targetPositionX() + "\n" +
                "Y цели: " + robotMovement.getM_targetPositionY() + "\n" +
                "Целей достигнуто: " + robotMovement.pointsReached +"\n";
        try {
            m_pos.setText(content);
        }
        catch (Exception e) {
            System.out.print(e.toString()); // It looks like very small window sizes give lots of errors.
        }
    }


}

