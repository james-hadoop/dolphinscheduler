/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.QueueServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.QueueMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import org.apache.commons.collections.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * queue service test
 */
@RunWith(MockitoJUnitRunner.class)
public class QueueServiceTest {

    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);
    private static final Logger queueServiceImplLogger = LoggerFactory.getLogger(QueueServiceImpl.class);

    @InjectMocks
    private QueueServiceImpl queueService;

    @Mock
    private QueueMapper queueMapper;

    @Mock
    private UserMapper userMapper;

    private static final String QUEUE = "queue";
    private static final String QUEUE_NAME = "queueName";
    private static final String EXISTS = "exists";
    private static final String NOT_EXISTS = "not_exists";
    private static final String NOT_EXISTS_FINAL = "not_exists_final";

    @Before
    public void setUp() {
    }

    @After
    public void after() {
    }

    @Test
    public void testQueryList() {
        Mockito.when(queueMapper.selectList(null)).thenReturn(getQueueList());
        Map<String, Object> result = queueService.queryList(getLoginUser());
        List<Queue> queueList = (List<Queue>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(queueList));

    }

    @Test
    public void testQueryListPage() {

        IPage<Queue> page = new Page<>(1, 10);
        page.setTotal(1L);
        page.setRecords(getQueueList());
        Mockito.when(queueMapper.queryQueuePaging(Mockito.any(Page.class), Mockito.eq(QUEUE_NAME))).thenReturn(page);
        Result result = queueService.queryList(getLoginUser(), QUEUE_NAME, 1, 10);
        PageInfo<Queue> pageInfo = (PageInfo<Queue>) result.getData();
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getTotalList()));
    }

    @Test
    public void testCreateQueue() {

        // queue is null
        Throwable exception = Assertions.assertThrows(ServiceException.class, () -> queueService.createQueue(getLoginUser(), null, QUEUE_NAME));
        String formatter = MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), Constants.QUEUE);
        Assertions.assertEquals(formatter, exception.getMessage());

        // queueName is null
        exception = Assertions.assertThrows(ServiceException.class, () -> queueService.createQueue(getLoginUser(), QUEUE_NAME, null));
        formatter = MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), Constants.QUEUE_NAME);
        Assertions.assertEquals(formatter, exception.getMessage());

        // correct
        Map<String, Object> result = queueService.createQueue(getLoginUser(), QUEUE_NAME, QUEUE_NAME);
        Assert.assertEquals(Status.SUCCESS, result.get(Constants.STATUS));
    }

    @Test
    public void testUpdateQueue() {
        Mockito.when(queueMapper.selectById(1)).thenReturn(getQUEUE());
        Mockito.when(queueMapper.existQueue(EXISTS, null)).thenReturn(true);
        Mockito.when(queueMapper.existQueue(null, EXISTS)).thenReturn(true);

        // not exist
        Throwable exception = Assertions.assertThrows(ServiceException.class, () -> queueService.updateQueue(getLoginUser(), 0, QUEUE, QUEUE_NAME));
        String formatter = MessageFormat.format(Status.QUEUE_NOT_EXIST.getMsg(), QUEUE);
        Assertions.assertEquals(formatter, exception.getMessage());

        //no need update
        exception = Assertions.assertThrows(ServiceException.class, () -> queueService.updateQueue(getLoginUser(), 1, QUEUE_NAME, QUEUE_NAME));
        Assertions.assertEquals(Status.NEED_NOT_UPDATE_QUEUE.getMsg(), exception.getMessage());

        //queue exist
        exception = Assertions.assertThrows(ServiceException.class, () -> queueService.updateQueue(getLoginUser(), 1, EXISTS, QUEUE_NAME));
        formatter = MessageFormat.format(Status.QUEUE_VALUE_EXIST.getMsg(), EXISTS);
        Assertions.assertEquals(formatter, exception.getMessage());

        // queueName exist
        exception = Assertions.assertThrows(ServiceException.class, () -> queueService.updateQueue(getLoginUser(), 1, NOT_EXISTS, EXISTS));
        formatter = MessageFormat.format(Status.QUEUE_NAME_EXIST.getMsg(), EXISTS);
        Assertions.assertEquals(formatter, exception.getMessage());

        //success
        Mockito.when(userMapper.existUser(Mockito.anyString())).thenReturn(false);
        Map<String, Object> result = queueService.updateQueue(getLoginUser(), 1, NOT_EXISTS, NOT_EXISTS);
        Assert.assertEquals(Status.SUCCESS.getCode(), ((Status) result.get(Constants.STATUS)).getCode());

        // success update with same queue name
        Mockito.when(queueMapper.existQueue(NOT_EXISTS_FINAL, null)).thenReturn(false);
        result = queueService.updateQueue(getLoginUser(), 1, NOT_EXISTS_FINAL, NOT_EXISTS);
        Assert.assertEquals(Status.SUCCESS.getCode(), ((Status) result.get(Constants.STATUS)).getCode());

        // success update with same queue value
        Mockito.when(queueMapper.existQueue(null, NOT_EXISTS_FINAL)).thenReturn(false);
        result = queueService.updateQueue(getLoginUser(), 1, NOT_EXISTS, NOT_EXISTS_FINAL);
        Assert.assertEquals(Status.SUCCESS.getCode(), ((Status) result.get(Constants.STATUS)).getCode());
    }

    @Test
    public void testVerifyQueue() {
        //queue null
        Throwable exception = Assertions.assertThrows(ServiceException.class, () -> queueService.verifyQueue(null, QUEUE_NAME));
        String formatter = MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), Constants.QUEUE);
        Assertions.assertEquals(formatter, exception.getMessage());

        //queueName null
        exception = Assertions.assertThrows(ServiceException.class, () -> queueService.verifyQueue(QUEUE_NAME, null));
        formatter = MessageFormat.format(Status.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), Constants.QUEUE_NAME);
        Assertions.assertEquals(formatter, exception.getMessage());

        //exist queueName
        Mockito.when(queueMapper.existQueue(EXISTS, null)).thenReturn(true);
        exception = Assertions.assertThrows(ServiceException.class, () -> queueService.verifyQueue(EXISTS, QUEUE_NAME));
        formatter = MessageFormat.format(Status.QUEUE_VALUE_EXIST.getMsg(), EXISTS);
        Assertions.assertEquals(formatter, exception.getMessage());

        //exist queue
        Mockito.when(queueMapper.existQueue(null, EXISTS)).thenReturn(true);
        exception = Assertions.assertThrows(ServiceException.class, () -> queueService.verifyQueue(QUEUE, EXISTS));
        formatter = MessageFormat.format(Status.QUEUE_NAME_EXIST.getMsg(), EXISTS);
        Assertions.assertEquals(formatter, exception.getMessage());

        // success
        Result<Object> result = queueService.verifyQueue(NOT_EXISTS, NOT_EXISTS);
        Assert.assertEquals(result.getCode().intValue(), Status.SUCCESS.getCode());
    }

    @Test
    public void testCreateQueueIfNotExists() {
        Queue queue;

        // queue exists
        Mockito.when(queueMapper.queryQueueName(QUEUE, QUEUE_NAME)).thenReturn(getQUEUE());
        queue = queueService.createQueueIfNotExists(QUEUE, QUEUE_NAME);
        Assert.assertEquals(getQUEUE(), queue);

        // queue not exists
        Mockito.when(queueMapper.queryQueueName(QUEUE, QUEUE_NAME)).thenReturn(null);
        queue = queueService.createQueueIfNotExists(QUEUE, QUEUE_NAME);
        Assert.assertEquals(new Queue(QUEUE_NAME, QUEUE), queue);
    }

    /**
     * create admin user
     */
    private User getLoginUser() {

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setId(99999999);
        return loginUser;
    }

    /**
     * get queue
     */
    private Queue getQUEUE() {
        Queue queue = new Queue();
        queue.setId(1);
        queue.setQueue(QUEUE_NAME);
        queue.setQueueName(QUEUE_NAME);
        return queue;
    }

    private List<Queue> getQueueList() {
        List<Queue> queueList = new ArrayList<>();
        queueList.add(getQUEUE());
        return queueList;
    }

}
