import jxl.write.WriteException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by Adriaan on 2016/11/11.
 */
public class Console extends JFrame
{
    DefaultListModel model;
    JList list;
    JButton btnStop;
    JScrollPane listScroller;
    public Console()
    {
        JPanel pnl = new JPanel(new BorderLayout());
        list = new JList();
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(-1);
        model = new DefaultListModel();
        model.addElement("Snake Trainer");
        list = new JList(model);
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        btnStop = new JButton("Stop");
        pnl.add("North", btnStop);
        pnl.add("Center", listScroller);
        this.add(pnl);
        setResizable(true); //we don't want to let the user resize the window
        pack(); //take the dimensions of the frame we are loading
        setTitle("console"); //we should be the generation number in the title.
        this.setBounds(200, 200, 500, 700);
        this.setVisible(true);

        btnStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                SnakeGame.btnStoppedPushed = true;
                addToConsole("Game Stopping...");
            }
        });
    }

    public void addToConsole(String message)
    {
        model.addElement(message);
        list = new JList(model);
        //listScroller.scrollRectToVisible(new Rectangle(250, 80));
        JScrollBar vertical = listScroller.getVerticalScrollBar();
        vertical.setValue(vertical.getMaximum());
    }


}
