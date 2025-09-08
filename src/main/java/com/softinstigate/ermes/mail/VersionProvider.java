/*-
 * ========================LICENSE_START=================================
 * ermes-mail
 * %%
 * Copyright (C) 2021 - 2025 SoftInstigate srl
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package com.softinstigate.ermes.mail;

import picocli.CommandLine.IVersionProvider;

/**
 * Provides version information to the command line interface.
 * See: https://picocli.info/#_dynamic_version_information
 */
class VersionProvider implements IVersionProvider {

    @Override
    public String[] getVersion() throws Exception {
        return new String[] {
                "@|yellow ErmesMail " + VersionProvider.class.getPackage().getImplementationVersion() + "|@",
                "Command: ${ROOT-COMMAND-NAME}",
                "Picocli: " + picocli.CommandLine.VERSION,
                "JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})",
                "OS: ${os.name} ${os.version} ${os.arch}",
                "@|green Copyright(c) 2022 SoftInstigate srl|@ (https://www.softinstigate.com)"
        };
    }

}
