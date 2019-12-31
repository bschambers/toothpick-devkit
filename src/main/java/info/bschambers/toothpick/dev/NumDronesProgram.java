package info.bschambers.toothpick.dev;

import info.bschambers.toothpick.MaintainDronesNum;
import info.bschambers.toothpick.TPProgram;
import info.bschambers.toothpick.ToothpickPhysics;
import info.bschambers.toothpick.actor.TPActor;
import info.bschambers.toothpick.actor.TPFactory;
import info.bschambers.toothpick.geom.Pt;
import java.util.function.Function;

/**
 * <p>Program maintains a set number of drones, spawning a new one after a drone is
 * killed.</p>
 *
 * <p>New drones a spawned from a list of actor-factory methods.</p>
 */
public class NumDronesProgram extends TPProgram {

    private MaintainDronesNum numBehaviour = new MaintainDronesNum();

    public NumDronesProgram() {
        addBehaviour(new ToothpickPhysics());
        addBehaviour(numBehaviour);
        setPlayer(TPFactory.playerLine(new Pt(getGeometry().getXCenter(),
                                              getGeometry().getYCenter())));
    }

    public NumDronesProgram(String title) {
        this();
        setTitle(title);
    }

    public int getDronesGoal() {
        return numBehaviour.getDronesGoal();
    }

    public void setDronesGoal(int val) {
        numBehaviour.setDronesGoal(val);
    }

    public void setDroneFunc(Function<TPProgram, TPActor> func) {
        numBehaviour.setDroneFunc(func);
    }

}
