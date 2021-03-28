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
import com.github._1c_syntax.mdclasses.metadata.additional.MessageDirection;
import com.github._1c_syntax.mdclasses.metadata.additional.ModuleType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class IntegrationServiceTest extends AbstractMDOTest {

    IntegrationServiceTest() {
        super(MDOType.INTEGRATION_SERVICE);
    }

    @Override
    @Test
    void testEDT() {
        var mdo = getMDObjectEDT("IntegrationServices/СервисИнтеграции1/СервисИнтеграции1.mdo");
        var service = (IntegrationService) mdo;
        checkBaseField(mdo, IntegrationService.class, "СервисИнтеграции1",
                "94ed2401-fd3c-4e92-b34d-1cdad2d8ee42");
        checkNoChildren(mdo);
        checkModules(service.getModules(), 1, "IntegrationServices/СервисИнтеграции1",
                ModuleType.IntegrationServiceModule);
        assertThat(service.getIntegrationChannels())
                .hasSize(2)
                .extracting(IntegrationServiceChannel::getReceiveMessageProcessing)
                .anyMatch("КаналСервисаИнтеграции1ОбработкаПолученияСообщения"::equals);
        assertThat(service.getExternalIntegrationServiceAddress()).isNotBlank();
        service.getIntegrationChannels().forEach(channel -> {
            checkChild("IntegrationService.СервисИнтеграции1", MDOType.INTEGRATION_SERVICE_CHANNEL,
                    ModuleType.UNKNOWN, channel);
            assertThat(channel.getExternalIntegrationServiceChannelName()).isNotBlank();
            assertThat(channel.getMessageDirection()).isBetween(MessageDirection.SEND, MessageDirection.RECEIVE);
        });
    }

    @Override
    @Test
    void testDesigner() {
        var mdo = getMDObjectDesigner("IntegrationServices/СервисИнтеграции1.xml");
        var service = (IntegrationService) mdo;
        checkBaseField(mdo, IntegrationService.class, "СервисИнтеграции1",
                "94ed2401-fd3c-4e92-b34d-1cdad2d8ee42");
        checkNoChildren(mdo);
        checkModules(service.getModules(), 1, "IntegrationServices/СервисИнтеграции1",
                ModuleType.IntegrationServiceModule);
        assertThat(service.getIntegrationChannels())
                .hasSize(2)
                .extracting(IntegrationServiceChannel::getReceiveMessageProcessing)
                .anyMatch("КаналСервисаИнтеграции1ОбработкаПолученияСообщения"::equals);
        assertThat(service.getExternalIntegrationServiceAddress()).isNotBlank();
        service.getIntegrationChannels().forEach(channel -> {
            checkChild("IntegrationService.СервисИнтеграции1", MDOType.INTEGRATION_SERVICE_CHANNEL,
                    ModuleType.UNKNOWN, channel);
            assertThat(channel.getExternalIntegrationServiceChannelName()).isNotBlank();
            assertThat(channel.getMessageDirection()).isBetween(MessageDirection.SEND, MessageDirection.RECEIVE);
        });
    }
}