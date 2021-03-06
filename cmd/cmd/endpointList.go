/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package cmd

import (
    "github.com/spf13/cobra"
    "github.com/wso2/micro-integrator/cmd/utils"
)

// List Endpoints command related usage info
const listEndpointsCmdLiteral = "endpoints"

// endpointsListCmd represents the list endpoints command
var endpointsListCmd = &cobra.Command{
    Use:   listEndpointsCmdLiteral,
    Short: showEndpointCmdShortDesc,
    Long:  showEndpointCmdLongDesc + showEndpointCmdExamples,
    Run: func(cmd *cobra.Command, args []string) {
        // defined in endpointInfo.go
        handleEndpointCmdArguments(args)
    },
}

func init() {
    showCmd.AddCommand(endpointsListCmd)
    endpointsListCmd.SetHelpTemplate(showEndpointCmdLongDesc + utils.GetCmdUsage(programName, showCmdLiteral, 
        showEndpointCmdLiteral, "[endpointname]") + showEndpointCmdExamples + utils.GetCmdFlags("endpoint(s)"))
}
