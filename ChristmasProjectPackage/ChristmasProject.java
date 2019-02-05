package ChristmasProjectPackage;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;


public class ChristmasProject {
    static Color[] colors = new Color[6]; //an array of colors
    static { //populating the array of colors with 6 different colors
        colors[0]= new Color(255, 254, 201); //the numbers are rgb values and can be changed to make new colors
        colors[1]= new Color(255, 154, 24);
        colors[2]= new Color(255,0,0);
        colors[3]= new Color(255, 17, 199);
        colors[4]= new Color(0, 255, 111);
        colors[5]= new Color(200, 23, 43);
    }

    class Light{ //the light object
        //state fields of the light object
        Graphics g;
        int x;
        int y;
        int width;
        int height;
        int colorIndex = 0;

        /**
         * This constructor creates the basic Light object
         * @param g A graphics object
         * @param x the x coordinate of the light
         * @param y the y coordinate of the light
         * @param width the width of the light
         * @param height the height of the light
         */
        public Light(Graphics g, int x, int y, int width, int height){
            this.g = g;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            draw(); //once a Light is instantiated, it is drawn
        }

        /**
         * This is an overloaded constructor for the light object
         * this constructor makes a light that is associated with a timer
         * delay and period are needed to create a timer object
         * @param g A graphics object
         * @param x the x coordinate of the light
         * @param y the y coordinate of the light
         * @param width the width of the light
         * @param height the height of the light
         * @param delay the timer delay
         * @param period the timer period
         * @param index the index of the color array that will decided the light's color
         */
        public Light(Graphics g, int x, int y, int width, int height, int delay, long period, int index) {
            this.g = g;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.colorIndex = index;

            TimerTask task = new TimerTask() { //timer object
                @Override
                public void run() {
                    SwingUtilities.invokeLater(
                            new Runnable() {
                                @Override
                                public void run() {
                                    draw();
                                }
                            }
                    );
                }
            };
            new Timer().schedule(task, delay,period);
        }

        /**
         * Draws the lights and sets their colors
         */
        public void draw(){
            g.setColor(colors[colorIndex]);
            colorIndex++; //creates a rotation of colors through the color array
            if(colorIndex==colors.length){
                colorIndex=0;
            }
            g.fillOval(x, y, width, height); // creates the Light shape

        }
    }

    //The main method which just runs the class
    public static void main(String[] args) {
        new ChristmasProject().run();
    }

    /**
     * This method controls the music, the music is looped continuously
     */
    public void playMusic(){
        try {
            Clip clip = AudioSystem.getClip();//the clip object assists in playing the music
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(ChristmasProject.class.getResourceAsStream("H.wav"));
            //this is a way to have the music work from any computer that has the music file
            //the music is not local but a file that is part of the project
            clip.open(inputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * The main run method, it creates the panel's shape and colors as well as other basic decorations
     * Anything that will be a stagnant part of the scene can be added in this class
     * Calls all the creation methods as well
     */
    public  void run() {
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        playMusic();

                        DrawingPanel panel = new DrawingPanel(550, 300);
                        Graphics g = panel.getGraphics();
                        Color prettyBlue = new Color(79, 240, 255);
                        Color bluePurple = new Color(100, 102, 255);
                        Color paintFun = new Color(0, 214, 255);
                        panel.setBackground(Color.BLACK);

                        //candle sticks
                        g.setColor(prettyBlue);
                        drawSticks(g, 60, 80, 10, 60);

                        //base
                        g.setColor(bluePurple);
                        g.fillRect(40, 140, 450, 30);
                        g.fillRect(240, 160, 50, 120);

                        createLights(g, 50, 40, 30, 40);

                        g.setColor(Color.yellow);

                        createStar(g,90,220);
                        createStar(g,440,220);

                    }});
    }

    /**
     * Creates the star shape
     * This method is here because multiple stars will be made
     * @param g Graphics object
     * @param centerX x value of the center of star
     * @param centerY y value of the center of star
     */
    public void createStar(Graphics g, int centerX, int centerY){
        int[] x = {centerX,centerX-30,centerX+30};
        int[] y = {centerY-20,centerY+10,centerY+10};
        g.fillPolygon(x,y,3);

        int[] x2 = {centerX,centerX-30,centerX+30};
        int[] y2 = {centerY+20,centerY-10,centerY-10};
        g.fillPolygon(x2,y2,3);

    }

    /**
     * Creates the candle sticks, they are basic uniform rectangles
     * @param g graphics object
     * @param x x value from corner
     * @param y y value from corner
     * @param width width of stick
     * @param height height of stick
     */
    public void drawSticks(Graphics g, int x, int y, int width, int height){
        for(int i = 1; i < 10; i++){
            if(i ==5){
                g.fillRect(x,y-20,width,height+20);
            }
            else {
                g.fillRect(x, y, width, height);
            }
            x += 50;

        }

    }

    /**
     * Actually makes the light objects to put them in the panel
     * Creates all of the light objects at once
     * @param g graphics object
     * @param x x coordinate
     * @param y y coordinate
     * @param width width of light
     * @param height height of light
     */
    public void createLights(Graphics g, int x, int y, int width, int height){
        int count = 1;
        for(int i = 1; i < 10; i++){
            if(i==5){
                new Light(g,x,y-20,width,height);
            }
            else{
                new Light(g,x,y,width,height,700*count,300,i%colors.length);
                count++;
            }
            x+=50;

        }
    }
}