# 动态跳转

```java
package me.mofeng.engine.activiti.cmd;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ExecutionEntityManager;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;

import java.util.List;

public class DynamicJumpCmd implements Command<Void> {

    protected String processInstanceId;

    protected String fromNodeId;

    protected String toNodeId;

    public DynamicJumpCmd(String processInstanceId, String fromNodeId, String toNodeId) {
        this.processInstanceId = processInstanceId;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
    }

    @Override
    public Void execute(CommandContext commandContext) {

        if (this.processInstanceId == null) {
            throw new ActivitiIllegalArgumentException("Process instance id is required");
        }

        //
        ExecutionEntityManager executionEntityManager = commandContext.getExecutionEntityManager();
        ExecutionEntity execution = executionEntityManager.findById(this.processInstanceId);

        if (execution == null) {
            throw new ActivitiException("Execution could not be found with id " + this.processInstanceId);
        }

        if (!execution.isProcessInstanceType()) {
            throw new ActivitiException("Execution is not a process instance type execution for id "
                                        + this.processInstanceId);
        }

        ExecutionEntity activeExecutionEntity = null;
        //获取所有的子执行实例
        List<ExecutionEntity> childExecutions = executionEntityManager.findChildExecutionsByParentExecutionId(execution.getId());
        for (ExecutionEntity childExecution : childExecutions) {
            if (childExecution.getCurrentActivityId().equalsIgnoreCase(this.fromNodeId)) {
                activeExecutionEntity = childExecution;
                break;
            }
        }
        if (activeExecutionEntity == null) {
            throw new ActivitiException("Active execution could not be found with activity id " + this.fromNodeId);
        }

        // 获取流程模型
        BpmnModel model = ProcessDefinitionUtil.getBpmnModel(execution.getProcessDefinitionId());

        //当前节点和目标节点
        FlowElement fromActivityElement = model.getFlowElement(this.fromNodeId);
        if (fromActivityElement == null) {
            throw new ActivitiException("Activity could not be found in process definition for id " + this.fromNodeId);
        }
        FlowElement toActivityElement = model.getFlowElement(this.toNodeId);
        if (toActivityElement == null) {
            throw new ActivitiException("Activity could not be found in process definition for id " + this.toNodeId);
        }

        boolean deleteParentExecution = false;
        ExecutionEntity parent = activeExecutionEntity.getParent();
        //兼容子流程节点的情况
        if ((fromActivityElement.getSubProcess() != null) && (!toActivityElement.getSubProcess()
                .getId()
                .equalsIgnoreCase(parent.getActivityId()))) {
            deleteParentExecution = true;
        }

        //删除当前节点所在的执行实例与相关数据
        executionEntityManager.deleteExecutionAndRelatedData(activeExecutionEntity,
                "Change acitivity to " + this.toNodeId,
                false);
        // 子流程节点，删除其所在的执行实例与相关数据
        if (deleteParentExecution) {
            executionEntityManager.deleteExecutionAndRelatedData(activeExecutionEntity,
                    "Change acitivity to " + this.toNodeId,
                    false);
        }

        //创建当前流程实例的子执行实例
        ExecutionEntity newChildExecution = executionEntityManager.createChildExecution(execution);
        newChildExecution.setCurrentFlowElement(toActivityElement);

        commandContext.getAgenda().planContinueProcessOperation(newChildExecution);
        return null;
        
    }
}


```