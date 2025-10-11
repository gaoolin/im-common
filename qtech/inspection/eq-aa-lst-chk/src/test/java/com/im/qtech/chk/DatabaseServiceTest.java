package com.im.qtech.chk;

import com.im.aa.inspection.entity.standard.EqLstTplDO;
import com.im.aa.inspection.entity.standard.EqLstTplInfoDO;
import com.im.aa.inspection.repository.EqLstTplInfoRepository;
import com.im.aa.inspection.repository.TemplateDataRepository;
import com.im.aa.inspection.service.DatabaseService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DatabaseService 单元测试类 (JUnit 4版本)
 *
 * @author gaozhilin
 * @email gaoolin@gmail.com
 * @since 2025/10/11
 */

public class DatabaseServiceTest {

    @Mock
    private EqLstTplInfoRepository templateInfoRepository;

    @Mock
    private TemplateDataRepository templateDataRepository;

    private DatabaseService databaseService;
    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // 创建 DatabaseService 实例
        databaseService = new DatabaseService();

        // 使用反射设置私有字段
        try {
            Field templateInfoRepoField = DatabaseService.class.getDeclaredField("templateInfoRepository");
            templateInfoRepoField.setAccessible(true);
            templateInfoRepoField.set(databaseService, templateInfoRepository);

            Field templateDataRepoField = DatabaseService.class.getDeclaredField("templateDataRepository");
            templateDataRepoField.setAccessible(true);
            templateDataRepoField.set(databaseService, templateDataRepository);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up mock repositories", e);
        }
    }

    @After
    public void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    public void testGetTplInfoSuccess() {
        // Given
        String module = "C08A38";
        EqLstTplInfoDO expected = new EqLstTplInfoDO();
        when(templateInfoRepository.findByModule(module)).thenReturn(expected);

        // When
        EqLstTplInfoDO result = databaseService.getTplInfo(module);

        // Then
        assertEquals(expected, result);
        verify(templateInfoRepository).findByModule(module);
    }

    @Test
    public void testGetTplInfoException() {
        // Given
        String module = "C08A38";
        when(templateInfoRepository.findByModule(module)).thenThrow(new RuntimeException("DB Error"));

        // When
        EqLstTplInfoDO result = databaseService.getTplInfo(module);

        // Then
        assertNull(result);
        verify(templateInfoRepository).findByModule(module);
    }

    @Test
    public void testGetTplSuccess() {
        // Given
        String module = "C08A38";
        EqLstTplDO expected = new EqLstTplDO();
        when(templateDataRepository.findByModule(module)).thenReturn(expected);

        // When
        EqLstTplDO result = databaseService.getTpl(module);

        // Then
        assertEquals(expected, result);
        verify(templateDataRepository).findByModule(module);
    }

    @Test
    public void testGetTplException() {
        // Given
        String module = "C08A38";
        when(templateDataRepository.findByModule(module)).thenThrow(new RuntimeException("DB Error"));

        // When
        EqLstTplDO result = databaseService.getTpl(module);

        // Then
        assertNotNull(result);
        assertTrue(result instanceof EqLstTplDO);
        verify(templateDataRepository).findByModule(module);
    }
}
