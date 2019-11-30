/*
    Basic CDK API use.
    Drew Gibson March 2016.
 */

package basiccdkapp;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.openscience.cdk.*;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.formula.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.templates.MoleculeFactory;
// next one added for fingerprints Nov 2019
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.fingerprint.IFingerprinter;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.fingerprint.ShortestPathFingerprinter;

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
    
        // for fingerprints
        IAtomContainer fpMolecule = new AtomContainer();
        IAtomContainer fpMoleculeCompare = new AtomContainer();
    
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
        //String filename = "sdf/cdktest.sdf";
        String filename = "sdf/cdktest_noH.sdf";
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream(filename);

        IteratingSDFReader reader = new IteratingSDFReader(ins, DefaultChemObjectBuilder.getInstance());
        
        int molCount = 0;
        while (reader.hasNext()) {
            Object object = reader.next();
            if (object instanceof IAtomContainer) {
                IAtomContainer m = (IAtomContainer) object;
                showMoleculeProperties(m, ++molCount);
                // going to grab SDF molecule 7 to calculate fingerprints
                if (molCount == 6) {
                    fpMoleculeCompare = m;
                }
                if (molCount == 7) {
                    fpMolecule = m;
                }
            }
        }
        reader.close();
        
        // now do some substructure searching
        printHeader("Substructure Searching");
        IAtomContainer propane = MoleculeFactory.makeAlkane(3);
        IAtomContainer butane = MoleculeFactory.makeAlkane(4);
        UniversalIsomorphismTester isomorphismTester = 
                new UniversalIsomorphismTester();
        System.out.println("Propane part of butane: " + 
                isomorphismTester.isSubgraph(butane, propane));
        System.out.println("Butane part of propane: " + 
                isomorphismTester.isSubgraph(propane, butane));
        
        List hits = isomorphismTester.getSubgraphAtomsMaps(butane, propane);
        System.out.printf("Number of hits: %d", hits.size());
        System.out.println(" (done in both directions)");
        System.out.println();
        
        
        /*
            This class generates molecular formulas within given mass range and
            elemental composition. There is no guaranteed order in which the
            formulas are generated. Usage:
        */
        printHeader("Mass to Molecular Formula");
        IsotopeFactory ifac = Isotopes.getInstance();
        IIsotope c = ifac.getMajorIsotope("C");
        IIsotope h = ifac.getMajorIsotope("H");
        IIsotope n = ifac.getMajorIsotope("N");
        IIsotope o = ifac.getMajorIsotope("O");
        IIsotope p = ifac.getMajorIsotope("P");
        IIsotope s = ifac.getMajorIsotope("S");

        MolecularFormulaRange mfRange = new MolecularFormulaRange();
        mfRange.addIsotope(c, 32, 50);
        mfRange.addIsotope(h, 10, 80);
        mfRange.addIsotope(o, 1, 28);
        mfRange.addIsotope(n, 1, 10);
        //mfRange.addIsotope(p, 0, 10);
        //mfRange.addIsotope(s, 0, 10);
       
        double mass = 921.385;
        double delta = 0.001;
        double minMass = mass - delta;
        double maxMass = mass + delta;

        DefaultChemObjectBuilder builder = (DefaultChemObjectBuilder) DefaultChemObjectBuilder.getInstance();
        MolecularFormulaGenerator mfg = new MolecularFormulaGenerator(builder,minMass, maxMass, mfRange);
        IMolecularFormulaSet mfSet = mfg.getAllFormulas();

        for (int i = 0; i < mfSet.size(); i++) {
            IMolecularFormula mForm = mfSet.getMolecularFormula(i);
            String mf = MolecularFormulaManipulator.getString(mForm);
            IsotopePatternGenerator ipg =  new IsotopePatternGenerator(0.0005);
            IsotopePattern ip = ipg.getIsotopes(mForm);
            
            for (int j = 0; j < ip.getNumberOfIsotopes(); j++) {
                IsotopeContainer ic = ip.getIsotope(j);
                ip.setCharge(1.0);
                System.out.printf("Molecular formula = %s, IP = %f, Int = %f \n", mf, ic.getMass(), ic.getIntensity());
            }
        }
        System.out.println("");
        
        // Molecular Formula Parsing
        String formula = "(CH3)3C(CH2)2CH(CH3)CH2COO.Na";
        printHeader("Molecular Formula Parsing - " + formula);
        IAtomContainer myAC = cdkMolecularFormulaParser.parseFormula(formula);
        
        /*
            Now for some fingerprints stuff.  Nov 2019.
            fpMolecule was grabbed from the SDF earlier, and was molCount = 7
            fpMoleculeCompare was grabbed from the SDF earlier, and was molCount = 6
        */
        printHeader("FingerPrints");
        showMoleculeProperties(fpMoleculeCompare, 6);
        showMoleculeProperties(fpMolecule, 7);
        
        /*
            from the javadoc...
                It is recommended to use atomtyped container before generating
                the fingerprints.  For example
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(atomContainer);
        */
        
        // sets implicit H's among other things
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(fpMolecule);
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(fpMoleculeCompare);
        
        /*
            also from the javadoc...
            "The FingerPrinter assumes that hydrogens are explicitly given!"
        */
        
        printHeader("Fingerprints - Before Explicit H's");
        printDescInt("atomCount", fpMolecule.getAtomCount());
        printDescInt("ImplicitHCount", AtomContainerManipulator.getImplicitHydrogenCount(fpMolecule));
        printDescInt("TotalHCount", AtomContainerManipulator.getTotalHydrogenCount(fpMolecule));
        
        // convert implicit H to explicit H
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(fpMolecule);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(fpMoleculeCompare);
        
        printHeader("Fingerprints - After Explicit H's");
        printDescInt("atomCount", fpMolecule.getAtomCount());
        printDescInt("ImplicitHCount", AtomContainerManipulator.getImplicitHydrogenCount(fpMolecule));
        printDescInt("TotalHCount", AtomContainerManipulator.getTotalHydrogenCount(fpMolecule));
        
        // now generate the fingerprint
        IFingerprinter fingerprinter = new ShortestPathFingerprinter();  // 1024 BY DEFAULT
        IBitFingerprint fingerprint = fingerprinter.getBitFingerprint(fpMolecule);
        IBitFingerprint fingerprintCompare = fingerprinter.getBitFingerprint(fpMoleculeCompare);
        printDescInt("Fingerprint Size", (int)fingerprint.size()); // returns 1024 by default
        printDescInt("No. of fingerprint bits on", fingerprint.cardinality());
        printDescInt("No. of fingerprintCompare bits on", fingerprintCompare.cardinality());
        
        /*
            simple Tanimoto calculation using IBitFingerprint methods...
            
            sim = num set bits the same in both (AND) / num total set bits in both (OR)
        */
        
        /*
            NB !!! This modifies the fingerprint !!!
            You CANNOT use 
                fingerprint.and(fingerprintCompare);
            followed by
                fingerprint.or(fingerprintCompare);
                because fingerprint is DIFFERENT after the first 
         */
        
        // doing it properly
        printHeader("Fingerprints - Tanimoto Calculation");
        
        // create 2 copies of the query for AND / OR comparison
        IBitFingerprint fingerprint_OR = fingerprinter.getBitFingerprint(fpMolecule);
        IBitFingerprint fingerprint_AND = fingerprinter.getBitFingerprint(fpMolecule);
        IBitFingerprint fingerprint_Compare = fingerprinter.getBitFingerprint(fpMoleculeCompare);
        
        fingerprint_AND.and(fingerprintCompare);
        fingerprint_OR.or(fingerprintCompare);
        
        int tanimoto_Or = fingerprint_OR.cardinality();
        int tanimoto_And = fingerprint_AND.cardinality();
        
        printDescInt("No. of fingerprint bits on", fingerprint.cardinality());
        printDescInt("No. of fingerprint_OR bits on", tanimoto_Or);
        printDescInt("No. of fingerprint_AND bits on", tanimoto_And);
        printDescInt("No. of fingerprintCompare bits on", fingerprintCompare.cardinality());
        
        double Similarity = (1.0 * tanimoto_And) / tanimoto_Or;
        
        // print to 3 decimal places
        System.out.printf("Similarity = %.3f%n", Similarity);
        
}
    
    private static void printHeader(String str) {
        System.out.println("################ " + str + " ################");
    }
    
    private static void printDescInt(String str, Integer i) {
        System.out.printf("Description: " + str + "; %d", i);
        System.out.println("\n");
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
