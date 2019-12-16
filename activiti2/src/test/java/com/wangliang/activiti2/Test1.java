package com.wangliang.activiti2;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @Author ydwf
 * @Date 2019/12/16 16:12
 * @Version 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Activiti2Application.class)
public class Test1 {
    @Autowired
    private ProcessEngineConfiguration processEngineConfiguration;
    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;
    //部署流程
    @Test
    public void deploy(){
        Deployment deploy = repositoryService.createDeployment().addClasspathResource("bpmn/RestProcess.bpmn").deploy();
        System.out.println("请假流程部署完成,流程ID："+deploy.getId()+",流程名称："+deploy.getName());
    }
    //启动流程实例
    @Test
    public void startProcess(){
        ProcessInstance restProcess = runtimeService.startProcessInstanceByKey("RestProccess");
        System.out.println("请假流程实例已经启动，实例ID："+restProcess.getId()+",BusinessKey:"+restProcess.getBusinessKey());
    }
    //执行申请请假任务
    @Test
    public void excuteTask(){
        String taskId = "37504";
        taskService.setVariable(taskId,"day",18);
        taskService.complete(taskId);
        System.out.println("37504任务执行完成!");
    }
    //获取当前流程实例流程图
    @Test
    public void getProcessImage() throws IOException {
        String excuteId = "37501";
        //解决生成图片中文乱码
        processEngineConfiguration.setActivityFontName("SimSun");
        processEngineConfiguration.setLabelFontName("SimSun");
        //获取流程实例
        ProcessInstance processInstance =
                runtimeService.createProcessInstanceQuery().processInstanceId(excuteId).singleResult();
        //根据流程实例ID获取bpmnModel
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        //获取流程实例
        ProcessDefinitionEntity processDefinition =
                (ProcessDefinitionEntity) repositoryService.createProcessDefinitionQuery()
                        .processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
        //获取活动的节点
        List<String> activeActivityIds = runtimeService.getActiveActivityIds(excuteId);

        //获取高亮显示节点集合
        List<String> highLightedFlows = getHighLightedFlows(processDefinition, processInstance.getProcessInstanceId());

        ProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator();
        String activityFontName=processEngineConfiguration.getActivityFontName();
        String labelFontName=processEngineConfiguration.getLabelFontName();
        InputStream png = diagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds, highLightedFlows, activityFontName, labelFontName, null, 1.0);
        byte[] b = new byte[1024];
        int len = 0;
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        while((len=png.read(b))!=-1){
            swapStream.write(b);
        }
        String b64 = new String(Base64.getEncoder().encode(swapStream.toByteArray()));
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(b64);
    }

    //获取高亮显示节点集合
    private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinition, String processInstanceId) {
        List<String> highLightedFlows = new ArrayList<String>();
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc().list();

        List<String> historicActivityInstanceList = new ArrayList<String>();
        for (HistoricActivityInstance hai : historicActivityInstances) {
            historicActivityInstanceList.add(hai.getActivityId());
        }

        // add current activities to list
        List<String> highLightedActivities = runtimeService.getActiveActivityIds(processInstanceId);
        historicActivityInstanceList.addAll(highLightedActivities);

        // activities and their sequence-flows
        for (ActivityImpl activity : processDefinition.getActivities()) {
            int index = historicActivityInstanceList.indexOf(activity.getId());

            if (index >= 0 && index + 1 < historicActivityInstanceList.size()) {
                List<PvmTransition> pvmTransitionList = activity
                        .getOutgoingTransitions();
                for (PvmTransition pvmTransition : pvmTransitionList) {
                    String destinationFlowId = pvmTransition.getDestination().getId();
                    if (destinationFlowId.equals(historicActivityInstanceList.get(index + 1))) {
                        highLightedFlows.add(pvmTransition.getId());
                    }
                }
            }
        }
        return highLightedFlows;
    }
    //执行任务部门经理审批  参数day>10
    @Test
    public void excuteTask2(){
        String taskId = "40003";
        Object day = taskService.getVariable(taskId, "day");
        System.out.println("获取的参数 day："+day);
        taskService.setVariable(taskId,"day",day);
        taskService.complete(taskId);
        System.out.println("部门经理任务执行完成！");
    }
    //执行任务总经理审批
    @Test
    public void excuteTask3(){
        String taskId = "42503";
        taskService.complete(taskId);
        System.out.println("总经理任务执行完成，流程结束了");
    }
}
