package cz.muni.crocs.appletstore.ui;

import cz.muni.crocs.appletstore.iface.AbsolutePositionEditable;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * @author Jiří Horák
 * @version 1.0
 */
public class UserLogger extends JPanel implements MouseMotionListener, MouseListener {

    private JPanel bar = new JPanel();
    private JScrollPane jScrollPane = new JScrollPane();
    private JTextPane logPane;
    private Point lastPoint;

    private boolean drags;
    private Frame window;
    private AbsoluteHorizontalWindowFillLayout layout;
    private int layoutIdx;

    public UserLogger(Frame parrent, AbsoluteHorizontalWindowFillLayout layout, int layoutIdx, Point at) {
        super(new MigLayout());
        this.window = parrent;
        this.layoutIdx = layoutIdx;
        this.layout = layout;
        this.lastPoint = at;

        bar.addMouseListener(this);
        bar.addMouseMotionListener(this);
        bar.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
        //bar.setPreferredSize(new Dimension(getParent().getWidth(), 25));
        bar.add(new JLabel("Nazdar"));
        add(bar, "growx, wrap");
        buildLogger();
    }

    private void append(String text) {
        StyledDocument doc = logPane.getStyledDocument();

        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        StyleConstants.setForeground(keyWord, Color.RED);
        StyleConstants.setBackground(keyWord, Color.YELLOW);
        StyleConstants.setBold(keyWord, true);

        try {
            doc.insertString(0, "Start of text\n", null);
            doc.insertString(doc.getLength(), "\nEnd of text", keyWord);
        } catch (BadLocationException e) {
            System.out.println(e);
        }
    }

    private void buildLogger() {
        jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        logPane = new JTextPane();
        for (int i = 0; i < 5; i++) {
            append("");
        }
        jScrollPane.setViewportView(logPane);
        jScrollPane.getViewport().setPreferredSize(getPreferredSize());
        add(jScrollPane, "grow, wrap");
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(window.getWidth(), 92);
    }

    @Override
    public Dimension getPreferredSize() {
        System.out.println(window.getHeight() - lastPoint.y);
        return new Dimension(window.getWidth(), window.getHeight() - lastPoint.y);
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        if (!drags)
            return;

        double delta = lastPoint.y - e.getPoint().y;
        int newPosY = getY() - (int) delta;
        int maxHeight = window.getHeight() - 92;

        if (newPosY < 10) newPosY = 10;
        else if (newPosY > maxHeight) newPosY = maxHeight;
        jScrollPane.getViewport().setSize(new Dimension(window.getWidth(), window.getHeight() - newPosY));


        setLocation(getX(), newPosY);
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        drags = true;
        lastPoint = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        layout.updateAbsolutePositioned(layoutIdx, getX(), getY());
        lastPoint = e.getPoint();
        drags = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
