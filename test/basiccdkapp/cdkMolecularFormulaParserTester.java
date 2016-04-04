/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package basiccdkapp;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author drew
 */
public class cdkMolecularFormulaParserTester {
    @Test
    public void testcdkMolecularFormulaParser() {
        cdkMolecularFormulaParser mfp = new cdkMolecularFormulaParser();
        Assert.assertNotNull(mfp);
    }
}
