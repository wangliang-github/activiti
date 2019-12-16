package com.wangliang.activiti2.config;

import org.activiti.engine.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author ydwf
 * @Date 2019/12/16 15:41
 * @Version 1.0
 */
@Configuration
public class BeanConfig {
    /*
    processEngineConfiguration：activiti默认的引擎配置管理名称。
    dataSource：数据库类型; activiti默认的是h2;这里我是把这个activiti.xml在我的datasource.xml引入。
    transactionManager：事务管理器,这个没什么好说的,spring的东西
    databaseSchemaUpdate：声明数据库脚本更新策略；false:什么都不做;true：当activiti表不存在自动创建。
    jobExecutorActivate：是否执行作业功能;false：什么都不做;true：引擎会不断的刷新数据库作业的任务。
    eventListeners：事件监听，这里我创建了一个全局事件监听器，这个我感觉工作流activiti在与自身业务结合时发挥了很大的作用,后面我会详细介绍这个东西
    */
    private ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration().
            setJdbcDriver("com.mysql.jdbc.Driver").
            setJdbcUrl("jdbc:mysql://localhost:3306/demo1212?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true&useSSL=false").
            setJdbcUsername("root").
            setJdbcPassword("root").
            setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    private ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
    //activiti配置中心
    @Bean
    public ProcessEngineConfiguration getProcessEngineConfiguration(){
        return processEngineConfiguration;
    }

    //activiti引擎
    @Bean
    public ProcessEngine getProcessEngine(){
        return processEngine;
    }

    //1. repositoryService：流程仓库service,用于管理流程仓库,增删改查流程资源。
    @Bean
    public RepositoryService getRepositoryService(){
        return processEngine.getRepositoryService();
    }

    //runtimeService：运行时service,处理正在运行状态的流程实例,任务等
    @Bean
    public RuntimeService getRuntimeService(){
        return processEngine.getRuntimeService();
    }

    //taskService：任务service,管理,查询任务，例如签收，办理,指派任务
    @Bean
    public TaskService getTaskService(){
        return processEngine.getTaskService();
    }

    //historyService：历史service,可以查询所有历史数据,例如,流程实例,任务,活动，变量，附件等
    @Bean
    public HistoryService getHistoryService(){
        return processEngine.getHistoryService();
    }

    // managementService：引擎管理service,和具体业务无关,主要是查询引擎配置，数据库，作业等
    @Bean
    public ManagementService getManagementService(){
        return processEngine.getManagementService();
    }

    //identityService：身份service,可以管理和查询用户,组之间的关系
    @Bean
    public IdentityService getIdentutyService(){
        return processEngine.getIdentityService();
    }

    //formService：表单service,处理正在运行状态的流程实例,任务等
    @Bean
    public FormService getFormService(){
        return processEngine.getFormService();
    }
}
