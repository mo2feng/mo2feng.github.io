# 动态跳转

动态跳转是本土化的流程审批的场景之一，要求流程能在节点间灵活跳转（既能跳转到已经执行的节点，也能跳转到未执行的节点）。

## 实现

Activiti/Flowable 原生不支持动态跳转，需要自行实现。
工作流引擎所有操作都使用命令模式，使用命令执行器执行命令。

核心逻辑就是 
* 先查询当前节点所在的流程实例和目标节点，删除当前节点所在的流程实例及相关数据；
* 然后创建当前流程实例的新执行实例，并设置该新执行实例的当前节点为要跳转的目标节点；
* 最后江当前执行实例添加到DefaultActivitiEngineAgenda类持有的操作链表operations中,流程引擎在运转过程中会从该链表中通过poll() 方法去除每一个操作并执行命令。

动态跳转Command命令类如下（以Activiti为例，Flowbale同理）：

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