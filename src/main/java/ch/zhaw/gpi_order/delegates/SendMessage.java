/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.zhaw.gpi_order.delegates;

import java.util.List;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Message;

/**
 *
 * @author kell
 */
public class SendMessage implements JavaDelegate {

    @Override
    public void execute(DelegateExecution de) throws Exception {
        
        Integer orderId = (Integer) de.getVariable("orderId");

        // get message name via BPMN API
        BpmnModelInstance modelInstance = de.getBpmnModelInstance();
        Message msg = modelInstance.getModelElementById("Message_0lcd1om");
        String msgName = msg.getName();
        System.out.println("gpi_order: SendMessage: Message Name: " + msgName);
        //msgName = msgName.replace("${orderId}", orderId.toString());
        //System.out.println("SendMessage: Altered Message Name: " + msgName);
        
        //querying message subscriptions
        List<Execution> executions = de.getProcessEngineServices()
                .getRuntimeService()
                .createExecutionQuery()
                .processVariableValueEquals("orderId", orderId)
                .list();
        for (Execution execution : executions){
            System.out.println("gpi_order: SendMessage: Query result: " + execution.getId() + " is ended: " + execution.isEnded());
        }

        System.out.println("gpi_order: SendMessage: OrderId: "+orderId.toString());
        try{
            MessageCorrelationResult result =  de.getProcessEngineServices()
                    .getRuntimeService()
                    .createMessageCorrelation(msgName)
                    .processInstanceVariableEquals("orderId", orderId)
                    .correlateWithResult();
            Execution exec = result.getExecution();
            System.out.println("gpi_order: SendMessage: Correlation result: " + exec.getId());
            de.getProcessEngineServices().getRuntimeService().messageEventReceived(msgName, exec.getId());
        } catch (Exception ex) {
            System.out.println("gpi_order: SendMessage: sending message failed");
            System.out.println(ex.getMessage());
        }

        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
