/*
 * The MIT License
 *
 * Copyright 2020 sg4e.
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
package sg4e.ygofm.gamedata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * @author sg4e
 */
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class Card {
    @JsonProperty("CardId")
    private int id;
    @JsonProperty("CardName")
    private String name;
    @JsonProperty("StarchipCost")
    private int starchips;
    private int level, attack, defense;
    @JsonProperty("GuardianStar1")
    private GuardianStar firstGuardianStar;
    @JsonProperty("GuardianStar2")
    private GuardianStar secondGuardianStar;
    private String description, type, attribute, password;
    //values relevant for sorting
    //UpperCamelCaseStrategy doesn't figure these names out for some reason; might be a rosetta bug
    @Getter(AccessLevel.PACKAGE)
    @JsonProperty("AbcSort")
    private int abcSort;
    @Getter(AccessLevel.PACKAGE)
    @JsonProperty("MaxSort")
    private int maxSort;
    @Getter(AccessLevel.PACKAGE)
    @JsonProperty("AtkSort")
    private int atkSort;
    @Getter(AccessLevel.PACKAGE)
    @JsonProperty("DefSort")
    private int defSort;
    @Getter(AccessLevel.PACKAGE)
    @JsonProperty("TypeSort")
    private int typeSort;
    @Getter(AccessLevel.PACKAGE)
    @JsonProperty("AiSort")
    private int aiSort;
    
    public boolean canEquip(Card equip, FMDB db) {
        return db.isEquippable(this, equip);
    }
}
