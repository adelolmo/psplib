package org.ado.psplib.scancontent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Andoni del Olmo
 * @since 25.08.17
 */
public class GameIdGeneratorTest {

    @Test
    public void colonToId() throws Exception {
        assertEquals("naruto-shippuden-ultimate-ninja-impact",
                GameIdGenerator.toId("Naruto Shippuden: Ultimate Ninja Impact"));
        assertEquals("naruto-shippuden-ultimate-ninja-impact",
                GameIdGenerator.toId("Naruto.Shippuden.Ultimate.Ninja.Impact"));
    }

    @Test
    public void colonAndSpaceToId() throws Exception {
        assertEquals("the-lord-of-the-rings-aragorns-quest",
                GameIdGenerator.toId("The Lord of the Rings: Aragorn's Quest"));
        assertEquals("the-lord-of-the-rings-aragorns-quest",
                GameIdGenerator.toId("The.Lord.of.the.Rings:Aragorns.Quest"));
    }

    @Test
    public void semicolonToId() throws Exception {
        assertEquals("title-with-semicolon-ready",
                GameIdGenerator.toId("Title with semicolon; Ready"));
        assertEquals("title-with-semicolon-ready",
                GameIdGenerator.toId("Title.with.semicolon;Ready"));
    }

    @Test
    public void apostropheToId() throws Exception {
        assertEquals("carol-vordermans-sudoku",
                GameIdGenerator.toId("Carol Vorderman's Sudoku"));
        assertEquals("carol-vordermans-sudoku",
                GameIdGenerator.toId("Carol.Vordermans.Sudoku"));
    }

    @Test
    public void dashToId() throws Exception {
        assertEquals("x-men-origins-wolverine",
                GameIdGenerator.toId("X-Men.Origins.Wolverine"));
        assertEquals("x-men-origins-wolverine",
                GameIdGenerator.toId("X-Men Origins: Wolverine"));
    }

    @Test
    public void dashWithSpaceToId() throws Exception {
        assertEquals("call-of-duty-roads-to-victory",
                GameIdGenerator.toId("Call of Duty: Roads to Victory"));
        assertEquals("call-of-duty-roads-to-victory",
                GameIdGenerator.toId("Call of Duty - Roads to Victory"));
    }

    @Test
    public void exclamationMarkInTheMiddleToId() throws Exception {
        assertEquals("sid-meiers-pirates-ops",
                GameIdGenerator.toId("Sid Meier's Pirates! Ops"));
        assertEquals("sid-meiers-pirates-ops",
                GameIdGenerator.toId("Sid.Meiers.Pirates!Ops"));
    }

    @Test
    public void exclamationMarkInTheEndToId() throws Exception {
        assertEquals("sid-meiers-pirates",
                GameIdGenerator.toId("Sid Meier's Pirates!"));
        assertEquals("sid-meiers-pirates",
                GameIdGenerator.toId("Sid.Meiers.Pirates!"));
    }

    @Test
    public void questionMarkInTheMiddleToId() throws Exception {
        assertEquals("question-mark-yes-please",
                GameIdGenerator.toId("Question Mark? Yes Please"));
        assertEquals("question-mark-yes-please",
                GameIdGenerator.toId("Question.Mark?Yes.Please"));
    }

    @Test
    public void questionMarkInTheEndToId() throws Exception {
        assertEquals("question-mark",
                GameIdGenerator.toId("Question Mark?"));
        assertEquals("question-mark",
                GameIdGenerator.toId("Question.Mark?"));
    }

    @Test
    public void dotsToId() throws Exception {
        assertEquals("u-s-navy-seals-fireteam-bravo-2",
                GameIdGenerator.toId("U.S. Navy SEALs Fireteam Bravo 2"));
        assertEquals("u-s-navy-seals-fireteam-bravo-2",
                GameIdGenerator.toId("U.S..Navy.Seals.Fireteam.Bravo.2"));
    }

    @Test
    public void ampersandToId() throws Exception {
        assertEquals("asterix-and-obelix-xxl-2-mission-wifix",
                GameIdGenerator.toId("Asterix & Obelix XXL 2: Mission: Wifix"));
        assertEquals("asterix-and-obelix-xxl-2-mission-wifix",
                GameIdGenerator.toId("Asterix.&.Obelix.XXL.2:.Mission:.Wifix"));
    }

    @Test
    public void romanInTheMiddleToId() throws Exception {
        assertEquals("final-fantasy-5-two-collection",
                GameIdGenerator.toId("Final Fantasy V: Two Collection"));
    }

    @Test
    public void withDotsRomanToId() throws Exception {
        assertEquals("final-fantasy-5-two-collection",
                GameIdGenerator.toId("Final.Fantasy.V.Two.Collection"));
    }

    @Test
    public void moreRomanInTheMiddleToId() throws Exception {
        assertEquals("ace-combat-6-skies-of-deception",
                GameIdGenerator.toId("Ace Combat VI Skies of Deception"));
    }

    @Test
    public void withDotsMoreRomanToId() throws Exception {
        assertEquals("ace-combat-6-skies-of-deception",
                GameIdGenerator.toId("Ace.Combat.VI.Skies.of.Deception"));
    }

    @Test
    public void romanEndToId() throws Exception {
        assertEquals("valkyria-chronicles-5",
                GameIdGenerator.toId("Valkyria Chronicles V"));
    }

    @Test
    public void withDotsRomanEndToId() throws Exception {
        assertEquals("valkyria-chronicles-5",
                GameIdGenerator.toId("Valkyria.Chronicles.V"));
    }

    @Test
    public void xMenToId() throws Exception {
        assertEquals("x-men-legends-6-rise-of-apocalypse",
                GameIdGenerator.toId("X-Men Legends VI: Rise of Apocalypse"));
    }

}