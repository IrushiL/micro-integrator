/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.micro.integrator.management.apis;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.task.TaskDescription;
import org.wso2.carbon.inbound.endpoint.internal.http.api.APIResource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.micro.integrator.management.apis.Utils.getQueryParameters;

public class TaskResource extends APIResource {

    private static Log log = LogFactory.getLog(TaskResource.class);

    private static final String ROOT_ELEMENT_TASKS = "<Tasks></Tasks>";
    private static final String COUNT_ELEMENT = "<Count></Count>";
    private static final String LIST_ELEMENT = "<List></List>";

    private static final String ROOT_ELEMENT_TASK = "<Task></Task>";
    private static final String NAME_ELEMENT = "<Name></Name>";
    private static final String TRIGGER_TYPE_ELEMENT = "<TriggerType></TriggerType>";
    private static final String TRIGGER_COUNT_ELEMENT = "<TriggerCount></TriggerCount>";
    private static final String TRIGGER_INTERVAL_ELEMENT = "<TriggerInterval></TriggerInterval>";
    private static final String TRIGGER_CRON_ELEMENT = "<TriggerCron></TriggerCron>";

    public TaskResource(String urlTemplate){
        super(urlTemplate);
    }

    @Override
    public Set<String> getMethods() {
        Set<String> methods = new HashSet<String>();
        methods.add("GET");
        methods.add("POST");
        return methods;
    }

    @Override
    public boolean invoke(MessageContext messageContext) {

        buildMessage(messageContext);
//        log.info("Message : " + messageContext.getEnvelope());

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        List<NameValuePair> queryParameter = getQueryParameters(axis2MessageContext);

        try {
            // if query params exists retrieve data about specific task
            if (null != queryParameter) {
                for (NameValuePair nvPair : queryParameter) {
                    if (nvPair.getName().equals("taskName")) {
                        populateTaskData(messageContext, nvPair.getValue());
                    }
                }
            } else {
                populateTasksList(messageContext);
            }

            axis2MessageContext.removeProperty("NO_ENTITY_BODY");
        } catch (XMLStreamException e) {
            log.error("Error occurred while processing response", e);
        }
        return true;
    }

    private void populateTasksList(MessageContext messageContext) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        SynapseConfiguration configuration = messageContext.getConfiguration();

        String[] taskNames = configuration.getTaskManager().getTaskNames();

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_TASKS);
        OMElement countElement = AXIOMUtil.stringToOM(COUNT_ELEMENT);
        OMElement listElement = AXIOMUtil.stringToOM(LIST_ELEMENT);

        countElement.setText(String.valueOf(taskNames.length));
        rootElement.addChild(countElement);

        rootElement.addChild(listElement);

        for (String taskName : taskNames) {

            OMElement taskElement = getTaskByName(messageContext, taskName);
            listElement.addChild(taskElement);
        }
        axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
    }

    private void populateTaskData(MessageContext messageContext, String taskName) throws XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        OMElement rootElement = getTaskByName(messageContext, taskName);

        if (null != rootElement) {
            axis2MessageContext.getEnvelope().getBody().addChild(rootElement);
        } else {
            axis2MessageContext.setProperty("HTTP_SC", "404");
        }
    }

    private OMElement getTaskByName(MessageContext messageContext, String taskName) throws XMLStreamException {

        SynapseConfiguration configuration = messageContext.getConfiguration();

        String []taskNames = configuration.getTaskManager().getTaskNames();
        for (String task : taskNames) {
            if (task.equals(taskName)) {
                return convertTaskToOMElement(configuration.getTaskManager().getTask(taskName));
            }
        }
        return null;
    }

    private OMElement convertTaskToOMElement(TaskDescription task) throws XMLStreamException{

        if (null == task) {
            return null;
        }

        OMElement rootElement = AXIOMUtil.stringToOM(ROOT_ELEMENT_TASK);
        OMElement nameElement = AXIOMUtil.stringToOM(NAME_ELEMENT);
        OMElement triggerTypeElement = AXIOMUtil.stringToOM(TRIGGER_TYPE_ELEMENT);
        OMElement triggerCountElement = AXIOMUtil.stringToOM(TRIGGER_COUNT_ELEMENT);
        OMElement triggerIntervalElement = AXIOMUtil.stringToOM(TRIGGER_INTERVAL_ELEMENT);
        OMElement triggerCronElement = AXIOMUtil.stringToOM(TRIGGER_CRON_ELEMENT);

        nameElement.setText(task.getName());
        rootElement.addChild(nameElement);

        String triggerType = "cron";

        if (null == task.getCronExpression()) {
            triggerType = "simple";
        }

        triggerTypeElement.setText(triggerType);
        rootElement.addChild(triggerTypeElement);

        triggerCountElement.setText(String.valueOf(task.getCount()));
        rootElement.addChild(triggerCountElement);

        triggerIntervalElement.setText(String.valueOf(task.getInterval()));
        rootElement.addChild(triggerIntervalElement);

        triggerCronElement.setText(task.getCronExpression());
        rootElement.addChild(triggerCronElement);

        return rootElement;
    }
}
