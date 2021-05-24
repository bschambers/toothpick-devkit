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
        root.add(new TPMenuItemSimple("TEST MATHS ANOMOLIES!", () -> mathsTests()));
        root.add(new TPMenuItemSimple("EXIT", () -> window.exit()));
        return root;
    }

    private void mathsTests() {
        System.out.println("MATHS TESTS");
        mathsTestGeomAngle(0, 1);
        mathsTestGeomAngle(3, 3);
        mathsTestGeomAngle(2, 0);
        mathsTestGeomAngle(0, -3);
        mathsTestGeomAngle(-3, -3);
        mathsTestGeomAngle(-5, 1);
        mathsTestAngle("Math.PI * 0.25", Math.PI * 0.25);
        mathsTestAngle("Math.PI * 0.5", Math.PI * 0.5);
        mathsTestAngle("Math.PI * 1.25", Math.PI * 1.25);
        mathsTestAngle("Math.PI * 1.5", Math.PI * 1.5);
    }

    private void mathsTestAngle(String label, double angle) {
        double power = 12;
        double x = ThrustInertiaInput.thrustAmountX(angle, power);
        double y = ThrustInertiaInput.thrustAmountX(angle, power);
        double angle2 = Geom.angle(0, 0, x, y);
        System.out.println("ANGLE TEST (" + label + ")..."
                           + "\n... input-angle=" + angle + " (to-degrees=" + Math.toDegrees(angle) + ")"
                           + "\n... thrust-inertia: power=" + power + ", x=" + x + ", y=" + y
                           + "\n... Geom.angle(0, 0, x, y)=" + angle2 + " (to-degrees=" + Math.toDegrees(angle2) + ")");
    }

    private void mathsTestGeomAngle(double x, double y) {
        double angle3 = Geom.angle(0, 0, x, y);
        System.out.println("Geom.angle(0, 0, " + x + ", " + y + ") = " + angle3
                           + "\n... to-degrees=" + Math.toDegrees(angle3));
    }

    private TPMenu makeMenuPresetProgram() {
        TPMenu m = new TPMenu("preset programs");
        m.add(makeProgMenu(introSlides));
        m.add(makeProgMenu(makeProgStaticToothpick()));
        m.add(makeProgMenuNumDrones(makeNumDronesProgram("Simple Num-Drones Game")));
        m.add(makeProgMenuNumDrones(makeRibbonGame()));
        m.add(makeProgMenuNumDrones(makeMixedDronesGame()));
        m.add(makeProgMenuNumDrones(makeIncrementNumDronesGame()));
        m.add(makeProgMenuNumDrones(makeScrollingGame()));
        m.add(makeSequencePlatformMenu(makeSequenceGameAttackWaves()));
        m.add(makeProgMenu(makeProgTextAndImages()));
        m.add(makeProgMenuNumDrones(makePowerupsGame()));
        m.add(makeProgMenuNumDrones(makePathAndPointAnchorGame()));
        m.add(makeProgMenu(makeProgStorySequence()));
        m.add(makeProgMenu(makeJointedDronesGame()));
        m.add(makeProgMenu(makeRainGame()));
        m.add(makeProgMenu(makeFastEnemiesGame()));
        m.add(makeProgMenuNumDrones(makeSeekerAvoiderGame()));
        m.add(makeProgMenuNumDrones(makeTrajectoryChangeDronesGame()));
        m.add(new TPMenuItemSimple("game with gravity wells",
                                   () -> System.out.println("gravity wells/attractors")));
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
        TPMenu m = TPMenuFactory.makeProgramMenu(ss, prog, base);
        m.add(new TPMenuItemSimple("save state to disk (XML)", () -> saveProgramXML(prog)));
        m.add(new TPMenuItemSimple(() -> (window.isEditorMode() ?
                                          "DEACTIVATE EDITOR" : "activate editor"),
                                   () -> window.setEditorMode(!window.isEditorMode())));
        m.add(makeOffscreenCanvasMenu(prog));
        return m;
    }

    private TPMenu makeOffscreenCanvasMenu(TPProgram prog) {
        TPMenu m = new TPMenu(() -> "offscreen smear-canvas (NOT ACTIVE)");
        m.add(new TPMenuItemSimple("view offscreen canvas",
                                   () -> System.out.println("TODO...")));
        m.add(new TPMenuItemSimple("ACTIVATE/DEACTIVATE offscreen canvas",
                                   () -> System.out.println("TODO...")));
        m.add(new TPMenuItemSimple("clear offscreen canvas",
                                   () -> System.out.println("TODO...")));
        return m;
    }

    private TPMenu makeProgMenuNumDrones(TPProgram prog) {
        TPMenu m = makeProgMenu(prog);
        m.add(new TPMenuItemIncr("num drones", () -> getDronesGoal(prog) + "",
                                 () -> incrDronesGoal(prog, -1),
                                 () -> incrDronesGoal(prog, 1)));
        m.add(makeDroneChooserMenu(getDronesNumBehaviour(prog).getChooser()));
        return m;
    }

    private int getDronesGoal(TPProgram prog) {
        return getDronesNumBehaviour(prog).getDronesGoal();
    }

    private void incrDronesGoal(TPProgram prog, int amt) {
        getDronesNumBehaviour(prog).incrDronesGoal(amt);
    }

    /**
     * <p>Gets MaintainNumDrones behaviour from the input program. If no such behaviour
     * exists, then create it and add to program before returning.</p>
     *
     * <p>WARNING! Method may have side effects - add behaviour to prog, if it doesn't
     * already exist.</p>
     */
    private PBMaintainDronesNum getDronesNumBehaviour(TPProgram prog) {
        PBMaintainDronesNum numDrones = null;
        for (int i = 0; i < prog.numBehaviours(); i++) {
            ProgramBehaviour pb = prog.getBehaviour(i);
            if (pb instanceof PBMaintainDronesNum) {
                numDrones = (PBMaintainDronesNum) pb;
            }
        }
        if (numDrones == null) {
            numDrones = new PBMaintainDronesNum();
            prog.addBehaviour(numDrones);
        }
        return numDrones;
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

    private TPMenu makeDroneChooserMenu(RandomChooser<Function<TPProgram, TPActor>> chooser) {
        TPMenu m = new TPMenu("Adjust Drone-Chooser Weighting");
        for (RandomChooser<Function<TPProgram, TPActor>>.ChooserItem item : chooser.chooserItemList()) {
            m.add(new TPMenuItemIncr(item.getDescription(),
                                     () -> "" + item.weight,
                                     () -> item.weight--,
                                     () -> item.weight++));
        }
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
        m.add(makePhysicsSwitcherItem(prog, new PBToothpickPhysics()));
        m.add(makePhysicsSwitcherItem(prog, new PBToothpickPhysicsLight()));
        return m;
    }

    private TPMenuItem makePhysicsSwitcherItem(TPProgram prog,
                                               PBToothpickPhysics physics) {
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
                    System.out.println("total num actors (inc children) = "
                                       + getTotalNumActors(prog));
                    System.out.println("==============================");
        }));
        return m;
    }

    private int getTotalNumActors(TPProgram prog) {
        int n = 0;
        for (int i = 0; i < prog.numActors(); i++) {
            n++;
            n += getNumChildren(prog.getActor(i));
        }
        return n;
    }

    private int getNumChildren(TPActor actor) {
        int n = 0;
        for (int i = 0; i < actor.numChildren(); i++) {
            n++;
            n += getNumChildren(actor.getChild(i));
        }
        return n;
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
        TPProgram prog = makeNumDronesProgram("Intro Slideshow");
        PBSlideshow ss = new PBSlideshow();
        addSlide(ss, "toothpick_slideshow01.png");
        addSlide(ss, "toothpick_slideshow02.png");
        addSlide(ss, "toothpick_slideshow03.png");
        prog.addBehaviour(ss);
        prog.setPauseForMenu(false);
        return prog;
    }

    private void addSlide(PBSlideshow slides, String filename) {
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
        TPProgram prog = new TPProgram("Static Toothpicks");
        prog.setShowIntersections(true);
        prog.addBehaviour(new PBToothpickPhysics());

        // drones
        prog.addActor(makeLineActor(45, 20, 200, 30));
        prog.addActor(makeLineActor(200, 350, 400, 250));
        prog.addActor(makeLineActor(500, 175, 550, 400));
        prog.addActor(makeLineActor(600, 600, 650, 600));
        prog.addActor(makeLineActor(620, 50, 620, 200));

        // player
        TPPart[] playerLines = new TPPart[] {
            TPFactory.lineStandard(-50, 0, 50, 0), // horiz
            TPFactory.lineStandard(0, -50, 0, 50), // vert
            TPFactory.lineStandard(-50, -50, 50, 50), // diag
            TPFactory.lineStandard(-50, 50, 50, -50), // diag
        };
        TPActor playerActor = new TPActor(new TPForm(playerLines));
        playerActor.x = 200;
        playerActor.y = 150;
        TPPlayer player = makePlayer(playerActor);
        prog.setPlayer(player);

        prog.init();
        prog.setResetSnapshot();
        return prog;
    }

    private TPProgram makeProgTextAndImages() {
        TPProgram prog = new TPProgram("Text and Images");
        Image img = loadImageFromFile("little_thingy.png");
        for (int n = 0; n < 5; n++) {

            // text drones
            TPActor a = new TPActor();
            a.setBoundaryBehaviour(TPActor.BoundaryBehaviour.WRAP_AT_BOUNDS);
            a.setPos(TPFactory.randBoundaryPos(prog));
            TPFactory.setRandHeading(a);
            TPForm form = new TPForm();
            form.addPart(new TPTextPart("Blah, Blah, Blah..."));
            a.setForm(form);
            prog.addActor(a);

            // image drones
            if (img != null) {
                a = new TPActor();
                a.setBoundaryBehaviour(TPActor.BoundaryBehaviour.WRAP_AT_BOUNDS);
                a.setPos(TPFactory.randBoundaryPos(prog));
                TPFactory.setRandHeading(a);
                form = new TPForm();
                form.addPart(new TPImagePart(img));
                a.setForm(form);
                prog.addActor(a);
            }
        }

        prog.init();
        prog.setResetSnapshot();
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
        return makePlayer(makeLineActor(x1, y1, x2, y2));
    }

    private TPPlayer makePlayer(TPActor actor) {
        TPPlayer player = new TPPlayer(actor);
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

    private TPProgram makeRibbonGame() {
        TPProgram prog = makeNumDronesProgram("Ribbon Game");
        prog.setSmearMode(true);
        TPPlayer p = TPMenuFactory.playerPresetLine(prog);
        p.getArchetype().setColorGetter(new ColorSmoothMono(Color.PINK));
        prog.setPlayer(p);
        prog.resetPlayer();
        prog.addResetBehaviour(new PBMisc((TPProgram tpp) ->
                                          tpp.setBGColor(ColorGetter.randColor())));
        prog.setResetSnapshot();
        return prog;
    }

    private TPProgram makeMixedDronesGame() {
        TPProgram prog = makeNumDronesProgram("Mixed Drones Game");
        prog.setBGColor(Color.BLACK);
        PBMaintainDronesNum dronesNum = getDronesNumBehaviour(prog);
        dronesNum.setDronesGoal(6);
        dronesNum.addDroneFunc("line", 1, TPFactory::lineActor);
        dronesNum.addDroneFunc("polygon", 1, TPFactory::regularPolygonActor);
        dronesNum.addDroneFunc("thistle", 1, TPFactory::regularThistleActor);
        dronesNum.addDroneFunc("segmented polygon", 1, TPFactory::segmentedPolygonActor);
        dronesNum.addDroneFunc("zig-zag", 1, TPFactory::zigzagActor);
        dronesNum.addDroneFunc("shooter", 1, TPFactory::shooterActor);
        dronesNum.addDroneFunc("key-part thistle", 1, TPFactory::regularThistleActorWithKeyPart);
        dronesNum.addDroneFunc("key-part polygon", 1, TPFactory::regularPolygonActorWithKeyPart);
        dronesNum.addDroneFunc("key-part zig-zag", 1, TPFactory::zigzagActorWithKeyPart);
        dronesNum.addDroneFunc("key-part knobbly stick", 1, this::makeKnobblyStickActorWithKeyPart);
        dronesNum.addDroneFunc("knobbly stick with jointed bristles", 1, this::makeKnobblyStickActorWithJointedBristles);
        prog.setResetSnapshot();
        return prog;
    }

    private TPProgram makeIncrementNumDronesGame() {
        TPProgram prog = makeMixedDronesGame();
        prog.setTitle("Increment Num-Drones Game");
        PBMaintainDronesNum dronesNum = getDronesNumBehaviour(prog);
        dronesNum.setDronesGoal(1);
        // goal-setter increments drones-goal number as player score increases
        dronesNum.setGoalSetter(this::dividendOfPlayerScore);
        prog.setResetSnapshot();
        return prog;
    }

    private int dividendOfPlayerScore(TPProgram prog) {
        return dividendOfPlayerScore(prog, 7.0);
    }

    private int dividendOfPlayerScore(TPProgram prog, double divisor) {
        TPPlayer player = prog.getPlayer();
        if (player == null)
            return 1;
        int num = player.getActor().numKills;
        return Math.max(1, (int) (num / divisor));
    }

    private TPProgram makeScrollingGame() {
        TPProgram prog = makeMixedDronesGame();
        prog.setTitle("Scrolling Game");
        prog.addBehaviour(new PBScrollWithPlayer());
        prog.setResetSnapshot();
        return prog;
    }

    private TPProgram makePowerupsGame() {
        TPProgram prog = makeNumDronesProgram("Powerups Game");
        PBMaintainDronesNum dronesNum = getDronesNumBehaviour(prog);
        dronesNum.setDronesGoal(6);
        dronesNum.addDroneFunc("line", 1, TPFactory::lineActor);
        dronesNum.addDroneFunc("key-part thistle", 1, TPFactory::regularThistleActorWithKeyPart);
        dronesNum.addDroneFunc("key-part polygon", 1, TPFactory::regularPolygonActorWithKeyPart);
        // dronesNum.addDroneFunc("key-part zig-zag", 1, TPFactory::zigzagActorWithKeyPart);
        dronesNum.addDroneFunc("shooter", 1, TPFactory::shooterActor);
        dronesNum.addDroneFunc("powerup: shooting", 1, TPFactory::powerupActorShooting);
        dronesNum.addDroneFunc("powerup: sticky", 1, TPFactory::powerupActorSticky);
        dronesNum.addDroneFunc("powerup: strong", 1, TPFactory::powerupActorStrong);
        prog.setResetSnapshot();
        return prog;
    }

    private TPProgram makePathAndPointAnchorGame() {
        TPProgram prog = makeNumDronesProgram("Path and Point-Anchor Drones Game");
        PBMaintainDronesNum dronesNum = getDronesNumBehaviour(prog);
        dronesNum.setDronesGoal(3);
        dronesNum.addDroneFunc("point-anchor (rectangle)", 1,
                               (TPProgram tpp) -> makePointAnchorActorRect(tpp));
        dronesNum.addDroneFunc("point-anchor (polygon)", 1,
                               (TPProgram tpp) -> makePointAnchorActorPoly(tpp));
        dronesNum.addDroneFunc("path-anchor (rectangle)", 1,
                               (TPProgram tpp) -> makePathAnchorActorRect(tpp));
        dronesNum.addDroneFunc("path-anchor (polygon)", 1,
                               (TPProgram tpp) -> makePathAnchorActorPoly(tpp));
        prog.setResetSnapshot();
        return prog;
    }

    private TPActor makePointAnchorActorRect(TPProgram prog) {
        return makePointAnchorActor(prog, TPFactory.rectangleForm(400, 100));
    }

    private TPActor makePointAnchorActorPoly(TPProgram prog) {
        double size = 40 + (Math.random() * 50);
        int numSides = 3 + (int) (Math.random() * 6);
        return makePointAnchorActor(prog, TPFactory.regularPolygonForm(size, numSides));
    }

    /**
     * <p>Anchor to a random point on anchorForm.</p>
     */
    private TPActor makePointAnchorActor(TPProgram prog, TPForm anchorForm) {

        TPActor railsActor = new TPActor(anchorForm);
        railsActor.name = "point-anchor";
        railsActor.setColorGetter(TPFactory.randColorGetter());
        railsActor.setBoundaryBehaviour(TPActor.BoundaryBehaviour.WRAP_PARTS_AT_BOUNDS);
        railsActor.setPos(TPFactory.randPos(prog));
        railsActor.xInertia = 0.3;

        // make child actor and anchor to point on rectangle
        int numSides = 3 + (int) (Math.random() * 6);
        TPForm thistleForm = TPFactory.regularThistleForm(30, numSides);
        TPActor thistleActor = new TPActor(thistleForm);
        thistleActor.angleInertia = 0.001;

        TPFactory.anchorToRandPoint(thistleActor, railsActor);
        return railsActor;
    }

    private TPActor makePathAnchorActorRect(TPProgram prog) {
        return makePathAnchorActor(prog, TPFactory.rectangleForm(400, 100));
    }

    private TPActor makePathAnchorActorPoly(TPProgram prog) {
        double size = 40 + (Math.random() * 50);
        int numSides = 3 + (int) (Math.random() * 6);
        return makePathAnchorActor(prog, TPFactory.regularPolygonForm(size, numSides));
    }

    private TPActor makePathAnchorActor(TPProgram prog, TPForm pathForm) {
        TPActor railsActor = new TPActor(pathForm);
        railsActor.name = "path-anchor";
        railsActor.setColorGetter(TPFactory.randColorGetter());
        railsActor.setBoundaryBehaviour(TPActor.BoundaryBehaviour.WRAP_PARTS_AT_BOUNDS);
        railsActor.setPos(TPFactory.randPos(prog));
        railsActor.xInertia = 0.3;

        // make child actor and anchor to point on rectangle
        int numSides = 3 + (int) (Math.random() * 6);
        TPForm thistleForm = TPFactory.regularThistleForm(30, numSides);
        TPActor thistleActor = new TPActor(thistleForm);
        thistleActor.angleInertia = 0.001;

        TPFactory.anchorToPath(thistleActor, railsActor);

        return railsActor;
    }

    private TPProgram makeJointedDronesGame() {
        TPProgram prog = makeNumDronesProgram("Jointed Drones Game");
        PBMaintainDronesNum dronesNum = getDronesNumBehaviour(prog);
        dronesNum.setDronesGoal(1);
        dronesNum.addDroneFunc("knobbly stick with jointed bristles", 1,
                               (TPProgram tpp) -> makeKnobblyStickActorWithJointedBristles(tpp));
        dronesNum.addDroneFunc("double-jointed arm", 1,
                               (TPProgram tpp) -> makeDoubleJointedArmActor(tpp));
        prog.setResetSnapshot();
        return prog;
    }

    private TPActor makeKnobblyStickActorWithJointedBristles(TPProgram prog) {
        double rotationSpeed = 0.001 + (Math.random() * 0.02);
        double bristleLength = 15 + (Math.random() * 50);
        boolean symmetrical = Math.random() < 0.5;

        TPActor actor = makeKnobblyStickActorWithKeyPart(prog);

        TPForm form = actor.getForm();
        int numLines = form.numLines();
        int halfWay = numLines / 2;
        for (int i = 0; i < numLines; i++) {
            // add bristles to odd numbered lines
            if (i % 2 == 1) {
                TPLine ln = form.getLine(i);

                TPActor child = new TPActor(TPFactory.singleLineForm(TPFactory.NEARLY_ZERO,
                                                                     TPFactory.NEARLY_ZERO,
                                                                     bristleLength,
                                                                     TPFactory.NEARLY_ZERO));
                child.angle = Math.random() * Math.PI;
                if (symmetrical && i >= halfWay) {
                    child.angleInertia = -rotationSpeed;
                } else {
                    child.angleInertia = rotationSpeed;
                }

                PointAnchor anchor = new PointAnchor();
                anchor.setAnchor(ln, form);

                child.addBehaviour(anchor);
                actor.addChild(child);
            }
        }
        return actor;
    }

    private TPActor makeKnobblyStickActorWithKeyPart(TPProgram prog) {
        // customisable variables
        double length = 500;
        double width = 40;
        int numSections = 4;

        TPForm form = knobblyStickForm(length, width, numSections);
        TPActor a = new TPActor(form);
        a.name = "knobbly stick (" + numSections + " sections)";
        TPFactory.setStrongWithRandomKeyLine(a);
        a.setColorGetter(TPFactory.randColorGetter());
        a.setBoundaryBehaviour(TPActor.BoundaryBehaviour.WRAP_PARTS_AT_BOUNDS);
        // random angle and heading
        a.angle = Math.random() * Math.PI;
        TPFactory.setRandHeading(a);
        return a;
    }

    private TPForm knobblyStickForm(double length, double width, int numSections) {
        Pt[] points = knobblyStickPoints(length, width, numSections);
        return TPFactory.closedLoopForm(points);
    }

    private Pt[] knobblyStickPoints(double length, double width, int numSections) {
        double sectionLength = length / numSections;
        double outWidth = width / 2;
        double inWidth = width / 4; // waist points
        double xStep = sectionLength / 2;
        Pt[] points = new Pt[numSections * 4];
        int n = 0;
        // outwards - upper edge, left to right
        double x = -(length / 2);
        double y = 0; // for end point
        for (int i = 0; i < numSections; i++) {
            points[n] = new Pt(x, y);
            x += xStep;
            y = outWidth;
            n++;
            points[n] = new Pt(x, y);
            x += xStep;
            y = inWidth;
            n++;
        }
        // return journey - lower edge, right to left
        x = length / 2;
        y = 0; // for end point
        for (int i = 0; i < numSections; i++) {
            points[n] = new Pt(x, y);
            x -= xStep;
            y = -outWidth;
            n++;
            points[n] = new Pt(x, y);
            x -= xStep;
            y = -inWidth;
            n++;
        }
        return points;
    }

    private TPActor makeDoubleJointedArmActor(TPProgram prog) {
        // triangular base-actor
        double size = TPFactory.rand(30, 70);
        TPActor baseActor = TPFactory.regularPolygonActor(prog, size, 3);
        TPFactory.setRandAngleAndHeading(baseActor);
        baseActor.angleInertia = 0;

        double jointLen = 70;

        TPActor child1 = new TPActor(TPFactory.singleLineForm(TPFactory.NEARLY_ZERO,
                                                              TPFactory.NEARLY_ZERO,
                                                              jointLen,
                                                              TPFactory.NEARLY_ZERO));
        child1.setColorGetter(TPFactory.randColorGetter());
        child1.angleInertia = Math.random() * 0.01;
        PointAnchor anchor1 = new PointAnchor();
        anchor1.setAnchor(baseActor.getForm().getLine(0), baseActor.getForm());
        child1.addBehaviour(anchor1);
        baseActor.addChild(child1);

        TPActor child2 = new TPActor(TPFactory.singleLineForm(TPFactory.NEARLY_ZERO,
                                                              TPFactory.NEARLY_ZERO,
                                                              jointLen,
                                                              TPFactory.NEARLY_ZERO));
        child2.setColorGetter(TPFactory.randColorGetter());
        child2.angleInertia = Math.random() * 0.01;
        PointAnchor anchor2 = new PointAnchor();
        anchor2.setAnchor(child1.getForm().getLine(0), child1.getForm(), false);
        child2.addBehaviour(anchor2);
        child1.addChild(child2);

        return baseActor;
    }

    private TPProgram makeRainGame() {

        // basic properties

        TPProgram prog = new TPProgram("Rain Game (horizontal wrap-around)");
        int width = 5000;
        int height = 800;
        int top = 30;
        int bot = 770;
        prog.setPlayArea(width, height);
        prog.setBGColor(Color.BLUE);
        prog.addBehaviour(new PBScrollWithPlayer());
        prog.addBehaviour(new PBToothpickPhysics());

        // make landscape

        TPActor sky = new TPActor();
        sky.x = 0.1;
        sky.y = 0.1;
        sky.name = "sky";
        TPLine skyLine = TPFactory.lineStrong(0, top, width, top, ColorMono.WHITE);
        sky.getForm().addPart(skyLine);
        Spawning raining = new Spawning();
        raining.setArchetype(new TPActor(TPFactory.singleLineForm(50)));
        raining.setOriginLine(skyLine);
        raining.setRelativeAngle(1.0);
        raining.setRelativeRotation(0.25);
        raining.setInterval(25);
        sky.addBehaviour(raining);

        TPActor floor = new TPActor();
        floor.x = 0.1;
        floor.y = 0.1;
        floor.name = "floor";
        floor.getForm().addPart(TPFactory.lineStrong(0, bot, width, bot,
                                                     ColorMono.GREEN));
        for (int i = 0; i < 20; i++) {
            double x = Math.random() * width;
            double y = Math.random() * 100;
            floor.getForm().addPart(TPFactory.lineStrong(x, bot, x, bot - y,
                                                         ColorMono.GREEN));
        }

        prog.addActor(sky);
        prog.addActor(floor);
        prog.init();
        prog.setResetSnapshot();
        return prog;
    }

    private TPProgram makeFastEnemiesGame() {
        TPProgram prog = new TPProgram("Fast moving enemies (try to match velocity)");
        TPGeometry geom = new TPGeometry();
        geom.setupAndCenter(2000, 800);
        prog.setGeometry(geom);
        prog.addBehaviour(new PBToothpickPhysics());
        prog.addBehaviour(new PBScrollWithPlayer());

        TPActor a1 = TPFactory.regularThistleActor(prog);
        a1.x = 100;
        a1.y = 200;
        a1.xInertia = 20;
        a1.yInertia = 0;
        prog.addActor(a1);

        TPActor a2 = TPFactory.regularThistleActor(prog);
        a2.x = 100;
        a2.y = 500;
        a2.xInertia = 30;
        a2.yInertia = 0;
        prog.addActor(a2);

        prog.init();
        prog.setResetSnapshot();
        return prog;
    }

    private TPProgram makeSeekerAvoiderGame() {
        TPProgram prog = makeNumDronesProgram("Seeker/Avoider Game");
        PBMaintainDronesNum dronesNum = getDronesNumBehaviour(prog);
        dronesNum.setDronesGoal(2);
        dronesNum.addDroneFunc("seeker", 1,
                               (TPProgram tpp) -> makeSeekerDrone(tpp));
        dronesNum.addDroneFunc("avoider", 1,
                               (TPProgram tpp) -> makeAvoiderDrone(tpp));
        prog.setResetSnapshot();
        return prog;
    }

    private TPActor makeSeekerDrone(TPProgram prog) {
        TPActor a = TPFactory.lineActor(prog);
        double speed = Math.random();
        a.addBehaviour(new SeekerBehaviour(speed, true));
        return a;
    }

    private TPActor makeAvoiderDrone(TPProgram prog) {
        TPActor a = TPFactory.lineActor(prog);
        double speed = Math.random();
        a.addBehaviour(new AvoiderBehaviour(speed, true));
        return a;
    }

    private TPProgram makeTrajectoryChangeDronesGame() {
        TPProgram prog = makeNumDronesProgram("Trajectory-Change Drones Game");
        prog.setBGColor(Color.BLUE);
        PBMaintainDronesNum dronesNum = getDronesNumBehaviour(prog);
        dronesNum.setDronesGoal(1);
        dronesNum.addDroneFunc("trajectory-change line", 1, this::makeTrajectoryChangeLineActor);
        prog.setSmearMode(true);
        prog.setResetSnapshot();
        return prog;
    }

    private TPActor makeTrajectoryChangeLineActor(TPProgram prog) {
        TPActor a = TPFactory.lineActor(prog);
        a.angleInertia = 0;
        a.addBehaviour(new TrajectoryChangeBehaviour());
        return a;
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
        TPProgram prog = makeNumDronesProgram(title + " wave");
        PBMaintainDronesNum dronesNum = getDronesNumBehaviour(prog);
        dronesNum.addDroneFunc(title, 1, droneFunc);
        prog.addBehaviour(new PBFinishAfterNumKills(killsTarget));
        prog.setBGColor(Color.BLACK);
        prog.setResetSnapshot();
        return prog;
    }

    private TPProgram makeProgStorySequence() {
        TPProgram tpp = new TPProgram("Story Sequence");
        return tpp;
    }

    private TPProgram makeNumDronesProgram(String title) {
        TPProgram prog = new TPProgram(title);
        prog.addBehaviour(new PBToothpickPhysics());
        prog.addBehaviour(new PBMaintainDronesNum());
        prog.setResetSnapshot();
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

    /*--------------------- static utility methods ---------------------*/

    private static String rgbStr(Color c) {
        return c.getRed() + ", " + c.getGreen() + ", " + c.getBlue();
    }

}
