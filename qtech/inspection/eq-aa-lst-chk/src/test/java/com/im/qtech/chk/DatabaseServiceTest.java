package com.im.qtech.chk;

import com.im.aa.inspection.entity.standard.EqLstTplInfoDO;
import com.im.aa.inspection.repository.EqLstTplInfoRepository;
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
 * @date 2025/10/11
 */

public class DatabaseServiceTest {

    @Mock
    private EqLstTplInfoRepository templateInfoRepository;
    private DatabaseService databaseService;
    private AutoCloseable closeable;

    @Before
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // 创建 DatabaseService 实例
        databaseService = DatabaseService.getInstance();

        // 使用反射设置私有字段
        try {
            Field templateInfoRepoField = DatabaseService.class.getDeclaredField("templateInfoRepository");
            templateInfoRepoField.setAccessible(true);
            templateInfoRepoField.set(databaseService, templateInfoRepository);

            Field templateDataRepoField = DatabaseService.class.getDeclaredField("templateDataRepository");
            templateDataRepoField.setAccessible(true);
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
        when(templateInfoRepository.findByModuleId(module)).thenReturn(expected);

        // When
        EqLstTplInfoDO result = databaseService.getTplInfo(module);

        // Then
        assertEquals(expected, result);
        verify(templateInfoRepository).findByModuleId(module);
    }

    @Test
    public void testGetTplInfoException() {
        // Given
        String module = "C08A38";
        when(templateInfoRepository.findByModuleId(module)).thenThrow(new RuntimeException("DB Error"));

        // When
        EqLstTplInfoDO result = databaseService.getTplInfo(module);

        // Then
        assertNull(result);
        verify(templateInfoRepository).findByModuleId(module);
    }
}
