
    /*
Marty Stepp and Stuart Reges
Oct 24 2006

The DrawingPanel class provides a simple interface for drawing persistent
images using a Graphics object.  An internal BufferedImage object is used
to keep track of what has been drawn.  A client of the class simply
constructs a DrawingPanel of a particular size and then draws on it with
the Graphics object, setting the background color if they so choose.

To ensure that the image is always displayed, a timer calls repaint at
regular intervals.
*/

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;

    // A drawing surface for painting 2D graphics.
    public class DrawingPanel extends FileFilter
            implements ActionListener, MouseMotionListener {
        // class constants
        private static final String TITLE = "CSE 142 Drawing Panel";
        private static final int DELAY = 250;   // delay between repaints in millis
        private static final String SAVE_PROPERTY = "drawingpanel.save";
        private static final String DIFF_PROPERTY = "drawingpanel.diff";

        // shared variables
        private static boolean PRETTY = true;  // true to anti-alias

        // fields
        private int width, height;    // dimensions of window frame
        private JFrame frame;         // overall window frame
        private JPanel panel;         // overall drawing surface
        private BufferedImage image;  // remembers drawing commands
        private Graphics2D g2;        // graphics context for painting
        private JLabel statusBar;     // status bar showing mouse position
        private JFileChooser chooser; // file chooser to save files
        private long createTime;      // time at which DrawingPanel was constructed
        private Timer timer;          // animation timer

        // construct a drawing panel of given width and height enclosed in a window
        public DrawingPanel(int width, int height) {
            this.width = width;
            this.height = height;
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            statusBar = new JLabel(" ");
            statusBar.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            panel.setBackground(Color.WHITE);
            panel.setPreferredSize(new Dimension(width, height));
            panel.add(new JLabel(new ImageIcon(image)));

            // listen to mouse movement
            panel.addMouseMotionListener(this);

            g2 = (Graphics2D)image.getGraphics();
            g2.setColor(Color.BLACK);
            if (PRETTY) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }

            // main window frame
            frame = new JFrame(TITLE);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(panel);
            frame.getContentPane().add(statusBar, "South");

            // menu bar
            setupMenuBar();


            frame.pack();
            frame.setVisible(true);
            if (System.getProperty(SAVE_PROPERTY) != null) {
                frame.toBack();
            } else {
                frame.toFront();
            }

            // repaint timer so that the screen will update
            createTime = System.currentTimeMillis();
            timer = new Timer(DELAY, this);
            timer.start();
        }

        // method of FileFilter interface
        public boolean accept(File file) {
            return !file.isDirectory() &&
                    file.getName().toLowerCase().endsWith(".png");
        }

        // used for an internal timer that keeps repainting
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof javax.swing.Timer) {
                // redraw the screen at regular intervals to catch all paint operations
                panel.repaint();
                if (System.getProperty(DIFF_PROPERTY) != null &&
                        System.currentTimeMillis() > createTime + 4 * DELAY) {
                    String expected = System.getProperty(DIFF_PROPERTY);
                    try {
                        String actual = saveToTempFile();
                        DiffImage diff = new DiffImage(expected, actual);
                        diff.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    } catch (IOException ioe) {
                        System.err.println("Error diffing image: " + ioe);
                    }
                    timer.stop();
                } else if (System.getProperty(SAVE_PROPERTY) != null &&
                        System.currentTimeMillis() > createTime + 4 * DELAY) {
                    // auto-save-and-close if desired
                    try {
                        save(System.getProperty(SAVE_PROPERTY));
                    } catch (IOException ioe) {
                        System.err.println("Error saving image: " + ioe);
                    }
                    exit();
                }
            } else if (e.getActionCommand().equals("Exit")) {
                exit();
            } else if (e.getActionCommand().equals("Compare to File...")) {
                compareToFile();
            } else if (e.getActionCommand().equals("Save As...")) {
                saveAs();
            } else if (e.getActionCommand().equals("About...")) {
                JOptionPane.showMessageDialog(frame,
                        "DrawingPanel\nwritten by Marty Stepp and Stuart Reges");
            }
        }

        // closes the frame and exits the program
        public void exit() {
            frame.setVisible(false);
            frame.dispose();
            System.exit(0);
        }

        // method of FileFilter interface
        public String getDescription() {
            return "PNG images (*.png)";
        }

        // obtain the Graphics object to draw on the panel
        public Graphics2D getGraphics() {
            return g2;
        }

        // listens to mouse dragging
        public void mouseDragged(MouseEvent e) {}

        // listens to mouse movement
        public void mouseMoved(MouseEvent e) {
            statusBar.setText("(" + e.getX() + ", " + e.getY() + ")");
        }

        // take the current contents of the panel and write them to a file
        public void save(String filename) throws IOException {
            String extension = filename.substring(filename.lastIndexOf(".") + 1);

            // create second image so we get the background color
            BufferedImage image2 = new BufferedImage(width, height,
                    BufferedImage.TYPE_INT_RGB);
            Graphics g = image2.getGraphics();
            g.setColor(panel.getBackground());
            g.fillRect(0, 0, width, height);
            g.drawImage(image, 0, 0, panel);

            // write file
            ImageIO.write(image2, extension, new File(filename));
        }

        // set the background color of the drawing panel
        public void setBackground(Color c) {
            panel.setBackground(c);
        }

        // show or hide the drawing panel on the screen
        public void setVisible(boolean visible) {
            frame.setVisible(visible);
        }

        // makes the program pause for the given amount of time,
        // allowing for animation
        public void sleep(int millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {}
        }

        // moves window on top of other windows
        public void toFront() {
            frame.toFront();
        }

        // moves given jframe to center of screen
        private void center(JFrame frame) {
            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension screen = tk.getScreenSize();
            frame.setLocation((screen.width - frame.getWidth()) / 2,
                    (screen.height - frame.getHeight()) / 2);
        }

        // constructs and initializes JFileChooser object if necessary
        private void checkChooser() {
            if (chooser == null) {
                chooser = new JFileChooser(System.getProperty("user.dir"));
                chooser.setMultiSelectionEnabled(false);
                chooser.setFileFilter(this);
            }
        }

        // compares current DrawingPanel image to an image file on disk
        private void compareToFile() {
            // save current image to a temp file
            try {
                String tempFile = saveToTempFile();

                // use file chooser dialog to find image to compare against
                checkChooser();
                if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                // user chose a file; let's diff it
                new DiffImage(chooser.getSelectedFile().toString(), tempFile);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(frame,
                        "Unable to compare images: \n" + ioe);
            }
        }

        // saves DrawingPanel image to a temporary file and returns file's name
        private String saveToTempFile() throws IOException {
            File currentImageFile = File.createTempFile("current_image", ".png");
            save(currentImageFile.toString());
            return currentImageFile.toString();
        }

        // called when user presses "Save As" menu item
        private void saveAs() {
            // use file chooser dialog to get filename to save into
            checkChooser();
            if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File selectedFile = chooser.getSelectedFile();
            try {
                String filename = selectedFile.toString();
                if (!filename.toLowerCase().endsWith("png")) {
                    // Windows is dumb about extensions with file choosers
                    filename += ".png";
                }

                // confirm overwrite of file
                if (new File(filename).exists() && JOptionPane.showConfirmDialog(
                        frame, "File exists.  Overwrite?", "Overwrite?",
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }

                save(filename);  // save the file
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Unable to save image:\n" + ex);
            }
        }

        // initializes DrawingPanel's menu bar items
        private void setupMenuBar() {
            JMenuItem saveAs = new JMenuItem("Save As...", 'A');
            saveAs.addActionListener(this);
            saveAs.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));

            JMenuItem compare = new JMenuItem("Compare to File...", 'C');
            compare.addActionListener(this);
            compare.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));

            JMenuItem exit = new JMenuItem("Exit", 'x');
            exit.addActionListener(this);

            JMenuItem about = new JMenuItem("About...", 'A');
            about.addActionListener(this);

            JMenu file = new JMenu("File");
            file.setMnemonic('F');
            file.add(saveAs);
            file.addSeparator();
            file.add(compare);
            file.addSeparator();
            file.add(exit);

            JMenu help = new JMenu("Help");
            help.setMnemonic('H');
            help.add(about);

            JMenuBar bar = new JMenuBar();
            bar.add(file);
            bar.add(help);
            frame.setJMenuBar(bar);
        }


        // Reports the differences between two images.
        private class DiffImage extends JPanel implements ActionListener,
                ChangeListener {
            private static final long serialVersionUID = 0;

            private BufferedImage image1;
            private BufferedImage image2;
            private int numDiffPixels;
            private int opacity = 50;
            private String label1Text = "Expected";
            private String label2Text = "Actual";
            private boolean highlightDiffs = false;

            private Color highlightColor = new Color(255, 228, 0);
            private JLabel image1Label;
            private JLabel image2Label;
            private JLabel diffPixelsLabel;
            private JSlider slider;
            private JCheckBox box;
            private JMenuItem saveAsItem;
            private JMenuItem setImage1Item;
            private JMenuItem setImage2Item;
            private JFrame frame;
            private JButton colorButton;

            public DiffImage() {}

            public DiffImage(String file1, String file2) throws IOException {
                setImage1(file1);
                setImage2(file2);
                display();
            }

            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source == box) {
                    highlightDiffs = box.isSelected();
                    repaint();
                } else if (source == colorButton) {
                    Color color = JColorChooser.showDialog(frame,
                            "Choose highlight color", highlightColor);
                    if (color != null) {
                        highlightColor = color;
                        colorButton.setBackground(color);
                        colorButton.setForeground(color);
                        repaint();
                    }
                } else if (source == saveAsItem) {
                    saveAs();
                } else if (source == setImage1Item) {
                    setImage1();
                } else if (source == setImage2Item) {
                    setImage2();
                }
            }

            // Counts number of pixels that differ between the two images.
            public void countDiffPixels() {
                if (image1 == null || image2 == null) {
                    return;
                }

                int w1 = image1.getWidth();
                int h1 = image1.getHeight();
                int w2 = image2.getWidth();
                int h2 = image2.getHeight();
                int wmax = Math.max(w1, w2);
                int hmax = Math.max(h1, h2);

                // check each pair of pixels
                numDiffPixels = 0;
                for (int y = 0; y < hmax; y++) {
                    for (int x = 0; x < wmax; x++) {
                        int pixel1 = (x < w1 && y < h1) ? image1.getRGB(x, y) : 0;
                        int pixel2 = (x < w2 && y < h2) ? image2.getRGB(x, y) : 0;
                        if (pixel1 != pixel2) {
                            numDiffPixels++;
                        }
                    }
                }
            }

            // Displays differences between the two given image files.
            public void diff(File input1, File input2) throws IOException {
                diff(ImageIO.read(input1), ImageIO.read(input2));
                label1Text = input1.getName();
                label2Text = input2.getName();
            }

            // Displays differences between the two given images.
            public void diff(InputStream input1, InputStream input2) throws IOException {
                diff(ImageIO.read(input1), ImageIO.read(input2));
            }

            // Displays differences between the two given images.
            public void diff(BufferedImage image1, BufferedImage image2) {
                if (image1 == null || image2 == null) {
                    throw new NullPointerException("Null images.\n\t" +
                            "image1: " + image1 + "\n\t" +
                            "image2: " + image2);
                }

                setImage1(image1);
                setImage2(image2);
                countDiffPixels();
            }

            // initializes diffimage panel
            public void display() {
                countDiffPixels();

                setupComponents();
                setupEvents();
                setupLayout();

                frame.pack();
                center(frame);

                frame.setVisible(true);
                frame.toFront();
            }

            // draws the given image onto the given graphics context
            public void drawImageFull(Graphics2D g2, BufferedImage image) {
                int iw = image.getWidth();
                int ih = image.getHeight();
                int w = getWidth();
                int h = getHeight();
                int dw = w - iw;
                int dh = h - ih;

                if (dw > 0) {
                    g2.fillRect(iw, 0, dw, ih);
                }
                if (dh > 0) {
                    g2.fillRect(0, ih, iw, dh);
                }
                if (dw > 0 && dh > 0) {
                    g2.fillRect(iw, ih, dw, dh);
                }
                g2.drawImage(image, 0, 0, this);
            }

            // paints the DiffImage panel
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                // draw the expected output (image 1)
                if (image1 != null) {
                    drawImageFull(g2, image1);
                }

                // draw the actual output (image 2)
                if (image2 != null) {
                    Composite oldComposite = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, ((float) opacity) / 100));
                    drawImageFull(g2, image2);
                    g2.setComposite(oldComposite);
                }
                g2.setColor(Color.BLACK);

                // draw the highlighted diffs (if so desired)
                if (highlightDiffs && image1 != null && image2 != null) {
                    int w1 = image1.getWidth();
                    int h1 = image1.getHeight();
                    int w2 = image2.getWidth();
                    int h2 = image2.getHeight();

                    int wmax = Math.max(w1, w2);
                    int hmax = Math.max(h1, h2);

                    // check each pair of pixels
                    g2.setColor(highlightColor);
                    for (int y = 0; y < hmax; y++) {
                        for (int x = 0; x < wmax; x++) {
                            int pixel1 = (x < w1 && y < h1) ? image1.getRGB(x, y) : 0;
                            int pixel2 = (x < w2 && y < h2) ? image2.getRGB(x, y) : 0;
                            if (pixel1 != pixel2) {
                                g2.fillRect(x, y, 1, 1);
                            }
                        }
                    }
                }
            }

            public void save(File file) throws IOException {
                // String extension = filename.substring(filename.lastIndexOf(".") + 1);
                // ImageIO.write(diffImage, extension, new File(filename));
                String filename = file.getName();
                String extension = filename.substring(filename.lastIndexOf(".") + 1);
                BufferedImage img = new BufferedImage(getPreferredSize().width, getPreferredSize().height, BufferedImage.TYPE_INT_ARGB);
                img.getGraphics().setColor(getBackground());
                img.getGraphics().fillRect(0, 0, img.getWidth(), img.getHeight());
                paintComponent(img.getGraphics());
                ImageIO.write(img, extension, file);
            }

            public void save(String filename) throws IOException {
                save(new File(filename));
            }

            // Called when "Save As" menu item is clicked
            public void saveAs() {
                checkChooser();
                if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File selectedFile = chooser.getSelectedFile();
                try {
                    save(selectedFile.toString());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Unable to save image:\n" + ex);
                }
            }

            // called when "Set Image 1" menu item is clicked
            public void setImage1() {
                checkChooser();
                if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File selectedFile = chooser.getSelectedFile();
                try {
                    setImage1(selectedFile.toString());
                    countDiffPixels();
                    diffPixelsLabel.setText("(" + numDiffPixels + " pixels differ)");
                    image1Label.setText(selectedFile.getName());
                    frame.pack();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Unable to set image 1:\n" + ex);
                }
            }

            // sets image 1 to be the given image
            public void setImage1(BufferedImage image) {
                if (image == null) {
                    throw new NullPointerException();
                }

                image1 = image;
                setPreferredSize(new Dimension(
                        Math.max(getPreferredSize().width, image.getWidth()),
                        Math.max(getPreferredSize().height, image.getHeight()))
                );
                if (frame != null) {
                    frame.pack();
                }
                repaint();
            }

            // loads image 1 from the given filename
            public void setImage1(String filename) throws IOException {
                setImage1(ImageIO.read(new File(filename)));
            }

            // called when "Set Image 2" menu item is clicked
            public void setImage2() {
                checkChooser();
                if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File selectedFile = chooser.getSelectedFile();
                try {
                    setImage2(selectedFile.toString());
                    countDiffPixels();
                    diffPixelsLabel.setText("(" + numDiffPixels + " pixels differ)");
                    image2Label.setText(selectedFile.getName());
                    frame.pack();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Unable to set image 2:\n" + ex);
                }
            }

            // sets image 2 to be the given image
            public void setImage2(BufferedImage image) {
                if (image == null) {
                    throw new NullPointerException();
                }

                image2 = image;
                setPreferredSize(new Dimension(
                        Math.max(getPreferredSize().width, image.getWidth()),
                        Math.max(getPreferredSize().height, image.getHeight()))
                );
                if (frame != null) {
                    frame.pack();
                }
                repaint();
            }

            // loads image 2 from the given filename
            public void setImage2(String filename) throws IOException {
                setImage2(ImageIO.read(new File(filename)));
            }

            private void setupComponents() {
                frame = new JFrame("DiffImage");
                frame.setResizable(false);
                // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                slider = new JSlider();
                slider.setPaintLabels(false);
                slider.setPaintTicks(true);
                slider.setSnapToTicks(true);
                slider.setMajorTickSpacing(25);
                slider.setMinorTickSpacing(5);

                box = new JCheckBox("Highlight diffs in color: ", highlightDiffs);

                colorButton = new JButton();
                colorButton.setBackground(highlightColor);
                colorButton.setForeground(highlightColor);
                colorButton.setPreferredSize(new Dimension(24, 24));

                diffPixelsLabel = new JLabel("(" + numDiffPixels + " pixels differ)");
                image1Label = new JLabel(label1Text);
                image2Label = new JLabel(label2Text);

                setupMenuBar();
            }

            // initializes layout of components
            private void setupLayout() {
                JPanel southPanel1 = new JPanel();
                southPanel1.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                southPanel1.add(image1Label);
                southPanel1.add(slider);
                southPanel1.add(image2Label);
                southPanel1.add(Box.createHorizontalStrut(20));

                JPanel southPanel2 = new JPanel();
                southPanel2.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
                southPanel2.add(diffPixelsLabel);
                southPanel2.add(Box.createHorizontalStrut(20));
                southPanel2.add(box);
                southPanel2.add(colorButton);

                Container southPanel = Box.createVerticalBox();
                southPanel.add(southPanel1);
                southPanel.add(southPanel2);

                frame.add(this, BorderLayout.CENTER);
                frame.add(southPanel, BorderLayout.SOUTH);
            }

            // initializes main menu bar
            private void setupMenuBar() {
                saveAsItem = new JMenuItem("Save As...", 'A');
                saveAsItem.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
                setImage1Item = new JMenuItem("Set Image 1...", '1');
                setImage1Item.setAccelerator(KeyStroke.getKeyStroke("ctrl 1"));
                setImage2Item = new JMenuItem("Set Image 2...", '2');
                setImage2Item.setAccelerator(KeyStroke.getKeyStroke("ctrl 2"));

                JMenu file = new JMenu("File");
                file.setMnemonic('F');
                file.add(setImage1Item);
                file.add(setImage2Item);
                file.addSeparator();
                file.add(saveAsItem);

                JMenuBar bar = new JMenuBar();
                bar.add(file);
                frame.setJMenuBar(bar);
            }

            // method of ChangeListener interface
            public void stateChanged(ChangeEvent e) {
                opacity = slider.getValue();
                repaint();
            }

            // adds event listeners to various components
            private void setupEvents() {
                slider.addChangeListener(this);
                box.addActionListener(this);
                colorButton.addActionListener(this);
                saveAsItem.addActionListener(this);
                this.setImage1Item.addActionListener(this);
                this.setImage2Item.addActionListener(this);
            }
        }
    }


