package info.bschambers.toothpick.dev;

import info.bschambers.toothpick.*;
import info.bschambers.toothpick.actor.*;
import info.bschambers.toothpick.dev.editor.TPEditor;
import info.bschambers.toothpick.diskops.TPXml;
import info.bschambers.toothpick.geom.*;
import info.bschambers.toothpick.ui.*;
import info.bschambers.toothpick.ui.swing.TPSwingUI;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class App {

    private TPEditor window;
    private TPBase base;
    private TPProgram introSlides;
    private int stopAfterVal = 5;
    private TPProgram loadedProg = new TPProgram("NULL PROGRAM");
    private String saveDir = "data/saved-games";

    public App() {
        window = new TPEditor();
        base = new TPBase();
        introSlides = makeIntroSlidesProg();
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
        root.add(() -> makeProgMenu(() -> "run program: " + loadedProg.getTitle(), loadedProg));
        root.add(new TPMenuItemSimple("load program from file (XML)", () -> loadProgramXML()));
        root.add(makeGlobalMenu());
        root.add(new TPMenuItemSimple("EXIT", () -> window.exit()));
        return root;
    }

    private TPMenu makeMenuPresetProgram() {
        TPMenu m = new TPMenu("preset programs");
        m.add(makeProgMenu(introSlides));
        m.add(makeProgMenu(makeProgStaticToothpick()));
        m.add(makeProgMenuNumDrones(makeProgSimpleNumDronesGame()));
        m.add(makeProgMenuNumDrones(new RibbonGame()));
        m.add(makeProgMenuNumDrones(new MixedDronesGame()));
        m.add(new TPMenuItemSimple("Increment num enemies game",
                                   () -> System.out.println("increment")));
        m.add(makeProgMenuNumDrones(makeScrollingGame()));
        m.add(new TPMenuItemSimple("boss battle game",
                                   () -> System.out.println("boss battle")));
        m.add(new TPMenuItemSimple("powerups game",
                                   () -> System.out.println("powerups")));
        m.add(new TPMenuItemSimple("levels game",
                                   () -> System.out.println("levels")));
        m.add(new TPMenuItemSimple("asteroids game",
                                   () -> System.out.println("asteroid")));
        m.add(new TPMenuItemSimple("gravity orbit game",
                                   () -> System.out.println("orbit")));
        return m;
    }

    private TPMenu makeProgMenu(TPProgram prog) {
        return makeProgMenu(() -> prog.getTitle(), prog);
    }

    private TPMenu makeProgMenu(Supplier<String> ss, TPProgram prog) {
        TPMenu m = new TPMenu(ss);
        m.setInitAction(() -> {
                System.out.println("prog-menu --> init-action");
                base.setProgram(prog);
            });
        m.add(new TPMenuItemSimple("RUN", () -> {
                    base.setProgram(prog);
                    base.hideMenu();
        }));
        m.add(new TPMenuItemSimple("revive player", () -> prog.revivePlayer(true)));
        m.add(new TPMenuItemSimple("RESET PROGRAM", () -> prog.init()));
        m.add(new TPMenuItemBool("pause when menu active ",
                                 prog::getPauseForMenu,
                                 prog::setPauseForMenu));
        m.add(new TPMenuItemSimple(() -> "step forward by " + stopAfterVal + " frames",
                                   () -> {
                                       prog.setPauseForMenu(true);
                                       prog.setStopAfter(stopAfterVal);
        }));
        m.add(new TPMenuItemIncr("set step-forward amount", () -> "" + stopAfterVal,
                                 () -> stopAfterVal--,
                                 () -> stopAfterVal++));
        m.add(makePlayerMenu(prog));
        m.add(new TPMenuItemIncr("TRANSFORM: x-offset",
                                 () -> "" + prog.getGeometry().xOffset,
                                 () -> prog.getGeometry().xOffset -= 10,
                                 () -> prog.getGeometry().xOffset += 10));
        m.add(new TPMenuItemIncr("TRANSFORM: y-offset",
                                 () -> "" + prog.getGeometry().yOffset,
                                 () -> prog.getGeometry().yOffset -= 10,
                                 () -> prog.getGeometry().yOffset += 10));
        m.add(new TPMenuItemIncr("TRANSFORM: scaling",
                                 () -> "" + prog.getGeometry().scale,
                                 () -> prog.getGeometry().scale -= 0.1,
                                 () -> prog.getGeometry().scale += 0.1));
        m.add(new TPMenuItemSimple("collision detection type",
                                   () -> System.out.println("collision detection")));
        m.add(makeInfoPrintMenu(prog));
        m.add(new TPMenuItemBool("show line-intersection points",
                                 prog::isShowIntersections,
                                 prog::setShowIntersections));
        m.add(new TPMenuItemBool("smear-mode",
                                 prog::isSmearMode,
                                 prog::setSmearMode));
        m.add(makeBGColorMenu(prog));
        m.add(new TPMenuItemSimple("save state to disk (XML)", () -> saveProgramXML(prog)));
        m.add(new TPMenuItemSimple(() -> (window.isEditorMode() ?
                                          "DEACTIVATE EDITOR" : "activate editor"),
                                   () -> window.setEditorMode(!window.isEditorMode())));
        return m;
    }

    private String rgbStr(Color c) {
        return c.getRed() + ", " + c.getGreen() + ", " + c.getBlue();
    }

    private TPMenu makeBGColorMenu(TPProgram prog) {
        TPMenu m = new TPMenu(() -> "Set BG Color (current: " + rgbStr(prog.getBGColor()) + ")");
        m.add(new TPMenuItemSimple("black", () -> prog.setBGColor(Color.BLACK)));
        m.add(new TPMenuItemSimple("white", () -> prog.setBGColor(Color.WHITE)));
        m.add(new TPMenuItemSimple("blue", () -> prog.setBGColor(Color.BLUE)));
        m.add(new TPMenuItemSimple("grey", () -> prog.setBGColor(Color.GRAY)));
        m.add(new TPMenuItemSimple("random", () -> prog.setBGColor(ColorGetter.randColor())));
        return m;
    }

    private TPMenu makeProgMenuNumDrones(NumDronesProgram prog) {
        TPMenu m = makeProgMenu(prog);
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

    private TPMenu makeInfoPrintMenu(TPProgram prog) {
        TPMenu m = new TPMenu("print info");
        m.add(new TPMenuItemSimple("print player info", () -> {
                    TPPlayer p = prog.getPlayer();
                    System.out.println("==============================");
                    System.out.println("TPPlayer INPUT = " + p.getInputHandler() + "\n");
                    System.out.println("ARCHETYPE:\n" + p.getArchetype().infoString());
                    System.out.println("ACTOR:\n" + p.getActor().infoString());
                    System.out.println("==============================");
        }));
        m.add(new TPMenuItemSimple("print game info", () -> {
                    System.out.println("==============================");
                    System.out.println("class = " + prog.getClass());
                    System.out.println("title = " + prog.getTitle());
                    System.out.println("geometry = " + prog.getGeometry());
                    System.out.println("bg color = " + prog.getBGColor());
                    System.out.println("smear-mode = " + prog.isSmearMode());
                    System.out.println("show intersection = " + prog.isShowIntersections());
                    System.out.println("pause for menu = " + prog.getPauseForMenu());
                    System.out.println("num actors = " + prog.numActors());
                    System.out.println("==============================");
        }));
        return m;
    }

    private TPProgram makeIntroSlidesProg() {
        NumDronesProgram prog = new NumDronesProgram("Intro Slideshow");
        Slideshow ss = new Slideshow();
        addSlide(ss, "toothpick_slideshow01.png");
        addSlide(ss, "toothpick_slideshow02.png");
        addSlide(ss, "toothpick_slideshow03.png");
        prog.addBehaviour(ss);
        prog.setPauseForMenu(false);
        return prog;
    }

    private void addSlide(Slideshow slides, String filename) {
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

    private TPProgram makeProgStaticToothpick() {
        TPProgram tpp = new TPProgram("Static Toothpicks") {
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
        tpp.addBehaviour(new ToothpickPhysics());
        return tpp;
    }

    private NumDronesProgram makeProgSimpleNumDronesGame() {
        NumDronesProgram prog = new NumDronesProgram("Simple Num-Drones Game");
        prog.setBGColor(Color.BLACK);
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

    public static class RibbonGame extends NumDronesProgram {

        public RibbonGame() {
            super("Ribbon Game");
            setSmearMode(true);
            getPlayer().getArchetype().setColorGetter(new ColorSmoothMono(Color.PINK));
            initPlayer();
        }

        @Override
        public void init() {
            super.init();
            setBGColor(ColorGetter.randColor());
        }
    }

    public static class MixedDronesGame extends NumDronesProgram {

        public MixedDronesGame() {
            super("Mixed Drones Game");
            setDroneFunc(this::makeDrone);
            setDronesGoal(6);
            setBGColor(Color.BLACK);
        }

        private TPActor makeDrone(TPProgram prog) {
            double r = Math.random();
            if (r < 0.333)
                return TPFactory.lineActor(prog);
            if (r < 0.666)
                return TPFactory.regularPolygonActor(prog);
            return TPFactory.regularThistleActor(prog);
        }
    }

    private NumDronesProgram makeScrollingGame() {
        NumDronesProgram prog = new MixedDronesGame();
        prog.setTitle("Scrolling Game");
        prog.addBehaviour(new ScrollWithPlayer());
        return prog;
    }

    /**
     * <p>Launches file chooser dialog and then attempts to load the selected file as a
     * TPProgram.</p>
     */
    private void loadProgramXML() {
        System.out.println("Load program from XML...");
        JFileChooser chooser = new JFileChooser(saveDir);
        FileNameExtensionFilter filter
            = new FileNameExtensionFilter("Toothpick XML files", "xml");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(window);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            System.out.println("... file chosen: " + f.getName());
            TPXml xml = new TPXml();
            TPProgram prog = xml.load(f);
            for (String msg : xml.getErrorMessages())
                System.out.println("TPXml Error: " + msg);
            if (prog == null) {
                System.out.println("... couldn't load file!");
            } else {
                System.out.println("... program loaded: " + prog.getTitle());
                loadedProg = prog;
            }
        }
    }

    private void saveProgramXML(TPProgram prog) {
        System.out.println("Save program to XML: " + prog.getTitle());
        JFileChooser chooser = new JFileChooser(saveDir);
        int returnVal = chooser.showSaveDialog(window);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            System.out.println("... file chosen: " + f.getName());
            TPXml xml = new TPXml();
            boolean success = xml.save(prog, f);
            for (String msg : xml.getErrorMessages())
                System.out.println("TPXml Error: " + msg);
            if (success) {
                System.out.println("... saved file");
            } else {
                System.out.println("... couldn't save file!");
            }
        }
    }

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

}
