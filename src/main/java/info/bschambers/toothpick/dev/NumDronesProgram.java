package info.bschambers.toothpick.dev;

import info.bschambers.toothpick.game.ToothpickProgram;
import info.bschambers.toothpick.actor.TPActor;
import info.bschambers.toothpick.actor.TPFactory;
import java.util.List;
import java.util.function.Supplier;

/**
 * <p>Program maintains a set number of drones, spawning a new one after a drone is
 * killed.</p>
 *
 * <p>New drones a spawned from a list of actor-factory methods.</p>
 */
public class NumDronesProgram extends ToothpickProgram {

    private int dronesGoal = 3;
    private int spawnDelay = 0;

    private Supplier<TPActor> droneSupplier
        = () -> TPFactory.randSingleLineEdgePos(getBounds());

    public NumDronesProgram(String title) {
        super(title);
    }

    @Override
    public void init() {
        super.init();
        setPlayer(TPFactory.playerLine(getBounds().center()));
    }

    public int getDronesGoal() { return dronesGoal; }

    public void setDronesGoal(int val) { dronesGoal = val; }

    @Override
    public void update() {
        doSpawning();
        super.update();
    }

    private void doSpawning() {
        if (numDrones() < dronesGoal) {
            if (spawnDelay < 1) {
                TPActor drone = droneSupplier.get();
                addActor(drone);
                spawnDelay = (int) (Math.random() * 600);
            } else {
                spawnDelay--;
            }
        }
    }

    @Override
    public List<String> getInfoLines() {
        List<String> lines = super.getInfoLines();
        lines.add("drone-num goal: " + dronesGoal);
        lines.add("spawn-delay: " + spawnDelay);
        return lines;
    }

    private int numDrones() {
        int n = 0;
        for (TPActor a : actors)
            if (a != getPlayer().getActor())
                n++;
        return n;
    }

}
