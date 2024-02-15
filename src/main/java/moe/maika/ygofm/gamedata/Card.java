/*
 * The MIT License
 *
 * Copyright 2024 sg4e.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package moe.maika.ygofm.gamedata;

/**
 * A representation of a card in Forbidden Memories. Use {@link FMDB#getCard(int)} and
 * related methods to obtain card instances. The card instances are immutable and
 * thread-safe.
 * @author sg4e
 */
public class Card {
    private final int id;
    private final String name;
    private final int starchips;
    private final int level;
    private final int attack;
    private final int defense;
    private final GuardianStar firstGuardianStar;
    private final GuardianStar secondGuardianStar;
    private final String description;
    private final String type;
    private final String attribute;
    private final String password;
    //values relevant for sorting
    private final int abcSort;
    private final int maxSort;
    private final int atkSort;
    private final int defSort;
    private final int typeSort;
    private final int aiSort;
    //Japanese sort values
    private final int jpAbcSort;
    private final int jpMaxSort;
    private final int jpAtkSort;
    private final int jpDefSort;
    private final int jpTypeSort;

    Card(int id, String name, String description, GuardianStar firstGuardianStar, GuardianStar secondGuardianStar, int level,
    String type, int attack, int defense, String attribute, String password, int starchips,
    int abcSort, int maxSort, int atkSort, int defSort, int typeSort, int aiSort,
    int jpAbcSort, int jpMaxSort, int jpAtkSort, int jpDefSort, int jpTypeSort) {
        this.id = id;
        this.name = name;
        this.starchips = starchips;
        this.level = level;
        this.attack = attack;
        this.defense = defense;
        this.firstGuardianStar = firstGuardianStar;
        this.secondGuardianStar = secondGuardianStar;
        this.description = description;
        this.type = type;
        this.attribute = attribute;
        this.password = password;
        this.abcSort = abcSort;
        this.maxSort = maxSort;
        this.atkSort = atkSort;
        this.defSort = defSort;
        this.typeSort = typeSort;
        this.aiSort = aiSort;
        this.jpAbcSort = jpAbcSort;
        this.jpMaxSort = jpMaxSort;
        this.jpAtkSort = jpAtkSort;
        this.jpDefSort = jpDefSort;
        this.jpTypeSort = jpTypeSort;
    }

    /**
     * Returns the ID of the card.
     * @return the ID of the card
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the name of the card.
     * @return the name of the card
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the starchip cost of the card.
     * @return the starchip cost of the card
     */
    public int getStarchips() {
        return starchips;
    }

    /**
     * Returns the level of the card.
     * @return the level of the card
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the attack points of the card.
     * @return the attack of the card
     */
    public int getAttack() {
        return attack;
    }

    /**
     * Returns the defense points of the card.
     * @return the defense of the card
     */
    public int getDefense() {
        return defense;
    }

    /**
     * Returns the primary Guardian Star of the card.
     * @return the pirst Guardian Star of the card
     */
    public GuardianStar getFirstGuardianStar() {
        return firstGuardianStar;
    }

    /**
     * Returns the secondary Guardian Star of the card.
     * @return the secondary Guardian Star of the card
     */
    public GuardianStar getSecondGuardianStar() {
        return secondGuardianStar;
    }

    /**
     * Returns the in-game description of the card, with line-breaks preserved.
     * @return the description of the card
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the type of the card. See fields in {@link FMDB} for possible values.
     * @return the type of the card
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the attribute of the card.
     * @return the attribute of the card
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * Returns the password of the card.
     * @return the password of the card
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the ordinal value of this card in the full list of cards when sorted
     * alphabetically.
     * @return the ABC sort value of the card
     */
    public int getAbcSort() {
        return abcSort;
    }

    /**
     * Returns the ordinal value of this card in the full list of cards when sorted
     * by its max sort value.
     * @return the max sort value of the card
     */
    public int getMaxSort() {
        return maxSort;
    }

    /**
     * Returns the ordinal value of this card in the full list of cards when sorted
     * by its attack points.
     * @return the attack sort value of the card
     */
    public int getAtkSort() {
        return atkSort;
    }

    /**
     * Returns the ordinal value of this card in the full list of cards when sorted
     * by its defense points.
     * @return the defense sort value of the card
     */
    public int getDefSort() {
        return defSort;
    }

    /**
     * Returns the ordinal value of this card in the full list of cards when sorted
     * by its type.
     * @return the type sort value of the card
     */
    public int getTypeSort() {
        return typeSort;
    }

    /**
     * Returns the ordinal value of this card in the full list of cards when sorted
     * by its AI sort value.
     * @return the AI sort value of the card
     */
    public int getAiSort() {
        return aiSort;
    }

    /**
     * Returns the ordinal value of this card in the full list of cards when sorted
     * lexicographically in Japanese.
     * @return the Japanese ABC sort value of the card
     */
    public int getJpAbcSort() {
        return jpAbcSort;
    }

    /**
     * Returns the ordinal value of this card in the full list of cards when sorted
     * by its max sort value in Japanese release.
     * @return the Japanese max sort value of the card
     */
    public int getJpMaxSort() {
        return jpMaxSort;
    }

    /**
     * Returns the ordinal value of this card in the full list of cards when sorted
     * by its attack points in Japanese release.
     * @return the Japanese attack sort value of the card
     */
    public int getJpAtkSort() {
        return jpAtkSort;
    }

    /**
     * Returns the ordinal value of this card in the full list of cards when sorted
     * by its defense points in Japanese release.
     * @return the Japanese defense sort value of the card
     */
    public int getJpDefSort() {
        return jpDefSort;
    }

    /**
     * Returns the ordinal value of this card in the full list of cards when sorted
     * by its type in Japanese release.
     * @return the Japanese type sort value of the card
     */
    public int getJpTypeSort() {
        return jpTypeSort;
    }

    /**
     * Returns whether this card can be equipped with the given equip card. The FMDB
     * will be loaded if it has not been already.
     * @return true if this card can be equipped with the given equip card
     */
    public boolean canEquip(Card equip) {
        return FMDB.getInstance().isEquippable(this, equip);
    }

    /**
     * Returns the name of the card
     * @return the name of the card
     */
    @Override
    public String toString() {
        return getName();
    }
}
