/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
    http://stackoverflow.com/questions/23602175/regex-for-parsing-chemical-formulas
    
    No parens regex
    String REGEX = "([A-Z][a-z]?)(\\d*)";

    // single level of parens regex
    String REGEX = "([A-Z][a-z]*\\d*|\\([^)]+\\)\\d*)";

    Nested parens regex (not well tested)
    String REGEX = "([A-Z][a-z]?\\d*|\\((?:[^()]*(?:\\(.*\\))?[^()]*)+\\)\\d+)";
*/

package basiccdkapp;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

/**
 *
 * @author Drew Gibson
 */

public class cdkMolecularFormulaParser {
    
    // characters we'll allow in a formula string
    static final String ALLOWEDCHARS = "[^0-9^A-Z^a-z() .]+";
    
    // single level of parens regex
    static final String REGEX = "([A-Z][a-z]*\\d*|\\([^)]+\\)\\d*)";
    
    // list of fragments
    public static IAtomContainer MOL = new AtomContainer();
    
    public static IAtomContainer parseFormula(String formula) {
        
        //formula = "(CH3)3C(CH2)2CHCH3CH2COO.Na";
        //formula = "(CH3)3C(CH2)2CH(CH3)CH2COO.Na";
        //formula = "CHNOS";
        
        //IAtomContainer mol = new AtomContainer();
        
        // check whether 'illegal' characters are present in the formula
        if (containsIllegalChars(formula)) {
            return MOL;
        }
        
        // don't currently support nested parens
        if (countParenNesting(formula) > 1) {
            return MOL;
        }
        
        /*Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(formula);
        
        while (matcher.find()) {
            parseFragment(mol, matcher.group(), 1);
        }*/
        matchFragments(formula, 1);
        
        System.out.printf("MOL atomCount = %d \n", MOL.getAtomCount());
        return MOL;
    }
    
    private static void matchFragments(String formula, Integer multiplier) {
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(formula);
        
        while (matcher.find()) {
            parseFragment(matcher.group(), multiplier);
        }
    }
    
    private static void parseFragment(String fragment, Integer multiplier) {
        if (containsParens(fragment)) {
            //System.out.println("Found paren-containing fragment " + fragment);
            //System.out.printf("Level of paren-nesting = %d \n", countParenNesting(fragment));
            String completeFragment = fragment;
            fragment = stripOuterParens(completeFragment);
            multiplier = getParenMultiplier(completeFragment, multiplier);
            // recursively call matchFragments as the fragment is likely to be complex or nested (eg CH3)
            matchFragments(fragment, multiplier);
        } else {
            System.out.printf("Fragment, multiplier are %s, %d \n", fragment, multiplier);
            String element = getElementFromFragment(fragment);
            Integer multi = getMultiplierFromFragment(fragment);
            
            multi = multi * multiplier;
            
            System.out.printf("element is %s, multiplier is %d \n", element, multi);
            
            for (int i = 0; i < multi; i++) {
                IAtom atom = new Atom(element);
                MOL.addAtom(atom);
            }
            
            showMolecularFormula(MOL);
        }
    }
    
    private static String getElementFromFragment(String fragment) {
        String match = "";
        Pattern pattern = Pattern.compile("[A-Z][a-z]*");
        Matcher matcher = pattern.matcher(fragment);
        while (matcher.find()) {
            match = matcher.group();
        }
        return match;
    }
    
    private static Integer getMultiplierFromFragment(String fragment) {
        String match = "";
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(fragment);
        while (matcher.find()) {
            match = matcher.group();
        }
        if (match == "") {
            match = "1";
        }
        return Integer.parseInt(match);
    }
    
    
    private static String stripOuterParens(String fragment) {
        // create a formula without parens
        String formula = "";
        
        int openBr = fragment.indexOf('(');
        int closeBr = fragment.lastIndexOf(')');
        
        //System.out.printf("Numbers are %d, %d \n", openBr, closeBr);
        
        // get the text between the parens        
        for (int i = openBr+1; i < closeBr; i++) {
            formula = formula + fragment.charAt(i);
        }
        
        //System.out.println("Formula = " + formula);
        return formula;
    }
    
    private static Integer getParenMultiplier(String fragment, Integer multiplier) {
        // capture the multiplier (if present)
        String mult = "";

        int closeBr = fragment.lastIndexOf(')');
        
        //System.out.printf("Mult numbers are %d, %d \n", closeBr, fragment.length());
        
        // get the multiplier
        for (int i = closeBr+1; i < fragment.length(); i++) {
            mult = mult + fragment.charAt(i);
        }
        
        if (mult == "") {
            mult = "0";
        }
        
        //System.out.println("Mult = " + mult);
        
        int intMult = Integer.parseInt(mult);
        if (intMult < 1) {
            intMult = 1;
        }
        
        intMult = intMult * multiplier;

        //System.out.printf("Multiplier = %d \n", intMult);
        return intMult;
    }
    
    private static boolean containsParens(String formula) {
        if (formula.indexOf("(") > -1) {
                return true;
            }
        return false;
    }
    
    private static boolean containsIllegalChars(String formula) {
        boolean result = false;
        Scanner scanner = new Scanner(formula);        
        String validationText = scanner.findInLine(ALLOWEDCHARS);
        if (validationText != null) {
            // Invalid character found.
            result = true;
            //System.out.println("Invalid character: " + validationResult);
        }
        return result;
    }
    
    private static Integer countParenNesting(String formula) {
        int openBr = 0, nest = 0;
        for (int i = 0; i < formula.length(); i++){
            char c = formula.charAt(i);
            switch (c) {
                case '(':   openBr++;
                            if (nest < openBr) {
                                nest = openBr;
                            }
                            break;
                case ')':   openBr--;
                            break;
            }
        }
        return nest;
    }
    
    private static void showMolecularFormula(IAtomContainer molecule) {
        IMolecularFormula mForm = MolecularFormulaManipulator.getMolecularFormula(molecule);
        String mf = MolecularFormulaManipulator.getString(mForm);
        System.out.println("Molecular formula = " + mf + "\n");
    }
}
    
    

