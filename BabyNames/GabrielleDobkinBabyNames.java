import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class GabrielleDobkinBabyNames {
    public static final File FILE = new File("names.txt");
    public static final int YEAR = 1900;
    public static final int DECADES_INPUT = 11;
    public static final int DECADE_WIDTH = 50;
    public static void main(String[] args) throws FileNotFoundException {
        intro();
        String name = getName();
        //scanner to look through the file
        Scanner input = new Scanner(FILE);
        if(!exists(input,name)){//ends program if the name doesn't exist
            System.out.println("\""+name+"\" not found");
            return;
        }
        popRank(input,name);
    }
    public static void intro(){
        System.out.println("This program graphs the popularity of a name" +
                "\nIn Social Security baby name statistics\n" +
                "recorded since the year 1900");
    }
    public static String getName(){
        //scanner to get the users input
        Scanner userInput = new Scanner(System.in);
        System.out.print("Type a name: ");
        return userInput.next();
    }
    public static boolean exists(Scanner input, String name){//checks if name exists
        while(input.hasNext()){
            if(input.hasNextInt()){
                input.next();
                continue;
            }
            String testName = input.next();
            if(name.toLowerCase().equals(testName.toLowerCase())){
                return true;
            }
        }
        return false;
    }
    public static void popRank(Scanner input, String name){
        /*
        by the time that the program reaches here
        it is already at the line of the name from previously
        and it has all the numbers ready to go all you have to do is present them
        */
        String willBePanelled = "";
        System.out.println("Popularity ranking of the name \""+name.substring(0,1).toUpperCase()+name.substring(1).toLowerCase()+"\"");
        for (int i = 1; i<=DECADES_INPUT;i++){
            int stat = input.nextInt();
            willBePanelled += stat + " ";
            System.out.println(YEAR+(10*(i-1)) + ": " + stat);
        }
        drawingPanel(willBePanelled, name);
    }
    public static void drawingPanel (String data, String name){//this is where the panel is drawn and the data put in
        DrawingPanel panel = new DrawingPanel (550,560);
        Graphics g = panel.getGraphics();
        g.setColor(Color.LIGHT_GRAY);
        for(int i = 1; i<=DECADES_INPUT; i++){ //horizontal
            g.drawLine(0,30+(DECADE_WIDTH*(i-1)),550,30+(DECADE_WIDTH*(i-1)));
        }
        for(int i = 1; i<=DECADES_INPUT+1; i++){//vertical
            g.drawLine(0+DECADE_WIDTH*(i-1),30,0+DECADE_WIDTH*(i-1),530);
        }
        g.setColor(Color.yellow);
        g.fillRect(0,0,550,30);
        g.fillRect(0,530,550,30);
        //everything above is generic, and below is for the individual's data
        g.setColor(Color.black);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        for(int i = 1; i<=11; i++) {
            g.drawString(YEAR + 10 * (i - 1) + "", 0 + DECADE_WIDTH * (i - 1), 546);
        }
        g.drawString("Ranking of the name \""+name.substring(0,1).toUpperCase()+name.substring(1).toLowerCase()
                +"\":",0,15);
        g.setColor(Color.red);
        Scanner dataScan = new Scanner(data);
        int plotPoint = dataScan.nextInt();
        for(int i = 1; i<DECADES_INPUT; i++) {
            g.setColor(Color.black);
            if (plotPoint == 1001||plotPoint==0) {
                g.drawString("0", 0 + DECADE_WIDTH * (i - 1), 530);
            } else{
                g.drawString(plotPoint + "", 0 + DECADE_WIDTH * (i - 1), ((plotPoint - 1) / 2) + 30);
            }
            g.setColor(Color.red);
            int nextPlotPoint = dataScan.nextInt();
            if(plotPoint==0){ //this way to can work for the math that I use to figure out the coordinates
                plotPoint=1001;
            }
            if(nextPlotPoint==0){
                nextPlotPoint=1001;
            }
            g.drawLine(0+DECADE_WIDTH*(i-1),((plotPoint-1)/2)+30,DECADE_WIDTH+DECADE_WIDTH*(i-1),((nextPlotPoint-1)/2)+30);
            plotPoint=nextPlotPoint;
        }
        g.setColor(Color.black);
        if (plotPoint == 1001||plotPoint==0) {
            g.drawString("0", 0 + 50 * (DECADES_INPUT - 1), 530);
        } else{
            g.drawString(plotPoint + "", 0 + 50 * (DECADES_INPUT - 1), ((plotPoint - 1) / 2) + 30);
        }
    }
}