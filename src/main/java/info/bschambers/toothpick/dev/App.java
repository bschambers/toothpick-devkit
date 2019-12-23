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
        m.add(makeProgramMenuNumDrones());
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
        m.add(new TPMenuItemSimple("RUN", () -> {
                    base.setProgram(prog);
                    base.hideMenu();
        }));
        m.add(new TPMenuItemSimple("revive player", () -> prog.revivePlayer(true)));
        m.add(new TPMenuItemSimple("RESET", () -> prog.init()));
        m.add(new TPMenuItemBool("pause when menu active: ",
                                 prog::getPauseForMenu,
                                 prog::setPauseForMenu));
        m.add(makePlayerMenu(prog));
        m.add(new TPMenuItemSimple("collision detection type",
                                   () -> System.out.println("collision detection")));
        m.add(new TPMenuItemSimple("print player info", () -> {
                    TPPlayer p = prog.getPlayer();
                    System.out.println("==============================\n");
                    System.out.println("TPPlayer INPUT = " + p.getInputHandler() + "\n");
                    System.out.println("ARCHETYPE:\n" + p.getArchetype().infoString());
                    System.out.println("ACTOR:\n" + p.getActor().infoString());
                    System.out.println("==============================");
        }));
        m.add(new TPMenuItemBool("show line-intersection points: ",
                                 prog::getShowIntersections,
                                 prog::setShowIntersections));
        m.add(new TPMenuItemSimple("open in editor",
                                   () -> System.out.println("open program in editor")));
        return m;
    }

    private TPMenu makeProgramMenuNumDrones() {
        NumDronesProgram prog = makeProgSimpleNumDronesGame();
        TPMenu m = makeProgramMenu(prog);
        m.add(new TPMenuItemIncr("num drones", () -> prog.getDronesGoal() + "",
                                 () -> prog.setDronesGoal(prog.getDronesGoal() - 1),
                                 () -> prog.setDronesGoal(prog.getDronesGoal() + 1)));
        return m;
    }

    private TPMenu makePlayerMenu(TPProgram prog) {
        TPMenu m = new TPMenu("Player Options: (" + prog.getTitle() + ")");
        m.add(makePlayerControllerMenu(prog));
        m.add(new TPMenuItemSimple("re-define keys", () -> System.out.println("...")));
        m.add(new TPMenuItemSimple("calibrate input", () -> System.out.println("...")));
        m.add(new TPMenuItemSimple("preset players", () -> System.out.println("...")));
        return m;
    }

    private TPMenu makePlayerControllerMenu(TPProgram prog) {
        TPMenu m = new TPMenu(() -> "Change Input Handler (current = "
                              + prog.getPlayer().getInputHandler().getClass().getSimpleName() + ")");
        m.add(makeInputSwitcherItem(prog, new ThrustInertiaInput()));
        m.add(makeInputSwitcherItem(prog, new EightWayInertiaInput()));
        m.add(makeInputSwitcherItem(prog, new ThrustInput()));
        m.add(makeInputSwitcherItem(prog, new EightWayInput()));
        return m;
    }

    private TPMenuItem makeInputSwitcherItem(TPProgram prog, KeyInputHandler ih) {
        return new TPMenuItemSimple(ih.getClass().getSimpleName(),
                                    () -> prog.getPlayer().setInputHandler(ih));
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
        ToothpickProgram tpp = new ToothpickProgram("Static Toothpicks") {
                @Override
                public void init() {
                    super.init();
                    // drones
                    addActor(makeLineActor(45, 20, 200, 30));
                    addActor(makeLineActor(200, 350, 400, 250));
                    addActor(makeLineActor(500, 175, 550, 400));
                    // player
                    TPPlayer player = makePlayer(150, 150, 250, 150);
                    setPlayer(player);
                }
            };
        tpp.setShowIntersections(true);
        return tpp;
    }

    private NumDronesProgram makeProgSimpleNumDronesGame() {
        NumDronesProgram prog = new NumDronesProgram("Simple Num-Drones Game");
        prog.setBGColor(Color.BLUE);
        return prog;
    }

    private TPActor makeLineActor(double x1, double y1, double x2, double y2) {
        Pt start = new Pt(x1, y1);
        Pt end = new Pt(x2, y2);
        Pt pos = Geom.midPoint(start, end);
        start = start.add(pos.invert());
        end = end.add(pos.invert());
        TPForm form = new TPForm(new TPPart[] { new TPLine(new Line(start, end)) });
        TPActor actor = new TPActor(form);
        actor.addBehaviour(TPFactory.WRAP_AT_BOUNDS);
        actor.setPos(pos);
        return actor;
    }

    private TPPlayer makePlayer(double x1, double y1, double x2, double y2) {
        TPPlayer player = new TPPlayer(makeLineActor(x1, y1, x2, y2));
        player.setInputHandler(new EightWayInput());
        return player;
    }

    private TPMenu makeGlobalMenu() {
        TPMenu m = new TPMenu("Global Options");
        m.add(new TPMenuItemIncr("goal fps", () -> base.getFpsGoal() + "",
                                 () -> base.setFpsGoal(base.getFpsGoal() - 1),
                                 () -> base.setFpsGoal(base.getFpsGoal() + 1)));
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
