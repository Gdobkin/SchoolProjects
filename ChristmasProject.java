import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class ChristmasProject {
    static Color[] colors = new Color[6];
    static {
        colors[0]= new Color(255, 254, 201);
        colors[1]= new Color(255, 154, 24);
        colors[2]= new Color(255,0,0);
        colors[3]= new Color(255, 17, 199);
        colors[4]= new Color(0, 255, 111);
        colors[5]= new Color(200, 23, 43);
    }


    class Light{
        Graphics g;
        int x;
        int y;
        int width;
        int height;
        int colorIndex = 0;

        public Light(Graphics g, int x, int y, int width, int height){
            this.g = g;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            draw();
        }

        public Light(Graphics g, int x, int y, int width, int height, int delay, long period, int index) {
            this.g = g;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.colorIndex = index;

            TimerTask task = new TimerTask() {
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

        public void draw(){
            g.setColor(colors[colorIndex]);
            colorIndex++;
            if(colorIndex==colors.length){
                colorIndex=0;
            }
            g.fillOval(x, y, width, height);

        }
    }

    public static void main(String[] args) {
        new ChristmasProject().run();
    }

    public void playMusic(){
        try {
            Clip clip = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(ChristmasProject.class.getResourceAsStream("H.wav"));
            clip.open(inputStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

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
    public void createStar(Graphics g, int centerX, int centerY){
        int[] x = {centerX,centerX-30,centerX+30};
        int[] y = {centerY-20,centerY+10,centerY+10};
        g.fillPolygon(x,y,3);

        int[] x2 = {centerX,centerX-30,centerX+30};
        int[] y2 = {centerY+20,centerY-10,centerY-10};
        g.fillPolygon(x2,y2,3);

    }

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