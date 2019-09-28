//import cz.muni.crocs.appletstore.Config;
//
//import java.awt.image.BufferedImage;
//import java.io.*;
//
///**
// * @author Jiří Horák
// * @version 1.0
// */
//public class Main {
//
//    public static void main(String[] args) throws IOException {
//        try (BufferedReader reader = new BufferedReader(new FileReader("C:/Users/Jirka/Desktop/well_known_rids.csv"));
//        BufferedWriter writer = new BufferedWriter(new FileWriter("C:/Users/Jirka/Desktop/a.txt"))) {
//            String line = reader.readLine();
//            while (line != null) {
//                String[] data = line.split(",");
//                if (!data[1].trim().isEmpty()) {
//                    writer.write("[" + data[0].trim() + "]\n");
//                    writer.write("author = " + data[1].trim() + "\n");
//                    writer.write("\n");
//                }
//                line = reader.readLine();
//            }
//        }
//    }
//}
