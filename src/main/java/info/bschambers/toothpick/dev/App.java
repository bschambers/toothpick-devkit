package info.bschambers.toothpick.dev;

import info.bschambers.toothpick.actor.*;
import info.bschambers.toothpick.game.*;
import info.bschambers.toothpick.geom.Line;
import info.bschambers.toothpick.geom.Pt;
import info.bschambers.toothpick.ui.TPMenu;
import info.bschambers.toothpick.ui.TPMenuItemSimple;
import info.bschambers.toothpick.ui.TPMenuItemIncr;
import info.bschambers.toothpick.ui.swing.SwingUI;
import java.awt.Image;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;
import info.bschambers.toothpick.actor.PlayerController;

public class App {

    private SwingUI window;
    private GameBase base;
    private SlideShowProgram introSlides;

    public App() {
        window = new SwingUI("Atomic Toothpick Test");
        base = new GameBase();
        introSlides = makeSlideShow();
        base.setProgram(introSlides);
        base.setUI(window);
        base.setMenu(makeMenu());
    }

    public void run() {
        window.setVisible(true);
        base.run();
    }

    private TPMenu makeMenu() {
        TPMenu root = new TPMenu("MAIN MENU");
        root.add(new TPMenuItemSimple("new game",
                                      () -> System.out.println("start game")));
        root.add(new TPMenuItemSimple("info",
                                      () -> System.out.println("info")));
        root.add(makeMenuPresetProgram());
        root.add(new TPMenuItemSimple("load program from file",
                                      () -> System.out.println("load game")));
        root.add(makeGlobalMenu());
        root.add(new TPMenuItemSimple("EXIT", () -> window.exit()));
        return root;
    }

    private TPMenu makeMenuPresetProgram() {
        TPMenu m = new TPMenu("preset programs");
        m.add(makeProgramMenu(introSlides));
        m.add(makeProgramMenu(makeProgStaticToothpick()));
        m.add(makeProgramMenu(makeProgSimpleToothpickGame()));
        m.add(new TPMenuItemSimple("toothpick mixed enemies game",
                                   () -> System.out.println("mixed toothpicks")));
        m.add(new TPMenuItemSimple("scrolling map game",
                                   () -> System.out.println("scrolling map")));
        m.add(new TPMenuItemSimple("boss battle game",
                                   () -> System.out.println("boss battle")));
        m.add(new TPMenuItemSimple("powerups game",
                                   () -> System.out.println("powerups")));
        m.add(new TPMenuItemSimple("levels game",
                                   () -> System.out.println("levels")));
        m.add(new TPMenuItemSimple("ribbon game",
                                   () -> System.out.println("ribbon")));
        m.add(new TPMenuItemSimple("asteroids game",
                                   () -> System.out.println("asteroid")));
        m.add(new TPMenuItemSimple("gravity orbit game",
                                   () -> System.out.println("orbit")));
        return m;
    }

    private TPMenu makeProgramMenu(GameProgram prog) {
        TPMenu m = new TPMenu(prog.getTitle());
        m.add(new TPMenuItemSimple("run", () -> {
                    base.setProgram(prog);
        }));
        m.add(makePlayerMenu(prog));
        m.add(new TPMenuItemSimple("open in editor",
                                   () -> System.out.println("open program in editor")));
        return m;
    }

    private TPMenu makePlayerMenu(GameProgram prog) {
        TPMenu m = new TPMenu("Player Options: (" + prog.getTitle() + ")");
        m.add(new TPMenuItemSimple("controller type", () -> System.out.println("...")));
        m.add(new TPMenuItemSimple("controller keys", () -> System.out.println("...")));
        m.add(new TPMenuItemSimple("thrust", () -> System.out.println("...")));
        m.add(new TPMenuItemSimple("type", () -> System.out.println("...")));
        m.add(new TPMenuItemSimple("presets players", () -> System.out.println("...")));
        return m;
    }

    private SlideShowProgram makeSlideShow() {
        SlideShowProgram slides = new SlideShowProgram("Intro Slides");
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

    private ToothpickProgram makeProgStaticToothpick() {
        ToothpickProgram tpp = new ToothpickProgram("Static Toothpicks");
        tpp.addActor(makeLineActor(45, 20, 200, 30));
        tpp.addActor(makeLineActor(200, 350, 400, 250));
        tpp.addActor(makeLineActor(500, 175, 550, 400));
        return tpp;
    }

    private ToothpickProgram makeProgSimpleToothpickGame() {
        ToothpickProgram tpp = new ToothpickProgram("Simple Toothpicks Game");
        tpp.setBGColor(Color.BLUE);
        // drones
        tpp.addActor(makeLineActor(10, 200, 20, 300));
        tpp.addActor(makeLineActor(30, 310, 50, 250));
        tpp.addActor(makeLineActor(300, 300, 550, 450));
        // player
        PlayerController pc = new EightWayController();
        Actor p = makeLineActor(50, 50, 150, 150);
        p.setController(pc);
        tpp.addActor(p);
        tpp.setPlayer(pc);
        return tpp;
    }

    private Actor makeLineActor(double x1, double y1, double x2, double y2) {
        Pt start = new Pt(x1, y1);
        Pt end = new Pt(x2, y2);
        LinesForm form = new LinesForm(new TPLine(new Line(start, end)));
        ActorController ctrl = new ActorController();
        return new Actor(form, ctrl);
    }

    private TPMenu makeGlobalMenu() {
        TPMenu m = new TPMenu("Global Options");
        m.add(new TPMenuItemIncr("goal fps", () -> base.getFpsGoal() + "",
                                 () -> base.setFpsGoal(base.getFpsGoal() - 1),
                                 () -> base.setFpsGoal(base.getFpsGoal() + 1)));
        return m;
    }

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

}
