package info.bschambers.toothpick.dev;

import info.bschambers.toothpick.*;
import info.bschambers.toothpick.actor.*;
import info.bschambers.toothpick.dev.editor.TPEditor;
import info.bschambers.toothpick.diskops.TPXml;
import info.bschambers.toothpick.geom.*;
import info.bschambers.toothpick.sound.*;
import info.bschambers.toothpick.ui.*;
import info.bschambers.toothpick.ui.swing.TPSwingUI;
import info.bschambers.toothpick.util.RandomChooser;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Function;
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
        base.setSound(makeSampledSoundModule());
    }

    public void run() {
        window.setVisible(true);
        base.run();
    }

    private TPSound makeSampledSoundModule() {
        TPSampledSound sampled = new TPSampledSound();
        addSfxFile(sampled, "sfx/cymbals/cymbal01.wav");
        addSfxFile(sampled, "sfx/cymbals/cymbal02.wav");
        addSfxFile(sampled, "sfx/cymbals/cymbal03.wav");
        addSfxFile(sampled, "sfx/cymbals/cymbal04.wav");
        addSfxFile(sampled, "sfx/cymbals/cymbal05.wav");
        addSfxFile(sampled, "sfx/cymbals/cymbal06.wav");
        addSfxFile(sampled, "sfx/cymbals/cymbal07.wav");
        addSfxFile(sampled, "sfx/cymbals/cymbal08.wav");
        return sampled;
    }

    private void addSfxFile(TPSampledSound sampled, String filename) {
        System.out.println("try to add sound file: " + filename);
        URL url = ClassLoader.getSystemClassLoader().getResource(filename);
        System.out.println("... got url: " + url);
        sampled.addSfx(new File(url.getPath()));
    }

    private TPMenu makeMenu() {
        TPMenu root = new TPMenu("MAIN MENU");
        root.add(makeMenuPresetProgram());
        root.add(() -> makeProgMenu(() -> "run program: " + loadedProg.getTitle(), loadedProg));
        root.add(new TPMenuItemSimple("load program from file", () -> loadProgramXML()));
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
        m.add(makeProgMenuNumDrones(makeIncrementNumDronesGame()));
        m.add(makeProgMenuNumDrones(makeScrollingGame()));
        m.add(makeSequencePlatformMenu(makeSequenceGameAttackWaves()));
        m.add(makeProgMenu(makeProgTextAndImages()));
        m.add(makeProgMenuNumDrones(makePowerupsGame()));
        m.add(makeProgMenuNumDrones(makeChildOnRailsGame()));
        m.add(makeProgMenu(makeProgStorySequence()));
        m.add(new TPMenuItemSimple("rain",
                                   () -> System.out.println("rain")));
        m.add(new TPMenuItemSimple("boss battle game",
                                   () -> System.out.println("boss battle")));
        m.add(new TPMenuItemSimple("levels game (loaded from disk)",
                                   () -> System.out.println("levels")));
        m.add(new TPMenuItemSimple("asteroids game",
                                   () -> System.out.println("asteroid")));
        m.add(new TPMenuItemSimple("gravity orbit game",
                                   () -> System.out.println("orbit")));
        m.add(new TPMenuItemSimple("two player duel",
                                   () -> System.out.println("two player duel")));
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
                prog.updateActorsInPlace();
            });
        m.add(new TPMenuItemSimple("RUN", () -> {
                    base.hideMenu();
        }));
        m.add(new TPMenuItemSimple("revive player", () -> {
                    if (prog.getPlayer() == TPPlayer.NULL) {
                        System.out.println("Null-player - adding default...");
                        prog.setPlayer(TPFactory.playerLine(centerPt(prog)));
                    }
                    prog.revivePlayer(true);
        }));
        m.add(new TPMenuItemSimple("RESET PROGRAM", () -> prog.reset()));
        m.add(new TPMenuItemBool("pause when menu active ",
                                 prog::getPauseForMenu,
                                 prog::setPauseForMenu));
        m.add(new TPMenuItemSimple(() -> "step forward by " + stopAfterVal + " frames",
                                   () -> prog.setPauseAfter(stopAfterVal)));
        m.add(new TPMenuItemIncr("set step-forward amount", () -> "" + stopAfterVal,
                                 () -> stopAfterVal--,
                                 () -> stopAfterVal++));
        m.add(makePlayerMenu(prog));
        m.add(makeScreenGeometryMenu(prog));
        m.add(makePhysicsMenu(prog));
        m.add(makeInfoPrintMenu(prog));
        m.add(makeDiagnosticsMenu(prog));
        m.add(new TPMenuItemBool("smear-mode",
                                 prog::isSmearMode,
                                 prog::setSmearMode));
        m.add(makeInfoLinesMenu(prog));
        m.add(makeBGColorMenu(prog));
        m.add(new TPMenuItemSimple("save state to disk (XML)", () -> saveProgramXML(prog)));
        m.add(new TPMenuItemSimple(() -> (window.isEditorMode() ?
                                          "DEACTIVATE EDITOR" : "activate editor"),
                                   () -> window.setEditorMode(!window.isEditorMode())));
        return m;
    }

    private TPMenu makeProgMenuNumDrones(NumDronesProgram prog) {
        TPMenu m = makeProgMenu(prog);
        m.add(new TPMenuItemIncr("num drones", () -> prog.getDronesGoal() + "",
                                 () -> prog.setDronesGoal(prog.getDronesGoal() - 1),
                                 () -> prog.setDronesGoal(prog.getDronesGoal() + 1)));
        m.add(makeDroneChooserMenu(prog));
        return m;
    }

    private TPMenu makeSequencePlatformMenu(TPSequencePlatform platform) {
        TPMenu m = new TPMenu(platform.getTitle());
        m.setInitAction(() -> {
                System.out.println("sequence-platform-menu --> init-action");
                base.setPlatform(platform);
                platform.getProgram().updateActorsInPlace();
            });
        m.add(new TPMenuItemSimple("RUN", () -> base.hideMenu()));
        // m.add(makeSequenceStagesMenu(platform));
        m.add(new TPMenuItemSimple(() -> (window.isEditorMode() ?
                                          "DEACTIVATE EDITOR" : "activate editor"),
                                   () -> window.setEditorMode(!window.isEditorMode())));
        return m;
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

    private TPMenu makeDroneChooserMenu(NumDronesProgram prog) {
        TPMenu m = new TPMenu("Adjust Drone-Chooser Weighting");
        for (RandomChooser<Function<TPProgram, TPActor>>.ChooserItem item : prog.getChooser().chooserItemList()) {
            m.add(new TPMenuItemIncr(item.getDescription(),
                                     () -> "" + item.weight,
                                     () -> item.weight--,
                                     () -> item.weight++));
        }
        return m;
    }

    private TPMenu makePlayerMenu(TPProgram prog) {
        TPMenu m = new TPMenu("Player Options: (" + prog.getTitle() + ")");
        m.add(makePlayerControllerMenu(prog));
        m.add(new TPMenuItemSimple("re-define keys", () -> System.out.println("...")));
        m.add(new TPMenuItemSimple("calibrate input", () -> System.out.println("...")));
        m.add(makePresetPlayersMenu(prog));
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

    private TPMenu makePresetPlayersMenu(TPProgram prog) {
        TPMenu m = new TPMenu("preset players");
        m.add(new TPMenuItemSimple("line-player",
                                   () -> prog.setPlayer(playerPresetLine(prog))));
        return m;
    }

    private TPMenu makeScreenGeometryMenu(TPProgram prog) {
        TPMenu m = new TPMenu("screen geometry");
        m.add(new TPMenuItemIncr("x-offset", () -> "" + prog.getGeometry().xOffset,
                                 () -> prog.getGeometry().xOffset -= 10,
                                 () -> prog.getGeometry().xOffset += 10));
        m.add(new TPMenuItemIncr("y-offset", () -> "" + prog.getGeometry().yOffset,
                                 () -> prog.getGeometry().yOffset -= 10,
                                 () -> prog.getGeometry().yOffset += 10));
        m.add(new TPMenuItemIncr("scaling", () -> "" + prog.getGeometry().scale,
                                 () -> prog.getGeometry().scale -= 0.1,
                                 () -> prog.getGeometry().scale += 0.1));
        return m;
    }

    private TPMenu makePhysicsMenu(TPProgram prog) {
        TPMenu m = new TPMenu("physics type");
        m.add(makePhysicsSwitcherItem(prog, new ToothpickPhysics()));
        m.add(makePhysicsSwitcherItem(prog, new ToothpickPhysicsLight()));
        return m;
    }

    private TPMenuItem makePhysicsSwitcherItem(TPProgram prog, ToothpickPhysics physics) {
        String name = physics.getClass().getSimpleName();
        return new TPMenuItemSimple(name, () -> {
                prog.addBehaviour(physics);
                System.out.println("Switch to " + name);
        });
    }

    private TPMenu makeInfoPrintMenu(TPProgram prog) {
        TPMenu m = new TPMenu("print info");
        m.add(new TPMenuItemSimple("print player info", () -> {
                    TPPlayer p = prog.getPlayer();
                    System.out.println("==============================");
                    System.out.println("PLAYER = " + p);
                    System.out.println("INPUT = " + p.getInputHandler() + "\n");
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

    private TPMenu makeDiagnosticsMenu(TPProgram prog) {
        TPMenu m = new TPMenu("diagnostic tools");
        m.add(new TPMenuItemBool("show line-intersection points",
                                 prog::isShowIntersections,
                                 prog::setShowIntersections));
        m.add(new TPMenuItemBool("show bounding boxes",
                                 prog::isShowBoundingBoxes,
                                 prog::setShowBoundingBoxes));
        return m;
    }

    private TPMenu makeInfoLinesMenu(TPProgram prog) {
        TPMenu m = new TPMenu("show info lines");
        m.add(new TPMenuItemBool("program info",
                                 prog::getShowProgramInfo,
                                 prog::setShowProgramInfo));
        m.add(new TPMenuItemBool("diagnostic info",
                                 prog::getShowDiagnosticInfo,
                                 prog::setShowDiagnosticInfo));
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
        Image img = loadImageFromFile(filename);
        if (img != null)
            slides.addImage(img);
    }

    private Image loadImageFromFile(String filename) {
        try {
            URL url = ClassLoader.getSystemClassLoader().getResource(filename);
            Image img = ImageIO.read(url);
            return img;
        } catch (IOException e) {
            System.out.println("ERROR - COULDN'T LOAD IMAGE: " + filename);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.out.println("ERROR - COULDN'T LOAD IMAGE: " + filename);
            e.printStackTrace();
        }
        return null;
    }

    private TPProgram makeProgStaticToothpick() {
        TPProgram tpp = new TPProgram("Static Toothpicks") {
                @Override
                public void reset() {
                    super.reset();
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
        tpp.reset();
        return tpp;
    }

    private TPProgram makeProgTextAndImages() {
        TPProgram tpp = new TPProgram("Text and Images") {
                @Override
                public void reset() {
                    super.reset();
                    Image img = loadImageFromFile("little_thingy.png");
                    for (int n = 0; n < 5; n++) {

                        // text drones
                        TPActor a = new TPActor();
                        a.setBoundaryBehaviour(TPActor.BoundaryBehaviour.WRAP_AT_BOUNDS);
                        a.setPos(TPFactory.randBoundaryPos(this));
                        TPFactory.setRandHeading(a);
                        TPForm form = new TPForm();
                        form.addPart(new TPText("Blah, Blah, Blah..."));
                        a.setForm(form);
                        addActor(a);

                        // image drones
                        if (img != null) {
                            a = new TPActor();
                            a.setBoundaryBehaviour(TPActor.BoundaryBehaviour.WRAP_AT_BOUNDS);
                            a.setPos(TPFactory.randBoundaryPos(this));
                            TPFactory.setRandHeading(a);
                            form = new TPForm();
                            form.addPart(new TPImage(img));
                            a.setForm(form);
                            addActor(a);
                        }

                    }
                }
            };
        tpp.reset();
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
        actor.setBoundaryBehaviour(TPActor.BoundaryBehaviour.WRAP_AT_BOUNDS);
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
        m.add(makeSoundMenu());
        return m;
    }

    private TPMenu makeSoundMenu() {
        TPMenu m = new TPMenu("Sound Settings");
        m.add(new TPMenuItemSimple("Sampled Sound (cymbals)",
                                   () -> base.setSound(makeSampledSoundModule())));
        m.add(new TPMenuItemSimple("No Sound",
                                   () -> base.setSound(TPSound.NULL)));
        return m;
    }

    public static class RibbonGame extends NumDronesProgram {

        public RibbonGame() {
            super("Ribbon Game");
            setSmearMode(true);
            TPPlayer p = playerPresetLine(this);
            p.getArchetype().setColorGetter(new ColorSmoothMono(Color.PINK));
            setPlayer(p);
            resetPlayer();
            setBGColor(ColorGetter.randColor());
        }

        @Override
        public void reset() {
            super.reset();
            setBGColor(ColorGetter.randColor());
        }
    }

    public static class MixedDronesGame extends NumDronesProgram {
        public MixedDronesGame() {
            super("Mixed Drones Game");
            addDroneFunc("line", 1, TPFactory::lineActor);
            addDroneFunc("polygon", 1, TPFactory::regularPolygonActor);
            addDroneFunc("thistle", 1, TPFactory::regularThistleActor);
            addDroneFunc("segmented polygon", 1, TPFactory::segmentedPolygonActor);
            addDroneFunc("zig-zag", 1, TPFactory::zigzagActor);
            addDroneFunc("shooter", 1, TPFactory::shooterActor);
            addDroneFunc("key-part thistle", 1, TPFactory::regularThistleActorWithKeyPart);
            addDroneFunc("key-part polygon", 1, TPFactory::regularPolygonActorWithKeyPart);
            addDroneFunc("key-part zig-zag", 1, TPFactory::zigzagActorWithKeyPart);
            setDronesGoal(6);
            setBGColor(Color.BLACK);
        }
    }

    private NumDronesProgram makeIncrementNumDronesGame() {
        NumDronesProgram prog = new MixedDronesGame();
        prog.setTitle("Increment Num-Drones Game");
        prog.setDronesGoal(1);
        prog.addBehaviour(new NumDronesProgram.IncrementNumDronesWithScore());
        return prog;
    }

    private NumDronesProgram makeScrollingGame() {
        NumDronesProgram prog = new MixedDronesGame();
        prog.setTitle("Scrolling Game");
        prog.addBehaviour(new ScrollWithPlayer());
        return prog;
    }

    private NumDronesProgram makePowerupsGame() {
        NumDronesProgram prog = new NumDronesProgram("Powerups Game");
        prog.addDroneFunc("line", 1, TPFactory::lineActor);
        prog.addDroneFunc("key-part thistle", 1, TPFactory::regularThistleActorWithKeyPart);
        prog.addDroneFunc("key-part polygon", 1, TPFactory::regularPolygonActorWithKeyPart);
        // prog.addDroneFunc("key-part zig-zag", 1, TPFactory::zigzagActorWithKeyPart);
        prog.addDroneFunc("shooter", 1, TPFactory::shooterActor);
        prog.addDroneFunc("powerup: shooting", 1, TPFactory::powerupActorShooting);
        prog.addDroneFunc("powerup: sticky", 1, TPFactory::powerupActorSticky);
        prog.addDroneFunc("powerup: strong", 1, TPFactory::powerupActorStrong);
        prog.setDronesGoal(6);
        return prog;
    }

    private NumDronesProgram makeChildOnRailsGame() {
        NumDronesProgram prog = new NumDronesProgram("Child-on-Rails Game");
        prog.setDronesGoal(2);
        // prog.addDroneFunc("point-anchor", 1, TPFactory::lineActor);
        // prog.addDroneFunc("rails-anchor (loop)", 1, TPFactory::lineActor);
        // prog.addDroneFunc("rails-anchor (bounce)", 1, TPFactory::lineActor);
        prog.addDroneFunc("point-anchor (rectangle)", 1,
                          (TPProgram tpp) -> makePointAnchorActorRect(tpp));
        return prog;
    }

    private TPActor makePointAnchorActorRect(TPProgram prog) {
        // rectangle form
        Pt a = new Pt(-200, -50);
        Pt b = new Pt(-200, 50);
        Pt c = new Pt(200, 50);
        Pt d = new Pt(200, -50);
        TPLine lineA = new TPLine(new Line(a, b));
        TPLine lineB = new TPLine(new Line(b, c));
        TPLine lineC = new TPLine(new Line(c, d));
        TPLine lineD = new TPLine(new Line(d, a));
        TPForm form = new TPForm(new TPPart[] { lineA, lineB, lineC, lineD });
        // rectangle actor
        TPActor railsActor = new TPActor(form);
        railsActor.name = "rails-anchor (loop)";
        railsActor.setColorGetter(TPFactory.randColorGetter());
        railsActor.setBoundaryBehaviour(TPActor.BoundaryBehaviour.WRAP_PARTS_AT_BOUNDS);
        railsActor.setPos(TPFactory.randBoundaryPos(prog));
        railsActor.xInertia = 0.3;
        railsActor.yInertia = 0.4;
        // make child actor and anchor to point on rectangle
        int numSides = 3 + (int) (Math.random() * 6);
        TPForm thistleForm = TPFactory.regularThistleForm(30, numSides);
        TPActor thistleActor = new TPActor(thistleForm);
        thistleActor.angleInertia = 0.001;
        thistleActor.addBehaviour(new PointAnchor());
        railsActor.addChild(thistleActor);
        return railsActor;
    }

    private TPSequencePlatform makeSequenceGameAttackWaves() {
        TPSequencePlatform platform = new TPSequencePlatform("Sequence Game: Attack Waves");
        platform.addProgram(makeAttackWaveLevel("lines", 4, TPFactory::lineActor));
        platform.addProgram(makeAttackWaveLevel("key-part thistle", 4,
                                                TPFactory::regularThistleActorWithKeyPart));
        platform.addProgram(makeAttackWaveLevel("key-part polygon", 4,
                                                TPFactory::regularPolygonActorWithKeyPart));
        platform.addProgram(makeAttackWaveLevel("key-part zig-zag", 6,
                                                TPFactory::zigzagActorWithKeyPart));
        platform.addProgram(makeAttackWaveLevel("zig-zag", 10, TPFactory::zigzagActor));
        platform.addProgram(makeAttackWaveLevel("thistle", 20, TPFactory::regularThistleActor));
        platform.addProgram(makeAttackWaveLevel("shooter", 5, TPFactory::shooterActor));
        platform.addProgram(makeAttackWaveLevel("polygon", 8, TPFactory::regularPolygonActor));
        platform.addProgram(makeAttackWaveLevel("segmented-polygon", 6,
                                                TPFactory::segmentedPolygonActor));
        platform.setPlayer(TPFactory.playerLine(new Pt(300, 300)));
        return platform;
    }

    private TPProgram makeAttackWaveLevel(String title, int killsTarget,
                                          Function<TPProgram, TPActor> droneFunc) {
        NumDronesProgram prog = new NumDronesProgram(title + " wave");
        prog.addDroneFunc(title, 1, droneFunc);
        prog.addBehaviour(new FinishAfterNumKills(killsTarget));
        prog.setBGColor(Color.BLACK);
        return prog;
    }

    private TPProgram makeProgStorySequence() {
        TPProgram tpp = new TPProgram("Story Sequence");
        return tpp;
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

    /*--------------------- static utility methods ---------------------*/

    private static Pt centerPt(TPProgram prog) {
        return new Pt(prog.getGeometry().getXCenter(),
                      prog.getGeometry().getYCenter());

    }

    private static String rgbStr(Color c) {
        return c.getRed() + ", " + c.getGreen() + ", " + c.getBlue();
    }

    private static TPPlayer playerPresetLine(TPProgram prog) {
        return TPFactory.playerLine(centerPt(prog));
    }

}
