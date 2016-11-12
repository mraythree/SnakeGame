import javax.swing.*;
import java.awt.*;

/**
 * Created by Adriaan on 2016/11/11.
 */
public class Console extends JFrame
{
    DefaultListModel model;
    JList list;
    JScrollPane listScroller;
    public Console()
    {
        list = new JList();
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(-1);
        model = new DefaultListModel();
        model.addElement("Snake Trainer");
        list = new JList(model);
        listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        this.add(listScroller);
        setResizable(true); //we don't want to let the user resize the window
        pack(); //take the dimensions of the frame we are loading
        setTitle("console"); //we should be the generation number in the title.
        this.setBounds(200, 200, 500, 700);
        this.setVisible(true);
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
