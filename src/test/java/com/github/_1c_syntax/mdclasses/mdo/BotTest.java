/*
 * This file is a part of MDClasses.
 *
 * Copyright © 2019 - 2021
 * Tymko Oleg <olegtymko@yandex.ru>, Maximov Valery <maximovvalery@gmail.com> and contributors
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * MDClasses is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * MDClasses is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with MDClasses.
 */
package com.github._1c_syntax.mdclasses.mdo;

import com.github._1c_syntax.mdclasses.metadata.additional.MDOType;
import com.github._1c_syntax.mdclasses.metadata.additional.ModuleType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BotTest extends AbstractMDOTest {

    BotTest() {
        super(MDOType.BOT);
    }

    @Override
    @Test
    void testEDT() {
        var mdo = getMDObjectEDT("Bots/Бот1/Бот1.mdo");
        checkBaseField(mdo, Bot.class, "Бот1",
                "89c58e6a-00ee-49b9-8717-d1dd272f9b96");

        checkNoChildren(mdo);
        checkModules(((MDObjectBSL) mdo).getModules(), 1,
                "Bots/Бот1", ModuleType.BotModule);
        var bot = (Bot) mdo;
        assertThat(bot.isPredefined()).isTrue();
    }

    @Override
    @Test
    void testDesigner() {
        var mdo = getMDObjectDesigner("Bots/Бот1.xml");
        checkBaseField(mdo, Bot.class, "Бот1",
                "89c58e6a-00ee-49b9-8717-d1dd272f9b96");

        checkNoChildren(mdo);
        checkModules(((MDObjectBSL) mdo).getModules(), 1,
                "Bots/Бот1", ModuleType.BotModule);
        var bot = (Bot) mdo;
        assertThat(bot.isPredefined()).isTrue();
    }
}