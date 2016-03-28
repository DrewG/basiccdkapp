/*
    Basic CDK API use.
    Drew Gibson March 2016.
 */

package basiccdkapp;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import org.openscience.cdk.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

public class BasicCDKApp {

    public static void main (String[] args) {
        try {
            BasicCDKApp obj = new BasicCDKApp();
            obj.run (args);
        }
        catch (Exception e) {
            e.printStackTrace ();
        }
}

// instance variables here

public void run (String[] args) throws Exception {       
        // first, create an instance of an atom
        IAtom atom = new Atom("C");
        
        // now show some atom info
        printHeader("Atom Details");
        printAtomDetails(atom);
        
        // now lets create a molecule - METHANE with explicit hydrogens
        IAtomContainer methane = new AtomContainer();
        methane.addAtom(new Atom("C"));             // mol has atom "C" at index 0
        for(int i = 1; i <= 4; i++) {
            methane.addAtom(new Atom("H"));         // adds 4 H atoms at index 1..4
        }
        
        //System.out.printf("atomcount is %d", mol.getAtomCount());
        
        printHeader("Molecule Details");
        // prove atom indexes are what we thought they were...
        for(int i = 0; i < methane.getAtomCount(); i++) {
            atom = methane.getAtom(i);
            printAtomDetailsWithIndex(atom, i);            
        }
        
        // create bonds - map C atom to all 4 H atoms, creating methane
        for(int i = 1; i <= 4; i++) {
            methane.addBond(0, i, IBond.Order.SINGLE);
        }
        
        // show the molecular formula
        printHeader("Molecular Formula");
        showMolecularFormula(methane);
        
        
        // lets make a salt
        IAtomContainer salt = new AtomContainer();
        IAtom sodium = new Atom("Na");
        sodium.setFormalCharge(+1);
        IAtom chloride = new Atom("Cl");
        chloride.setFormalCharge(-1);
        salt.addAtom(sodium);
        salt.addAtom(chloride);
        
        printHeader("Molecular Formula");
        showMolecularFormula(salt);  // NB uses Hill rules so will look odd !
        
        
        // a little more interesting stuff now...
        // load data in from an SDFile - it's built into the project
        String filename = "sdf/cdktest.sdf";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);

        IteratingSDFReader reader = new IteratingSDFReader(ins, DefaultChemObjectBuilder.getInstance());
        
        int molCount = 0;
        while (reader.hasNext()) {
            Object object = reader.next();
            if (object instanceof IAtomContainer) {
                IAtomContainer m = (IAtomContainer) object;
                showMoleculeProperties(m, ++molCount);
            }
        }
        reader.close(); 
    }
    
    private static void printHeader(String str) {
        System.out.println("################ " + str + " ################");
    }
    
    private static void printAtomDetails(IAtom atom) {
        System.out.printf("Atom symbol and atomic number are %s, %d", atom.getSymbol(), atom.getAtomicNumber());
        System.out.println("\n");
    }
    
    private static void printAtomDetailsWithIndex(IAtom atom, Integer index) {
        System.out.printf("Atom symbol at index %d is %s.", index, atom.getSymbol());
        System.out.println("\n");
    }
    
    private static void showMolecularFormula(IAtomContainer molecule) {
        IMolecularFormula mForm = MolecularFormulaManipulator.getMolecularFormula(molecule);
        String mf = MolecularFormulaManipulator.getString(mForm);
        System.out.println("Molecular formula = " + mf);
        System.out.println("\n");
    }
    
    private static void showMoleculeProperties(IAtomContainer molecule, Integer molCount) {
        System.out.printf("################ SDF Molecule Number %d ################", molCount);
        System.out.println("\n");
        showMolecularFormula(molecule);
        Map map = molecule.getProperties();
        
        // the molecule title from the top of an sdfile record
        System.out.println("Molecule title = " + molecule.getProperty(CDKConstants.TITLE));
        
        // if title exists it can also be picked up from the sdfile properties
        Iterator it = map.keySet().iterator();
        while(it.hasNext()){
            Object key = it.next().toString();
            Object value = map.get(key);
            System.out.printf("Molecule property %s has value %s", key.toString(), value.toString() + "\n");
        }
        System.out.println("\n");
    }    
}
