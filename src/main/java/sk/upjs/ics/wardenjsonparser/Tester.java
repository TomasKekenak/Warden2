/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.upjs.ics.wardenjsonparser;

/**
 *
 * @author tomas
 */
public class Tester {
    public static void main(String[] args) {
        boolean b = EventParserScanner.isNumberOrBoolean("1.2");
        System.out.println(b);
    }
}
