/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package basiccdkapp;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 *
 * @author drew
 */
public class cdkMolecularFormulaParserTest {
    
    public cdkMolecularFormulaParserTest() {
        testParseFormula();
        testcdkMolecularFormulaParser();
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parseFormula method, of class cdkMolecularFormulaParser.
     */
    @Test
    public void testParseFormula() {
        System.out.println("parseFormula");
        String formula = "";
        IAtomContainer expResult = null;
        IAtomContainer result = cdkMolecularFormulaParser.parseFormula(formula);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
    /**
     * Test of instance creation
     */
    @Test
    public void testcdkMolecularFormulaParser() {
        cdkMolecularFormulaParser mfp = new cdkMolecularFormulaParser();
        Assert.assertNotNull(mfp);
    }
    
}
