package info.bschambers.toothpick.dev;

import info.bschambers.toothpick.actor.*;
import info.bschambers.toothpick.game.*;
import info.bschambers.toothpick.geom.*;
import info.bschambers.toothpick.ui.*;
import info.bschambers.toothpick.ui.swing.SwingUI;
import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;

public class App {

    private SwingUI window;
    private TPBase base;
    private SlideShowProgram introSlides;

    public App() {
        window = new SwingUI("Atomic Toothpick Test");
        base = new TPBase();
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

    private TPMenu makeProgramMenu(TPProgram prog) {
        TPMenu m = new TPMenu(prog.getTitle());
        m.add(new TPMenuItemSimple("run", () -> {
                    base.setProgram(prog);
        }));
        m.add(makePlayerMenu(prog));
        m.add(new TPMenuItemSimple("collision detection type",
                                   () -> System.out.println("collision detection")));
        m.add(new TPMenuItemSimple("open in editor",
                                   () -> System.out.println("open program in editor")));
        return m;
    }

    private TPMenu makePlayerMenu(TPProgram prog) {
        TPMenu m = new TPMenu("Player Options: (" + prog.getTitle() + ")");
        m.add(makePlayerControllerMenu(prog));
        m.add(new TPMenuItemSimple("controller keys", () -> System.out.println("...")));
        m.add(new TPMenuItemSimple("thrust", () -> System.out.println("...")));
        m.add(new TPMenuItemSimple("type", () -> System.out.println("...")));
        m.add(new TPMenuItemSimple("presets players", () -> System.out.println("...")));
        return m;
    }

    private TPMenu makePlayerControllerMenu(TPProgram prog) {
        TPMenu m = new TPMenu(() -> "Change Player-Controller (current = "
                              + prog.getPlayerController().getClass().getSimpleName() + ")");
        m.add(makeControllerSwitcherItem(prog, new EightWayController()));
        m.add(makeControllerSwitcherItem(prog, new EightWayInertiaController()));
        m.add(makeControllerSwitcherItem(prog, new ThrustInertiaController()));
        return m;
    }

    private TPMenuItem makeControllerSwitcherItem(TPProgram prog, PlayerController ctrl) {

        return new TPMenuItemSimple(ctrl.getClass().getSimpleName(),
                                    () -> prog.changePlayerController(ctrl));

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
        // drones
        tpp.addActor(makeLineActor(45, 20, 200, 30));
        tpp.addActor(makeLineActor(200, 350, 400, 250));
        tpp.addActor(makeLineActor(500, 175, 550, 400));
        // player
        TPActor player = makePlayerActor(150, 150, 250, 150);
        tpp.setPlayer(player);
        return tpp;
    }

    private ToothpickProgram makeProgSimpleToothpickGame() {
        ToothpickProgram tpp = new ToothpickProgram("Simple Toothpicks Game");
        tpp.setBGColor(Color.BLUE);
        // drones
        TPActor a = makeLineActor(10, 200, 20, 300);
        a.setController(new SimpleController(new Pt(3, 1)));
        tpp.addActor(a);
        TPActor b = makeLineActor(30, 310, 50, 250);
        b.setController(new SimpleController(new Pt(2, -1)));
        tpp.addActor(b);
        TPActor c = makeLineActor(300, 300, 550, 450);
        c.setController(new SimpleController(new Pt(-1, -2)));
        tpp.addActor(c);
        // player
        TPActor player = makePlayerActor(150, 150, 250, 150);
        tpp.setPlayer(player);
        return tpp;
    }

    private TPActor makeLineActor(double x1, double y1, double x2, double y2) {
        Pt start = new Pt(x1, y1);
        Pt end = new Pt(x2, y2);
        Pt pos = Geom.midPoint(start, end);
        start = start.add(pos.invert());
        end = end.add(pos.invert());
        LinesForm form = new LinesForm(new TPLine(new Line(start, end)));
        TPController ctrl = new TPController();
        ctrl.setPos(pos);
        return new TPActor(form, ctrl);
    }

    private TPActor makePlayerActor(double x1, double y1, double x2, double y2) {
        TPActor player = makeLineActor(x1, y1, x2, y2);
        player.setController(new EightWayController(), true);
        return player;
    }

    private TPMenu makeGlobalMenu() {
        TPMenu m = new TPMenu("Global Options");
        m.add(new TPMenuItemIncr("goal fps", () -> base.getFpsGoal() + "",
                                 () -> base.setFpsGoal(base.getFpsGoal() - 1),
                                 () -> base.setFpsGoal(base.getFpsGoal() + 1)));
        m.add(new TPMenuItemSimple("show intersection points",
                                   () -> System.out.println("intersections")));
        m.add(new TPMenuItemSimple("show bounding boxes",
                                   () -> System.out.println("bounding boxes")));
        m.add(new TPMenuItemSimple("show diagnostics",
                                   () -> System.out.println("diagnostics")));
        return m;
    }

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

}
