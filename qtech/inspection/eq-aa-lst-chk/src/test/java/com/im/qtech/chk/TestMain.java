package com.im.qtech.chk;

import com.im.aa.inspection.entity.standard.EqLstTplDO;
import com.im.aa.inspection.entity.standard.EqLstTplInfoDO;
import com.im.aa.inspection.service.DatabaseService;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestMain {

    public static void main(String[] args) {
        // ✅ 1. 初始化 DatabaseService
        DatabaseService databaseService = DatabaseService.getInstance();
        System.out.println("✅ DatabaseService 初始化成功");

        // ✅ 2. 获取 Session
        Session session = databaseService.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            // ✅ 3. 测试插入 EqLstTplInfoDO
            EqLstTplInfoDO infoPO = new EqLstTplInfoDO();
            infoPO.setModuleId("MODULE_TEST");
            infoPO.setProvider("PROVIDER_TEST");

            session.saveOrUpdate(infoPO);
            System.out.println("✅ 插入 EqLstTplInfoDO 成功，ID = " + infoPO.getId());

            // ✅ 4. 测试插入 EqLstTplDO
            EqLstTplDO tplDO = new EqLstTplDO();
            tplDO.setModuleId("MODULE_TEST");
            tplDO.setRemark("模板内容示例");

            session.saveOrUpdate(tplDO);
            System.out.println("✅ 插入 EqLstTplDO 成功，ID = " + tplDO.getId());


            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }

        // ✅ 6. 测试查询
        try {
            EqLstTplInfoDO loaded = databaseService.getTplInfo("MODULE_TEST");
            if (loaded != null) {
                System.out.println("✅ findByModule 查询成功 -> " + loaded.getModuleId());
            } else {
                System.out.println("❌ findByModule 返回 null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ✅ 7. 关闭工厂
        databaseService.close();
        System.out.println("✅ 测试结束");
    }
}
