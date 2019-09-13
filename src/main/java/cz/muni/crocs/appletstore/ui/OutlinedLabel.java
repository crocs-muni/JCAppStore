//package cz.muni.crocs.appletstore.ui;
//
//import javax.swing.*;
//import java.awt.*;
//
///**
// * @author Jiří Horák
// * @version 1.0
// */
//public class OutlinedLabel extends JLabel {
//
//
//    public OutlinedLabel(String text) {
//        super(text);
//        setE
//    }
//
//    @Override
//    public void paint(Graphics g) {
//
//        String text = getText();
//        if ( text == null || text.length() == 0 ) {
//            super.paint(g);
//            return;
//        }
//
//        // 1 2 3
//        // 8 9 4
//        // 7 6 5
//
//        if ( isOpaque() )
//            super.paint(g);
//
//        forceTransparent = true;
//        isPaintingOutline = true;
//        g.translate(-1, -1); super.paint(g); // 1
//        g.translate( 1,  0); super.paint(g); // 2
//        g.translate( 1,  0); super.paint(g); // 3
//        g.translate( 0,  1); super.paint(g); // 4
//        g.translate( 0,  1); super.paint(g); // 5
//        g.translate(-1,  0); super.paint(g); // 6
//        g.translate(-1,  0); super.paint(g); // 7
//        g.translate( 0, -1); super.paint(g); // 8
//        g.translate( 1,  0); // 9
//        isPaintingOutline = false;
//
//        super.paint(g);
//        forceTransparent = false;
//
//    }
//
//
//}
