package info.bschambers.toothpick.dev;

import info.bschambers.toothpick.game.GameBase;
import info.bschambers.toothpick.game.SlideShowProgram;
import info.bschambers.toothpick.ui.ATMenu;
import info.bschambers.toothpick.ui.swing.SwingFrame;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;

public class App {

    private SwingFrame window;
    private GameBase base;

    public App() {
        window = new SwingFrame("Atomic Toothpick Test");
        base = new GameBase();
        base.setProgram(makeSlideShow());
        base.setUI(window);
        base.setMenu(makeMenu());
    }

    public void run() {
        window.setVisible(true);
        base.run();
    }

    private ATMenu makeMenu() {
        ATMenu root = new ATMenu("MAIN MENU");
        return root;
    }

    private SlideShowProgram makeSlideShow() {
        SlideShowProgram slides = new SlideShowProgram();
        addSlide(slides, "toothpick_slideshow01.png");
        addSlide(slides, "toothpick_slideshow02.png");
        addSlide(slides, "toothpick_slideshow03.png");
        return slides;
    }

    private void addSlide(SlideShowProgram slides, String filename) {
        try {
            URL url = ClassLoader.getSystemClassLoader().getResource(filename);
            Image img = ImageIO.read(url);
            slides.addImage(img);
        } catch (IOException e) {
            System.out.println("ERROR - COULDN'T LOAD IMAGE: " + filename);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR - COULDN'T LOAD IMAGE: " + filename);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

}
