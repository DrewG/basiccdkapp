/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package basiccdkapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 *
 * @author Drew Gibson
 */

public class cdkMolecularFormulaParser {
    
    static final String ALLOWEDCHARS = "[^0-9^A-Z^a-z() .]+";
    
    public static IAtomContainer parseFormula(String formula) {
        // http://stackoverflow.com/questions/23602175/regex-for-parsing-chemical-formulas
        
        /*
            No parens regex
            String regex = "([A-Z][a-z]?)(\\d*)";
        */
        /*
            Nested parens regex (not tested)
            String regex = "([A-Z][a-z]?\\d*|\\((?:[^()]*(?:\\(.*\\))?[^()]*)+\\)\\d+)";
        */
        
        // check whether 'illegal' characters are present in the formula
        if (containsIllegalChars(formula)) {
            return null;
        }
        
        // don't currently support nested parens
        if (countParenNesting(formula) > 1) {
            return null;
        }
        
        // single level of parens regex
        String regex = "([A-Z][a-z]*\\d*|\\([^)]+\\)\\d*)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(formula);
        
        IAtomContainer mol = new AtomContainer();
        
        while (matcher.find()) {
            parseFragment(mol, matcher.group(), 1);
        }
        
        return mol;
    }
    
    private static void parseFragment(IAtomContainer mol, String fragment, Integer multiplier) {
        if (containsParens(fragment)) {
            System.out.println("Found paren-containing fragment " + fragment);
            System.out.printf("Level of paren-nesting = %d \n", countParenNesting(fragment));
            fragment = stripParens(fragment, multiplier);
        }
    }
    
    private static String stripParens(String fragment, Integer multiplier) {
        // create a formula without parens, and capture the multiplier (if present)
        System.out.println("got here");
        
        String formula = "", mult = "";
        
        int openBr = fragment.indexOf('(');
        int closeBr = fragment.indexOf(')');
        
        System.out.printf("Numbers are %d, %d \n", openBr, closeBr);
        
        int intMult = Integer.parseInt(mult);
        
        for (int i = openBr; i <= closeBr; i++) {
            char c = fragment.charAt(i);
            formula = formula + c;
        }
        System.out.println(formula);
        return formula;
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
        String validationResult = scanner.findInLine(ALLOWEDCHARS);
        if (validationResult != null) {
            // Invalid character found.
            result = true;
            System.out.println("Invalid character: " + validationResult);
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
}
    
    

