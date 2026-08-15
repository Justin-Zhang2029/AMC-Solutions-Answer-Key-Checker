import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.util.List;
import java.util.ArrayList;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.Word;
import net.sourceforge.tess4j.ITessAPI.TessPageIteratorLevel;

public class Main {
    public static void main(String[] args) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG & GIF Images", "jpg", "jpeg", "png", "gif", "docx");
        chooser.setFileFilter(filter);
        boolean fileSelected = false;
        
        while (!fileSelected) {
            int returnVal = chooser.showOpenDialog(null);

            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = chooser.getSelectedFile();

                try {
                    BufferedImage image = ImageIO.read(selectedFile);

                    if (image != null) {
                        fileSelected = true;
                        System.out.println("Successful");

                        Graphics2D g2d =  image.createGraphics();

                        g2d.setStroke(new BasicStroke(1));
                        
                        Tesseract tesseract = new Tesseract();
                        tesseract.setDatapath("src/main/resources/tesseract/");
                        tesseract.setLanguage("equ+eng");

                        List<Word> words = tesseract.getWords(image, TessPageIteratorLevel.RIL_WORD);

                        Rectangle solutionBox = null;
                        ArrayList<String> solutions = new ArrayList<>();

                        for (Word word : words) {

                            String text = word.getText().trim().toLowerCase();

                            if (text.contains("solution:")) {
                                solutionBox = word.getBoundingBox();
                                g2d.setColor(Color.RED);
                                g2d.drawRect(solutionBox.x, solutionBox.y, solutionBox.width, solutionBox.height);


                                int scanXMiddle = (int)(solutionBox.y + (solutionBox.height * 0.5)   );

                                solutionBox.x = (int)(solutionBox.x + (solutionBox.width * 1.05));
                                solutionBox.y = (int)(solutionBox.y - (solutionBox.height * 0.4));
                                
                                boolean endX = true;
                                int counterX = 0;
                                int newX = solutionBox.x;

                                while (endX) {
                                    
                                    if (image.getRGB(newX, scanXMiddle) == -1) {
                                        if (counterX < 8) {
                                            newX++;
                                            counterX++;
                                        } else {
                                            endX = false;
                                            solutionBox.width = newX - solutionBox.x;
                                        }   
                                    } else {
                                        newX++;
                                        counterX = 0;
                                    }
                                }

                                boolean endY = true; 
                                int counterY = 0;
                                int newY = solutionBox.y;

                                while (endY) {
                                    if (image.getRGB((solutionBox.x + (solutionBox.width / 2)), newY) == -1) {
                                        if (counterY < 9) {
                                            newY++;
                                            counterY++;
                                        } else {
                                            endY = false;
                                            solutionBox.height = newY - solutionBox.y;
                                        }
                                    } else {
                                        newY++;
                                        counterY = 0;
                                    }
                                }
                                

                                //Draw
                                g2d.setColor(Color.BLUE);
                                g2d.drawRect(solutionBox.x, solutionBox.y, solutionBox.width, solutionBox.height);

                                String actualAnswer = tesseract.doOCR(image, solutionBox).trim();
                                if (!actualAnswer.isEmpty() && (actualAnswer.charAt(0)) == ':') {
                                    actualAnswer = actualAnswer.substring(1).trim();
                                    solutions.add(actualAnswer);
                                } else {
                                    solutions.add(actualAnswer);
                                }
                                
                            }
                        }

                        g2d.dispose(); // Cleans up system memory resources

                        File outputFile = new File(selectedFile.getParent(), "debug_" + selectedFile.getName());
                        ImageIO.write(image, "png", outputFile);
                        System.out.println("Debug image saved to: " + outputFile.getAbsolutePath());

                        for (String s : solutions) {
                            System.out.println(s);
                        }

                    } else {
                        System.out.println("The file is not a valid image format");
                    }
                    
                } catch (Exception e) {
                    System.out.println("Error reading file" + e.getMessage());
                }
            } else {
                System.out.println("User canceled selection window");
                break;
            }
        }
    }
}


